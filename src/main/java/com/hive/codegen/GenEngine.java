package com.hive.codegen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.google.gson.Gson;

public class GenEngine {

  private static final Logger log = Logger.getLogger(GenEngine.class);
  private static final Pattern TABLE_PATTERN = Pattern
      .compile("^.*\\s*create\\s+table\\s+([^\\(]*)(\\s*.*|\\s*\\(.*)$");
  private static final Pattern PROPERTY_PATTERN = Pattern.compile("^\\s*[a-z_\\-0-9]+\\s*=.*");
  private static final Pattern TABLE_COMMENT_PATTERN= Pattern.compile("^.*\\s*engine=.*comment\\s+\'(.*)\';$");
  public static final String RT = "\n";
  public static final String VM = ".vm";
  private boolean truncateTablePrefix = true;
  private static String REMOVE_CHARS_FROM_BEGINNING = "{}";
  private static String SQ = "'";

  private VelocityEngine getVelocityEngine(String templateBaseDir, String targetDir)
      throws Exception {
    VelocityEngine ve = new VelocityEngine();
    Properties pp = new Properties();
    pp.setProperty("resource.loader", "file");
    pp.setProperty("file.resource.loader.class",
        "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
    pp.setProperty("file.resource.loader.path", templateBaseDir);
    pp.setProperty(VelocityEngine.RUNTIME_LOG, targetDir + File.separator + "velocity.log");
    String velocityProperties = "velocity.properties";

    File f = new File(velocityProperties);
    if (f.exists()) {
      log.info("init with file:" + velocityProperties);
      FileInputStream is = new FileInputStream(f);
      pp.load(is);
    }
    ve.init(pp);
    return ve;
  }

  public boolean isTruncateTablePrefix() {
    return truncateTablePrefix;
  }

  public void setTruncateTablePrefix(boolean truncateTablePrefix) {
    this.truncateTablePrefix = truncateTablePrefix;
  }

  public String renderString(String templateStr, VelocityContext rendContext) {
    try {
      File file = File.createTempFile("tmp", VM);
      Writer writer = new FileWriter(file);
      writer.write(templateStr);
      writer.flush();
      writer.close();

      File fileOut = File.createTempFile("tmp", ".out");
      VelocityEngine ve = this.getVelocityEngine(file.getParent(), file.getParent());
      rendTemplate(ve, file.getName(), rendContext, fileOut);
      StringBuffer strBuf = new StringBuffer();
      char[] buf = new char[1024];
      Reader in = new FileReader(fileOut);
      int count = in.read(buf);
      while (count > 0) {
        strBuf.append(buf, 0, count);
        count = in.read(buf);
      }
      in.close();
      templateStr = new String(strBuf);
      try {
        file.delete();
        fileOut.delete();
      } catch (Exception e) {
        log.error("", e);
      }
    } catch (Exception e) {
      log.error("", e);
      throw new RuntimeException(e.getMessage());
    }
    return templateStr;
  }

  private boolean createFileIfNotExist(File file) throws IOException {
    File parent = file.getParentFile();
    if (parent != null && !parent.exists()) {
      parent.mkdirs();
    }
    if (!file.exists()) {
      file.createNewFile();
    }
    return true;
  }

  private void rendTemplate(VelocityEngine engine, String templateFile, VelocityContext context,
      File responseFile) {
    try {
      Template template = engine.getTemplate(templateFile);
      if (template != null) {
        createFileIfNotExist(responseFile);
        Writer response =
            new OutputStreamWriter(new FileOutputStream(responseFile),
                Charset.forName((String) engine.getProperty(VelocityEngine.OUTPUT_ENCODING)));
        template.merge(context, response);
        response.flush();
        response.close();
      }
    } catch (Exception ex) {
      log.error("", ex);
      throw new RuntimeException(ex);
    }
  }

  public String render(String valueStr, String templateStr, boolean isDir) throws Exception {
     return this.render(valueStr, templateStr, isDir , null , null);
  }

  public String render(String valueStr, String templateStr, boolean isDir , Map<String,Object>extInfos , Map<String,Object> outInfos ) throws Exception {
    String rtn = "";
    if (isDir) {
      int rootDirIndex = 0;
      String[] fileList = templateStr.split(RT);
      String rootDir = fileList[0];
      if (rootDir != null) {
        rootDir = rootDir.trim();
        int rootDirLen = rootDir.length();
        String targetDir = rootDir + "_target";
        VelocityEngine ve = this.getVelocityEngine(rootDir, targetDir);
        VelocityContext rendContext = makeContext(valueStr,extInfos, outInfos);
        for (int i = rootDirIndex + 1; i < fileList.length; i++) {
          String file = fileList[i].trim();
          if (file != null) {
            file = file.trim();
            if (file.length() > 0) {
              rendTemplate(ve, file.substring(rootDirLen + 1), rendContext, new File(targetDir
                  + file.substring(rootDirLen)));
            }
          }
        }
      } else {
        rtn = "No root directory found!";
      }
      return rtn + "All template rend over!";
    } else {
      rtn = renderOnStrStr(valueStr, templateStr,extInfos,outInfos);
    }
    return rtn;
  }
 
  public String renderOnStrStr(String valueStr, String templateStr) {
    return renderString(templateStr, makeContext(valueStr,null,null));
  }

  public String renderOnStrStr(String valueStr, String templateStr,Map<String,Object>extInfos , Map<String,Object> outInfos) {
    return renderString(templateStr, makeContext(valueStr,extInfos,outInfos));
  }

  private VelocityContext makeContext(String contextStr) {
    return this.makeContext(contextStr,null,null);
  }
  private VelocityContext makeContext(String contextStr, Map<String,Object> extInfos , Map<String,Object> outInfos) {
    if (contextStr == null) {
      return null;
    }
    Map<String, Object> infos = prepareInfo(contextStr);
    Formator FMT = new Formator();
    VelocityContext myContext = new VelocityContext();
    myContext.put("FMT", FMT);
    for (String key : infos.keySet()) {
      myContext.put(key, infos.get(key));
      if( outInfos != null ){
        outInfos.put(key, infos.get(key));
      }
    }
    if (extInfos != null) {
      for (String key : extInfos.keySet()) {
        myContext.put(key, extInfos.get(key));
        if( outInfos != null ){
          outInfos.put(key, extInfos.get(key));
        }
      }
    }
    
    return myContext;
  }

  private Map<String, Object> prepareInfo(String str) {
    Map<String, Object> infos = new HashMap<String, Object>();
    List<Object> colsList = new ArrayList<Object>();
    str = str.replaceAll("`", "");

    String[] line = str.split(RT);
    String splitStr = " ";
    List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();
    for (int i = 0; i < line.length; i++) {
      String caseRetainLine = removeCharsFromBeginning(line[i].trim().replaceAll("\t", " "));
      caseRetainLine = removeWsWithinSingleQuotation(caseRetainLine);
      String aLine = caseRetainLine.toLowerCase();
      if (aLine.length() > 1) {
        Matcher matcher = TABLE_PATTERN.matcher(aLine);
        Matcher propertyMatcher = PROPERTY_PATTERN.matcher(aLine);
        Matcher commentMatcher = TABLE_COMMENT_PATTERN.matcher(aLine);
        if (matcher.matches()) {
          String table = matcher.group(1);
          infos.put("realTable", table.trim());
          if (truncateTablePrefix) {
            int index = table.indexOf("_");
            if (index > 0) {
              table = table.substring(index + 1);
            }
          }
          infos.put("table", table.trim());
        } else if (propertyMatcher.matches()) {
          int index = caseRetainLine.indexOf("=");
          infos.put(caseRetainLine.substring(0, index).trim(), caseRetainLine.substring(index + 1)
              .trim());
        }
        else if( commentMatcher.matches() ){
          String tableComment = commentMatcher.group(1);
          infos.put("tableComment", tableComment.trim());
        } else {
          int oldLen = 1;
          int newLen = 0;
          if (aLine.contains("key ")) {
            continue;
          }
          while (oldLen != newLen) {
            oldLen = aLine.length();
            aLine = aLine.replaceAll(splitStr + splitStr, splitStr);
            newLen = aLine.length();
          }
          String[] cols = aLine.split(splitStr);
          // cols by index
          List<String> colsInLine = new ArrayList<String>();
          for (int j = 0; j < cols.length; j++) {
            colsInLine.add(cols[j]);
          }
          colsList.add(colsInLine);

          Map<String, Object> colsInfoMap = colsInfo(cols);
          if (colsInfoMap != null) {
            values.add(colsInfoMap);
          }
        }
      }
      if (values.size() > 1) {
        infos.put("values", values);
      }
      infos.put("cols", colsList);
    }
    return infos;
  }

  private String removeCharsFromBeginning(String aLine) {
    for (int j = 0; j < REMOVE_CHARS_FROM_BEGINNING.length(); j++) {
      String aChar = REMOVE_CHARS_FROM_BEGINNING.substring(j, j + 1);
      if (aLine.startsWith(aChar)) {
        aLine = aLine.substring(1).trim();
        break;
      }
    }
    return aLine;
  }

  private String removeWsWithinSingleQuotation(String str) {
    int startIndex = str.indexOf(SQ);
    if (startIndex > 0) {
      int endIndex = str.indexOf(SQ, startIndex + 1);
      if (endIndex > 0) {
        return str.substring(0, startIndex) + str.substring(startIndex, endIndex).replace(" ", "")
            + str.substring(endIndex);
      }
    }

    return str;

  }

  private Map<String, Object> colsInfo(String[] cols) {
    if (cols.length >= 0 && ")".equals(cols[0])) {
      return null;
    }
    if (cols.length > 2) {
      Map<String, Object> map = new HashMap<String, Object>();
      for (int j = 0; j < cols.length; j++) {
        String nullAble = "true";
        if (j == 0) {
          String name = cols[j].trim();
          if (cols[1].trim().equals("bit(1)")) {
            name = name.replace("is_", "");
          }
          map.put("name", name);
        } else if (j == 1) {
          map.put("type", fieldTypeToJava(cols[j].trim()));
        } else if (cols[j].contains("comment")) {
          String comment = cols[j + 1];
          Pattern pattern= Pattern.compile("^'(.*)'\\s*,$");
          Matcher matcher = pattern.matcher(comment);
          if(matcher.matches()){
            comment = matcher.group(1);
          }
          // comment = comment.replace("'", "");
          // comment = comment.replace(",", "");
          int sign = comment.indexOf("#{");
          if (sign < 0) {
            String tag = comment;
            tag = StringUtils.strip(tag);
            map.put("comment", tag);
          } else {
            String tag = comment.substring(0, sign);;
            tag = StringUtils.strip(tag);
            map.put("comment", tag);
            int endIndex = comment.lastIndexOf("}");
            if (endIndex > sign) {
              String vmSign = comment.substring(sign + 1, endIndex + 1);
              Gson gson = new Gson();
              try {
                map.put("atts", gson.fromJson(vmSign, Map.class));
              } catch (com.google.gson.JsonSyntaxException e) {
                throw new RuntimeException("jsonSytaxException: " + vmSign);
              }
              map.put("vmSign", vmSign);
            } else {
              throw new RuntimeException("Invalid Json:" + comment);
            }
          }

        } else if (cols[j].equals("not") && cols.length > j + 1) {
          if (cols[j + 1].equals("null")) {
            nullAble = "false";
          }
        }
        map.put("nullable", nullAble);
      }
      return map;
    } else {
      return null;
    }
  }

  static Map<String, String> FIELD_TYPE_2_JAVA = new HashMap<String, String>();
  static {
    FIELD_TYPE_2_JAVA.put("bigint", "Long");
    FIELD_TYPE_2_JAVA.put("int(11)", "Long");
    FIELD_TYPE_2_JAVA.put("int(10)", "Long");
    FIELD_TYPE_2_JAVA.put("char", "String");
    FIELD_TYPE_2_JAVA.put("varchar", "String");
    FIELD_TYPE_2_JAVA.put("text", "String");
    FIELD_TYPE_2_JAVA.put("longtext", "String");
    FIELD_TYPE_2_JAVA.put("date", "Date");
    FIELD_TYPE_2_JAVA.put("datetime", "Date");
    FIELD_TYPE_2_JAVA.put("decimal", "BigDecimal");
    FIELD_TYPE_2_JAVA.put("mediumint", "Long");
    FIELD_TYPE_2_JAVA.put("tinyint", "Integer");
    FIELD_TYPE_2_JAVA.put("smallint", "Integer");
    FIELD_TYPE_2_JAVA.put("timestamp", "Date");
    FIELD_TYPE_2_JAVA.put("bit", "Boolean");
    FIELD_TYPE_2_JAVA.put("money", "BigDecimal");
  }

  private String fieldTypeToJava(String sqlType) {
    String str = sqlType.toLowerCase();
    if (FIELD_TYPE_2_JAVA.containsKey(str)) {
      return FIELD_TYPE_2_JAVA.get(str);
    }
    int index = str.indexOf("(");
    if (index > 0) {
      str = str.substring(0, index);
      if (FIELD_TYPE_2_JAVA.containsKey(str)) {
        return FIELD_TYPE_2_JAVA.get(str);
      }
    }
    return "TODO:ADD \"" + sqlType + "\" FIELD_TYPE_2_JAVA";
  }
}

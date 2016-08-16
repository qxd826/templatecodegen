package com.hive.codegen;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hive.GenBatchConfig;
import com.hive.ParamWrap;
import com.hive.TemplateConfigObject;

public class GenBatch {

  private static final Pattern TEMPFILE_SUFFIX_PATTERN = Pattern.compile("\\.vm$");
  private static final Pattern TEMPFILE_FRONT_PATTERN = Pattern.compile("\\.html|css|js$");

  private static void printUsage() {
    System.out.println(" -p configFilePath ");
  }

  public static Properties loadPropertis(String path) {
    File file = new File(path);
    try {
      Properties p = new Properties();
      FileInputStream inputStream = FileUtils.openInputStream(file);
      p.load(inputStream);
      return p;
    } catch (IOException e) {
      return null;
    }
  }

  public static void main(String[] args) {
    Getopt g = new Getopt("Sync", args, "c:");
    int c;
    String configPath = "";
    while ((c = g.getopt()) != -1) {
      switch (c) {
        case 'c':
          configPath = g.getOptarg();
          break;
        case '?':
        default:
          printUsage();
          return;
      }
    }
    if (StringUtils.isEmpty(configPath)) {
      configPath = "genBatch.config";
    }
    GenBatchConfig config = new GenBatchConfig();
    config = loadConfig(configPath);

    ParamWrap<String> msgWrap = new ParamWrap<String>();
    boolean checkResult = config.check(msgWrap);
    if (!checkResult) {
      System.out.println(msgWrap.get());
    }
    List<File> tmpFiles = new ArrayList<File>();
    getAllFilesInDir(tmpFiles, config.getTemplatesDir());

    try {
      String sqlFileContent = FileUtils.readFileToString(new File(config.getTableSqlPath()));
      String result = "";

      File outDir = new File(config.getOutputDir());
      if (outDir.isDirectory() == false) {
        outDir.mkdirs();
      }

      Map<String, Object> extInfos = new HashMap<String, Object>();
      Map<String, Object> allInfos = new HashMap<String, Object>();
      Formator FMT = new Formator();

      for (File file : tmpFiles) {
        result = "";
        String filePath = "";

        String fileContent = FileUtils.readFileToString(file);
        GenEngine engine = new GenEngine();
        extInfos.clear();
        allInfos.clear();

        for (Map.Entry<String, TemplateConfigObject> entry : config.getTemplates().entrySet()) {
          String extInfoKey = entry.getKey() + ".pkgPrefix";
          extInfoKey = extInfoKey.replaceAll("\\.", "_");
          extInfos.put(extInfoKey,
              entry.getValue().getPackagePrefix() + "." + config.getSuperPakage());
        }
        extInfos.put("project", config.getProject());
        extInfos.put("pkgPrefix",
            config.getPackagePrefix(file.getName()) + "." + config.getSuperPakage());
        extInfos.put("superPackage", config.getSuperPakage());

        result = engine.renderOnStrStr(sqlFileContent, fileContent, extInfos, allInfos);
        String outFileSuffix = config.getOutFileSuffix(file.getName());

        if (StringUtils.isEmpty(outFileSuffix)) {
          outFileSuffix = getOutPutFileNameSuffixByVMFileName(file.getName());
        }

        Matcher matcher = TEMPFILE_FRONT_PATTERN.matcher(outFileSuffix);
        String tableName = FMT.XyzAbc((String) allInfos.get("table"));
        if (matcher.find()) {
          tableName = FMT.xyzAbc((String) allInfos.get("table"));
        }
        filePath = outDir.getAbsolutePath() + "/" + tableName + outFileSuffix;
        File outFile = new File(filePath);
        FileUtils.writeStringToFile(outFile, result);
        System.out.println("#####################################################");
        System.out.println("###  out put to file:" + filePath);
        System.out.println("#####################################################");
        System.out.println(result);
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  public static String getOutPutFileNameSuffixByVMFileName(String vmFilename) {
    String temp = vmFilename.replaceAll("\\.vm$", "");

    Formator FMT = new Formator();
    return FMT.XyzAbc(temp);
  }

  public static GenBatchConfig loadConfig(String path) {
    File configFile = new File(path);
    if (!configFile.exists()) {
      return null;
    }
    GenBatchConfig config = null;
    String jsonConfig;
    try {
      jsonConfig = FileUtils.readFileToString(configFile);
      Gson gson = new Gson();
      Type typelist = new TypeToken<GenBatchConfig>() {}.getType();
      config = gson.fromJson(jsonConfig, typelist);
    } catch (IOException e) {
      System.out.println("load config file error!");
      return null;
    }
    return config;
  }


  private static void getAllFilesInDir(List<File> files, String dir) {
    for (File file : new File(dir).listFiles()) {
      if (file.isFile()) {
        // System.out.println( file.getName());
        Matcher matcher = TEMPFILE_SUFFIX_PATTERN.matcher(file.getName());
        if (matcher.find()) {
          files.add(file);
        }
      } else {
        getAllFilesInDir(files, file.getAbsolutePath());
      }
    }
  }
}

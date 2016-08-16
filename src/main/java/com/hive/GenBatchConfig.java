package com.hive;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;


public class GenBatchConfig {
  private String templatesDir;
  private String tableSqlPath;
  private String outputDir;
  private String superPakage;
  private String project;
  private Map<String,TemplateConfigObject> templates;


  public boolean check(ParamWrap<String> msg) {
   if (StringUtils.isEmpty(this.templatesDir)) {
      msg.set( "templatesDir is empty");
      return false;
    }
   if (StringUtils.isEmpty(this.tableSqlPath)) {
      msg.set( "templatesDir is empty");
      return false;
    }
   if (StringUtils.isEmpty(this.outputDir)) {
      msg.set( "templatesDir is empty");
      return false;
    }
    if (StringUtils.isEmpty(this.superPakage)) {
      msg.set( "templatesDir is empty");
      return false;
    }
    return true;
  }

  public String getOutFileSuffix(String tempFileName) {
    TemplateConfigObject temp = this.templates.get(tempFileName);
    if (temp == null) {
      return null;
    }
    return temp.getOutFileSuffix();
  }

  public String getPackagePrefix(String tempFileName) {
    TemplateConfigObject temp = this.templates.get(tempFileName);
    if (temp == null) {
      return null;
    }
    return temp.getPackagePrefix();
  }
  
  public String getTemplatesDir() {
    return templatesDir;
  }

  public void setTemplatesDir(String templatesDir) {
    this.templatesDir = templatesDir;
  }

  public String getTableSqlPath() {
    return tableSqlPath;
  }

  public void setTableSqlPath(String tableSqlPath) {
    this.tableSqlPath = tableSqlPath;
  }

  public String getOutputDir() {
    return outputDir;
  }

  public void setOutputDir(String outputDir) {
    this.outputDir = outputDir;
  }

  public Map<String, TemplateConfigObject> getTemplates() {
    return templates;
  }

  public void setTemplates(Map<String, TemplateConfigObject> templates) {
    this.templates = templates;
  }

  public String getSuperPakage() {
    return superPakage;
  }

  public void setSuperPakage(String superPakage) {
    this.superPakage = superPakage;
  }

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }
  
}

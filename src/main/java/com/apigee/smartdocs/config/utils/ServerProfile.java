/**
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apigee.smartdocs.config.utils;

import java.util.Map;

public class ServerProfile {

  private String options;
  private String configFile;
    
  // Portal Parameters
  private String portalUserName; // Developer Portal Username
  private String portalPassword; // Developer Portal Password
  private String portalDirectory; // Directory holding OpenAPI specs
  private String portalURL; // Developer Portal URL
  private String portalPath; // Developer Portal REST base path
  private String portalFormat; // OpenAPI spec format
  private String portalModelVocabulary; // Model Vocabulary
  private String portalCronKey; // Dev portal Cron Key
  private String portalModelNameConfig; // OPTIONAL configuration for Model Name
  private String apiIdField; // OPTIONAL configuration for the API Identification field
  private String portalAPIDocFormat; // Dev Portal API Doc Format
  private Map<String, PortalField> portalModelFields; // OpenAPI spec format

  /**
   * @return the portalUser
   */
  public String getPortalUserName() {
    return portalUserName;
  }

  /**
   * @param portalUserName the portalUserName to set
   */
  public void setPortalUserName(String portalUserName) {
    this.portalUserName = portalUserName;
  }

  /**
   * @return the portalPassword
   */
  public String getPortalPassword() {
    return portalPassword;
  }

  /**
   * @param portalPassword the portalPassword to set
   */
  public void setPortalPassword(String portalPassword) {
    this.portalPassword = portalPassword;
  }

  /**
   * @return the portalDirectory
   */
  public String getPortalDirectory() {
    return portalDirectory;
  }

  /**
   * @param portalDirectory the portalDirectory to set
   */
  public void setPortalDirectory(String portalDirectory) {
    this.portalDirectory = portalDirectory;
  }

  /**
   * @return the portalURL
   */
  public String getPortalURL() {
    return portalURL;
  }

  /**
   * @param portalURL the portalURL to set
   */
  public void setPortalURL(String portalURL) {
    this.portalURL = portalURL;
  }

  /**
   * @return the portalPath
   */
  public String getPortalPath() {
    return portalPath;
  }

  /**
   * @param portalPath the portalPath to set
   */
  public void setPortalPath(String portalPath) {
    this.portalPath = portalPath;
  }

  /**
   * @return the portalFormat
   */
  public String getPortalFormat() {
    return portalFormat;
  }

  /**
   * @param portalFormat the portalFormat to set
   */
  public void setPortalFormat(String portalFormat) {
    this.portalFormat = portalFormat;
  }

  /**
   * @param portalModelVocabulary the portalModelVocabulary to set
   */
  public void setPortalModelVocabulary(String portalModelVocabulary) {
    this.portalModelVocabulary = portalModelVocabulary;
  }

  /**
   * @return the portalModelVocabulary
   */
  public String getPortalModelVocabulary() {
    return portalModelVocabulary;
  }

  /**
   * @param portalCronKey the portalCronKey to set
   */
  public void setPortalCronKey(String portalCronKey) {
    this.portalCronKey = portalCronKey;
  }

  /**
   * @return the portalCronKey
   */
  public String getPortalCronKey() {
    return portalCronKey;
  }

  /**
   * @return the portalModelFields
   */
  public Map<String, PortalField> getPortalModelFields() {
    return portalModelFields;
  }

  /**
   * @param portalModelFields the portalModelFields to set
   */
  public void setPortalModelFields(Map<String, PortalField> portalModelFields) {
    this.portalModelFields = portalModelFields;
  }

  /**
   * @param configFile the options to set
   */
  public String getConfigFile() {
    return configFile;
  }

  public void setConfigFile(String configFile) {
    this.configFile = configFile;
  }

  /**
   * @param configuration for the API Identification field
   */
  public String getApiIdField() {
    return apiIdField;
  }

  public void setApiIdField(String apiIdField) {
    this.apiIdField = apiIdField;
  }
  
  /**
   * @param options the options to set
   */
  public String getOptions() {
    return options;
  }

  public void setOptions(String options) {
    this.options = options;
  }

  /**
   * @return the PortalModelNameConfig
   */
  public String getPortalModelNameConfig() {
    return portalModelNameConfig;
  }

  /**
   * @param portalModelNameConfig the portalModelNameConfig to set
   */
  public void setPortalModelNameConfig(String portalModelNameConfig) {
    this.portalModelNameConfig = portalModelNameConfig;
  }

	/**
	 * @return the portalAPIDocFormat
	 */
	public String getPortalAPIDocFormat() {
		return portalAPIDocFormat;
	}
	
	/**
	 * @param portalAPIDocFormat the portalAPIDocFormat to set
	 */
	public void setPortalAPIDocFormat(String portalAPIDocFormat) {
		this.portalAPIDocFormat = portalAPIDocFormat;
	}
}

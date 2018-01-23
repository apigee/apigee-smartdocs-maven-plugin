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

package com.apigee.smartdocs.config.mavenplugin;

import java.io.File;
import java.util.Map;
import org.apache.maven.plugin.AbstractMojo;
import com.apigee.smartdocs.config.utils.ServerProfile;
import com.apigee.smartdocs.config.utils.PortalField;

public abstract class GatewayAbstractMojo extends AbstractMojo {

  /**
   * Directory containing the build files.
   *
   * @parameter property="project.build.directory"
   */
  private File buildDirectory;

  /**
   * Base directory of the project.
   *
   * @parameter property="basedir"
   */
  private File baseDirectory;

  /**
   * Project Name
   *
   * @parameter property="project.name"
   */
  private String projectName;

  /**
   * Project version
   *
   * @parameter property="project.version"
   */
  private String projectVersion;

  /**
   * Project artifact id
   *
   * @parameter property="project.artifactId"
   */
  private String artifactId;

  /**
   * Profile id
   *
   * @parameter property="apigee.profile"
   */
  private String id;

  /**
   * Build option
   *
   * @parameter property="build.option"
   */
  private String buildOption;

  /**
   * Gateway options
   *
   * @parameter property="apigee.smartdocs.config.options"
   */
  private String options;

  /**
   * Config dir
   *
   * @parameter property="apigee.smartdocs.config.dir"
   */
  private String configDir;

  /**
   * Portal User Name
   *
   * @parameter property="portal.username"
   */
  private String portalUserName;

  /**
   * Portal Password
   *
   * @parameter property="portal.password"
   */
  private String portalPassword;

  /**
   * OpenAPI Spec Directory
   *
   * @parameter property="portal.directory"
   */
  private String portalDirectory;

  /**
   * Portal URL
   *
   * @parameter property="portal.url"
   */
  private String portalURL;

  /**
   * Portal Path
   *
   * @parameter property="portal.path"
   */
  private String portalPath;

  /**
   * Portal Format
   *
   * @parameter property="portal.format"
   */
  private String portalFormat;

  /**
   * Portal Format
   *
   * @parameter alias="portal.model.vocabulary"
   */
  private String portalModelVocabulary;

  /**
   * Portal Model Fields
   *
   * @parameter alias="portal.model.fields"
   */
  private Map<String, PortalField> portalModelFields;

  /**
   * Skip running this plugin. Default is false.
   *   
* @parameter default-value="false"
   */
  private boolean skip = false;

  public ServerProfile buildProfile;

  public GatewayAbstractMojo() {
    super();
  }

  public ServerProfile getProfile() {
    this.buildProfile = new ServerProfile();
    this.buildProfile.setOptions(this.options);
    this.buildProfile.setPortalUserName(this.portalUserName);
    this.buildProfile.setPortalPassword(this.portalPassword);
    this.buildProfile.setPortalDirectory(this.portalDirectory);
    this.buildProfile.setPortalURL(this.portalURL);
    this.buildProfile.setPortalPath(this.portalPath);
    this.buildProfile.setPortalFormat(this.portalFormat);
    this.buildProfile.setPortalModelFields(this.portalModelFields);
    this.buildProfile.setPortalModelVocabulary(this.portalModelVocabulary);

    return buildProfile;
  }

  public void setProfile(ServerProfile profile) {
    this.buildProfile = profile;
  }

  public void setBaseDirectory(File baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  public String getBuildDirectory() {
    return this.buildDirectory.getAbsolutePath();
  }

  public String getBaseDirectoryPath() {
    return this.baseDirectory.getAbsolutePath();
  }

  public String getBuildOption() {
    return buildOption;
  }

  public void setBuildOption(String buildOption) {
    this.buildOption = buildOption;
  }

  public String getOptions() {
    return options;
  }

  public void setOptions(String options) {
    this.options = options;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  public boolean isSkip() {
    return skip;
  }

  public void setSkip(boolean skip) {
    this.skip = skip;
  }

}

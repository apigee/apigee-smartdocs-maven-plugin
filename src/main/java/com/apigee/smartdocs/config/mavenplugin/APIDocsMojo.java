/**
 * Copyright 2019 Google Inc.
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
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apigee.smartdocs.config.rest.PortalRestUtil;
import com.apigee.smartdocs.config.utils.ServerProfile;
import com.google.api.client.util.Key;

/**                                                                                                                                     ¡¡
 * Goal to create API Docs in Apigee Developer Portal
 * scope: org
 *
 * @author ssvaidyanathan
 * @goal apidoc
 * @phase install
 */

public class APIDocsMojo extends GatewayAbstractMojo {
  static Logger logger = LoggerFactory.getLogger(APIDocsMojo.class);
  private static File[] files = null;

  public static final String ____ATTENTION_MARKER____ =
  "************************************************************************";

  enum OPTIONS {
    none, create, update, delete, sync
  }

  OPTIONS buildOption = OPTIONS.none;

  private ServerProfile serverProfile;

  /**
   * Constructor.
   */
  public APIDocsMojo() {
    super();
  }

  public void init() throws MojoExecutionException, MojoFailureException {
    try {
      logger.info(____ATTENTION_MARKER____);
      logger.info("API Docs");
      logger.info(____ATTENTION_MARKER____);

      String options="";
      serverProfile = super.getProfile();

      options = super.getOptions();
      if (options != null) {
        buildOption = OPTIONS.valueOf(options);
      }
      if (buildOption == OPTIONS.none) {
        logger.info("Skipping APIDoc (default action)");
        return;
      }

      logger.debug("Build option " + buildOption.name());

      if (serverProfile.getPortalURL() == null) {
        throw new MojoExecutionException(
          "Developer portal URL not found in profile");
      }
      if (serverProfile.getPortalURL() != null && serverProfile.getPortalURL().endsWith("/")) {
          throw new MojoExecutionException(
            "Please provide the url of the developer portal without the trailing \"/\"");
        }
      if (serverProfile.getPortalUserName() == null) {
        throw new MojoExecutionException(
          "Developer portal username not found in profile");
      }
      if (serverProfile.getPortalPassword() == null) {
        throw new MojoExecutionException(
          "Developer portal password not found in profile");
      }
      if (serverProfile.getPortalFormat() == null) {
          throw new MojoExecutionException(
            "Developer portal file format not found in profile");
      }
      if (serverProfile.getPortalAPIDocFormat() == null) {
          throw new MojoExecutionException(
            "Developer portal API Doc format not found in profile");
      }
      
      // Scan to make sure there are swagger files to send.
      getOpenAPISpecs();

    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Invalid apigee.option provided");
    } catch (RuntimeException e) {
      throw e;
    } catch (MojoExecutionException e) {
      throw e;
    }

  }

  /**
   * Entry point for the mojo.
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (super.isSkip()) {
      getLog().info("Skipping");
      return;
    }

    Logger logger = LoggerFactory.getLogger(APIDocsMojo.class);

    try {
      init();
      if (buildOption == OPTIONS.none) {
        return;
      }
      
      if (buildOption == OPTIONS.create ||
        buildOption == OPTIONS.update) {
        doUpdate();
      }
      
      if (buildOption == OPTIONS.delete) {
        doDelete();
      }

      if (buildOption == OPTIONS.sync) {
    	  doDelete();
    	  doUpdate();
      }

    } catch (MojoFailureException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    }
  }

  /**
   * Posts an update based on available OpenAPI specs. Will update base
   * information, then upload the current OpenAPI spec.
   * @throws MojoExecutionException
   */
  public void doUpdate() throws MojoExecutionException {
    try {
      for (File file : files) {
        PortalRestUtil.postAPIDoc(serverProfile, file);
      }
    }
    catch (IOException e) {
      throw new RuntimeException("Update failure: " + e.getMessage());
    }
  }
  
  /**
   * Deletes API Doc that exist in the API and not in the file system.
   * @throws MojoExecutionException
   */
  public void doDelete() throws MojoExecutionException {
	  try {
	      for (File file : files) {
	        PortalRestUtil.deleteAPIDoc(serverProfile, file);
	      }
	    }
    catch (IOException e) {
      throw new RuntimeException("Delete failure: " + e.getMessage());
    }
  }

  /**
   * Pulls a list of OpenAPI specs frpm a directory to be sent 
   * to a Developer Portal instance.
   * 
   * @throws MojoExecutionException
   */
  public void getOpenAPISpecs() throws MojoExecutionException {
    // Ensure we have a directory to read.
    if (serverProfile.getPortalDirectory() == null) {
      throw new MojoExecutionException(
        "Developer portal directory not found in profile");
    }

    // Scan the directory for files.
    String directory = serverProfile.getPortalDirectory();
    logger.info("Get OpenAPI Specs from " + directory);
    files = new File(directory).listFiles();
  }
}





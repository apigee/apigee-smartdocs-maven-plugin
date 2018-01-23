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

import com.apigee.smartdocs.config.rest.PortalRestUtil;
import com.apigee.smartdocs.config.utils.ServerProfile;
import com.apigee.smartdocs.config.utils.PortalField;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.Key;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.internal.LinkedTreeMap;
import java.util.Collection;
import java.util.HashMap;

/**                                                                                                                                     ¡¡
 * Goal to create API Models in Apigee Developer Portal
 * scope: org
 *
 * @author william.oconnor
 * @goal apimodel
 * @phase install
 */

public class APIModelMojo extends GatewayAbstractMojo {
  static Logger logger = LoggerFactory.getLogger(APIModelMojo.class);
  private static File[] files = null;

  public static final String ____ATTENTION_MARKER____ =
  "************************************************************************";

  enum OPTIONS {
    none, create, update, delete, sync, render
  }

  OPTIONS buildOption = OPTIONS.none;

  private ServerProfile serverProfile;

  public static class APIModel {
    @Key
    public String title;
  }

  /**
   * Constructor.
   */
  public APIModelMojo() {
    super();
  }

  public void init() throws MojoExecutionException, MojoFailureException {
    try {
      logger.info(____ATTENTION_MARKER____);
      logger.info("Smart Docs");
      logger.info(____ATTENTION_MARKER____);

      String options="";
      serverProfile = super.getProfile();

      options = super.getOptions();
      if (options != null) {
        buildOption = OPTIONS.valueOf(options);
      }
      if (buildOption == OPTIONS.none) {
        logger.info("Skipping APIModel (default action)");
        return;
      }

      logger.debug("Build option " + buildOption.name());
      logger.debug("Portal Path " + serverProfile.getPortalPath());

      // Ensure we have parameters (type, directory, url, path, credentials)
      if (serverProfile.getPortalFormat() == null) {
        throw new MojoExecutionException(
          "Developer portal file format not found in profile");
      }
      if (serverProfile.getPortalURL() == null) {
        throw new MojoExecutionException(
          "Developer portal URL not found in profile");
      }
      if (serverProfile.getPortalPath() == null) {
        throw new MojoExecutionException(
          "Developer portal path not found in profile");
      }
      if (serverProfile.getPortalUserName() == null) {
        throw new MojoExecutionException(
          "Developer portal username not found in profile");
      }
      if (serverProfile.getPortalPassword() == null) {
        throw new MojoExecutionException(
          "Developer portal password not found in profile");
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

    Logger logger = LoggerFactory.getLogger(APIModelMojo.class);

    try {
      init();
      if (buildOption == OPTIONS.none) {
        return;
      }
      
      if (buildOption == OPTIONS.create ||
        buildOption == OPTIONS.update) {
        doUpdate();
        addFieldData();
      }
      
      if (buildOption == OPTIONS.delete) {
        doDelete();
      }
      
      if (buildOption == OPTIONS.render) {
        doRender();
      }

      if (buildOption == OPTIONS.sync) {
        doUpdate();
        addFieldData();
        doRender();
        doDelete();
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
   */
  public void doUpdate() throws MojoExecutionException {
    try {
      for (File file : files) {
        PortalRestUtil.postAPIModel(serverProfile, file);
      }
      logger.info("Updated all models found in the OpenAPI Spec directory.");
    }
    catch (IOException e) {
      throw new RuntimeException("Update failure: " + e.getMessage());
    }
  }
  
  /**
   * Sends a render request for models that exist in the file system.
   */
  public void doRender() throws MojoExecutionException {
    try {
      for (File file : files) {
        PortalRestUtil.renderAPIModel(serverProfile, file);
      }
      logger.info("Rendered all models found in the OpenAPI Spec directory.");
    }
    catch (IOException e) {
      throw new RuntimeException("Render failure: " + e.getMessage());
    }
  }
  
  /**
   * Deletes models that exist in the API and not in the file system.
   */
  public void doDelete() throws MojoExecutionException {
    try {
      // Create a list of all specs we have on the file system.
      List<String> specNames = new ArrayList<String>();
      if (files != null && files.length > 0) {
        for (File file : files) {
          logger.info("FilePath: " + file.getPath());
          PortalRestUtil.SpecObject spec = PortalRestUtil.parseSpec(serverProfile, file);
          specNames.add(spec.getName());
        }
      }

      // Iterate over all models and if one does not exist on the file system, delete it.
      PortalRestUtil.ModelObjects modelObjectArray = PortalRestUtil.getAPIModels(serverProfile);
      for (PortalRestUtil.ModelObject mo : modelObjectArray.modelObjects) {
        if (!specNames.contains(mo.name)) {
          PortalRestUtil.deleteAPIModel(serverProfile, mo.name);
        }
      }
      logger.info("Deleted all models not found in the OpenAPI Spec directory.");
    }
    catch (IOException e) {
      throw new RuntimeException("Deletion failure: " + e.getMessage());
    }
  }

  /**
   * Pulls a list of OpenAPI specs frpm a directory to be sent 
   * to a Developer Portal instance.
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
  
  public void addFieldData() {
    try {
      logger.info("Add field data.");
      Map<String,PortalField> modelFields = serverProfile.getPortalModelFields();
      if (modelFields != null) {
        // Pull vocabulary based on name.
        PortalRestUtil.VocabularyObject vo = PortalRestUtil.getVocabulary(serverProfile, serverProfile.getPortalModelVocabulary());
      
        // Pull taxonomy terms in vocabulary.
        Collection<PortalRestUtil.TaxonomyTermObject> tos = PortalRestUtil.getTaxonomyTerms(serverProfile, vo.vid);
        
        for (File file : files) {
          PortalRestUtil.SpecObject spec = PortalRestUtil.parseSpec(serverProfile, file);
          logger.info("Pushing fields for " + spec.getTitle());
          for (PortalRestUtil.TaxonomyTermObject to: tos) {
            // Match file and taxonomy term.
            if (to.name.equals(spec.getName())) {
              HashMap hs = new HashMap();
              for (PortalField pf : modelFields.values()) {
                // Elements can be embedded within the info object, so
                // find the location, and extract the value.
                String[] pathParts = pf.getPath().split("\\|");
                LinkedTreeMap jo = spec.info;
                for (String pathPart : pathParts) {
                  // Only traverse down if the key exists.
                  if (jo.containsKey(pathPart)) {
                    Object o = jo.get(pathPart);
                    // If we still need to go deeper, we have a tree.
                    if (o instanceof LinkedTreeMap) {
                      jo = (LinkedTreeMap)o;
                    }
                    else {
                      // Otherwise, store the value in our hash.
                      hs.put(pf.getField(), o.toString());
                      logger.debug("Push " + pf.getField() + " as " + o.toString());
                    }
                  }
                  else {
                    // If we ever fail to find an item, break out.
                    break;
                  }
                }
              }
              
              if (!hs.isEmpty()) {
                // If we have values, set keys and push the update.
                hs.put("tid", to.tid);
                hs.put("vid", to.vid);
                hs.put("name", to.name);
                PortalRestUtil.updateTaxonomyTerm(serverProfile, hs);
              }
              // Remove our matched term so we don't have to check it again.
              tos.remove(to);
              break;
            }
          } // End loop over taxonomy terms (models).
        } // End loop over files.
      }
    }
    catch (IOException e) {
      throw new RuntimeException("Adding field data failure: " + e.getMessage());
    }
  }

}





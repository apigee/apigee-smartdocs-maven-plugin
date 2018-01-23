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

package com.apigee.smartdocs.config.rest;

import com.apigee.smartdocs.config.utils.ServerProfile;
import java.io.File;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;

import com.google.api.client.util.Key;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONValue;
import org.yaml.snakeyaml.Yaml;

public class PortalRestUtil {

  static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  static final JsonFactory JSON_FACTORY = new JacksonFactory();
  static String versionRevision;
  static Logger logger = LoggerFactory.getLogger(PortalRestUtil.class);
  static String accessToken = null;

  static HttpRequestFactory REQUEST_FACTORY = HTTP_TRANSPORT
          .createRequestFactory(new HttpRequestInitializer() {
            // @Override
            public void initialize(HttpRequest request) {
              request.setParser(JSON_FACTORY.createJsonObjectParser());
              XTrustProvider.install();
              FakeHostnameVerifier _hostnameVerifier = new FakeHostnameVerifier();
              // Install the all-trusting host name verifier:
              HttpsURLConnection.setDefaultHostnameVerifier(_hostnameVerifier);
            }
          });

  // Header values to ensure headers are set and then stored.
  public static Boolean headersSet = false;
  public static HttpHeaders headers = null;

  // Class to handle our authentication response.
  public static class AuthObject {

    @Key
    public String token;
    public String sessid;
    public String session_name;
  }

  public static class SpecObject {

    public LinkedTreeMap info;

    public String getName() {
      return info.get("title").toString().replace(" ", "-");
    }

    public String getTitle() {
      return info.get("title").toString();
    }

    public String getDescription() {
      if (info.get("description") != null) {
        return info.get("description").toString();
      }
      return "";
    }
    
    @Override
    public String toString() {
      return "(SpecObject) Name: " + getName() + "; Title: " + getTitle(); 
    }
  }

  public static class ModelObjects {

    public List<ModelObject> modelObjects = new ArrayList<ModelObject>();

    public void addModelObject(ModelObject modelObject) {
      modelObjects.add(modelObject);
    }
  }

  public static class ModelObject {

    public String id;
    public String name;
    public String displayName;
    public String description;
    public String latestRevisionNumber;
    public String createdTime;
    public String modifiedTime;
  }

  public static class VocabularyObject {

    public String vid;
  }

  public static class TaxonomyTermObject {

    public String tid;
    public String vid;
    public String name;
    public String description;
  }

  /**
   * Helps with deserializing the object return into a list of modelobject
   * items.
   */
  public static class ModelObjectsDeserializer implements JsonDeserializer<ModelObjects> {

    public ModelObjects deserialize(JsonElement jElement, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      ModelObjects mos = new ModelObjects();
      JsonObject jObject = jElement.getAsJsonObject();
      Set<Map.Entry<String, JsonElement>> entries = jObject.entrySet();//will return members of your object
      for (Map.Entry<String, JsonElement> entry : entries) {
        Gson gson = new Gson();
        ModelObject mo = gson.fromJson(entry.getValue(), ModelObject.class);
        mos.addModelObject(mo);
      }

      return mos;
    }
  }

  // Get headers that have been set.
  public static HttpHeaders getHeaders() {
    return headers;
  }

  // Store/update headers.
  public static HttpHeaders setHeaders(HttpHeaders tempHeaders) {
    headers = tempHeaders;
    return headers;
  }

  /**
   * Authenticate against the Developer portal and set necessary headers to
   * facilitate additional transactions.
   */
  public static HttpResponse authenticate(ServerProfile profile) throws IOException {
    HttpResponse response = null;
    if (headersSet == false) {
      String payload = "{\"username\": \"" + profile.getPortalUserName()
              + "\", \"password\":\"" + profile.getPortalPassword() + "\"}";
      ByteArrayContent content = new ByteArrayContent("application/json",
              payload.getBytes());

      HttpRequest restRequest = REQUEST_FACTORY
              .buildPostRequest(new GenericUrl(
                              profile.getPortalURL() + "/" + profile.getPortalPath()
                              + "/user/login.json"), content);
      restRequest.setReadTimeout(0);

      try {
        // Call execute directly since we don't have headers yet.
        response = restRequest.execute();

        InputStream source = response.getContent(); //Get the data in the entity
        Reader reader = new InputStreamReader(source);

        Gson gson = new Gson();
        AuthObject auth = gson.fromJson(reader, AuthObject.class);
        headersSet = true;

        HttpHeaders tempHeaders = new HttpHeaders();
        tempHeaders.setCookie(auth.session_name + "=" + auth.sessid);
        tempHeaders.set("X-CSRF-Token", auth.token);
        setHeaders(tempHeaders);

      } catch (HttpResponseException e) {
        logger.error(e.getMessage());
        // Throw an error as there is no point in continuing.
        throw e;
      }
    }

    return response;
  }

  /**
   * Retrieve all existing models.
   */
  public static ModelObjects getAPIModels(ServerProfile profile) throws IOException {
    HttpResponse response = null;
    try {
      // First authenticate.
      authenticate(profile);

      HttpRequest restRequest = REQUEST_FACTORY
              .buildGetRequest(new GenericUrl(
                              profile.getPortalURL() + "/" + profile.getPortalPath()
                              + "/smartdocs.json"));
      logger.info("Retrieving all models.");

      response = PortalRestUtil.executeRequest(restRequest);

      Gson gson = new Gson();
      InputStream source = response.getContent(); //Get the data in the entity
      Reader reader = new InputStreamReader(source);

      GsonBuilder gsonBldr = new GsonBuilder();
      gsonBldr.registerTypeAdapter(ModelObjects.class, new ModelObjectsDeserializer());
      ModelObjects models = gsonBldr.create().fromJson(reader, ModelObjects.class);

      return models;
    } catch (HttpResponseException e) {
      if (e.getStatusCode() == 404) {
        logger.info("Model does not currently exist.");
        return null;
      } else {
        throw e;
      }
    }
  }

  /**
   * Retrieve an existing model element. Returns null if the model does not
   * currently exist.
   */
  public static ModelObject getAPIModel(ServerProfile profile, File file) throws IOException {
    HttpResponse response = null;
    try {
      // First authenticate.
      authenticate(profile);

      SpecObject spec = parseSpec(profile, file);
      HttpRequest restRequest = REQUEST_FACTORY
              .buildGetRequest(new GenericUrl(
                              profile.getPortalURL() + "/" + profile.getPortalPath()
                              + "/smartdocs/" + spec.getName() + ".json"));
      logger.info("Retrieve " + spec.getTitle() + " model.");

      response = PortalRestUtil.executeRequest(restRequest);

      Gson gson = new Gson();
      InputStream source = response.getContent(); //Get the data in the entity
      Reader reader = new InputStreamReader(source);
      ModelObject model = gson.fromJson(reader, ModelObject.class);
      return model;
    } catch (HttpResponseException e) {
      if (e.getStatusCode() == 404) {
        logger.info("Model does not currently exist.");
        return null;
      } else {
        throw e;
      }
    }
  }

  /**
   * Create a base model. The model must exist prior to sending an OpenAPI spec
   * or the OpenAPI spec send will fail.
   */
  public static void createAPIModel(ServerProfile profile, File file) throws IOException {
    HttpResponse response = null;
    try {
      // First authenticate.
      authenticate(profile);

      SpecObject spec = parseSpec(profile, file);
      ByteArrayContent content = getAPIModelContent(profile, spec);
      HttpRequest restRequest = REQUEST_FACTORY
              .buildPostRequest(new GenericUrl(
                              profile.getPortalURL() + "/" + profile.getPortalPath()
                              + "/smartdocs.json"), content);
      logger.info("Creating " + spec.getTitle() + " model.");

      response = PortalRestUtil.executeRequest(restRequest);
    } catch (HttpResponseException e) {
      throw e;
    }
  }

  /**
   * Update an existing model. Resending a create command will force a new
   * revision of the model. This simply updates parameters of a model without
   * modifying the version information.
   */
  public static void updateAPIModel(ServerProfile profile, File file) throws IOException {
    HttpResponse response = null;
    try {
      // First authenticate.
      authenticate(profile);

      SpecObject spec = parseSpec(profile, file);
      ByteArrayContent content = getAPIModelContent(profile, spec);
      HttpRequest restRequest = REQUEST_FACTORY
              .buildPutRequest(new GenericUrl(
                              profile.getPortalURL() + "/" + profile.getPortalPath()
                              + "/smartdocs/" + spec.getName() + ".json"), content);
      logger.info("Updating " + spec.getTitle() + " model.");

      response = PortalRestUtil.executeRequest(restRequest);
    } catch (HttpResponseException e) {
      throw e;
    }
  }

  /**
   * Delete an existing model.
   */
  public static void deleteAPIModel(ServerProfile profile, String specName) throws IOException {
    HttpResponse response = null;
    try {
      // First authenticate.
      authenticate(profile);

      HttpRequest restRequest = REQUEST_FACTORY
              .buildDeleteRequest(new GenericUrl(
                              profile.getPortalURL() + "/" + profile.getPortalPath()
                              + "/smartdocs/" + specName + ".json"));
      logger.info("Deleting " + specName + " model.");

      response = PortalRestUtil.executeRequest(restRequest);
    } catch (HttpResponseException e) {
      throw e;
    }
  }

  /**
   * Render an existing model.
   */
  public static void renderAPIModel(ServerProfile profile, File file) throws IOException {
    HttpResponse response = null;
    try {
      // First authenticate.
      authenticate(profile);

      SpecObject spec = parseSpec(profile, file);
      ByteArrayContent content = new ByteArrayContent("application/json",
              "".getBytes());
      HttpRequest restRequest = REQUEST_FACTORY
              .buildPostRequest(new GenericUrl(
                              profile.getPortalURL() + "/" + profile.getPortalPath()
                              + "/smartdocs/" + spec.getName() + "/render.json"), content);
      logger.info("Rendering " + spec.getTitle() + " OpenAPI spec.");

      response = PortalRestUtil.executeRequest(restRequest);
    } catch (HttpResponseException e) {
      throw e;
    }
  }

  /**
   * Helper function to build the body for model creations and updates.
   */
  private static ByteArrayContent getAPIModelContent(ServerProfile profile,
          SpecObject spec)
          throws IOException {

    try {
      // First authenticate.
      authenticate(profile);

      String description = "";
      if (spec.getDescription() != null) {
        description = spec.getDescription();
      }

      String payload = "{"
              + "\"name\": \"" + spec.getName() + "\","
              + "\"display_name\":\"" + spec.getTitle() + "\","
              + "\"description\":\"" + description + "\""
              + "}";
      ByteArrayContent content = new ByteArrayContent("application/json",
              payload.getBytes());

      return content;
    } catch (HttpResponseException e) {
      throw e;
    }
  }

  /**
   * Posts the OpenAPI Spec to a APIModel in Developer Portal.
   */
  public static void postAPIModel(ServerProfile profile, File file) throws IOException {
    HttpResponse response = null;
    try {
      // First authenticate.
      authenticate(profile);

      if (getAPIModel(profile, file) == null) {
        createAPIModel(profile, file);
      } else {
        updateAPIModel(profile, file);
      }
      
      String contentType = "application/json";
      String fileType = "json";
      if (profile.getPortalFormat().equals("yaml")) {
        contentType = "application/x-yaml";
        fileType = "yaml";
      }

      FileContent tempFileContent = new FileContent(contentType, file);
      SpecObject spec = parseSpec(profile, file);

      // Then build the OpenAPI Spec command.
      MultipartContent.Part filePart = new MultipartContent.Part(tempFileContent)
              .setHeaders(new HttpHeaders().set(
                              "Content-Disposition",
                              String.format("form-data; name=\"api_definition\"; filename=\"%s\"", file.getName())
                      ));

      MultipartContent.Part typePart = new MultipartContent.Part(
              new ByteArrayContent(null, fileType.getBytes())
      )
              .setHeaders(new HttpHeaders().set(
                              "Content-Disposition",
                              "form-data; name=\"type\""
                      ));

      MultipartContent.Part namePart = new MultipartContent.Part(
              new ByteArrayContent(null, spec.getTitle().getBytes())
      )
              .setHeaders(new HttpHeaders().set(
                              "Content-Disposition",
                              "form-data; name=\"name\""
                      ));

      MultipartContent content = new MultipartContent().setMediaType(
              new HttpMediaType("multipart/form-data")
              .setParameter("boundary", "__END_OF_PART__")
      );
      content.addPart(filePart);
      content.addPart(typePart);
      content.addPart(namePart);

      HttpRequest restRequest = REQUEST_FACTORY
              .buildPostRequest(new GenericUrl(
                              profile.getPortalURL() + "/" + profile.getPortalPath()
                              + "/smartdocs/" + spec.getName() + "/import.json"), content);
      logger.info("Posting " + spec.getTitle() + " OpenAPI spec.");

      response = PortalRestUtil.executeRequest(restRequest);

      // Finally push the OpenAPI Spec and communicate response.
    } catch (HttpResponseException e) {
      logger.error(e.getMessage());
      throw e;
    }
  }

  /**
   * Helper function to parse a json formatted OpenAPI spec and turn it into an
   * object containing the properties we will reuse.
   */
  public static SpecObject parseSpec(ServerProfile profile, File file) throws FileNotFoundException {
    
    SpecObject spec = new SpecObject();
    try {
      Reader reader = null;
      if (profile.getPortalFormat().equals("yaml")) {

        // Read in the YAML file.
        FileContent tempFileContent = new FileContent("application/x-yaml", file);
        Reader yamlReader = new InputStreamReader(tempFileContent.getInputStream());
        Yaml yaml = new Yaml();
        Object obj = yaml.load(yamlReader);

        // Convert it to a JSON file for consistent handling.
        String YAMLString = JSONValue.toJSONString(obj);
        reader = new StringReader(YAMLString);
      }
      else {
        FileContent tempFileContent = new FileContent("application/json", file);
        reader = new InputStreamReader(tempFileContent.getInputStream());
      }
      Gson gson = new Gson();
      spec = gson.fromJson(reader, SpecObject.class);
    }
    catch (IOException e) {
      logger.error(e.getMessage());
    }

    return spec;
  }

  public static VocabularyObject getVocabulary(ServerProfile profile, String machineName)
          throws IOException {
    HttpResponse response = null;
    try {
      // First authenticate.
      authenticate(profile);

      ByteArrayContent content = new ByteArrayContent("application/json",
              ("{\"machine_name\": \"" + machineName + "\"}").getBytes());
      HttpRequest restRequest = REQUEST_FACTORY
              .buildPostRequest(new GenericUrl(
                              profile.getPortalURL() + "/" + profile.getPortalPath()
                              + "/taxonomy_vocabulary/retrieveByMachineName.json"), content);
      logger.info("Retrieving " + machineName + " vocabulary.");

      response = PortalRestUtil.executeRequest(restRequest);

      Gson gson = new Gson();
      InputStream source = response.getContent(); //Get the data in the entity
      Reader reader = new InputStreamReader(source);
      VocabularyObject vo = gson.fromJson(reader, VocabularyObject.class);
      return vo;
    } catch (HttpResponseException e) {
      throw e;
    }
  }

  public static Collection<TaxonomyTermObject> getTaxonomyTerms(ServerProfile profile, String vid)
          throws IOException {
    HttpResponse response = null;
    try {
      // First authenticate.
      authenticate(profile);

      ByteArrayContent content = new ByteArrayContent("application/json",
              ("{\"vid\": \"" + vid + "\"}").getBytes());
      HttpRequest restRequest = REQUEST_FACTORY
              .buildPostRequest(new GenericUrl(
                              profile.getPortalURL() + "/" + profile.getPortalPath()
                              + "/taxonomy_vocabulary/getTree.json"), content);
      logger.info("Retrieving taxonomy terms.");

      response = PortalRestUtil.executeRequest(restRequest);

      Gson gson = new Gson();
      InputStream source = response.getContent(); //Get the data in the entity
      Reader reader = new InputStreamReader(source);
      Type collectionType = new TypeToken<Collection<TaxonomyTermObject>>() {
      }.getType();
      Collection<TaxonomyTermObject> tos = gson.fromJson(reader, collectionType);
      return tos;
    } catch (HttpResponseException e) {
      throw e;
    }
  }

  /**
   * Update an existing taxonomy term (model fields).
   *
   * This updates an existing taxonomy term (model) with field data.
   */
  public static void updateTaxonomyTerm(ServerProfile profile, HashMap hs) throws IOException {
    HttpResponse response = null;
    try {
      // First authenticate.
      authenticate(profile);

      String tid = hs.remove("tid").toString();
      String vid = hs.remove("vid").toString();
      String name = hs.remove("name").toString();
      Iterator it = hs.entrySet().iterator();
      String updateFields = "";
      while (it.hasNext()) {
        Map.Entry me = (Map.Entry) it.next();
        updateFields += ",\"" + me.getKey().toString() + "\":{\"und\":[{\"value\":\"" + me.getValue().toString() + "\"}]}";
      }
      ByteArrayContent content = new ByteArrayContent("application/json",
              ("{"
              + "\"tid\": \"" + tid + "\","
              + "\"vid\": \"" + vid + "\","
              + "\"name\": \"" + name + "\""
              + updateFields
              + "}").getBytes());

      HttpRequest restRequest = REQUEST_FACTORY
              .buildPutRequest(new GenericUrl(
                              profile.getPortalURL() + "/" + profile.getPortalPath()
                              + "/taxonomy_term/" + tid + ".json"), content);

      logger.info("Updating taxonomy term with tid " + tid + ".");
      response = PortalRestUtil.executeRequest(restRequest);
    } catch (HttpResponseException e) {
      throw e;
    }
  }

  public static HttpResponse executeRequest(HttpRequest restRequest) throws IOException {
    HttpResponse response = null;
    try {
      restRequest.setHeaders(getHeaders());
      restRequest.setReadTimeout(0);
      response = restRequest.execute();
      logger.info(response.getStatusMessage());
    } catch (HttpResponseException e) {
      throw e;
    }
    return response;
  }

}

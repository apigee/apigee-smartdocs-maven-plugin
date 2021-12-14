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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONValue;
import org.yaml.snakeyaml.Yaml;

import com.apigee.smartdocs.config.utils.ServerProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.Key;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

public class PortalRestUtil {

  static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  static final HttpTransport APACHE_HTTP_TRANSPORT = new ApacheHttpTransport();
  static final JsonFactory JSON_FACTORY = new JacksonFactory();
  static String versionRevision;
  static Logger logger = LogManager.getLogger(PortalRestUtil.class);
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
 
  static HttpRequestFactory APACHE_REQUEST_FACTORY = APACHE_HTTP_TRANSPORT
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

    public String getName(String portalModelNameConfig) {
      if(portalModelNameConfig == null) {
        return info.get("title").toString().replace(" ", "-");
      } else {
        return getConfigModelName(portalModelNameConfig);
      }
    }

    private String getConfigModelName(String portalModelNameConfig) {
      //default return
      String returnString = getName(null);
      if(portalModelNameConfig == null) {
        return returnString;
      }
      //Generate Model Name From Config
      ArrayList<String> al = new ArrayList<String>();
      String[] nameParts = portalModelNameConfig.split("\\^");
      LinkedTreeMap jo = info;
      for (String namePart : nameParts) {
        //Reset head of tree to info
        jo = info;
        //Split each config name into model information
        String[] namePartPaths = namePart.split("\\|");
        for (String namePartPath : namePartPaths) {
          // Only traverse down if the key exists.
          if (jo.containsKey(namePartPath)) {
            Object o = jo.get(namePartPath);
            // If we still need to go deeper, we have a tree.
            if (o instanceof LinkedTreeMap) {
              jo = (LinkedTreeMap) o;
            } else {
              // Otherwise, store the value in our ArrayList
              al.add(o.toString().replace(".", "-").replace(" ","-"));
            }
          }
        }
      }
      if(al.isEmpty()) {
        return returnString;
      }
      returnString = "";
      Iterator it = al.iterator();
      while (it.hasNext()) {
        String namePart = (String) it.next();
        returnString += namePart;
        //Separate fields with dash
        if(it.hasNext()) {
            returnString += "-";
        }
      }
      //Clean the return string to only include Alphanumeric characters and the dash character
      returnString = returnString.replaceAll("[^A-Za-z0-9-]","");
      //set Max length to be 255
      if(returnString.length() > 255) {
        returnString = returnString.substring(0,255);
      }
      logger.debug("API Model Name: " + returnString);
      return returnString;
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
      return "(SpecObject) Name: " + getName(null) + "; Title: " + getTitle();
    }
  }

  
  public static class APIDocResponseObject {

	    public JSONAPI jsonapi;
	    public List<Data> data;
	    public Object links;
  }
  
  public static class APIDocObject {
	    public JSONAPI jsonapi;
	    public Data data;
	    public Object links;
}
  
  public static class ImageDocObject {
	    public JSONAPI jsonapi;
	    public Data data;
	    public Object links;
}
  
  public static class APIErrorObject {
	  public JSONAPI jsonapi;
	  public List<Error> errors;
  }
  
  public static class JSONAPI {
	  public String version;
	  public Object meta;
  }
  
  public static class Error{
	  public String title;
	  public String status;
	  public String detail;
  }
  
  public static class Data {
	  public String type;
	  public String id;
	  public Attributes attributes;
	  public Relationships relationships;
	  public Object links;
  }
  
  public static class Attributes {
	  public boolean status;
	  public String title;
	  public String name;
	  public Body body;
	  public String field_apidoc_spec_file_source;
	  public Object relationships;
	  public FileLink file_link;
	  public Object links;
  }
  
  public static class Body{
	  public String value;
	  public String format;
  }
  
  public static class Relationships{
	  public Relationships_Spec field_apidoc_spec;
	  public Relationships_Spec field_image;
  }
  
  public static class Relationships_Data{
	  public String type;
	  public String id;
  }
  
  public static class Relationships_Spec{
	  public Relationships_Data data;
  }
  
  public static class FileLink{
	  public String uri;
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
   * Run Cron
   */
  public static void runCron(ServerProfile profile) throws IOException {
    try {
      // First authenticate.
      authenticate(profile);

      HttpRequest restRequest = REQUEST_FACTORY
              .buildGetRequest(new GenericUrl(profile.getPortalURL() + "/cron.php?cron_key="+ profile.getPortalCronKey()));
      restRequest.setReadTimeout(0);
      logger.info("Running Cron");

      PortalRestUtil.executeRequest(restRequest);
    } catch (HttpResponseException e) {
      throw e;
    }
  }
  

  /**
   * Helper function to build the body for API Doc creations and updates.
   */
  private static ByteArrayContent constructAPIDocRequestBody(ServerProfile profile, SpecObject spec, String uuid, String docId, String imageId, boolean isUpdate) throws IOException {
	  boolean hasImage = false;
	  File imageFile = null;
	  Gson gson = new Gson();
	  JsonObject body = new JsonObject();
	  if (spec.getDescription() != null) {
		  body.addProperty("value", StringEscapeUtils.escapeJava(spec.getDescription()));
	  }
	  body.addProperty("format", profile.getPortalAPIDocFormat());

	  JsonObject attributes = new JsonObject();
	  attributes.addProperty("status", true);
	  attributes.addProperty("title", spec.getTitle());
	  attributes.add("body", body);
	  attributes.addProperty("field_apidoc_spec_file_source", "file");
	  
	  JsonObject relationships = new JsonObject();
	  
	  //config
	  if(profile.getConfigFile()!=null && !profile.getConfigFile().equalsIgnoreCase("")) {
		  FileContent tempFileContent = new FileContent("application/json", new File(profile.getConfigFile()).getAbsoluteFile());
	      Reader reader = new InputStreamReader(tempFileContent.getInputStream());
		  Map<String, Object> result = new ObjectMapper().readValue(reader, HashMap.class);
		  if(result!=null && result.size()>0) {
			  //For Custom Fields
			  Map<String, Object> fieldsMap = (Map<String, Object>) result.get("fields");
			  if(fieldsMap!=null && fieldsMap.size()>0) {
				  for (String key : fieldsMap.keySet()) {
					  if(fieldsMap.get(key) instanceof List){
						  attributes.add(key, gson.toJsonTree(fieldsMap.get(key)));
					  }
					  //no need to add to attributes for "field_image"
					  else if (key!=null && key.equals("field_image")){
						  hasImage = true; 
						  imageFile = new File(profile.getPortalDirectory()+"/"+(String)fieldsMap.get(key));
					  }
					  else
						  attributes.addProperty(key, (String)fieldsMap.get(key)); 
				  }
			  }
			  
			  //For Custom taxonomy_terms
			  List<Map<String, Object>> taxonomyTerm = (List<Map<String, Object>>) result.get("taxonomy_terms");
			  if(taxonomyTerm!=null && taxonomyTerm.size()>0) {
				  for (Map<String, Object> map : taxonomyTerm) {
					  Map<String, List<String>> taxonomyTermsIdMap = getTaxonomyTermId(profile, map);
					  if(taxonomyTermsIdMap!=null && taxonomyTermsIdMap.size()>0) {
						  JsonArray taxonomyData = new JsonArray();
						  for (String key : taxonomyTermsIdMap.keySet()) {
							  List<String> list = taxonomyTermsIdMap.get(key);
							  if(list!=null && list.size()>0) {
								  for (String id : taxonomyTermsIdMap.get(key)) {
									  JsonObject taxonomyId = new JsonObject();
									  taxonomyId.addProperty("type", "taxonomy_term--"+(String)map.get("vocabulary"));
									  taxonomyId.addProperty("id", id);
									  taxonomyData.add(taxonomyId);
								  }
							  }
							  JsonObject taxonomyDataObj = new JsonObject();
							  taxonomyDataObj.add("data", taxonomyData);
							  relationships.add(key, taxonomyDataObj);
						}
					  }
				  }
			  }
		  }
	  }	 
	  JsonObject field_apidoc_spec_data = new JsonObject();
	  field_apidoc_spec_data.addProperty("type", "file--file");
	  if(!isUpdate)
		  field_apidoc_spec_data.addProperty("id", uuid);
	  else
		  field_apidoc_spec_data.addProperty("id", docId);
	  JsonObject field_apidoc_spec = new JsonObject();
	  field_apidoc_spec.add("data", field_apidoc_spec_data);
	  
	  relationships.add("field_apidoc_spec", field_apidoc_spec);
	  
	  //field_image
	  if(hasImage) {
		  JsonObject field_image_data = new JsonObject();
		  field_image_data.addProperty("type", "media--image");
		  if(!isUpdate) {
			  ImageDocObject imageDoc = importImage(profile, imageFile);
			  String mediaImageId = importMediaImage(profile, imageFile.getName(), imageDoc.data.id);
			  field_image_data.addProperty("id", mediaImageId);
		  }
		  else {
			  field_image_data.addProperty("id", imageId);
		  }
		  JsonObject field_image = new JsonObject();
		  field_image.add("data", field_image_data);
	
		  relationships.add("field_image", field_image);
	  }

	  JsonObject data = new JsonObject();
	  data.addProperty("type", "node--apidoc");
	  if(isUpdate)
		  data.addProperty("id", uuid); //set this only for update call
	  data.add("attributes", attributes);
	  data.add("relationships", relationships); 
	  
	  JsonObject payloadData = new JsonObject();
	  payloadData.add("data", data);
	  
	  String payload = gson.toJson(payloadData);
	  
	  logger.debug("Request payload: \n" + payload);
	  ByteArrayContent content = new ByteArrayContent("application/vnd.api+json", payload.getBytes());
	  return content; 
  }
      
  /**
   * Posts the OpenAPI Spec to a APIDoc in Developer Portal.
   */
  public static void postAPIDoc(ServerProfile profile, File file, boolean isCreate) throws IOException {
    try {
      APIDocResponseObject respObj = getAPIDoc(profile, file);
      if (respObj == null) {
    	  APIDocObject obj = importAPIDoc(profile, file);
    	  if(obj == null)
    		  throw new IOException("Error occured while importing spec");
    	  else
    		  createAPIDoc(profile, file, obj);
      } else {
    	  if(isCreate) {
    		  logger.info("Skipping as the spec already exist. Please use \"sync\" or \"update\" options");
    		  return;
    	  }
        updateAPIDoc(profile, file, respObj);
      }
    } catch (HttpResponseException e) {
      logger.error(e.getMessage());
      throw e;
    }
  }
  
  /**
   * Get the Taxonomy Id
   * @param profile
   * @param taxonomyTermsMap
   * @return
   * @throws IOException
   */
  public static Map<String, List<String>> getTaxonomyTermId(ServerProfile profile, Map<String, Object> taxonomyTermsMap) throws IOException{
	  Map<String, List<String>> taxonomyTermIdMap = new HashMap<String, List<String>>();
	  try{
		  String vocabulary = (String) taxonomyTermsMap.get("vocabulary");
		  String field = (String) taxonomyTermsMap.get("field");
		  List<String> dataList = (List<String>) taxonomyTermsMap.get("data");
		  if(vocabulary!=null && !vocabulary.equals("")) {
			  logger.info("Retrieving taxonomy_term for " + vocabulary);
			  HttpRequest restRequest = REQUEST_FACTORY
		              .buildGetRequest(new GenericUrl(profile.getPortalURL() + "/jsonapi/taxonomy_term/"+vocabulary));
		      HttpHeaders headers = restRequest.getHeaders();
		      headers.setAccept("application/vnd.api+json");
		      headers.setBasicAuthentication(profile.getPortalUserName(), profile.getPortalPassword());
		      restRequest.setReadTimeout(0);
		      HttpResponse response = restRequest.execute();
		      Gson gson = new Gson();
		      Reader reader = new InputStreamReader(response.getContent());
		      APIDocResponseObject model = gson.fromJson(reader, APIDocResponseObject.class);
		      List<String> idList = new ArrayList<String>();
		      List<String> termList = new ArrayList<String>();
		      for (Data data : model.data) {
		    	  termList.add(data.attributes.name);
		    	  if(dataList.contains(data.attributes.name)) {
		    		  idList.add(data.id);
		    	  }
		      }
		      dataList.removeAll(termList);
		      //Throw exception if the taxonomy terms from the config file does not exist
		      if(dataList!=null && dataList.size()>0) {
		    	  throw new IOException("Terms "+ dataList +" does not exist");
		      }
		    taxonomyTermIdMap.put(field, idList);
		  }
	  }catch (HttpResponseException e) {
		  throw new IOException(e.getStatusMessage());
	    }
	  return taxonomyTermIdMap;
  }
  
  /**
   * Retrieve an existing API Doc. Returns null if the doc does not exist
   */
  public static APIDocResponseObject getAPIDoc(ServerProfile profile, File file) throws IOException {
    HttpResponse response = null;
    try {
	      SpecObject spec = parseSpec(profile, file);  
	      logger.info("Getting API doc for "+ spec.getTitle());
	      HttpRequest restRequest = REQUEST_FACTORY
	              .buildGetRequest(new GenericUrl(profile.getPortalURL() + "/jsonapi/node/apidoc?filter[title]=" + spec.getTitle()));
	      HttpHeaders headers = restRequest.getHeaders();
	      headers.setAccept("application/vnd.api+json");
	      headers.setBasicAuthentication(profile.getPortalUserName(), profile.getPortalPassword());
	      logger.info("Retrieving " + spec.getTitle() + " doc.");
	      restRequest.setReadTimeout(0);
	      response = restRequest.execute();
	      Gson gson = new Gson();
	      Reader reader = new InputStreamReader(response.getContent());
	      APIDocResponseObject model = gson.fromJson(reader, APIDocResponseObject.class);
	      if(model != null && model.data!=null && model.data.size()>0) {
	    	  logger.info("API Doc uuid:" + model.data.get(0).id);
		      return model;
	      } else {
	    	  logger.info("API Doc: "+ spec.getTitle()+" does not exist");
	    	  return null;
	      }
	      
    } catch (HttpResponseException e) {
    	throw e;
    }
  }
  
  /**
   * Import an API Doc
   */
  public static APIDocObject importAPIDoc(ServerProfile profile, File file) throws IOException {
    HttpResponse response = null;
    try {
	      SpecObject spec = parseSpec(profile, file);  
	      
	      logger.info("Importing spec..");
	      byte[] fileBytes = Files.readAllBytes(file.toPath());
		  ByteArrayContent fileContent = new ByteArrayContent("application/octet-stream", fileBytes);
		  HttpRequest restRequest = REQUEST_FACTORY
	              .buildPostRequest(new GenericUrl(profile.getPortalURL() + "/jsonapi/node/apidoc/field_apidoc_spec"), fileContent);
		  HttpHeaders headers = restRequest.getHeaders();
		  headers.setAccept("application/vnd.api+json");
		  headers.set("Content-Disposition", "file; filename=\""+spec.getTitle()+"."+profile.getPortalFormat()+"\"");
	      headers.setBasicAuthentication(profile.getPortalUserName(), profile.getPortalPassword());
	      restRequest.setReadTimeout(0);
	      response = restRequest.execute();
		  logger.info("Spec import complete..");
	      Gson gson = new Gson();
	      Reader reader = new InputStreamReader(response.getContent());
	      APIDocObject model = gson.fromJson(reader, APIDocObject.class);
	      if(model != null && model.data!=null) {
	    	  logger.info("File uuid:" + model.data.id);
		      return model;
	      }
	      return null;
	      
    } catch (HttpResponseException e) {
    	throw new IOException(exceptionHandler(e));
    }
  }
  
  /**
   * Import an image
   */
  public static ImageDocObject importImage(ServerProfile profile, File imageFile) throws IOException {
    HttpResponse response = null;
    try {	      
	      logger.info("Importing image..");
	      byte[] fileBytes = Files.readAllBytes(imageFile.toPath());
		  ByteArrayContent fileContent = new ByteArrayContent("application/octet-stream", fileBytes);
		  HttpRequest restRequest = REQUEST_FACTORY
	              .buildPostRequest(new GenericUrl(profile.getPortalURL() + "/jsonapi/media/image/field_media_image"), fileContent);
		  HttpHeaders headers = restRequest.getHeaders();
		  headers.setAccept("application/vnd.api+json");
		  headers.set("Content-Disposition", "file; filename=\""+imageFile.getName()+"\"");
	      headers.setBasicAuthentication(profile.getPortalUserName(), profile.getPortalPassword());
	      restRequest.setReadTimeout(0);
	      response = restRequest.execute();
		  logger.info("Image import complete..");
	      Gson gson = new Gson();
	      Reader reader = new InputStreamReader(response.getContent());
	      
	      ImageDocObject model = gson.fromJson(reader, ImageDocObject.class);
	      if(model != null && model.data!=null) {
	    	  logger.info("Image uuid:" + model.data.id);
		      return model;
	      }
	      return null;
	      
    } catch (HttpResponseException e) {
    	throw new IOException(exceptionHandler(e));
    }
  }
  
  /**
   * Configure media-image
   */
  
  public static String importMediaImage(ServerProfile profile, String fileName, String imageId) throws IOException {
	  HttpResponse response = null;
	  try {
		    String payload = "{\n"
				+ "    \"data\": {\n"
				+ "        \"type\": \"media--image\",\n"
				+ "        \"attributes\": {\n"
				+ "            \"name\": \""+fileName+"\"\n"
				+ "        },\n"
				+ "        \"relationships\": {\n"
				+ "            \"field_media_image\": {\n"
				+ "                \"data\": {\n"
				+ "                    \"type\": \"file--file\",\n"
				+ "                    \"id\": \""+imageId+"\"\n"
				+ "                }\n"
				+ "            }\n"
				+ "        }\n"
				+ "    }\n"
				+ "}";
			  ByteArrayContent content = new ByteArrayContent("application/vnd.api+json", payload.getBytes());
			  HttpRequest restRequest = REQUEST_FACTORY
			          .buildPostRequest(new GenericUrl(profile.getPortalURL() + "/jsonapi/media/image"), content);
			  HttpHeaders headers = restRequest.getHeaders();
			  headers.setAccept("application/vnd.api+json");
			  headers.setBasicAuthentication(profile.getPortalUserName(), profile.getPortalPassword());
			  restRequest.setReadTimeout(0);
			  response = restRequest.execute();
			  Gson gson = new Gson();
			  Reader reader = new InputStreamReader(response.getContent());
			  ImageDocObject model = gson.fromJson(reader, ImageDocObject.class);
			  if(model != null && model.data!=null) {
				  logger.info("media--image uuid:" + model.data.id);
			      return model.data.id;
			  }
	  	}
	  catch (HttpResponseException e) {
	    	throw new IOException(exceptionHandler(e));
	    }
	  return null;
  }
  
  /**
   * Import an API Doc
   */
  public static void updateAPIDoc(ServerProfile profile, File file, APIDocResponseObject doc) throws IOException {
    try {
	      SpecObject spec = parseSpec(profile, file);  
	      
	      logger.info("Update API catalog");
	      ByteArrayContent content = constructAPIDocRequestBody(profile, spec, doc.data.get(0).id, doc.data.get(0).relationships.field_apidoc_spec.data.id, doc.data.get(0).relationships.field_image.data.id, true);
	      HttpRequest restPatchRequest = APACHE_REQUEST_FACTORY.buildRequest(HttpMethods.PATCH, new GenericUrl(profile.getPortalURL() + "/jsonapi/node/apidoc/"+doc.data.get(0).id),
					content);
	      HttpHeaders patchHeaders = restPatchRequest.getHeaders();
	      patchHeaders.setAccept("application/vnd.api+json");
	      patchHeaders.setBasicAuthentication(profile.getPortalUserName(), profile.getPortalPassword());
	      restPatchRequest.setReadTimeout(0);
	      restPatchRequest.execute();
	      
	      logger.info("Update API Doc..");
	      byte[] fileBytes = Files.readAllBytes(file.toPath());
		  ByteArrayContent fileContent = new ByteArrayContent("application/octet-stream", fileBytes);
		  HttpRequest restRequest = REQUEST_FACTORY
	              .buildPostRequest(new GenericUrl(profile.getPortalURL() + "/jsonapi/node/apidoc/"+doc.data.get(0).id+"/field_apidoc_spec"), fileContent);
		  HttpHeaders headers = restRequest.getHeaders();
		  headers.setAccept("application/vnd.api+json");
		  headers.set("Content-Disposition", "file; filename=\""+spec.getTitle()+"."+profile.getPortalFormat()+"\"");
	      headers.setBasicAuthentication(profile.getPortalUserName(), profile.getPortalPassword());
	      restRequest.setReadTimeout(0);
	      restRequest.execute();
		  logger.info("Update API Doc complete..");
    } catch (HttpResponseException e) {
    	throw new IOException(exceptionHandler(e));
    }
  }
  
  /**
   * Create an API Doc
   */
  public static String createAPIDoc(ServerProfile profile, File file, APIDocObject doc) throws IOException {
    HttpResponse response = null;
    try {
	      SpecObject spec = parseSpec(profile, file);  
	      
	      logger.info("Creating spec.." + doc.data.id);
	      ByteArrayContent content = constructAPIDocRequestBody(profile, spec, doc.data.id, null, null, false);
	      HttpRequest restRequest = REQUEST_FACTORY
	              .buildPostRequest(new GenericUrl(profile.getPortalURL() + "/jsonapi/node/apidoc"), content);
	      HttpHeaders headers = restRequest.getHeaders();
	      headers.setAccept("application/vnd.api+json");
	      headers.setBasicAuthentication(profile.getPortalUserName(), profile.getPortalPassword());
	      restRequest.setReadTimeout(0);
	      response = restRequest.execute();
	      Gson gson = new Gson();
	      Reader reader = new InputStreamReader(response.getContent());
	      APIDocObject model = gson.fromJson(reader, APIDocObject.class);
	      logger.info("API Doc:  " + spec.getTitle()+ " created");
	      if(model != null && model.data!=null) {
	    	  logger.info("API Doc uuid:" + model.data.id);
		      return model.data.id;
	      }
	      return null;
	      
    } catch (HttpResponseException e) {
    	throw new IOException(exceptionHandler(e));
    }
  }
  
  /**
   * Delete an API Doc
   */
  public static void deleteAPIDoc(ServerProfile profile, File file) throws IOException {
	  try {
		  APIDocResponseObject respObj = getAPIDoc(profile, file);
	      if (respObj != null) {
		      logger.info("Delete API Doc..");
			  HttpRequest restRequest = REQUEST_FACTORY
		              .buildDeleteRequest(new GenericUrl(profile.getPortalURL() + "/jsonapi/node/apidoc/"+respObj.data.get(0).id));
			  HttpHeaders headers = restRequest.getHeaders();
			  headers.setAccept("application/vnd.api+json");
		      headers.setBasicAuthentication(profile.getPortalUserName(), profile.getPortalPassword());
		      restRequest.setReadTimeout(0);
		      HttpResponse response = restRequest.execute();
			  logger.info("Delete API Doc complete..");
	      }
    } catch (HttpResponseException e) {
    	throw new IOException(exceptionHandler(e));
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
 
  public static String exceptionHandler(HttpResponseException hre) {
	  Gson gson = new Gson();
	  APIErrorObject errorObj = gson.fromJson(hre.getContent(), APIErrorObject.class);
	  if(errorObj!=null && errorObj.errors!=null && errorObj.errors.size()>0){
		  String errorMsg = "\nStatus code: "+ errorObj.errors.get(0).status +"\n";
		  errorMsg = errorMsg + "Status Message: " + errorObj.errors.get(0).title +"\n";
		  errorMsg = errorMsg + "Detailed Message: " + errorObj.errors.get(0).detail +"\n";
		  return errorMsg;
	  }
	  return "";
  }

}

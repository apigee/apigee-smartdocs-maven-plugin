------------
Plugin Usage
------------
```
mvn install -Pdev -Dapigee.smartdocs.config.options=create

  # Options

  -P<profile>
    Pick a profile in the parent pom.xml (shared-pom.xml in the example).
    Apigee org and env information comes from the profile.

  -Dapigee.smartdocs.config.options
    none   - No action (default)
    create - Creates the doc found in the OpenAPI Spec directory
    update - Updates the doc found in the OpenAPI Spec directory
    delete - Deletes the doc found in the OpenAPI Spec directory
    sync   - executes delete and update options mentioned above
    
```


# Samples

## Prerequisites (Developer Portal setup)
- This sample is for **Drupal 8 version of Developer portal**. The version of the plugin used in the pom **should be 2.x**
- If you are using Drupal 7 version of Developer portal, please follow the instructions [here](https://github.com/apigee/apigee-smartdocs-maven-plugin/tree/master/samples/Drupal7)
- To utilize this example, you will need a working developer portal instance with the [API Docs](https://www.drupal.org/docs/8/modules/apigee-api-catalog/expose-rest-apis-to-interact-with-api-docs#s-prerequisites) installed and enabled. That module will expose endpoints for use by the SmartDocs Maven Plugin.
- For Apigee API Catalog 1.x module, use version 2.0.1 (Apigee API Catalog 1.x will be deprecated)
- For Apigee API Catalog 2.x module, use version 2.1.0 or later


## DevPortal

### Basic Implementation

**Please ensure all prerequisites have been followed prior to continuing.**

Goal: Import OpenAPI specs and create API Docs in the developer portal instance.

```
/samples/DevPortal
```

This project demonstrates use of apigee-smartdocs-maven-plugin to create API Docs using OpenAPI specs to a developer portal. 

To use, edit samples/DevPortal/pom.xml and update portal values as specified.

      <portal.url>${purl}</portal.url><!-- URL of the developer portal. --> 
      <portal.username>${pusername}</portal.username><!-- Username for the developer portal. -->
      <portal.password>${ppassword}</portal.password><!-- Password for the developer portal. -->
      <portal.format>yaml</portal.format><!-- Format of the OpenAPI specs. -->
      <portal.api.doc.format>basic_html</portal.api.doc.format><!-- Format in the portal. Values: basic_html, restricted_html, full_html -->
      <portal.directory>${pdirectory}</portal.directory><!-- Directory where OpenAPI specs are accessible. -->

**NOTE:** Please provide the url of the developer portal without the trailing "/" 

To run, jump to the sample project `cd /samples/DevPortal` and run 

`mvn install -Pdev -Dapigee.smartdocs.config.options=create`

### Advanced Implementation

**To use these features make sure you have the Apigee API Catalog module version 8.x-2.2 or higher.  Please read through the [release notes](https://www.drupal.org/project/apigee_api_catalog/releases/8.x-2.2).** 

If you want to configure/manage fields and taxonomy, you can create a json file and pass that as `apigee.smartdocs.config.file` argument. A simple example below:

**NOTE: The fields should be pre-configured on the API Doc content type in the portal

#### Sample

![](./media/screensho1.png)

Types of fields supported :
 - Text (single or multivalued textfields or dropdowns)
 - Entity Reference (Taxonomy)

To use Taxonomy reference fields make sure that you are using a term already defined in the Vocabulary. In the config file, we will need to specify the vocabulary name for every taxonomy field that is being used. Additionally we have to ensure that the JSONAPIs for the related taxonomy terms are enabled.

![](./media/screensho1.png)

For example, The default Categories field on API Doc is associated with the “API Category” Vocabulary and has Data Append, Identity, Mobile, Payments , etc as acceptable term values.

Here is a  sample metadata file (apicatalog-config.json) : 

```
{
   "fields":{
      "field_foo": "bar",
      "field_list":[
         "item1",
         "item2"
      ]
   },
   "taxonomy_terms":[
      {
         "vocabulary":"capability",
         "field":"field_capability",
         "data":[
            "Capability1",
            "Capability2"
         ]
      },
      {
         "vocabulary":"api_category",
         "field":"field_categories",
         "data":[
            "Mobile"
         ]
      }
   ]
}
```

NOTE: The fields and taxonomy should be pre-configured in the portal. Please provide the correct the field, vocabulary names.

`mvn install -Pdev -Dapigee.smartdocs.config.options=create -Dapigee.smartdocs.config.file=./apicatalog-config.json`


### Troubleshooting

#### 404 Error
Validate that the module is installed and enabled as described in the prerequisites. If you are still encountering errors, try downloading an app such as [Postman](https://www.getpostman.com/) and making a direct request to endpoints defined [here](https://www.drupal.org/docs/8/modules/apigee-api-catalog/expose-rest-apis-to-interact-with-api-docs#s-interacting-with-the-rest-api).

#### Handshake_failure
If you receive a handshake failure, it is likely an issue with TLS mismatch between your machine and the server. Add the following to the end of the `mvn install ...` line: `-Dhttps.protocols=TLSv1.2`

e.g. `mvn install -Pdev -Dapigee.smartdocs.config.options=create -Dhttps.protocols=TLSv1.2` 

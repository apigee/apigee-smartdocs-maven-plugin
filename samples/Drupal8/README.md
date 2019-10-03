# Samples

## Prerequisites (Developer Portal setup)
- This sample is for Drupal 8 Developer portal. The version of the plugin used in the pom is 2.x
- If you are using Drupal 7 version of Developer portal, please follow the instructions [here](https://github.com/apigee/apigee-smartdocs-maven-plugin/tree/master/samples/Drupal7)
- To utilize this example, you will need a working developer portal instance with the [API Docs](https://www.drupal.org/docs/8/modules/apigee-api-catalog/expose-rest-apis-to-interact-with-api-docs#s-prerequisites) installed and enabled. That module will expose endpoints for use by the SmartDocs Maven Plugin.**

## DevPortal

### Basic Implementation (Model import)

**Please ensure all prerequisites have been followed prior to continuing.**

Goal: Create models and import OpenAPI specs to a developer portal instance.

```
/samples/DevPortal
```

This project demonstrates use of apigee-smartdocs-maven-plugin to create models and import OpenAPI specs to a developer portal. The example project performs a data import defined in pom.xml

To use, edit samples/DevPortal/pom.xml and update portal values as specified.

      <portal.username>${pusername}</portal.username><!-- Username for the developer portal. -->
      <portal.password>${ppassword}</portal.password><!-- Password for the developer portal. -->
      <portal.directory>${pdirectory}</portal.directory><!-- Directory whered OpenAPI specs are accessible. -->
      <portal.url>${purl}</portal.url><!-- URL of the developer portal. -->
      <portal.path>${ppath}</portal.path><!-- Servies path defined in the developer portal. -->
      <portal.format>json</portal.format><!-- Format of the OpenAPI specs. -->

To run, jump to the sample project `cd /samples/DevPortal` and run 

`mvn install -Pdev -Dapigee.smartdocs.config.options=create`

### Troubleshooting

#### 404 Error
Validate that the module is installed and enabled and that resource endpoints are set as described in the prerequisites. If you are still encountering errors, try downloading an app such as [Postman](https://www.getpostman.com/) and making a direct request to endpoints defined [here](https://www.drupal.org/docs/8/modules/apigee-api-catalog/expose-rest-apis-to-interact-with-api-docs#s-interacting-with-the-rest-api).

#### Handshake_failure
If you receive a handshake failure, it is likely an issue with TLS mismatch between your machine and the server. Add the following to the end of the `mvn install ...` line: `-Dhttps.protocols=TLSv1.2`

e.g. `mvn install -Pdev -Dapigee.smartdocs.config.options=create -Dhttps.protocols=TLSv1.2` 

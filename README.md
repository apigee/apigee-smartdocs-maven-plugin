# apigee-smartdocs-maven-plugin

----------------
About the Plugin
----------------

apigee-smatdocs-maven-plugin is a utility for creating API models and rendering the OpenAPI Specficiation to Smart docs in the Apigee Developer Portal
The code is distributed under the Apache License 2.0.

------------
TL;DR
------------

The [samples folder](https://github.com/apigee/apigee-smartdocs-maven-plugin/tree/master/samples) provides a Readme with Getting Started steps and commands to hit the ground quickly.

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
    create - Creates the model found in the OpenAPI Spec directory
    update - Updates the model found in the OpenAPI Spec directory
    delete - Deleted all models not found in the OpenAPI Spec directory
    render - Renders the smart docs 
    sync   - executes the delete option (mentioned above) and recreates the models found in the OpenAPI Spec directory. This also renders the smart docs as well
    
   If you use create or update option, you will need to run the command again with option as render
```

## Support
* Please send feature requests using [issues](https://github.com/apigee/apigee-smartdocs-maven-plugin/issues)
* Post a question in [Apigee community](https://community.apigee.com/index.html)
* Create an [issue](https://github.com/apigee/apigee-smartdocs-maven-plugin/issues/new)


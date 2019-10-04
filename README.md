# apigee-smartdocs-maven-plugin

----------------
About the Plugin
----------------

apigee-smatdocs-maven-plugin is a utility for creating API models and rendering the OpenAPI Specficiation to Smart docs in the Apigee Developer Portal
The code is distributed under the Apache License 2.0.

------------
TL;DR
------------

- Version **1.x** of this plugin should be used for **Drupal 7** version of the Developer portal and the goal is `apimodel`. For example in your pom.xml
  ```xml
    <plugin>
        <groupId>com.apigee.smartdocs.config</groupId>
        <artifactId>apigee-smartdocs-maven-plugin</artifactId>
        <version>1.0.8</version>
        <executions>
            <execution>
                <id>smartdocs-deploy</id>
                <phase>install</phase>
                <goals>
                    <goal>apimodel</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
  ```
- Version **2.x** of this plugin should be used for **Drupal 8** version of the Developer portal and the goal is `apidoc`. For example in your pom.xml
```xml
    <plugin>
        <groupId>com.apigee.smartdocs.config</groupId>
        <artifactId>apigee-smartdocs-maven-plugin</artifactId>
        <version>2.0.0</version>
        <executions>
            <execution>
                <id>smartdocs-deploy</id>
                <phase>install</phase>
                <goals>
                    <goal>apidoc</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
  ```

The [samples folder](https://github.com/apigee/apigee-smartdocs-maven-plugin/tree/master/samples) provides a Readme with Getting Started steps and commands to hit the ground quickly. Contains samples for Drupal 7 and Drupal 8 version of Developer portal


## Support
* Please send feature requests using [issues](https://github.com/apigee/apigee-smartdocs-maven-plugin/issues)
* Post a question in [Apigee community](https://community.apigee.com/index.html)
* Create an [issue](https://github.com/apigee/apigee-smartdocs-maven-plugin/issues/new)


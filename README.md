# Address Book Portlet Demo  
An example project to showcase how Vaadin portlet support works in a portal based on the Java Portlet API 3.0. 
Clone the repository and import the project to the IDE of your choice as a Maven project. 
You need to have Java 8 or 11 installed.

The documentation for Vaadin Portlet support is available [here](https://github.com/vaadin/flow-and-components-documentation/blob/master/documentation/portlet-support/overview.asciidoc).

## Running the portlet

Before the portlet application can be run, it must be deployed to a portal. 
We currently support [Apache Pluto](https://portals.apache.org/pluto/). The
easiest way to try out your application is to run a Maven goal which downloads 
and starts an embedded Tomcat 8 serving the Pluto Portal driver:

First build the whole project using `mvn install` in the root

then in the module `portal` execute
`mvn package cargo:run -Pautosetup`

Visit http://localhost:8080/pluto, and log in as `pluto`, password `pluto`.

The deployed portlet needs to be added to a portal page. Do this by
1) Selecting `Pluto Admin` page
2) Select `About Apache Pluto` from the drop-down under "Portal Pages"
3) Select `/address-book-grid` from the left drop-down under "Portlet Applications"
4) Select `Grid` from the drop-down on the right
5) Click the `Add Portlet` button
6) Select `About Apache Pluto` from the drop-down under "Portal Pages"
7) Select `/address-book-form` from the left drop-down under "Portlet Applications"
8) Select `Form` from the drop-down on the right
9) Click the `Add Portlet` button

Once you navigate to `About Apache Pluto` page, the `Grid` and the `Form` portlets should be
visible on the page.

For the consecutive runs after installing, use the following command to reuse the already downloaded Tomcat and Pluto:

`mvn package cargo:run -Pautocopy`

## Remote debugging for Portal

Remote debugging (JDWP) is available on port 8000 (to activate
in IntelliJ, choose `Run -> Attach to Process...`). 

## Production build
To build the production .war run:

`mvn package -Pproduction`

Deploy all wars `addressbook-grid/target/address-book-grid.war`, `addressbook-form/target/address-book-form.war`
and `addressbook-bundle/target/vaadin-portlet-static.war` folder to your web server / portal. 

## Adding a new Portlet module

To add a new Portlet module to the project create a default vaadin portlet module.
The module should contain its own portlet.xml file.

Add to the new module the following plugin:
````xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-antrun-plugin</artifactId>
    <executions>
        <execution>
            <id>copy-files</id>
            <phase>generate-resources</phase>
            <goals>
                <goal>run</goal>
            </goals>
            <configuration>
                <tasks>
                    <copy todir="${project.build.directory}/classes/META-INF/VAADIN/config">
                        <fileset dir="../target/META-INF/VAADIN/config" />
                    </copy>
                </tasks>
            </configuration>
        </execution>
    </executions>
</plugin>
````

Add the module sources to the bundle module `build-helper-maven-plugin` as added sources:

````xml
<sources>
  <source>../moduleName/src/main/java</source>
  ...
</sources>
````

Then build the whole project again with `mvn install`

## Notes about the project

Vaadin 14+ portlet support feature is still under development and changes to
both the API and this project are possible.

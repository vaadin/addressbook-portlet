# Address Book Portlet Demo for Liferay 7
An example project to showcase how Vaadin 23.1+ portlet support works in a Liferay 7 container. 
Clone the repository and import the project to the IDE of your choice as a Maven project. 
You need to have Java 11 installed.

The documentation for Vaadin Portlet support is available [here](https://vaadin.com/docs/latest/flow/integrations/portlet).

## Running the portlet under Liferay

Before the portlet application can be run, it must be deployed to a portal for this
branch the portal supported is [Liferay](https://www.liferay.com/downloads-community):

1. Build the whole project using `mvn install` in the root

2. We assume Liferay is running in http://localhost:8080/, an easy way to run a local
copy of Liferay is to use their official [docker images](https://hub.docker.com/r/liferay/portal). 
Below is an example of a docker-compose file you can use (note the used Liferay version, 7.2+ should
work).

````
version: "2.2"
services:
    liferay-dev:
        image: liferay/portal:7.2.1-ga2
        environment:
            - LIFERAY_JAVASCRIPT_PERIOD_SINGLE_PERIOD_PAGE_PERIOD_APPLICATION_PERIOD_ENABLED=false
        ports:
            - 8080:8080
            - 8000:8000
        volumes:
            - ./deploy:/mnt/liferay/deploy
            - ./files:/mnt/liferay/files
````

3. Add the following to the end of the last line in Tomcat's `setenv.sh`
(`/var/liferay/tomcat-<version>/bin`) before starting Liferay. When
using the above docker-compose file place an edited copy of `setenv.sh`
in `./files/tomcat/bin`.

````
 -Dvaadin.portlet.static.resources.mapping=/o/vaadin-portlet-static/
````

4. Download and add the Jna dependency JARs of a certain version into 
   `/var/liferay/tomcat-<version>/webapps/ROOT/WEB-INF/lib/` (or `shielded-container-lib/`):
   - [net.java.dev.jna:jna:5.11.0](https://mvnrepository.com/artifact/net.java.dev.jna/jna/5.11.0)
   - [net.java.dev.jna:jna-platform:5.11.0](https://mvnrepository.com/artifact/net.java.dev.jna/jna-platform/5.11.0)
  
   How to copy these files is described [here](https://learn.liferay.
com/dxp/latest/en/installation-and-upgrades/installing-liferay/using-liferay
-docker-images/providing-files-to-the-container.html#using-docker-cp)
   
   This is needed because Liferay uses an older version of Jna and running 
Vaadin Portlet in dev mode causes a conflict of dependencies used by Liferay 
and Vaadin License Checker (`NoClassDefFound` exception).

   Here is a useful [docs](https://learn.liferay.
 com/dxp/latest/en/building-applications/reference/jars-excluded-from-wabs.
   html) describing how to add third-party dependency version you want.
     
5. Run `docker-compose up`

6. Deploy all wars: `addressbook-bundle/target/vaadin-portlet-static.war`, `addressbook-grid/target/address-book-grid.war` and 
`addressbook-form/target/address-book-form.war` 
to your docker container by copying them to `./deploy/` (the copied files should disappear when deployed).

7. Wait for the bundles to start, then visit http://localhost:8080/.
   Set up a new user if you're running Liferay for the first time. Default is `test@liferay.com`/`test`.
   Log in into Liferay.

8. The deployed portlet needs to be added to a portal page. Do this by
- Selecting the Plus or the Pen icon near top right of the page (exact 
  placement and look
varies by Liferay version) add elements to the current page.
- Under Widgets on the right sidebar find Vaadin Sample category under which 
  you will find
entries for Contact List and Contact Form, drag and drop them onto the page.
- If at the top right of the page, in edit mode, you see a Publish button, 
  use it to make your
changes public (7.3+).

## Remote debugging for Liferay

In order to remote debug your portlet under Liferay add the following to the end of the last line in 
Tomcat's `setenv.sh` (`/var/liferay/tomcat-<version>/bin`) before starting Liferay. When using the
above docker-compose file place an edited copy of `setenv.sh` in `./files/tomcat/bin` before
`docker-compose up`.

````
 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000
````

Remote debugging (JDWP) is now available on port 8000 (to activate
in IntelliJ, choose `Run -> Attach to Process...`). 

## Production build

To build the production .war run:

`mvn package -Pproduction`

Deploy all wars: `addressbook-grid/target/address-book-grid.war`, `addressbook-form/target/address-book-form.war`
and `addressbook-bundle/target/vaadin-portlet-static.war`, to your web server / portal. 

## Adding a new Portlet module

To add a new Portlet module to the project create a default vaadin portlet module.
The module should contain its own portlet.xml file.

Add to the new module the resource file `flow-build-info.json` into `./src/main/resources/META-INF/VAADIN/config`
with the contents:
````json
{
  "externalStatsUrl": "/o/vaadin-portlet-static/VAADIN/config/stats.json"
}
````

Add the module sources to the bundle module `build-helper-maven-plugin` as added sources:

````xml
<sources>
  <source>../moduleName/src/main/java</source>
  ...
</sources>
````

Then build the whole project again with `mvn install`

### Current known issues running under Liferay

See Vaadin Portlet [release notes](https://github.com/vaadin/portlet/releases) for a limitation and known issues list.

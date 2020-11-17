# Address Book Portlet Demo  
An example project to showcase how Vaadin portlet support works in a portal based on the Java Portlet API 3.0. 
Clone the repository and import the project to the IDE of your choice as a Maven project. 
You need to have Java 8 or 11 installed.

The documentation for Vaadin Portlet support is available [here](https://github.com/vaadin/flow-and-components-documentation/blob/master/documentation/portlet-support/overview.asciidoc).

## Running the portlet under Liferay

Before the portlet application can be run, it must be deployed to a portal for this
branch the portal supported is [Liferay](https://www.liferay.com/downloads-community).

First build the whole project using `mvn install` in the root

We assume Liferay is running in http://localhost:8080/, an easy way to run a local
copy of Liferay is to use their official [docker images](https://hub.docker.com/r/liferay/portal). 
Below is an example of a docker-compose file you can use (note the used Liferay version, 7.2+ should
work).

````
version: "2.2"
services:
    liferay-dev:
        image: liferay/portal:7.2.1-ga2
        ports:
            - 8080:8080
            - 8000:8000
        volumes:
            - ./deploy:/mnt/liferay/deploy
            - ./files:/mnt/liferay/files
````

Add the following to the end of the last line in Tomcat's `setenv.sh`
(`/var/liferay/tomcat-<version>/bin`) before starting Liferay. When
using the above docker-compose file place an edited copy of `setenv.sh`
in `./files/tomcat/bin`.

````
 -Dvaadin.portlet.static.resources.mapping=/o/vaadin-portlet-static/
````

Run `docker-compose up`

Deploy all wars: `addressbook-grid/target/address-book-grid.war`, 
`addressbook-form/target/address-book-form.war` and `addressbook-bundle/target/vaadin-portlet-static.war`, 
to your docker container by copying them to `./deploy/` (the copied files should disappear when deployed).

Wait for the bundles to start, then visit http://localhost:8080/, log in as `test@liferay.com` with
password `test`.

The deployed portlet needs to be added to a portal page. Do this by
1) Selecting the Plus or the Pen icon near top right of the page (exact placement and look
varies by Liferay version) add elements to the current page.
2) Under Widgets on the right sidebar find Vaadin Sample category under which you will find
entries for Contact List and Contact Form, drag and drop them onto the page.
3) If at the top right of the page, in edit mode, you see a Publish button, use it to make your
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

## Notes about the project

Vaadin 14+ portlet support feature is still under development and changes to
both the API and this project are possible.

### Current known issues running under Liferay

* The frontend UI may *sometimes* not render correctly when a
Vaadin portlet is first added to a page or when the page is being edited. Reloading or
publishing the page will resolve the issue (depending on exact Liferay version).
* When impersonating users in Liferay 7.2 the portlets consistently
render as blank (empty `vaadin-vertical-layout` web component in DOM).
* Some javascript methods can get called by Vaadin frontend code on an
undefined `$server` object when navigating to, or within, the same page multiple times repeatedly 
(portlets also render blank similar to other two issues).
* Benign exception about not being able to detect websocket support during deployment of .war files from 
Atmosphere (ServerContainer is null), Vaadin features reliant on WebSockets may not work.

The portlets rendering with an empty layout is a common symptom of any problem with loading the portlet 
frontend UI that happens after Liferay has commited the portlet response, often with not necessarily any
visible errors logged on browser or serverside.

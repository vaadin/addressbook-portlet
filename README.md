# Address Book Portlet Demo for Liferay
An example project to showcase how Vaadin 24 portlet support works in a Liferay 2025.Q1+ container (Jakarta EE 10).
Clone the repository and import the project to the IDE of your choice as a Maven project.
You need to have Java 17 or later installed.

The documentation for Vaadin Portlet support is available [here](https://vaadin.com/docs/latest/flow/integrations/portlet).

## Running the portlet under Liferay

Before the portlet application can be run, it must be deployed to a portal. This
branch targets [Liferay](https://www.liferay.com/downloads-community) 2025.Q1 or later
(any release that ships with Jakarta EE 10 / Portlet 3.0 on Jakarta namespaces).

1. Build the whole project using `mvn install` in the root

2. We assume Liferay is running at http://localhost:8080/. An easy way to run a local
copy of Liferay is to use their official [docker images](https://hub.docker.com/r/liferay/portal).
Below is an example of a docker-compose file you can use:

````
version: "2.2"
services:
    liferay-dev:
        image: liferay/dxp:2025.q1.7
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

4. Run `docker-compose up`

5. Deploy all wars: `address-book-grid.war`, `address-book-form.war`,
`address-book-upload.war` and `vaadin-portlet-static.war` to your docker container
by copying them to `./deploy/` (the copied files should disappear when deployed).
When using `mvn package`, all WARs are collected in the `build/` directory for convenience.

6. Wait for the bundles to start, then visit http://localhost:8080/.
   Set up a new user if you're running Liferay for the first time. Default is `test@liferay.com`/`test`.
   Log in into Liferay.

7. The deployed portlet needs to be added to a portal page. Do this by
- Selecting the Plus or the Pen icon near top right of the page (exact
  placement and look
varies by Liferay version) add elements to the current page.
- Under Widgets on the right sidebar find Vaadin Sample category under which
  you will find
entries for Contact List, Contact Form and Contact Upload — drag and drop them onto the page.
- If at the top right of the page, in edit mode, you see a Publish button,
  use it to make your
changes public.

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

Deploy all wars: `address-book-grid.war`, `address-book-form.war`, `address-book-upload.war`
and `vaadin-portlet-static.war` from the `build/` directory to your web server / portal.

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

Add the module to the root `pom.xml` `<modules>` list and add a `classes` classifier dependency
in `addressbook-bundle/pom.xml` so the Vaadin frontend build picks up the module's components:

````xml
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>address-book-yourmodule</artifactId>
    <version>${project.version}</version>
    <classifier>classes</classifier>
</dependency>
````

Then build the whole project again with `mvn install`.

## Disabling Liferay SPA (Single Page Application)

Liferay's SPA navigation (powered by senna.js / `frontend-js-spa-web`) must be disabled
on Liferay instances that host Vaadin portlets. Vaadin's frontend is built on ES modules,
which only execute once per page lifecycle. After an SPA navigation, `FlowClient.init()`
does not re-run, leaving stale clients that cannot serve new UIs — portlets appear blank.

The recommended approach is to **exclude portlet pages from SPA** rather than disabling
it globally. This keeps SPA enabled for non-portlet pages while forcing full page reloads
only where needed:

1. Log in as administrator
2. Navigate to **Control Panel → Instance Settings → Infrastructure → Frontend SPA Infrastructure**
3. Add portlet page paths to **Custom Excluded Paths** (regex), e.g. `/address-book.*`
4. Save

Alternatively, disable SPA globally via Instance Settings (uncheck **Enable SPA**) or
set the portal property (requires server restart):

````properties
javascript.single.page.application.enabled=false
````

> **Note:** On Liferay 2025.Q4+ this property only sets the default for new instances.
> Already-initialized instances must be configured through Instance Settings.

### Current known issues running under Liferay

See Vaadin Portlet [release notes](https://github.com/vaadin/portlet/releases) for a limitation and known issues list.

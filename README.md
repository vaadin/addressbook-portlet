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

`mvn package cargo:run -Pautosetup,production`

Visit http://localhost:8080/pluto, and log in as `pluto`, password `pluto`.

The deployed portlet needs to be added to a portal page. Do this by
1) Selecting `Pluto Admin` page
2) Select `About Apache Pluto` from the drop-down under "Portal Pages"
3) Select `/portlet-address-book` from the left drop-down under "Portlet Applications"
4) Select `Grid` from the drop-down on the right
5) Click the `Add Portlet` button
6) Repeat steps 2-5 for the `Form` portlet

Once you navigate to `About Apache Pluto` page, the `Grid` and the `Form` portlets should be
visible on the page.

For the consecutive runs, use the following command to reuse the already downloaded Tomcat and Pluto:

`mvn package cargo:run -Pautocopy,production`

## Remote debugging for Portal

Remote debugging (JDWP) is available on port 8000 (to activate
in IntelliJ, choose `Run -> Attach to Process...`). 

## Production build
To build the production .war run:

`mvn package -Pproduction`

Deploy both `portlet-address-book.war` and `vaadin-portlet-static.war` from `/target`
folder to your web server / portal. 

## Notes about the project

Vaadin 14+ portlet support feature is still under development and changes to
both the API and this project are possible.

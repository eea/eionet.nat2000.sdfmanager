
Introduction & pre-requisites
=============================

This README contains instructions on how to build and/or run the software.

Building is relevant only if you have the source code. If you only have the pre-built ZIP package, then you can skip the building part.

For running the software, you will need the following software components installed on your machine:
* A 32-bit version of Java Runtime Environment (JRE) 6 or 7
* MySQL database server 5.0 or 5.1. It must be running and there must be at least an empty database named "natura2000".


Building
========

For building you will need at least JDK 1.6

In the root directory of your source code, execute the following command:

    ant dist


As a result of the build, the following JAR file should be created in the root directory (where the ' * ' marks version identifier)

    SDF_Manager.jar

Proceed to the next chapter for how to run this JAR.


As a result, a ZIP should be created in the root directory, bearing the same name as the above JAR file. This is your distributable package, i.e. the pre-built ZIP package.

Packaging
=========

Create a zip file that includes the built jar file. Besides the jar the zip should contain following folders:
- config

- database

- images

- import

- lib 

- logs

- xsd

- xsl

and files:

- log4j.properties

- SDF_Manager.jar



Running
=======

If you have built the above JAR file or you have unzipped a downloaded pre-built ZIP package, then you have two options to run the software.

* unzip the files to a created folder
* adjust database properties in database/sdf_database.properties

* On a Windows machine you can simply double-click the "SDFmanager-*.jar" in the root directory.
* On any machine it should also be runnable by executing the following command:

    java -jar SDFmanager-*.jar




Introduction & pre-requisites
=============================

This README contains instructions on how to build and/or run the software.

Building is relevant only if you have the source code. If you only have the pre-built ZIP package, then you can skip the building part.

For running the software, you will need the following software components installed on your machine:
* A 32-bit version of Java Runtime Environment (JRE) 6 or 7
* MySQL database server 5.0 or 5.1. It must be running and there must be at least an empty database named "natura2000".


    
Building a distributable package on Windows
===========================================
* Download and install Inno Setup from http://www.jrsoftware.org/isdl.php#stable (NB! Be sure to install also the *Inno Setup Preprocessor* that the installer suggests).
* Add the Inno Setup root path where _iscc.exe_ resides to Windows system environment PATH variable.
* Copy a JRE to [project folder]/jre  **NB! The JRE must be 32-bit!**

Run:

    mvn -Dmaven.test.skip=true clean install
    
An installer exe file named SDFManagerSetup_v[ver no].exe will be created in the project root directory




Installing the tool
===================
* The intallation package does not contain database installation. Mysql 5.1 has to be installed separately.
* The intaller asks you some configuration-related questions:
	* The mode you wish to run the tool in (i.e. Natura 2000 or EMERALD)
	* The host name, port, user and password of your MySQL database mentioned in the pre-requisites above.



Introduction & pre-requisites
=============================

This README contains instructions on how to build and/or run the software.

Building is relevant only if you have the source code. If you only have the pre-built ZIP package, then you can skip the building part.

For running the software, you will need the following software components installed on your machine:
* Java Runtime Environment (JRE) 8
* MySQL database server 8.0 or later (must be running).




Build a local jar file 
===========================================
-   mvn -Dmaven.test.skip=true clean verify 
    
Building a distributable package on Windows
===========================================
* Download and install Inno Setup from http://www.jrsoftware.org/isdl.php#stable (NB! Be sure to install also the *Inno Setup Preprocessor* that the installer suggests).
* Add the Inno Setup root path where _iscc.exe_ resides to Windows system environment PATH variable.
* Copy a JRE to [project folder]/jre






Run:

    mvn -Dmaven.test.skip=true clean install
    
An installer exe file named SDFManagerSetup_v[ver no].exe will be created in the project root directory




Installing the tool
===================
* The installation package does not contain database installation. MySQL has to be installed separately.
* The installer asks you some configuration-related questions:
	* The mode you wish to run the tool in (i.e. Natura 2000 or EMERALD)
	* The host name, port, user and password of your MySQL database mentioned in the pre-requisites above.


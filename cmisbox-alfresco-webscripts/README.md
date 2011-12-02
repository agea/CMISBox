CMISBox
=======
Synchronize and share your files with your CMIS Repository


About
-----
CMISBox lets you choose a folder in a CMIS enabled repository (at the time of this writing
it has been tested only with Alfresco), and keeps it synchronized in your computer.

Every local change you make (create, update rename or delete a file) is reflected in the
remote folder and vice versa


Alfresco webscripts
-------------------
To install the webscripts in your alfresco installation drop the jar

  alfresco.webscripts-latest.jar
  
in ALFRESCO_HOME/tomcat/shared/lib

(you may need to create 'lib' the folder)
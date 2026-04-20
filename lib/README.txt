============================================================
 EcoLearnSwing – MySQL Connector/J Setup Instructions
============================================================

This folder (lib/) must contain the MySQL JDBC driver JAR before
the project can be compiled or run.

DOWNLOAD STEPS:
  1. Open: https://dev.mysql.com/downloads/connector/j/
  2. Select "Platform Independent" from the Operating System dropdown
  3. Click "Download" on the ZIP file (no account needed — click "No thanks")
  4. Extract the ZIP file
  5. Copy the file named:
       mysql-connector-java-8.x.x.jar
     OR (newer versions):
       mysql-connector-j-8.x.x.jar
     into THIS lib/ folder.

EXAMPLE — the lib/ folder should look like:
  lib/
  ├── README.txt                (this file)
  └── mysql-connector-java-8.0.33.jar   (your downloaded JAR)

Once the JAR is here, run build.bat from the project root to
compile and launch the application.

NOTE: The exact version number (8.x.x) does not matter as long
as it is version 8.x. Version 9.x may also work.
============================================================

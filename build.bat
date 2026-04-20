@echo off
REM ============================================================
REM  EcoLearnSwing – Windows Build & Run Script
REM  Usage: Double-click build.bat  OR  run from cmd:
REM         cd EcoLearnSwing && build.bat
REM ============================================================

setlocal

REM ── Find the MySQL Connector JAR automatically ───────────────
set JAR=
for %%f in (lib\mysql-connector-java-*.jar lib\mysql-connector-j-*.jar) do set JAR=%%f

if "%JAR%"=="" (
    echo.
    echo  ERROR: MySQL Connector/J JAR not found in lib\
    echo.
    echo  Please:
    echo    1. Go to https://dev.mysql.com/downloads/connector/j/
    echo    2. Download the "Platform Independent" ZIP
    echo    3. Extract and copy the .jar file into the lib\ folder
    echo    4. Re-run this script
    echo.
    pause
    exit /b 1
)

echo  Found JDBC driver: %JAR%

REM ── Create output directory ──────────────────────────────────
if not exist out mkdir out

REM ── Compile ──────────────────────────────────────────────────
echo  Compiling...
javac -encoding UTF-8 -cp "%JAR%" -d out ^
  src\main\MainApp.java ^
  src\ui\*.java ^
  src\controller\*.java ^
  src\service\*.java ^
  src\dao\*.java ^
  src\model\*.java ^
  src\db\*.java ^
  src\utils\*.java

if %ERRORLEVEL% neq 0 (
    echo.
    echo  COMPILATION FAILED. Fix the errors above and retry.
    echo.
    pause
    exit /b %ERRORLEVEL%
)

echo  Compilation successful!
echo.

REM ── Run ──────────────────────────────────────────────────────
echo  Starting EcoLearnSwing...
java -cp "out;%JAR%" main.MainApp

endlocal

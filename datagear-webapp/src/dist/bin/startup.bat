@echo off

rem when not run at application path, need the following set

if "%DG_APP_HOME%" == "" goto setAppHomeAuto
if not "%DG_APP_HOME%" == "" goto setAppHomePreSet

:setAppHomeAuto
set "DG_APP_HOME=%~dp0"

:setAppHomePreSet
set "DG_APP_HOME=%DG_APP_HOME%"

set "DG_APP_FULL_NAME=%DG_APP_HOME%${productNameJar}"
set "DG_APP_CONFIG_PATH=%DG_APP_HOME%\config\application.properties"

if "%JAVA_HOME%" == "" goto runNoJavaHome

if not "%JAVA_HOME%" == "" goto runJavaHome

:runNoJavaHome
java -jar "%DG_APP_FULL_NAME%" --spring.config.additional-location="%DG_APP_CONFIG_PATH%"

:runJavaHome
echo Using JAVA_HOME "%JAVA_HOME%"
"%JAVA_HOME%/bin/java" -jar "%DG_APP_FULL_NAME%" --spring.config.additional-location="%DG_APP_CONFIG_PATH%"

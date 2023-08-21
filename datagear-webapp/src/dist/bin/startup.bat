@echo off

rem JAVA_HOME
rem JAVA_OPTS
rem DG_APP_HOME this make application can run at other path

if "%DG_APP_HOME%" == "" goto setAppHomeAuto
if not "%DG_APP_HOME%" == "" goto okAppHome

:setAppHomeAuto
set DG_APP_HOME=%~dp0
goto okAppHome

:okAppHome
set DG_APP_FULL_NAME=%DG_APP_HOME%${productNameJar}
set DG_SPRING_OPTS=--spring.config.additional-location=%DG_APP_HOME%config\application.properties

if "%JAVA_HOME%" == "" goto runNoJavaHome
if not "%JAVA_HOME%" == "" goto runJavaHome

:runNoJavaHome
java %JAVA_OPTS% -jar "%DG_APP_FULL_NAME%" %DG_SPRING_OPTS%
goto end

:runJavaHome
echo Using JAVA_HOME "%JAVA_HOME%"
"%JAVA_HOME%/bin/java" %JAVA_OPTS% -jar "%DG_APP_FULL_NAME%" %DG_SPRING_OPTS%
goto end

:end

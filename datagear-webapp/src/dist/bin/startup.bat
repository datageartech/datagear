@echo off

rem JAVA_HOME
rem DG_APP_HOME this make application can run at other path

set DG_APP_NAME=${productNameJar}

if "%DG_APP_HOME%" == "" goto setAppHomeAuto
if not "%DG_APP_HOME%" == "" goto okAppHome

:setAppHomeAuto
set DG_APP_HOME=%~dp0
goto okAppHome

:okAppHome
set DG_APP_FULL_NAME=%DG_APP_HOME%\%DG_APP_NAME%
set DG_SPRING_OPTS=--spring.config.additional-location=%DG_APP_HOME%\config\application.properties

if "%JAVA_HOME%" == "" goto setCmdNoJavaHome
if not "%JAVA_HOME%" == "" goto setCmdHasJavaHome

:setCmdNoJavaHome
set DG_JAVA_CMD=java
echo Using PATH env command
goto run

:setCmdHasJavaHome
set DG_JAVA_CMD=%JAVA_HOME%\bin\java
echo Using JAVA_HOME "%JAVA_HOME%"
goto run

:run
"%DG_JAVA_CMD%" -cp %DG_APP_FULL_NAME% -Dloader.home=%DG_APP_HOME% -Dloader.path=%DG_APP_NAME%!/WEB-INF/lib-provided,%DG_APP_NAME%!/WEB-INF/lib,lib,%DG_APP_NAME%!/WEB-INF/classes org.springframework.boot.loader.PropertiesLauncher %DG_SPRING_OPTS%
goto end

:end

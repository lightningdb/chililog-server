@ echo off
SETLOCAL ENABLEDELAYEDEXPANSION

rem Change directory to the directory where the script is located
cd /d %~dp0
cd ..

rem Setup install args
set SERVICE_NAME=ChiliLogServer
set "CHILILOG_HOME=%cd%"

rem Check if the apache daemon program exists
set "EXECUTABLE=%CHILILOG_HOME%\bin\ChiliLogServer.exe"
if exist "%EXECUTABLE%" goto doUninstall
echo The ChiliLogServer.exe was not found...
echo The CHILILOG_HOME environment variable is not defined correctly.
echo This environment variable is needed to run this program
goto end

:doUninstall
rem Install the service
echo UNINSTALLING the service '%SERVICE_NAME%' ...
"%EXECUTABLE%" //DS//%SERVICE_NAME%
if not errorlevel 1 goto uninstalled
echo Failed uninstalling '%SERVICE_NAME%' service
goto end
:uninstalled
rem Clear the environment variables. They are not needed any more.
set PR_DISPLAYNAME=
set PR_DESCRIPTION=
set PR_INSTALL=
set PR_LOGPATH=
set PR_CLASSPATH=
set PR_JVM=

echo The service '%SERVICE_NAME%' has been uninstalled.

:end
ENDLOCAL

@ echo off
SETLOCAL ENABLEDELAYEDEXPANSION

rem ***************************************************************************************
rem This bat file uninstalls ChiliLog Server as a windows service
rem 
rem USAGE: windows_service_uninstall.bat [32|64|ia64]
rem        - 32 = 32 bit Windows and 32 bit java run time
rem        - 64 = 64 bit Windows with Intel or AMD processor and 64 bit java run time
rem        - ia64 = 64 bit Windows with Itanium processor and 64 bit java run time
rem ***************************************************************************************

rem check for parameters
if [%1]==[] goto usage
goto validParameters

:usage
echo USAGE: windows_service_uninstall.bat [32 ^| 64 ^| ia64]
echo        - 32 = 32 bit Windows and 32 bit java run time
echo        - 64 = 64 bit Windows with Intel or AMD processor and 64 bit java run time
echo        - ia64 = 64 bit Windows with Itanium processor and 64 bit java run time
goto end

:validParameters
rem Change directory to the directory where the script is located
cd /d %~dp0
cd ..

rem Setup install args
set SERVICE_NAME=ChiliLogServer
set "CHILILOG_HOME=%cd%"

rem Check if the apache daemon program exists
set "EXECUTABLE=%CHILILOG_HOME%\bin\ChiliLogServer%1.exe"
if exist "%EXECUTABLE%" goto doUninstall
echo "The ChiliLogServer%1.exe was not found..."
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

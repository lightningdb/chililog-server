@ echo off
SETLOCAL ENABLEDELAYEDEXPANSION

rem ***************************************************************************************
rem This bat file installs ChiliLog Server as a windows service
rem 
rem USAGE: windows_service_install.bat [32|64|ia64]
rem        - 32 = 32 bit Windows and 32 bit java run time
rem        - 64 = 64 bit Windows with Intel or AMD processor and 64 bit java run time
rem        - ia64 = 64 bit Windows with Itanium processor and 64 bit java run time
rem ***************************************************************************************

rem check for parameters
if [%1]==[] goto usage
goto validParameters

:usage
echo USAGE: windows_service_install.bat [32 ^| 64 ^| ia64]
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

rem Setup JVM arguments
set JVM_ARGS="-Xmx512M"

rem Setup Classpath to configuration files and jars
set "CLASSPATH=.;%CHILILOG_HOME%\config"
for /R %CHILILOG_HOME%\lib %%A in (*.jar) do (
    set CLASSPATH=!CLASSPATH!;%%A
)

:gotHome
rem Check if the apache daemon program exists
set "EXECUTABLE=%CHILILOG_HOME%\bin\ChiliLogServer%1.exe"
if exist "%EXECUTABLE%" goto okHome
echo "The ChiliLogServer%1.exe was not found..."
echo The CHILILOG_HOME environment variable is not defined correctly.
echo This environment variable is needed to run this program
goto end
:okHome
rem Make sure prerequisite environment variables are set
if not "%JAVA_HOME%" == "" goto gotJdkHome
if not "%JRE_HOME%" == "" goto gotJreHome
echo Neither the JAVA_HOME nor the JRE_HOME environment variable is defined
echo Service will try to guess them from the registry.
goto okJavaHome
:gotJreHome
if not exist "%JRE_HOME%\bin\java.exe" goto noJavaHome
if not exist "%JRE_HOME%\bin\javaw.exe" goto noJavaHome
goto okJavaHome
:gotJdkHome
if not exist "%JAVA_HOME%\jre\bin\java.exe" goto noJavaHome
if not exist "%JAVA_HOME%\jre\bin\javaw.exe" goto noJavaHome
if not exist "%JAVA_HOME%\bin\javac.exe" goto noJavaHome
if not "%JRE_HOME%" == "" goto okJavaHome
set "JRE_HOME=%JAVA_HOME%\jre"
goto okJavaHome
:noJavaHome
echo The JAVA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
echo NB: JAVA_HOME should point to a JDK not a JRE
goto end
:okJavaHome


:doInstall
rem Install the service
echo Installing the service '%SERVICE_NAME%' ...
echo Using CHILILOG_HOME:    "%CHILILOG_HOME%"
echo Using JAVA_HOME:        "%JAVA_HOME%"
echo Using JRE_HOME:         "%JRE_HOME%"

rem Use the environment variables as an example
rem Each command line option is prefixed with PR_
set PR_DESCRIPTION=Real Time Log Aggregation, Analysis and Monitoring Server
set PR_DISPLAYNAME=ChiliLog Server
set PR_JVMMX=512
set PR_STARTMODE=jvm
set PR_STOPMODE=jvm
set PR_STOPTIMEOUT=7
set "PR_STARTPATH=%CHILILOG_HOME%\bin"
set "PR_STOPPATH=%CHILILOG_HOME%\bin"
set "PR_INSTALL=%EXECUTABLE%"
set "PR_LOGPATH=%CHILILOG_HOME%\logs"
set PR_STDOUTPUT=auto
set PR_STDERROR=auto
set "PR_CLASSPATH=%CLASSPATH%"
rem Set the server jvm from JAVA_HOME
set "PR_JVM=%JRE_HOME%\bin\server\jvm.dll"
if exist "%PR_JVM%" goto foundJvm
rem Set the client jvm from JAVA_HOME
set "PR_JVM=%JRE_HOME%\bin\client\jvm.dll"
if exist "%PR_JVM%" goto foundJvm
set PR_JVM=auto

:foundJvm
echo Using JVM:             "%PR_JVM%"
"%EXECUTABLE%" //IS//%SERVICE_NAME% --StartClass org.chililog.server.App --StopClass org.chililog.server.App --StartMethod start --StopMethod stop
if not errorlevel 1 goto installed
echo Failed installing '%SERVICE_NAME%' service
goto end
:installed
rem Clear the environment variables. They are not needed any more.
set PR_DISPLAYNAME=
set PR_DESCRIPTION=
set PR_INSTALL=
set PR_LOGPATH=
set PR_CLASSPATH=
set PR_JVM=

echo The service '%SERVICE_NAME%' has been installed.
echo Run ChiliLogServerw.exe to perform further configuration.

:end
ENDLOCAL

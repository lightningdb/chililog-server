@ echo off
SETLOCAL ENABLEDELAYEDEXPANSION

rem Change directory to the directory where the script is located
cd /d %~dp0

rem Setup JVM arguments
set JVM_ARGS="-Xmx512M"

rem Setup Classpath to configuration files and jars
set CLASSPATH=.;..\config
for /R ..\lib %%A in (*.jar) do (
    set CLASSPATH=!CLASSPATH!;%%A
)

rem Run command
echo ***********************************************************************************
echo "java %JVM_ARGS% -classpath %CLASSPATH% org.chililog.server.App"
echo ***********************************************************************************
java %JVM_ARGS% -classpath %CLASSPATH% org.chililog.server.App
 
ENDLOCAL

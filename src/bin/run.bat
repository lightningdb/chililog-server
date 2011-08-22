@ echo off
SETLOCAL ENABLEDELAYEDEXPANSION

set JVM_ARGS="-Xmx512M"

set CLASSPATH=.
set CLASSPATH=%%CLASSPATH;cc
for /R ..\lib %%A in (*.jar) do (
    set CLASSPATH=!CLASSPATH!;%%A
)

echo ***********************************************************************************
echo "java %JVM_ARGS% -classpath %CLASSPATH% org.chililog.server.App"
echo ***********************************************************************************
java %JVM_ARGS% -classpath %CLASSPATH% org.chililog.server.App
 
ENDLOCAL

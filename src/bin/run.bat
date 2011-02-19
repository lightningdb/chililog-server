@ echo off

set JVM_ARGS="-Xmx512M"

set CLASSPATH=.
for /R ..\lib %%A in (*.jar) do (
    SET CLASSPATH=!CLASSPATH!;%%A
)

echo ***********************************************************************************
echo "java %JVM_ARGS% -classpath %CLASSPATH% com.chililog.server.App"
echo ***********************************************************************************
java %JVM_ARGS% -classpath %CLASSPATH% com.chililog.server.App
 

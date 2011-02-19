#!/bin/sh
 
export JVM_ARGS="-Xmx512M"
 
export CLASSPATH=.
for i in `ls ../lib/*.jar`; do
  CLASSPATH=$i:$CLASSPATH
done

echo "***********************************************************************************"
echo "java $JVM_ARGS -classpath $CLASSPATH com.chililog.server.App"
echo "***********************************************************************************"
java $JVM_ARGS -classpath $CLASSPATH com.chililog.server.App


#!/bin/sh
 
export JVM_ARGS="-Xmx512M"
 
export CLASSPATH=.:../config
for i in `ls ../lib/*.jar`; do
  CLASSPATH=$CLASSPATH:$i
done

echo "***********************************************************************************"
echo "java $JVM_ARGS -classpath $CLASSPATH org.chililog.server.App"
echo "***********************************************************************************"
java $JVM_ARGS -classpath $CLASSPATH org.chililog.server.App


#!/bin/sh
 
 mvn clean
 mvn -P release -Dmaven.test.skip=true package

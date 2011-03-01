#!/bin/sh
 
 mvn -P release -Dmaven.test.skip=true package

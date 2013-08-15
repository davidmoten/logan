#!/bin/bash
export MAVEN_OPTS="$MAVEN_OPTS -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n"
mvn clean jetty:run 

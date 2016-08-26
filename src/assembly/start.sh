#!/bin/bash
set -e
## use first parameter, if missing default to 8080
export PORT=${1-8080}
## use first parameter, if missing default to 9797
export STOP_PORT=${2-9797}
export STOP_KEY=logan
java -Dlogan.config=configuration.xml -jar jetty-runner.jar --port $PORT --stop-port $STOP_PORT --stop-key $STOP_KEY --out yyyy_mm_dd-logan.log logan.war

#!/bin/bash
set -e
export PORT=8080
export STOP_PORT=9292
export STOP_KEY=logan
java -Dlogan.config=configuration.xml -jar jetty-runner.jar --port $PORT --stop-port $STOP_PORT --stop-key $STOP_KEY --out yyyy_mm_dd-logan.log logan.war

#!/bin/bash
set -e
## use first parameter, if missing default to 8080
export PORT=${1-8080}
## use first parameter, if missing default to 9797
export STOP_PORT=${2-9797}
export STOP_KEY=logan
export CONFIG=${3-configuration.xml}
java -Dlogan.config=$CONFIG $4 $5 $6 $7 $8 $9 -jar jetty-runner.jar --port $PORT --stop-port $STOP_PORT --stop-key $STOP_KEY --out yyyy_mm_dd-logan.log logan.war

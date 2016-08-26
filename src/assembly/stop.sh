#!/bin/bash
set -e
## use first parameter, if missing default to 9797
export STOP_PORT=${1-9797}
export STOP_KEY=logan
java -jar start.jar STOP.PORT=$STOP_PORT STOP.KEY=$STOP_KEY --stop

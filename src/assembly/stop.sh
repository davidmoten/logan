#!/bin/bash
export STOP_PORT=9292
export STOP_KEY=logan
java -jar start.jar STOP.PORT=$STOP_PORT STOP.KEY=$STOP_KEY --stop

#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"
cd $DIR
cd ..

LOG_PREFIX=`date +%Y%m%d.%H%M%S`

. config/config.properties

function start {
    echo START API SERVICE
    nohup java -jar bin/booking-api.jar server config/bookingapi.yaml > logs/${LOG_PREFIX}.booking-api.log 2>&1 &
    echo STARTED
}

function stop {
    echo STOP API SERVICE
    kill -SIGTERM `ps -ef | grep "bin/booking-api.jar server config/bookingapi.yaml" | grep -v grep | awk '{print $2}'`
    echo STOPPED
}

function restart {
    stop
    sleep 1
    start
}

if [ $# -lt 1 ]; then
    echo "Usage: ./service [stop|start|restart]"
elif [ $# -eq 1 ]; then
    $1
fi

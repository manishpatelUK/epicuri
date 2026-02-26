#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"
cd $DIR
cd ..

REQUIRED_Q="v3.2"
export QHOME=${DIR}/../../kdb/${REQUIRED_Q}/q

QBIN32=$QHOME/l32/q

LOG_PREFIX=`date +%Y%m%d.%H%M%S`

export TZ="UTC"

. config/config.properties

function start {
    if [ $# -lt 1 ]; then
        start_aurusit
    elif [ $# -eq 1 ]; then
        start_$1
    fi
}

function start_aurusit () {
    nohup $QBIN32 data -g 1 -o 0 -p $PORT > logs/${LOG_PREFIX}.aurusit.log 2>&1 &
}

function stop {
    if [ $# -lt 1 ]; then
        stop_aurusit
    elif [ $# -eq 1 ]; then
        stop_$1
    fi
}

function stop_aurusit {
    $QBIN32 ../common/stop.q localhost:${PORT}
}

function restart {
    if [ $# -lt 1 ]; then
        stop
        sleep 1
        start
    elif [ $# -eq 1 ]; then
        stop_$1
        sleep 1
        start_$1
    fi
}

if [ $# -lt 1 ]; then
    echo "Usage: ./service [stop|start|restart] [aurusit]"
elif [ $# -eq 1 ]; then
    $1
elif [ $# -eq 1 ]; then
    $1 $2
fi


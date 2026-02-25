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
        start_statics
    elif [ $# -eq 1 ]; then
        start_$1
    fi
}

function start_statics {
    nohup $QBIN32 data -g 1 -o 0 -p $PORT -U config/statics.pass > logs/${LOG_PREFIX}.statics.log 2>&1 &
}

function stop {
    if [ $# -lt 1 ]; then
        stop_statics
    elif [ $# -eq 1 ]; then
        stop_$1
    fi
}

function stop_statics {
    $QBIN32 ../common/stop.q localhost:${PORT}:config/statics.pass
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
    echo "Usage: ./service [stop|start|restart] [statics]"
elif [ $# -eq 1 ]; then
    $1
elif [ $# -eq 1 ]; then
    $1 $2
fi


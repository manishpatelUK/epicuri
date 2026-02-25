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
        start_tp
        start_rt
        start_hdb
    elif [ $# -eq 1 ]; then
        start_$1
    fi
}

function start_tp {
    nohup $QBIN32 tick.q emails data -o 0 -p $TP_PORT -U config/emails.pass > logs/${LOG_PREFIX}.tp-emails.log 2>&1 &
}

function start_rt {
    CREDS=$(cat config/emails.pass)
    nohup $QBIN32 startup/emailsrt.q localhost:${TP_PORT}:${CREDS} -U config/emails.pass localhost:${HDB_PORT} -o 0 -p $RT_PORT > logs/${LOG_PREFIX}.rt-emails.log 2>&1 &
}

function start_hdb {
    nohup $QBIN32 startup/emailshdb.q -o 0 -p $HDB_PORT -g 1 -U config/emails.pass > logs/${LOG_PREFIX}.hdb-emails.log 2>&1 &
}

function stop {
    if [ $# -lt 1 ]; then
        stop_tp
        stop_rt
        stop_hdb
    elif [ $# -eq 1 ]; then
        stop_$1
    fi
}

function stop_tp {
    $QBIN32 ../common/stop.q localhost:${TP_PORT}:config/emails.pass
}

function stop_rt {
    $QBIN32 ../common/stop.q localhost:${RT_PORT}:config/emails.pass
}

function stop_hdb {
    $QBIN32 ../common/stop.q localhost:${HDB_PORT}:config/emails.pass
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
    echo "Usage: ./service [stop|start|restart] [tp|rt|hdb|triplet]"
elif [ $# -eq 1 ]; then
    $1
elif [ $# -eq 1 ]; then
    $1 $2
fi


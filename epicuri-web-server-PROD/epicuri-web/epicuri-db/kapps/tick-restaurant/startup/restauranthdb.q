\l data/restaurant

/ expects `timeout`restaurant`reservationId
.rest.hdb.getCurrentCheckins:{[dict]
    smallestDate:`date$.z.p-dict`timeout;
    convertToLive select from checkins where date>=smallestDate, sym=`$string dict`restaurantId, time > .z.p-dict`timeout, reservationId=dict`reservationId
    };

//todo below should be in a common lib
convertToLive:{[t]
    `time`sym xcols delete date from t
    }
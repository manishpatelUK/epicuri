// loaded by init.q
// see globals in that file

/ expects `restaurantId`reservationId
.rest.getCurrentCheckins:{[dict]
    / get checkin time out
    timeout:tryParseAndRelay[(`.md.getDefault;dict,enlist[`defaultName]!enlist`CheckInExpirationTime);00:15];
    / select from live and hdb if required
    dict:dict,enlist[`timeout]!enlist timeout;
    t:tryParseAndRelay[(`.rest.live.getCurrentCheckins; dict);()];
    if[0>.z.t-timeout;t,tryParseAndRelay[(`.rest.hdb.getCurrentCheckins; dict);()]];
    / return
    t
    };

/ expects `restaurantId`defaultName`checkinId
.rest.getSpecificCheckin:{[dict]
    select from .rest.getCurrentCheckins[dict] where id=dict`checkinId
    };
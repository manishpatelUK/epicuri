/ expects `timeout`restaurantId`reservationId
.rest.live.getCurrentCheckins:{[dict]
    select from checkins where time > .z.p-dict`timeout, sym=`$string dict`restaurantId, reservationId=dict`reservationId
    };

.rest.live.reserveNextCheckinId:{[dict]
    currentCheckInId::currentCheckInId+1;
    currentCheckInId
    };

/ custom upds
customUpds:`checkins!`customupdci; //todo more?

customupdci:{[x;y]
    };

/ ----------- RTDB FUNCTIONALITY ---------
/q tick/r.q [host]:port[:usr:pwd] [host]:port[:usr:pwd]
/2008.09.09 .k ->.q

if[not "w"=first string .z.o;system "sleep 1"];

/ specialised upd
upd:{[x;y] $[x in key customUpds;customUpds[x][x;y];insert[x;y]]};

/ get the ticker plant and history ports, defaults are 5010,5012
.u.x:.z.x,(count .z.x)_(":5010";":5012");

/ end of day: save, clear, hdb reload
.u.oend:{t:tables`.;t@:where `g=attr each t@\:`sym;.Q.hdpf[`$":",.u.x 1;`:.;x;`sym];@[;`sym;`g#] each t;};
.u.end:{
    //todo
    };

/ init schema and sync up from log file;cd to hdb(so client save can run)
.u.rep:{(.[;();:;].)each x;if[null first y;:()];-11!y;system "cd ",1_-10_string first reverse y};
/ HARDCODE \cd if other than logdir/db

/ connect to ticker plant for (schema;(logcount;log))
.u.rep .(hopen `$":",.u.x 0)"(.u.sub[`;`];`.u `i`L)";
/ ----------------------------------------



/ apply attributes where possible
/ todo do this after .u.end too
@[@[`checkins;`customerId;`s#];::;{-1 "Could not apply sort to checkin id"}];

/ init globals
currentCheckInId:max[checkins`id];
if[max[`int$()]=currentCheckInId;currentCheckInId:0i];
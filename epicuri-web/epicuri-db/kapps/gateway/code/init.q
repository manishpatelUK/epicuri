/ q code/init.q [-p x -g 1] -config <file>

// load all libs
\l ../lib-comm/code/lib-comm.q
\l code/live.q

// connection handling
// .ca.* goes to customer-auth db
// .ha.* goes to host-auth
connections:([] dbKey:`symbol$(); dbSubKey:`symbol$(); handle:`symbol$(); socket:`int$(); callCount:`int$());

.z.pc:{
    update handle:0N from `connections where handle=x;
    }

.z.pg:{[x]
    $[type[first x]~-11h;
        $[count["." vs string first x]>0;parseAndRelay x;value x];
        value x
     ]
    }

getNextConnection:{[DBKey;DBSubKey]
    s:first exec socket from `callCount xdesc select callCount,socket from connections where dbKey=DBKey, dbSubKey=DBSubKey, not null socket;
    $[null s;'`conn;s]
    };

parseAndRelay:{[x]
    $[count[y:"." vs string[x 0]]=3;
        relay[y 1;`;;] . x;
      count[y]=4;
        relay[y 1;y 2;;] . x;
      value x;
     ];
    };

tryParseAndRelay:{[x;default]
    $[count[y:"." vs string[x 0]]=3;
        @[relay[y 1;`;;] . x;::;{[x;y] x}[default;]];
      count[y]=4;
        @[relay[y 1;y 2;;] . x;::;{[x;y] x}[default;]];
      @[value;x;{[x;y] x}[default;]];
     ];
    };

relay:{[dbKey;dbSubKey;funcName;param]
    $[all ` = dbKey,dbSubKey;
        funcName param;
        getNextConnection[dbKey;dbSubKey](funcName;param)
     ]
    };


/ --------- CONFIG -----------
/
 expected format of config:
 dbKey.dbSubKey:[handleString]
    where handleString is of form host:x-y:u:p[,host:x-y:u:p ...]
\
loadConfig:{[]
    lines:read0 `$":",.Q.opt[.z.x]`config
    .private.readLine each lines
    };

.private.readLine:{[line]
    kv:first ":" vs line;
    cons:1 _ '":" vs' "," vs line;
    conns[;2]:"I"$"-" vs' conns[;2];
        {
            r:"I"$x[2];
            p:hsym `$":" sv' string (`$x[1]),'(min[r] + til max[1+r]-min r),'`$count[r]#x[3];
            `connections insert (p#`$kv[0]; p#`$kv[1];p; p#0N; p#0)
        } each conns;
    };

@[loadConfig;`;{-1 "Error loading config:",string x; exit 1}];

/ ----------------------------

/ timer
.z.ts:{
    // retry closed connections
    {[h] update socket:.comm.tryOpenTimeOutHsym[h;500] from `connections where handle=h} each exec handle from connections where null socket;
    }

\t 10000
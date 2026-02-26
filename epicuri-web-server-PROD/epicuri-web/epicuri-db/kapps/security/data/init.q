validTimespan:0D01:00;

insertPass:{[ID;token]
    delete from `passes where id=ID;
    `passes insert(.z.p;ID;md:"" sv string md5 token; token);
    save `:passes;
    md
    };

getToken:{[m]
    r:select token from passes where md like m;
    $[count r;updateTokenTime last r`token;'`access]
    };

updateTokenTime:{[t]
    update time:.z.p+validTimespan from `passes where token like t;
    save `:passes;
    t
    };

getInternalTokenById:{[ID]
    r:select md from passes where id=ID;
    $[count r;updateMDTime last r`md;(),""]
    };

updateMDTime:{[MD]
    update time:.z.p+validTimespan from `passes where md like MD;
    save `:passes;
    MD
    };

protractExpiry:{[ID]
    update time:.z.p+2D from `passes where id=ID;
    save `:passes;
    };

.z.ts:{
    delete from `passes where time<.z.p-validTimespan;
    save `:passes;
    };

.z.exit:{[]
    save`:passes;
    -1 "Exited gracefully";
    };

\t 60000

if[not `passes in tables`.;passes:([] time:`timestamp$(); id:`symbol$(); md:(); token:());@[`passes;`time;`s#]];
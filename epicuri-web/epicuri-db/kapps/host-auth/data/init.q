/ tokens:([id:`long$()] time:`s#`timestamp$(); token:())

\l ../common/auth.q

expirationTime:30D;

// dict expects `id
.ha.insertAuthKey:{[dict]
    .auth.insertAuthKey dict
    };

// dict expects `token
.ha.authenticate:{[dict]
    .auth.authenticate dict
    };

 .z.ts:{
    delete from `tokens where time<.z.p-expirationTime;
    save`:tokens;
    };

\t 300000







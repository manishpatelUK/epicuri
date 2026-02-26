/ tokens:([id:`long$()] time:`s#`timestamp$(); token:())

\l ../common/auth.q

expirationTime:30D;

// dict expects `id
.ca.insertAuthKey:{[dict]
    .auth.insertAuthKey dict
    };

// dict expects `token
.ca.authenticate:{[dict]
    .auth.authenticate dict
    };

 .z.ts:{
    delete from `tokens where time<.z.p-expirationTime;
    save`:tokens;
    };

\t 300000







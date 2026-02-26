/ tokens:([id:`long$()] time:`s#`timestamp$(); token:())

// dict expects `id
.auth.insertAuthKey:{[dict]
    md5d:string[dict`id],"-","" sv string md5 string .z.p;
    `tokens insert (.z.p; dict[`id]; md5d);
    md5d
    };

// dict expects `token
.auth.authenticate:{[dict]
    anId:"J"$first "-" vs dict`token;
    `boolean$count select from tokens where id=anId, token like dict`token
    };




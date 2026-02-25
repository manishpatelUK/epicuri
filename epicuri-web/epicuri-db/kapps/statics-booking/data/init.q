getStatics:{[lang]
    a:select from statics where language=lang;
    0!$[count a;a;select from statics where language=`en]
    };

getEmailStatics:{[lang;recip]
    a:select from emailStatics where language=lang, recipient=recip;
    0!$[count a;a;select from statics where language=`en, recipient=recip]
    };

.z.ts:{
    @[readTable;;{[x]}] each tables[];
    };

readTable:{[t]
    @[`.;t;:;@[get;hsym t;{[x]}]];
    };

.z.exit:{[]
    save`:statics;
    -1 "Exited gracefully";
    };

\t 60000
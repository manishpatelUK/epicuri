// stops a local kdb instance
// usage:
// q stop.q <port>[:<credentials>] [timeout]
// q stop.q <port>[:file.pass] [timeout]
// q stop.q 5001
// q stop.q 5001:username:password 10000
// q stop.q 5001:file.pass 10000

postfix:.z.x 0;
timeout:.z.x 1;

gracefulexit:{exit 0};

if[.z.x~();0N!"parameters missing";gracefulexit[]];

if[postfix like "*.pass";
    filecomp:hsym `$last comps:":" vs postfix;
    postfix:(":" sv -1 _ comps),":",first read0 filecomp
    ];

param:`$$[count[":" vs postfix]=1;":localhost:",postfix;":",postfix];
if[not ""~timeout;param:(param;"I"$timeout)];
h:@[hopen;param;{0N!"Could not open connection to ",postfix," - exiting";gracefulexit[]}];
0N!string[.z.z],"GMT: Attempt to kill KDB process...", postfix;
@[{x(gracefulexit;::)};h;{}];
gracefulexit[];
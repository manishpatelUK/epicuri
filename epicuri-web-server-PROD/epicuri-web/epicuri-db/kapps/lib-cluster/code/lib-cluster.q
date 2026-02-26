\l lib-comm/code/lib-comm.q

.cluster.connections: ([h:`symbol$()] c:`int$());
.cluster.timeout: 4000;

// this instance is the designated WRITER in the cluster?
.cluster.WRITER:0b;

.cluster.addconn:{[host;port;username;password]
    h: .comm.handle[host;port;username;password];
    c: .comm.tryOpenTimeOutHsym[handle;.cluster.timeout];
    if[not null c;`.cluster.connections upsert (h;c)];
    };


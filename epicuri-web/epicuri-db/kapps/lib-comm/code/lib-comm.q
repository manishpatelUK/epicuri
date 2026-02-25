.comm.STDOUT:-1

.comm.tryOpen:{[host;port;username;password]
    handle:hsym `$string[host],":",string[port],$[any `=username,password;"";string[username],":",string[password]];
    @[hopen;handle;{0N}]
    };

.comm.tryOpenTimeOut:{[host;port;username;password;timeout]
    handle:.comm.handle[host;port;username;password];
    .comm.tryOpenTimeOutHsym[handle;timeout]
    };

.comm.handle:{[host;port;username;password]
    hsym `$string[host],":",string[port],$[any `=username,password;"";string[username],":",string[password]]
    };

.comm.tryOpenTimeOutHsym:{[handle;timeout]
    @[hopen;(handle;timeout);{0N}]
    };

.comm.log:{[message]
    .comm.STDOUT":" sv (string[.z.z];string[.z.f];$[10h~type message;message;-3!message]);
    };
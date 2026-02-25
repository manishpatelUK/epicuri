using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class WaitingPartyAndOrderPayload
    {
        public WaitingParty party {get; set;}
        public OrderPayload[] order { get; set; }

    
    }
}
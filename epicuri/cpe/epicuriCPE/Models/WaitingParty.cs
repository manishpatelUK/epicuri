using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class WaitingParty : Party
    {
        public Customer LeadCustomer;

        public int[] Tables { get; set; }
        public Boolean CreateSession { get; set; }
        public int ServiceId { get; set; }
        public Boolean IsAdHoc { get; set; }
    }
}
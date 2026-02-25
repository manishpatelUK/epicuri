using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.API.Models
{
    public class DeliveryCostPayload
    {
        public string Street { get; set; }
        public string Town { get; set; }
        public string City { get; set; }
        public string Postcode { get; set; }
    }
}
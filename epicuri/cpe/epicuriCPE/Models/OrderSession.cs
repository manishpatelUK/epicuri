using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class OrderSession
    {
        public string KitchenIdentifiers;
        public IEnumerable<IGrouping<int,Order>> Orders;
        public Double Created;
    }
}
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using epicuri.Core.DatabaseModel;

namespace epicuri.API.Models
{
    public class dbCustomer
    {
        public static int GetEffectiveBlackMarkCount(epicuri.Core.DatabaseModel.Customer c)
        {
            return c.BlackMarks.Count(bm => DateTime.UtcNow < bm.Expires);
                             
        }

        public static bool OKToOrder(epicuri.Core.DatabaseModel.Customer c)
        {
            int blackMarkCount = GetEffectiveBlackMarkCount(c);
            if (blackMarkCount < 3)
                return true;
            else
                return false;
        }
    }
}
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuriOnBoarding.Models
{
    public class TakeAwayServiceEnum
    {
        public TakeAwayService takeAwayService { get; set; }

        public enum TakeAwayService
        {
            Not_Offered = 0,
            Delivery_Only,
            Collection_Only,
            Delivery_and_Collection
        }
    }
}
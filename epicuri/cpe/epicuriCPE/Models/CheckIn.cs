using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class CheckIn : DataModel
    {
        public int Id;
        public int RestaurantId;
        public Customer Customer;
        public double Time;
        public int PartyId;

        public CheckIn(Core.DatabaseModel.CheckIn checkin)
        {
            Id = checkin.Id;
            Customer = new Models.Customer(checkin.Customer);
            RestaurantId = checkin.Restaurant.Id;
            Time = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(checkin.Time);
            if (checkin.Party != null)
                PartyId = checkin.Party.Id;
        }
    }
}
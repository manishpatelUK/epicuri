using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using epicuri.Core.DatabaseModel;

namespace epicuri.API.Models
{
    public class CheckIn : CPE.Models.DataModel
    {
        public int Id;
        public int RestaurantId;
        public int? ReservationId;
        public double Time;
        private Core.DatabaseModel.Customer Customer;
        private Core.DatabaseModel.Party Party;
        public int SessionId;
        public IEnumerable<int> Tables;

        public CheckIn()
        {

        }

        public CheckIn(Core.DatabaseModel.CheckIn checkin)
        {
           
        }

        public CheckIn(Core.DatabaseModel.Customer customer, int checkInId)
        {
            Init();
            /*
             * Get the checkin from DB
             */
            epicuri.Core.DatabaseModel.CheckIn checkin = db.CheckIns.FirstOrDefault(c => c.Id == checkInId);

            /*
             * Validate
             */
            if (checkin == null)
            {
                throw new Exception("Checkin not found");
            }

            if (checkin.Customer.Id != customer.Id)
            {
                throw new Exception("Checkin belongs to a different customer");
            }

            /*
             * Set the values
             */
            Id = checkin.Id;
            Time = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(checkin.Time);
            RestaurantId = checkin.Restaurant.Id;

            if (checkin.Party != null)
                ReservationId = checkin.Party.Id;

            if (checkin.Diner == null)
            {
                SessionId = 0;
            }
            else
            {

                if (checkin.Diner.SeatedSessionId.HasValue)
                {
                    SessionId = checkin.Diner.SeatedSessionId.Value;
                    Tables = db.Sessions.OfType<SeatedSession>()
                        .Single(s => s.Id == checkin.Diner.SeatedSessionId)
                        .Tables
                        .Select(t => t.Id)
                        .AsEnumerable();
                }
                else
                {
                    SessionId = 0;
                }
            }

            





        }

        public void SetParty(Core.DatabaseModel.Party party)
        {
            Party = party;
        }

        public void SetCustomer(Core.DatabaseModel.Customer customer)
        {
            Customer = customer;
        }

        public Core.DatabaseModel.CheckIn ToCheckIn()
        {
            return new Core.DatabaseModel.CheckIn
            {
                Customer = Customer,
                Party = Party,
                Time = DateTime.UtcNow
            };
        }
    }
}
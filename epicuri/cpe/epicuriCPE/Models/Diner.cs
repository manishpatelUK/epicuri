using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    
    public class Diner
    {
        public int Id;
        public bool IsTable;

        public Customer EpicuriUser {get;set;}

        public IEnumerable<int> Orders;
        public int SessionId;
        public bool IsBirthday;

        public decimal SubTotal { get; set; }
        public decimal SharedTotal { get; set; }
        public DateTime InteractionTime { get; set; }
        public Boolean IsTakeawaySession;
       
        public Diner(){}


        public Diner(Core.DatabaseModel.Diner diner)
        {
            Id = diner.Id;
            IsTable = diner.IsTable;
            Orders = from order in diner.Orders
                     select order.Id;
           
            IsBirthday = false;
            EpicuriUser = diner.Customer == null ?
                        null :
                        new Models.Customer(diner.Customer);


            
        }
        
        public Diner(Core.DatabaseModel.Diner diner, Core.DatabaseModel.SeatedSession sess)
        {

            Id = diner.Id;
            IsTable = diner.IsTable;
            Orders = from order in diner.Orders
                        select order.Id;
            SessionId = sess.Id;
            IsBirthday = false;
            EpicuriUser = diner.Customer == null ?
                        null :
                        new Models.Customer(diner.Customer);
            
            // Check if users birthday
            int birthdayTimeSpan = epicuri.Core.Settings.Setting<int>(sess.RestaurantId, "BirthdayTimespan");

            if (diner.Customer != null && diner.Customer.Birthday.HasValue)
            {
                DateTime birthdayThisYear = new DateTime(DateTime.UtcNow.Year, diner.Customer.Birthday.Value.Month, diner.Customer.Birthday.Value.Day);
                DateTime startBirthdayPeriod = birthdayThisYear.AddDays(-1 * Math.Ceiling(Convert.ToDouble(birthdayTimeSpan) / 2));
                DateTime endBirthdayPeriod = birthdayThisYear.AddDays(Math.Ceiling(Convert.ToDouble(birthdayTimeSpan) / 2));

                if (DateTime.UtcNow >= startBirthdayPeriod && DateTime.UtcNow <= endBirthdayPeriod)
                {
                    IsBirthday = true;
                }
            }
            
        }
   
        public Diner(Core.DatabaseModel.Diner diner, Core.DatabaseModel.TakeAwaySession sess)
        {
            Id = diner.Id;
            IsTable = diner.IsTable;
            Orders = from order in diner.Orders
                     select order.Id;
            SessionId = sess.Id;

            EpicuriUser = diner.Customer == null ?
                        null :
                        new Models.Customer(diner.Customer);
        }
  
        public Core.DatabaseModel.Diner ToDiner()
        {
            return new Core.DatabaseModel.Diner
            {
                SeatedSessionId = SessionId,
                IsTable = false
            };
        }


    }
}
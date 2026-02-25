using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
//using System.ComponentModel.DataAnnotations.Schema;
//using System.Data.Objects;
using epicuri.Core.DatabaseModel;
using epicuri.Core;
using NodaTime;

namespace epicuri.CPE.Models
{
    public class Ticket : DataModel
    {
        public int Id;
        public string Summary { get; private set; }
        public string AdditionalInfo { get; private set; }
        public string AdditionalInfo2 { get; private set; }
        public Ticket() { }
        //public  IEnumerable<Models.Order> Orders { get; private set; }
        public List<Models.TicketCourse> Courses { get; private set; }
        public IEnumerable<int> OrderIds { get; private set; }
        public Boolean AllComplete { get; private set; }
        public Boolean IsAdHoc { get; private set; }
        public DateTime? Completed { get; private set; }
        public DateTime SortTime { get; private set; }
        public Ticket(int printer, Core.DatabaseModel.Session sess)
        {
            Init();

            this.Id = sess.Id;
            this.Completed = sess.ClosedTime;



            DateTime minTime = DateTime.UtcNow.AddMinutes(-epicuri.Core.Settings.Setting<int>(sess.RestaurantId, "TakeawayMinimumTime"));
            var timezone = sess.Restaurant.IANATimezone;

            /*
             * If we're a seated session then we need to get extra info about the table and the number of covers
             */
            if (sess.GetType() == typeof(Core.DatabaseModel.SeatedSession))
            {
                Core.DatabaseModel.SeatedSession sessionInfo = ((Core.DatabaseModel.SeatedSession)sess);
                Summary = "Dine in" + (sessionInfo.Party!=null ? " - "+sessionInfo.Party.Name : "");

                if (sessionInfo.IsAdHoc)
                {
                    Summary = "Ad-Hoc Session #"+sessionInfo.Id;
                }

                AdditionalInfo = "" + sessionInfo.Diners.Count(d => !d.IsTable) + " covers. " + (sessionInfo.Tables.Any() ? "Table: " + sessionInfo.Tables.Select(t => t.Name).Aggregate((current, next) => current + ", " + next) : "");
                Courses = sess.Orders.
                    //EP-222
                    //Where(o => !o.Completed.HasValue || o.Completed.Value >= minTime).
                    Where(o =>
                            (
                            printer == 0 ||     //If dont we have a printer then this block is skipped
                            sess.Restaurant.Batches.Any(b => b.Id == o.BatchId) && // If we have a batch 
                            sess.Restaurant.Batches.Single(b => b.Id == o.BatchId).PrinterId == printer //And the batch is for us then ok
                            )).
                            //EP-222
                            //&&
                            //(!o.Completed.HasValue || o.Completed.Value >= minTime)).
                            Select(o => new Models.Order(o)).OrderBy(p => p.Course.Ordering).GroupBy(o => o.Course.Name).Select(grp => new TicketCourse(grp.Key.ToUpper(), grp.ToList())).ToList();
                this.SortTime = sess.StartTime;
            }
            else
            {
                /*
                 * If its a takeaway sesion we need ot get the order time etc
                 */
                Core.DatabaseModel.TakeAwaySession sessionInfo = (Core.DatabaseModel.TakeAwaySession)sess;

                string name;

                if (sessionInfo.Name != null)
                {
                    name = sessionInfo.Name;
                }
                else
                {
                    name = sessionInfo.Diner.Customer.Name.Firstname + " " + sessionInfo.Diner.Customer.Name.Surname;
                }
    
                Summary = (sessionInfo.Delivery ? "Delivery" : "Collection") + " - " + name;
                AdditionalInfo = "Expected " + formattedTime(timezone, sessionInfo.ExpectedTime);
                Courses = new List<TicketCourse>();
                //EP-222
                //var orders = sess.Orders.Where(o => !o.Completed.HasValue || o.Completed.Value >= minTime).Select(o => new Models.Order(o)).OrderBy(p => p.Course.Ordering);
                var orders = sess.Orders.Select(o => new Models.Order(o)).OrderBy(p => p.Course.Ordering);
                if (orders.Count() > 0)
                {
                    Courses.Add(new TicketCourse("",
                        sess.Orders
                        .Where(o =>
                            (
                            printer == 0 ||     //If dont we have a printer then this block is skipped
                            sess.Restaurant.Batches.Any(b => b.Id == o.BatchId) && // If we have a batch 
                            sess.Restaurant.Batches.Single(b => b.Id == o.BatchId).PrinterId == printer //And the batch is for us then ok
                            )) 
                            //EP-222
                            //&&
                            //(!o.Completed.HasValue || o.Completed.Value >= minTime))
                        .Select(o => new Models.Order(o)).OrderBy(p => p.Course.Ordering)));
                }

                this.SortTime = sessionInfo.ExpectedTime;

            }

            

            OrderIds = sess.Orders.Select(o => o.Id).ToList();
            //EP-219
            //Logic that marks all the items as done
            AllComplete = Courses.All(o => o.AllComplete);// || sess.ClosedTime.HasValue;

            if (sess.GetType() == typeof(Core.DatabaseModel.SeatedSession))
            {
                IsAdHoc = (((Core.DatabaseModel.SeatedSession)sess).IsAdHoc);
            }

            

            if (sess.ClosedTime.HasValue)
            {
                AdditionalInfo2 += "Closed " + formattedTime(timezone, sess.ClosedTime.Value);
            }
            else
            {
                AdditionalInfo2 += "Opened " + formattedTime(timezone, sess.StartTime);
            }


            //Orders = sess.Orders.Select(o=>new Models.Order(o)).ToList();



        }

        private static string formattedTime(String timezone, DateTime dt)
        {
            var tz = DateTimeZoneProviders.Tzdb[timezone];

            ZonedDateTime utc = new LocalDateTime(dt.Year, dt.Month, dt.Day, dt.Hour, dt.Minute, dt.Second).InUtc();
            ZonedDateTime newTime = utc.ToInstant().InZone(tz);

            return newTime.Hour.ToString("D2") + ":" + newTime.Minute.ToString("D2") + ", " + newTime.Day + "/" + newTime.Month + "/" + newTime.Year;
        }
    }

    
}
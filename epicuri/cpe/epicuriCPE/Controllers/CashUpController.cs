using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Web.Http;
using epicuri.Core.DatabaseModel;
using Newtonsoft.Json;
using System.Data.Objects.DataClasses;

namespace epicuri.CPE.Controllers
{
    public class CashUpController : Models.EpicuriApiController
    {
        [HttpGet]
        [ActionName("Index")]
        public HttpResponseMessage GetCashUp()
        {
            try
            {
                Authenticate();
            }
            catch { return Request.CreateResponse(HttpStatusCode.Unauthorized); }

            return Request.CreateResponse(HttpStatusCode.OK, this.Restaurant.CashUpDays.Select(cud => new Models.CashUpDay(cud)));

        }

        [HttpGet]
        [ActionName("View")]
        public HttpResponseMessage GetCashUp(int id)
        {
            try
            {
                Authenticate();
            }
            catch { return Request.CreateResponse(HttpStatusCode.Unauthorized); }

            var cashUpDay = this.Restaurant.CashUpDays.FirstOrDefault(cud => cud.Id == id);

            if (cashUpDay == null)
                return Request.CreateResponse(HttpStatusCode.NotFound);

            return Request.CreateResponse(HttpStatusCode.OK, new Models.CashUpDay(cashUpDay));

        }

        [HttpGet]
        [ActionName("IsOkToCashup")]
        public HttpResponseMessage IsOkToCashUp()
        {

            try
            {
                Authenticate();
            }
            catch { return Request.CreateResponse(HttpStatusCode.Unauthorized); }


            // All sessions must be closed for cash up. Return error if there are open sessions
            // EP-663
            DateTime takeawayWindow = DateTime.UtcNow.AddMinutes(epicuri.Core.Settings.Setting<int>(Restaurant.Id, "TakeawayMinimumTime"));
            var sessions = from sess in this.Restaurant.Sessions.OfType<epicuri.Core.DatabaseModel.SeatedSession>()
                           where sess.ClosedTime == null
                           select new Models.SeatedSession(sess);
            var takeaways = from sess in this.Restaurant.Sessions.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>()
                            where sess.ClosedTime == null
                            && sess.Rejected == false
                            && sess.Deleted == false
                            && sess.Accepted == true
                            && sess.ExpectedTime < takeawayWindow
                            select new Models.TakeawaySession(sess);


            if (sessions.Any() || takeaways.Any())
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest,new Exception("All open sessions must be closed before cash-up"));
            }

            return Request.CreateResponse(HttpStatusCode.OK, new {Message="OK To Cash up" });
        }


        [HttpPost]
        [ActionName("Index")]
        public HttpResponseMessage PostCashUp(Models.CashUpDay cashUpDay)
        {
            try
            {
                Authenticate();
            }
            catch { return Request.CreateResponse(HttpStatusCode.Unauthorized); }

            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state"));
            }


            // All sessions must be closed for cash up. Return error if there are open sessions
            // EP-663
            DateTime takeawayWindow = DateTime.UtcNow.AddMinutes(epicuri.Core.Settings.Setting<int>(Restaurant.Id, "TakeawayMinimumTime"));
            var sessions = from sess in this.Restaurant.Sessions.OfType<epicuri.Core.DatabaseModel.SeatedSession>()
                           where sess.ClosedTime == null
                           select new Models.SeatedSession(sess);
            var takeaways = from sess in this.Restaurant.Sessions.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>()
                            where sess.ClosedTime == null
                            && sess.Rejected == false
                            && sess.Deleted == false
                            && sess.Accepted == true
                            && sess.ExpectedTime < takeawayWindow
                            select new Models.TakeawaySession(sess);


            if (sessions.Any() || takeaways.Any())
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest,new Exception("All open sessions must be closed before cash-up"));
            }



            var newCashUp = GetCashUp(db, this.Restaurant, this.Staff.Id, cashUpDay);
            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK, new Models.CashUpDay(newCashUp));

        }




        [HttpPost]
        [ActionName("Simulate")]
        public HttpResponseMessage SimulateCashup(Models.CashUpDay cashUpDay)
        {
            try
            {
                Authenticate();
            }
            catch { return Request.CreateResponse(HttpStatusCode.Unauthorized); }

            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state"));
            }

            using (var db = new epicuri.Core.DatabaseModel.epicuriContainer())
            {
                var newCashUp = GetCashUp(db, db.Restaurants.Single(r=>r.Id==this.Restaurant.Id), this.Staff.Id, cashUpDay);
                return Request.CreateResponse(HttpStatusCode.OK, new Models.CashUpDay(newCashUp));
            }
           

        }


        [HttpPost]
        [ActionName("Sessions")]
        public HttpResponseMessage Sessions(Models.CashUpDay cashUpDay)
        {
            try
            {
                Authenticate();
            }
            catch { return Request.CreateResponse(HttpStatusCode.Unauthorized); }

            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state"));
            }

            return Request.CreateResponse(HttpStatusCode.OK, this.Restaurant.Sessions.Where(s => s.Paid == true && s.ClosedTime != null).Select(s => new Models.Session(s)).Select(s => new {Payments=s.RealPayments, Total=s.Total, Change=s.Change, OverPayments=s.OverPayments, RemaiingTotal =s.RemainingTotal }));
            


        }


        [HttpPost]
        [ActionName("Time")]
        public HttpResponseMessage Time(Models.CashUpDay cashUpDay)
        {
            try
            {
                Authenticate();
            }
            catch { return Request.CreateResponse(HttpStatusCode.Unauthorized); }

            

            return Request.CreateResponse(HttpStatusCode.OK,cashUpDay);


        }


        private static  CashUpDay GetCashUp(Core.DatabaseModel.epicuriContainer db, Core.DatabaseModel.Restaurant restaurant, int staffId,  Models.CashUpDay cashUpDay)
        {
            // If there are sessions with start times earlier than the proposed cash up time, create 2 cash ups, one to wrap up the previous events and then one for the specfied period

            DateTime startTime = (DateTime)System.Data.SqlTypes.SqlDateTime.MinValue;

            // Check if a wrap up is needed
            if (restaurant.CashUpDays.FirstOrDefault() == null)
            {
                if (cashUpDay.StartTime.HasValue)
                {
                    // Set the date for teh new (not wrap up cash up)
                    startTime = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(cashUpDay.StartTime.Value);

                    // Check if there are any non-cashed up sessions before this 
                    var wrapUpSeatedSessionsQuery = restaurant.Sessions.OfType<epicuri.Core.DatabaseModel.SeatedSession>().Where(s => s.CashUpDay == null && !s.RemoveFromReports && s.StartTime >= (DateTime)System.Data.SqlTypes.SqlDateTime.MinValue && s.StartTime <= startTime.AddMilliseconds(-1)).AsEnumerable<Session>();
                    var wrapUpTakeawaySessionsQuery = restaurant.Sessions.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>().Where(s => s.CashUpDay == null && !s.RemoveFromReports && s.StartTime >= (DateTime)System.Data.SqlTypes.SqlDateTime.MinValue && s.StartTime <= startTime.AddMilliseconds(-1) && s.Accepted == true && s.Rejected == false).AsEnumerable<Session>();

                    if (wrapUpSeatedSessionsQuery.Count() + wrapUpTakeawaySessionsQuery.Count() > 0)
                    {
                        // Restaurant has unassigned sessions which were started before this cashing up period

                        // Create wrap up entry
                        CashUpDay wrapUp = new CashUpDay();
                        wrapUp.WrapUp = true;
                        wrapUp.StartTime = (DateTime)System.Data.SqlTypes.SqlDateTime.MinValue;
                        wrapUp.EndTime = startTime.AddMilliseconds(-1);
                        wrapUp.Restaurant = restaurant;
                        wrapUp.StaffId = staffId;

                        var wrapUpSessionQuery = wrapUpSeatedSessionsQuery.Concat(wrapUpTakeawaySessionsQuery).AsQueryable<Session>();

                        // Perform cashing up process for wrap up
                        wrapUp.Report = JsonConvert.SerializeObject(Models.CashUpDay.CreateCashUpReport(wrapUp, wrapUpSessionQuery, db));

                        // Update all sessions with cashup reference
                        foreach (Session session in wrapUpSessionQuery)
                        {
                            session.CashUpDay = wrapUp;
                        }

                        db.CashUpDay.AddObject(wrapUp);

                        // Now perform normal cashing up based upon request
                      
                    }
                    else
                    {
                        // No uncashed up sessions
                    }

                }
                else
                {
                    // The current cashup is the one to be created - no wrap up needed
                    startTime = (DateTime)System.Data.SqlTypes.SqlDateTime.MinValue;
                }
            }
            else
            {
                if (!cashUpDay.StartTime.HasValue)
                {
                    // Set the start time to the end of the last cash up
                    startTime = restaurant.CashUpDays.OrderByDescending(cud => cud.EndTime).First().EndTime.AddMilliseconds(1);
                }
            }

            // Start normal cashing up process
            CashUpDay newCashUp = new CashUpDay();
            newCashUp.StartTime = startTime;
            newCashUp.EndTime = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(cashUpDay.EndTime);
            //EP-620 to prevent cashups from appearing in the future caused by lag between waiter app time and server time
            if(newCashUp.EndTime>DateTime.UtcNow)
            {
                newCashUp.EndTime = DateTime.UtcNow;
            }
            newCashUp.StaffId = staffId;
            newCashUp.Restaurant =  restaurant;

            // Produce a query of the sessions
            var allSessionHistory = GetSessionQuery(restaurant.Sessions, newCashUp);

           

            var sessionQuery = allSessionHistory.Where(s => !s.RemoveFromReports);

            // Perform cash up and serialise the result
            newCashUp.Report = JsonConvert.SerializeObject(Models.CashUpDay.CreateCashUpReport(newCashUp, sessionQuery, db));

            newCashUp.PaymentReport = JsonConvert.SerializeObject(Models.CashUpDay.CreatePaymentReport(sessionQuery));
            newCashUp.AdjustmentReport = JsonConvert.SerializeObject(Models.CashUpDay.CreateAdjustmentReport(sessionQuery));
            newCashUp.ItemAdjustmentReport = JsonConvert.SerializeObject(Models.CashUpDay.CreateItemAdjustmentReport(sessionQuery));
            newCashUp.ItemAdjustmentLossReport = JsonConvert.SerializeObject(Models.CashUpDay.CreateItemAdjustmentLossReport(sessionQuery));
                
            
            // Update all sessions with cashup reference
            foreach (Session session in allSessionHistory)
            {
                session.CashUpDay = newCashUp;
            }

            // Update the database
            db.CashUpDay.AddObject(newCashUp);
            return newCashUp;
        }

        public static IQueryable<Session> GetSessionQuery(EntityCollection<Session> sessions, CashUpDay newCashUp)
        {
            var seatedSessionQuery = sessions.OfType<epicuri.Core.DatabaseModel.SeatedSession>().Where(s => s.CashUpDay == null && s.StartTime >= newCashUp.StartTime && s.StartTime <= newCashUp.EndTime).AsEnumerable<Session>();
            var takeawaySessionQuery = sessions.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>().Where(s => s.CashUpDay == null && s.ClosedTime >= newCashUp.StartTime && s.ClosedTime <= newCashUp.EndTime && s.Accepted == true && s.Rejected == false).AsEnumerable<Session>();
            var sessionQuery = seatedSessionQuery.Concat(takeawaySessionQuery).AsQueryable<Session>();


            return sessionQuery;
        }


       

            
    

    }
}

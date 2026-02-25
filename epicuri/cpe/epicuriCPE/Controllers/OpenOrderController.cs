using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.Core.DatabaseModel;
using System.Net.Http;
using System.Net;
using System.Diagnostics;

namespace epicuri.CPE.Controllers
{
    public class OpenOrderController : Models.EpicuriApiController
    {
        public HttpResponseMessage GetOpenOrder(int printer)
        {
            try
            {
                Authenticate();
            }
            catch (Exception)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden);
            }

            if (printer>0 && !Restaurant.Printers.Any(p => p.Id == printer))
            {
                return Request.CreateErrorResponse(HttpStatusCode.NotFound, "Printer not found");
            }

            /*
             * Select all sessions which are not closed, have orders, which are not completed and are not accepted
             * Also get AdHoc sessions that were closed less than x minutes ago
             */
            var takeawayMinTime =  epicuri.Core.Settings.Setting<int>(Restaurant.Id, "TakeawayMinimumTime");
            // EP-663
            DateTime maxFutureTime = DateTime.UtcNow.AddMinutes(takeawayMinTime);
            DateTime minClosedTime = DateTime.UtcNow.AddMinutes(-5);

            // Sessions are returned based on the following parameters:
            // Session/Tab
                // Ordered
                // Session open or recently closed
            // Takeaway
                // Ordered
                // Session is accepted
                // Due time is 'soon'
                // Not paid + closed
            // Quick Order (AdHoc)
                // Ordered
                // Session paid and closed recently

            var Sessions = this.Restaurant.Sessions.

                // Quick Order (AdHoc)
                Where(s => s.GetType() == typeof(Core.DatabaseModel.SeatedSession) &&  
                    ((Core.DatabaseModel.SeatedSession)s).IsAdHoc ?
                    ((Core.DatabaseModel.SeatedSession)s).Paid : true).

                // Takeaway
                Where(s =>
                    s.GetType() == typeof(Core.DatabaseModel.TakeAwaySession) ?
                    !((Core.DatabaseModel.TakeAwaySession)s).Deleted &&
                     ((Core.DatabaseModel.TakeAwaySession)s).Accepted &&
                     ((Core.DatabaseModel.TakeAwaySession)s).ExpectedTime <= maxFutureTime : true).

                // Others
                Where(s => s.ClosedTime == null || s.ClosedTime.Value >= minClosedTime).
               
                Select(t => new epicuri.CPE.Models.Ticket(printer, t)).
                
                    Where(t => t.Courses.Any() 
                        && t.Courses.First().Orders.Any()).

                    OrderBy(a => a.AllComplete).
                
                    ThenBy(a => a.Completed);


            // Check All AdHoc Sessions and mark as 'Completed' if currentime > ordertime + QuickOrderSessionTimeout
            DefaultSetting defaultSetting = db.DefaultSettings.FirstOrDefault(s => s.Key == "QuickOrderSessionTimeout");
            // Timeout in Minutes

            int adHocTimeout;
            // Check the string value in the db can actually be parsed
            if(Int32.TryParse(defaultSetting.Value, out adHocTimeout)){

                var adHocSessions = this.Restaurant.Sessions.Where(s => s.GetType() == typeof(SeatedSession) && ((SeatedSession)s).IsAdHoc).Where(s => s.ClosedTime == null || s.ClosedTime.Value >= minClosedTime);

                foreach (Session seatedSess in adHocSessions)
                {
                    DateTime timeout = seatedSess.StartTime;
                    timeout = timeout.AddMinutes(adHocTimeout);

                    if(DateTime.UtcNow > timeout){

                        foreach (Order order in seatedSess.Orders)
                        {
                            if (order.Completed == null)
                            {
                                order.Completed = DateTime.UtcNow;
                            }

                           
                        }

                        db.SaveChanges();

                    }

                }
            }
            else
            {
                Console.WriteLine("QuickOrderSessionTimeout is set incorrectly, cannot use this value for AdHocTimeout");
            }    

            

            return Request.CreateResponse(HttpStatusCode.OK, Sessions);
        }

        [ActionName("Close")]
        public HttpResponseMessage PostOpenOrderClose(ListOfIds ids, int printer)
        {
            Debug.WriteLine("PRINTERID: " + printer.ToString());

            // AdHoc QuickOrder orders are handled in the following fashion
            // After an amount of time set by the QuickOrderSessionTimeout, an AdHoc session is auto-marked as completed.
            // If the user then re-opens the AdHoc session, it is pinned until it is manually marked as completed by the kitchen staff

            try
            {
                Authenticate();
            }
            catch (Exception)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden);
            }


            foreach(int id in ids.ids)
            {
                var order = db.Orders.Where(o => o.Session.RestaurantId == this.Restaurant.Id && o.Id == id).FirstOrDefault();

                var batch = db.Batches.Where(b => b.Id == order.BatchId && b.PrinterId == printer).SingleOrDefault();

                if (batch != null)
                {

                    if (order == null)
                    {
                        return Request.CreateResponse(HttpStatusCode.NotFound, "Not found");
                    }

                    if (ids.method != null && ids.method.Equals("reopen"))
                    {

                        order.Completed = null;

                        // If AdHoc, reset time of order in order for auto-closing to work

                        Session orderSession = order.Session;
                        if (orderSession.GetType() == typeof(Core.DatabaseModel.SeatedSession) && ((Core.DatabaseModel.SeatedSession)orderSession).IsAdHoc)
                        {
                            order.Session.StartTime = DateTime.UtcNow;
                        }                        

                    }
                    else
                    {
                        order.Completed = DateTime.UtcNow;
                    }


                    db.SaveChanges();
                }
            }
            return GetOpenOrder(printer);
        }

        
 
         
    }
}

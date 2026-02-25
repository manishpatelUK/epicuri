using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using epicuri.Core.DatabaseModel;
using epicuri.CPE.Models;

namespace epicuri.CPE.Controllers
{
    public class PrintController : Models.EpicuriApiController
    {
        [HttpPut]
        public HttpResponseMessage PutPrint(Models.BatchPayload data)
        {
            try
            {
                Authenticate();
            }
            catch (Exception e)
            {
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, e);
                response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                return response;
            }

            List<int> idArray = (List<int>)data.batchId;

            foreach (int id in idArray)
            {
                var printBatch = Restaurant.Batches.SingleOrDefault(b => b.Id == id);

                if (printBatch == null)
                {
                    return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Batch not found"));
                }

                Core.DatabaseModel.Batch requiredBatch = (Core.DatabaseModel.Batch)printBatch;

                requiredBatch.PrintedTime = DateTime.UtcNow;
                db.SaveChanges();
            }

            return Request.CreateResponse(HttpStatusCode.OK);
        }

        [HttpGet]
        public HttpResponseMessage GetPrints()
        {
            try
            {
                Authenticate();
            }
            catch (Exception e)
            {
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, e);
                response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                return response;
            }

            DateTime spoolStamp = DateTime.UtcNow;

            // Batches are respooled after 2 minutes.
            int minutesTillRespool = 2;

            // Get Batches that have not yet been printed.
            var batches = this.Restaurant.Batches.Where(b => !b.PrintedTime.HasValue);
                
                // Check the batch as atleast one order
            batches = batches.Where(b => (b.Orders.FirstOrDefault() != null) &&

                         // If it is seated
                         b.Orders.First().Session.GetType() == typeof(Core.DatabaseModel.SeatedSession) ?
                         // If AdHoc
                            (((Core.DatabaseModel.SeatedSession)b.Orders.First().Session).IsAdHoc ?
                              // Only return if Paid
                              b.Orders.First().Session.Paid :
                              // If not AdHoc, only return if the session hasn't been closed
                              b.Orders.First().Session.ClosedTime == null)

                         : (b.Orders.FirstOrDefault() != null));

            // Only return those which haven't recently been spooled
            batches = batches.Where(b => (!b.SpoolTime.HasValue || (b.SpoolTime.HasValue && DateTime.UtcNow > b.SpoolTime.Value.AddMinutes(minutesTillRespool))));

            // Only physical printers (they have an IP)
            batches = batches.Where(b => !string.IsNullOrEmpty(b.Printer.IP));


            //var maxFutureTime = DateTime.UtcNow.AddMinutes(30);

            ////// Takeaway
            //batches = batches.Where(b =>
            //    b.GetType() == typeof(Core.DatabaseModel.TakeAwaySession) ?
            //    !((Core.DatabaseModel.TakeAwaySession)b.Orders.First().Session).Deleted &&
            //        ((Core.DatabaseModel.TakeAwaySession)b.Orders.First().Session).Accepted &&
            //        ((Core.DatabaseModel.TakeAwaySession)b.Orders.First().Session).ExpectedTime <= maxFutureTime : true); 

            //// Others
            //Where(s => s.ClosedTime == null || s.ClosedTime.Value >= minClosedTime).

            //Select(t => new epicuri.CPE.Models.Ticket(printer, t)).

            //    Where(t => t.Courses.Any()
            //        && t.Courses.First().Orders.Any()).

            //    OrderBy(a => a.AllComplete).

            //    ThenBy(a => a.Completed);



            //var batches = from batch in this.Restaurant.Batches
            //              where !batch.PrintedTime.HasValue   // Hasn't yet been printed
            //              && (batch.Orders.FirstOrDefault() != null && batch.Orders.First().Session.ClosedTime == null) // Has orders, and orders are for an open session
            //              && (!batch.SpoolTime.HasValue || (batch.SpoolTime.HasValue && DateTime.UtcNow > batch.SpoolTime.Value.AddMinutes(2)))  // It hasn't been spooled in the past 2 minutes
            //              && !string.IsNullOrEmpty(batch.Printer.IP)  // It's a physical printer
            //              && (batch.Orders.First().Session.GetType() == typeof(Core.DatabaseModel.SeatedSession) && ((Core.DatabaseModel.SeatedSession)batch.Orders.First().Session).IsAdHoc  ?
            //        ((Core.DatabaseModel.SeatedSession)batch.Orders.First().Session).Paid : true)
            //              select batch;

            //Where(s => s.GetType() == typeof(Core.DatabaseModel.SeatedSession) &&  
            //        ((Core.DatabaseModel.SeatedSession)s).IsAdHoc ?
            //        ((Core.DatabaseModel.SeatedSession)s).Paid : true).

            List<epicuri.Core.DatabaseModel.Batch> AcceptedBatches = new List<epicuri.Core.DatabaseModel.Batch>();
            foreach (epicuri.Core.DatabaseModel.Batch b in batches)
            {
                TakeAwaySession t = null;
                try
                {
                    t = db.Sessions.Where(s => s.Id == b.Orders.First().SessionId).OfType<TakeAwaySession>().FirstOrDefault();
                }
                catch { }

                if (t == null)
                {
                    b.SpoolTime = spoolStamp;
                    AcceptedBatches.Add(b);
                }
                else
                {

                    if (t.Accepted)  // takeaway isn't waiting for the waiter for cofnirmations
                    {
                        b.SpoolTime = spoolStamp;
                        AcceptedBatches.Add(b);
                    }

                }

            }

            db.SaveChanges();

            var ModelBatches = from batch in AcceptedBatches select new Models.Batch(batch);

            List<Models.Batch> returnBatches = new List<Models.Batch>();

            foreach (Models.Batch mb in ModelBatches)
            {
                int sessionId = mb.Orders.FirstOrDefault().SessionId;

                var session = db.Sessions.Where(s => s.Id == sessionId).FirstOrDefault();

                if (session.GetType() == typeof(Core.DatabaseModel.SeatedSession))
                {

                    Core.DatabaseModel.SeatedSession seatedSess = (Core.DatabaseModel.SeatedSession)session;
                    

                    // Seated Session
                    mb.Covers = db.Diners.Where(d => d.SeatedSessionId == sessionId && !d.IsTable).Count();
                    if (mb.Orders.FirstOrDefault().InstantiatedFromId == 0)
                    {
                        mb.IsSelfService = false;
                    }
                    else
                    {
                        mb.IsSelfService = true;
                    }

                    if (!((Core.DatabaseModel.SeatedSession)session).Tables.Any())
                    {
                        mb.OrderName = ((Core.DatabaseModel.SeatedSession)session).Party == null ? "" : ((Core.DatabaseModel.SeatedSession)session).Party.Name;
                        mb.Identifier = "Tab";
                    }

                    if(((Core.DatabaseModel.SeatedSession)session).IsAdHoc)
                    {
                        // Check if the session is AdHoc and mark it as such if so
                        mb.Identifier = "AdHoc";
                        mb.OrderName = "Quick Order";
                        
                        // If the session is AdHoc, check it has been closed, and paid for

                        if (session.ClosedTime != null && session.Paid)
                        {
                            returnBatches.Add(mb);
                        }

                    } else  if (session.ClosedTime == null)
                    {
                        returnBatches.Add(mb);
                    }
                }
                else
                {
                    // Takeaway Session
                    Core.DatabaseModel.TakeAwaySession takeawaySess = (Core.DatabaseModel.TakeAwaySession)session;

                    if (takeawaySess.Delivery)
                    {
                        mb.BatchType = "Delivery";
                    }
                    else
                    {
                        mb.BatchType = "Collection";
                    }

                    mb.DueDate = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(takeawaySess.ExpectedTime);

                    //mb.OrderName = takeawaySess.Name;
                    if (takeawaySess.Diner.Customer != null && takeawaySess.Diner.Customer.Name != null)
                        mb.OrderName = takeawaySess.Diner.Customer.Name.Firstname + " " + takeawaySess.Diner.Customer.Name.Surname;
                    else
                        mb.OrderName = takeawaySess.Name;


                    mb.Notes = takeawaySess.Message;

                    var takeawayMinTime =  epicuri.Core.Settings.Setting<int>(Restaurant.Id, "TakeawayMinimumTime");
                    DateTime maxFutureTime = DateTime.UtcNow.AddMinutes(takeawayMinTime);

                    if (takeawaySess.Deleted == false && 
                        takeawaySess.ClosedTime == null &&
                        takeawaySess.Accepted == true &&
                        takeawaySess.Rejected == false && 
                        takeawaySess.ExpectedTime<=maxFutureTime)
                    {
                        returnBatches.Add(mb);
                    }
                }
            }

            return Request.CreateResponse(HttpStatusCode.OK, returnBatches);

        }
    }

}

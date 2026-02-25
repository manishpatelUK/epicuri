using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using epicuri.Core.DatabaseModel;
using epicuri.Core;

namespace epicuri.CPE.Controllers
{
    public class TakeAwayController : Models.EpicuriApiController
    {
        [HttpGet]
        public HttpResponseMessage Get(double fromTime = 0, double toTime = 0, bool pendingWaiterAction = false)
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

            DateTime ft;

            bool timeSpecified = false;

            if (fromTime == 0)
            {
                ft = DateTime.Today;
            }
            else
            {
                ft = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(fromTime);
                timeSpecified = true;
            }

            //DateTime ft = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(fromTime);

            if (toTime == 0)
            {
                toTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(ft.AddMonths(3));
            }

            DateTime tt = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(toTime);

            

            if (pendingWaiterAction)
            {
               var takeaways = from sess in this.Restaurant.Sessions.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>()
                            where sess.ExpectedTime >= ft
                            && sess.ExpectedTime <= tt
                            && sess.Accepted == false && sess.Rejected == false
                            select new Models.TakeawaySession(sess);

               return Request.CreateResponse(HttpStatusCode.OK, takeaways.ToList<Models.Session>());
            }
            else
            {
                if (timeSpecified)
                {
                    var takeaways = from sess in this.Restaurant.Sessions.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>()
                                    where sess.ExpectedTime >= ft
                                    && sess.ExpectedTime <= tt
                                    select new Models.TakeawaySession(sess);

                    return Request.CreateResponse(HttpStatusCode.OK, takeaways.ToList<Models.Session>());
                }
                else
                {
                    var takeaways = from sess in this.Restaurant.Sessions.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>()
                                    where sess.ExpectedTime >= ft
                                    && sess.ExpectedTime <= tt && sess.Accepted == true
                                    select new Models.TakeawaySession(sess);

                    return Request.CreateResponse(HttpStatusCode.OK, takeaways.ToList<Models.Session>());
                }
            }
            //return Request.CreateResponse(HttpStatusCode.OK, takeaways.ToList<Models.Session>());

        }
        
        [HttpPost]
        [ActionName("Takeaway")]
        public HttpResponseMessage PostTakeaway(Models.TakeawayPayload newSessionInfo)
        {
            try
            {
                Authenticate();
            }
            catch (Exception e)
            {
                return Request.CreateResponse(HttpStatusCode.Unauthorized, e);
            }

            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Model State Invalid."));
            }

            String rejectionMessage = "";
            if (!newSessionInfo.CheckMaxTakeawaysPerHour())
            {
                rejectionMessage += "Restaurant is fully booked.";
            }

            double surcharge = 0;
            
            if (newSessionInfo.Delivery)
            {
                if (!newSessionInfo.CheckDeliveryRadius(Restaurant.Id))
                {
                    if (!String.IsNullOrEmpty(rejectionMessage))
                        rejectionMessage = rejectionMessage + "; ";

                    rejectionMessage += "Delivery too far away.";
                }

                if (!newSessionInfo.CheckDeliverySurcharge(Restaurant.Id))
                {
                    surcharge += Core.Settings.Setting<double>(Restaurant.Id, "DeliverySurcharge");
                }

            }

            /*
             * Create takeaway session
             */
            epicuri.Core.DatabaseModel.TakeAwaySession takeaway = null;

            System.DateTime requestedDateTime = new DateTime(1970, 1, 1, 0, 0, 0, 0);
            requestedDateTime = requestedDateTime.AddSeconds(newSessionInfo.RequestedTime);

            if (requestedDateTime < DateTime.UtcNow)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Requested time in the past"));
            }

            takeaway = new epicuri.Core.DatabaseModel.TakeAwaySession
            {
                Started = false,
                StartTime = DateTime.UtcNow,
                DeliveryAddress = new epicuri.Core.DatabaseModel.Address
                {
                    City = "",
                    PostCode = "",
                    Town = "",
                    Street = "",
                },
                RestaurantId = Restaurant.Id,
                Delivery = newSessionInfo.Delivery,
                ExpectedTime = requestedDateTime,
                //ExpectedTime = newSessionInfo.GetExpectedTime(), - WAITER can override and set any practicable time
                //Accepted = true,
                Message = newSessionInfo.Message,
                RejectionNotice = rejectionMessage,
                Telephone = newSessionInfo.Telephone,
                Name = newSessionInfo.Name,
                Paid = newSessionInfo.Paid,
                RequestedBill = newSessionInfo.RequestedBill,
                Deleted = false
            };

            if (newSessionInfo.Delivery)
            {
                if (newSessionInfo.Address == null)
                {
                    rejectionMessage = rejectionMessage + "Address needed";
                }
                takeaway.DeliveryAddress = new epicuri.Core.DatabaseModel.Address
                {
                    City = newSessionInfo.Address.City,
                    PostCode = newSessionInfo.Address.PostCode,
                    Town = newSessionInfo.Address.Town,
                    Street = newSessionInfo.Address.Street,
                };
            }

            var dd = new Diner
            {
                IsTable = false,

            };
            takeaway.Diner = dd;

            if (newSessionInfo.LeadCustomerId != 0)
            {
                var customer = db.Customers.FirstOrDefault(cust => cust.Id == newSessionInfo.LeadCustomerId);
                if (customer != null)
                    takeaway.Diner.Customer = customer;
            }

            

            Restaurant.Sessions.Add(takeaway);

            //(db.Sessions.OfType<TakeAwaySession>().Single(s => s.Id == takeaway.Id)
            db.SaveChanges(System.Data.Objects.SaveOptions.AcceptAllChangesAfterSave);


            return Request.CreateResponse(HttpStatusCode.Created, new Models.TakeawaySession((TakeAwaySession)takeaway));

        }

        // I think this method is now redundant, as Pete doesnt believe its used - AM 20140819
        [HttpPut]
        [ActionName("TakeawayCheck")]
        public HttpResponseMessage PutTakeawayCheck(Models.TakeawayPayload newSessionInfo)
        {
            try
            {
                Authenticate();
            }
            catch (Exception e)
            {
                return Request.CreateResponse(HttpStatusCode.Unauthorized, e);
            }

            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Model State Invalid."));
            }

            Dictionary<string, object> ReturnDict = new Dictionary<string, object>();
            List<string> WarningMessages = new List<string>();

            DateTime timeCheck = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(newSessionInfo.RequestedTime);

            var duplicateCheck = from session in db.Sessions.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>()
                                 where session.Diner.CustomerId == newSessionInfo.LeadCustomerId &&
                                 session.ExpectedTime == timeCheck &&
                                 session.Rejected == false &&
                                 session.Id != newSessionInfo.Id &&
                                 session.Deleted == false
                                 select session;

            if (duplicateCheck.Count() > 0)
            {
                //EP-39
                string returnDT = duplicateCheck.First().ExpectedTime.ToString("dd/MM/yyyy HH:mm");
                //EP-39 End

                WarningMessages.Add("A takeaway for this guest already exists at " + returnDT + ".");
            }

            newSessionInfo.RestaurantId = Restaurant.Id;

            if (!newSessionInfo.CheckMaxTakeawaysPerHour())
            {
                WarningMessages.Add("Restaurant kitchen is reaching its capacity for takeaways during this time.");
            }

            bool badPostcode = false;


            if (newSessionInfo.Delivery)
            {
                bool orderAccepted = newSessionInfo.Accepted;

                if (!newSessionInfo.CheckDeliveryRadius(Restaurant.Id))
                {
                    string maxRadius = Core.Settings.Setting<string>(newSessionInfo.RestaurantId, "MaxDeliveryRadius");
                    WarningMessages.Add("Address outside delivery radius (Max " + maxRadius + ")"); 
                }

                if (!newSessionInfo.Accepted)
                {
                    badPostcode = true;
                    newSessionInfo.Accepted = false;
                }

                if (badPostcode && !newSessionInfo.Accepted)
                {
                    WarningMessages.Add("Unrecognized Postcode");
                }
                newSessionInfo.Accepted = true;
            }

            var takeawayMinimumTime = epicuri.Core.Settings.Setting<int>(Restaurant.Id, "TakeawayMinimumTime");
            var rmtUnix = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(DateTime.UtcNow.AddMinutes(takeawayMinimumTime));

            if (newSessionInfo.RequestedTime < rmtUnix)
            {
                WarningMessages.Add("Due within " + takeawayMinimumTime + " mins");
            }

            double surcharge = 0;

            foreach (var constraint in Restaurant.DateConstraints.Where(c => c.TargetSession))
            {
                System.DateTime reservationDateTime = new System.DateTime(1970, 1, 1, 0, 0, 0, 0);

                // Add the number of seconds in UNIX timestamp to be converted.
                reservationDateTime = reservationDateTime.AddSeconds(newSessionInfo.RequestedTime);

                TimeZoneInfo timezone = Restaurant.GetTimeZoneInfo();

                if (constraint.Matches(timezone, reservationDateTime))
                {
                    WarningMessages.Add("Blackout exists for this date/time.");
                }

                /* Removed as commented out in API method - and seems to be a duplicate - AM 20140819
                if (constraint.Matches())
                {
                    WarningMessages.Add("Blackout exists for this date/time.");
                }
                */
            }

            Customer customerCheck = db.Customers.FirstOrDefault(cust => cust.Id == newSessionInfo.LeadCustomerId);
            if (customerCheck != null)
            {
                if (!epicuri.CPE.Models.Customer.OKToOrder(customerCheck))
                {
                    WarningMessages.Add("Customer has blackmarks. Please give careful attention");
                }
            }

            if (newSessionInfo.Delivery)
            {
                if (newSessionInfo.Address == null)
                {

                    WarningMessages.Add("Address needed");
                }

                if (!newSessionInfo.CheckDeliverySurcharge(Restaurant.Id))
                {
                    surcharge += Core.Settings.Setting<double>(newSessionInfo.RestaurantId, "DeliverySurcharge");
                }

            }

            /* NOT REQUIRED IN THE WAITER APP
            if (!newSessionInfo.CheckMinPrice())
            {
                WarningMessages.Add("Low takeaway order value (Min " + Core.Settings.Setting<string>(newSessionInfo.RestaurantId, "MinTakeawayValue") + ")");
            }

            if (!newSessionInfo.CheckMaxPrice())
            {
                WarningMessages.Add("High takeaway order value (Max " + Core.Settings.Setting<string>(newSessionInfo.RestaurantId, "MaxTakeawayValue") + ")");
            }
            */

            ReturnDict.Add("Cost", surcharge);
            ReturnDict.Add("Warning", WarningMessages);
            return Request.CreateResponse(HttpStatusCode.OK, ReturnDict);

        }


        [HttpPut]
        [ActionName("Takeaway")]
        public HttpResponseMessage PutTakeaway(int id, Models.TakeawayPayload existingSession)
        {
            existingSession.Id = id;

            try
            {
                Authenticate();
            }
            catch (Exception e)
            {
                Request.CreateResponse(HttpStatusCode.Unauthorized, e);
            }

            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Model State Invalid."));
            }

            String message = "";
            if (!existingSession.CheckMaxTakeawaysPerHour())
            {
                message += "Restaurant is fully booked.";
            }

            double surcharge = 0;
            if (existingSession.Delivery)
            {
                if (!existingSession.CheckDeliveryRadius(Restaurant.Id))
                {
                    message += "Delivery too far away.";
                }

                if (!existingSession.CheckDeliverySurcharge(Restaurant.Id))
                {
                    surcharge += Core.Settings.Setting<double>(Restaurant.Id, "DeliverySurcharge");
                }
            }

            /*
             * Create takeaway session
             */
            epicuri.Core.DatabaseModel.TakeAwaySession takeaway
                = db.Sessions.OfType<TakeAwaySession>().FirstOrDefault(session => session.Id == existingSession.Id);

            if (takeaway == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Session not found."));
            }

            takeaway.Delivery = existingSession.Delivery;
            takeaway.ExpectedTime = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(existingSession.ExpectedTime);
            takeaway.Telephone = existingSession.Telephone;
            takeaway.Name = existingSession.Name;
            takeaway.Paid = existingSession.Paid;
            takeaway.RequestedBill = existingSession.RequestedBill;
            takeaway.Deleted = false;
            takeaway.Message = existingSession.Message;

            if (existingSession.Delivery)
            {
                if (existingSession.Address == null)
                {
                   message += "Address needed";
                }
                takeaway.DeliveryAddress = new epicuri.Core.DatabaseModel.Address
                {
                    City = existingSession.Address.City,
                    PostCode = existingSession.Address.PostCode,
                    Town = existingSession.Address.Town,
                    Street = existingSession.Address.Street,
                };
            }

            db.SaveChanges(System.Data.Objects.SaveOptions.AcceptAllChangesAfterSave);

            return Request.CreateResponse(HttpStatusCode.Created, new Models.TakeawaySession((TakeAwaySession)takeaway));

        }

        public HttpResponseMessage DeleteTakeaway(int id)
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

            if (!this.ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid Model State:" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }

            Core.DatabaseModel.TakeAwaySession session = db.Sessions.OfType<Core.DatabaseModel.TakeAwaySession>().FirstOrDefault(s => s.Id == id);

            if (session == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Session not found"));
            }

            // Set the deleted flag for the takeaaway
            session.Deleted = true;

            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.OK);
        }

    }
}

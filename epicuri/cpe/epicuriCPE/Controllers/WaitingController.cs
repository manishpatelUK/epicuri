using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using epicuri.Core.DatabaseModel;
using epicuri.CPE.Controllers;
using Newtonsoft.Json;

namespace epicuri.CPE.Controllers
{
    public class WaitingController : Models.EpicuriApiController
    {
        [HttpGet]
        public HttpResponseMessage GetWaiting()
        {
            // THESE METHODS MAY BE OBSOLETE - THE PARTY METHOD IS USED OVER THIS (UNLESS THIS IS USED SERVER SIDE)
            return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("THESE METHODS ARENT USED"));

            /*
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


            var Waiting = from w in db.Parties.OfType<WaitingList>()
                          orderby w.CreatedTime ascending
                          where w.RestaurantId == this.Restaurant.Id && (w.Session == null ? true : w.Session.Tables.Count() == 0) && w.Deleted == false
                          select w;

            List<Models.WaitingParty> list = new List<Models.WaitingParty>();

            foreach (Party p in Waiting)
            {
                if (p.GetType() == typeof(WaitingList))
                {
                    list.Add(new Models.WaitingParty
                    {
                        Id = p.Id,
                        Name = p.Name,
                        Created = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(p.CreatedTime),
                        NumberOfPeople = p.NumberOfPeople,
                        SessionId = p.Session == null ? 0 : p.Session.Id,
                        LeadCustomer = p.LeadCustomer != null ? new Models.Customer 
                        {
                            Id = p.LeadCustomer.Id,
                            Name = p.LeadCustomer.Name,
                            Address = p.LeadCustomer.Address,
                            Email = p.LeadCustomer.Email,
                            PhoneNumber = p.LeadCustomer.PhoneNumber
                        }:null,
                    });
                }
            }
            return Request.CreateResponse(HttpStatusCode.OK, list);
             */
        }

        [HttpPost]
        [ActionName("PostWaitingWithOrder")]
        public HttpResponseMessage PostWaitingWithOrder(Models.WaitingPartyAndOrderPayload WaitingPartyOrderPayload)
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
            
            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.NotAcceptable, new Exception("Model state is invalid" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }

            Models.WaitingParty party = WaitingPartyOrderPayload.party;
            Models.OrderPayload[] orders = WaitingPartyOrderPayload.order;

            // Need to create the party/session and get the table number before attaching the order to that table id. 
            // Should return standard response for creating party

            // Are these always AdHoc?

            HttpResponseMessage responseFromCreatingParty = PostWaiting(party);



            HttpContent httpContent = responseFromCreatingParty.Content;
            string jsonContent = httpContent.ReadAsStringAsync().Result;
            Models.WaitingParty waitingParty = JsonConvert.DeserializeObject<Models.WaitingParty>(jsonContent);

            // Get Session Table

            Diner dinerTable = db.Diners.Where(d => d.SeatedSessionId == waitingParty.SessionId).FirstOrDefault();


            if (dinerTable == null || responseFromCreatingParty.StatusCode == HttpStatusCode.BadRequest)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, "Error creating new Session for Party. Please check JSON format.");
            }

            // Need to modify order to add table ID (which is the Id of the diner)

            foreach (Models.OrderPayload order in orders)
            {
                order.DinerId = dinerTable.Id;
            }

            HttpResponseMessage responseToReturn = null;

            Dictionary<int, epicuri.Core.DatabaseModel.Batch> batches = new Dictionary<int, epicuri.Core.DatabaseModel.Batch>();

            foreach (epicuri.CPE.Models.OrderPayload order in orders)
            {

                HttpResponseMessage orderResponse = epicuri.CPE.Models.Order.CreateOrder(Request,
                                                                                         db,
                                                                                         order,
                                                                                         this.Restaurant,
                                                                                         this.Staff,
                                                                                         batches);


                if (orderResponse.StatusCode != HttpStatusCode.Created)
                {
                    responseToReturn = orderResponse;
                }
            }

            if (responseToReturn != null)
            {
                // Should probably delete party
                return responseToReturn;
            }
            else return responseFromCreatingParty;

        }

        [HttpPost]
        [ActionName("PostWaitingWithoutOrder")]
        public HttpResponseMessage PostWaiting(Models.WaitingParty NewParty)
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
                return Request.CreateResponse(HttpStatusCode.BadRequest, this.ModelState);
            }

            
            WaitingList w = new WaitingList
            {
                Name = NewParty.Name,
                NumberOfPeople = NewParty.NumberOfPeople,

                CreatedTime = DateTime.UtcNow,
                RestaurantId = Restaurant.Id,
            };

            /*
             * Check that the epicuri user is OK
             */
            if (NewParty.LeadCustomer != null)
            {
                DateTime min = DateTime.UtcNow.AddHours(-12);
                CheckIn c = db.CheckIns.Where(ci => ci.Time > min && ci.Restaurant.Id == this.Restaurant.Id && ci.Customer.Id == NewParty.LeadCustomer.Id && ci.Party == null).OrderByDescending(ci => ci.Time).FirstOrDefault();

                if (c == null)
                {
                    Core.DatabaseModel.CheckIn newCheckIn = new Core.DatabaseModel.CheckIn();
                    Customer cust = db.Customers.SingleOrDefault(customer => customer.Id == NewParty.LeadCustomer.Id);

                    if (cust == null)
                        return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("User not found or has not checked in recently"));

                    newCheckIn.Restaurant = Restaurant;
                    newCheckIn.Time = DateTime.UtcNow;
                    newCheckIn.Customer = cust;
                    newCheckIn.Party = w;

                    w.LeadCustomer = newCheckIn.Customer;

                    //return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("User not found or has not checked in recently"));
                }
                else
                {
                   w.LeadCustomer = c.Customer;
                   c.Party = w;
                }
            }

            db.AddToParties(w);

            // If it's AdHoc all we care about is the name. Fill in NumberOfPeople, CreateSession, Tables and ServiceId with default data

            if (NewParty.IsAdHoc)
            {
                NewParty.CreateSession = true;
                NewParty.Tables = new int[0];
                NewParty.ServiceId = 999;
                NewParty.NumberOfPeople = 1;
            }

            try
            {
                if (NewParty.CreateSession)
                {

                   

                    var session = SessionController.CreateSession(db, Restaurant, w, new Models.SessionPayload
                    {
                        ServiceId = NewParty.ServiceId,
                        Tables = NewParty.Tables,
                        IsAdHoc = NewParty.IsAdHoc
                    });

                    NewParty.ServiceId = session.ServiceId;

                    db.SaveChanges();
                    NewParty.SessionId = session.Id;
                    
                }
            }
            catch (Exception e)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, e);
            }
            
            db.SaveChanges();

            NewParty.Id = w.Id;
            NewParty.Created = Core.Utils.Time.DateTimeToUnixTimestamp(w.CreatedTime);
            return Request.CreateResponse(HttpStatusCode.Created, NewParty);
        }

        [HttpPut]
        public HttpResponseMessage PutWaiting(int id, Models.WaitingParty waiting)
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
                return Request.CreateResponse(HttpStatusCode.BadRequest);
            }


            WaitingList targetWaiting;
            try
            {
                targetWaiting = db.Parties.OfType<WaitingList>().Single(w => w.Id == id && w.Deleted == false);

            }
            catch (Exception)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound);
            }

            if (targetWaiting == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound);
            }

            if (targetWaiting.RestaurantId != Restaurant.Id)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden);
            }


            
            targetWaiting.Name = waiting.Name;
            targetWaiting.NumberOfPeople = waiting.NumberOfPeople;

            waiting.Id = id;
            waiting.Created = Core.Utils.Time.DateTimeToUnixTimestamp( targetWaiting.CreatedTime);
            

            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK, waiting);
        }

        [HttpDelete]
        public HttpResponseMessage DeleteWaiting(int id, bool withPrejudice)
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

            bool andBlackMark;
            if (!ModelState.IsValid)
            {
                andBlackMark = false;
            }
            else
            {
                andBlackMark = withPrejudice;
            }

            WaitingList targetWaiting;
            try
            {
                targetWaiting = db.Parties.OfType<WaitingList>().Single(w => w.Id == id && w.Deleted == false);

            }
            catch (Exception)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound);
            }

            if (targetWaiting == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound);
            }

            if (targetWaiting.RestaurantId != Restaurant.Id)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden);
            }

            targetWaiting.Deleted = true;


            /*
             * Add black mark if needs be
             */
            if (andBlackMark && targetWaiting.LeadCustomer !=null)
            {
                BlackMark bm = new BlackMark
                {
                    Reason = "Waiting List",
                    Expires = DateTime.UtcNow.AddMonths(1),
                    CustomerId = targetWaiting.LeadCustomer.Id,
                    Added = DateTime.UtcNow,
                };
                db.AddToBlackMarks(bm);
            }

            var associatedCheckIns = db.CheckIns.Where(c => c.Party.Id == targetWaiting.Id);

            foreach (CheckIn checkIn in associatedCheckIns)
            {
                db.CheckIns.DeleteObject(checkIn);
            }

            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.NoContent);
        }
    }
}

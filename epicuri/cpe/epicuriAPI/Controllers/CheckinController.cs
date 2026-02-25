using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using epicuri.Core.DatabaseModel;
using System.ComponentModel.DataAnnotations;
using epicuri.API.Models;
using epicuri.Core;


namespace epicuri.API.Controllers
{
    public class CheckInController : Support.APIController
    {
        [HttpPost]
        public HttpResponseMessage PostCheckIn(Models.CheckIn checkIn)
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


            
            /*
             * Check the restaurant is valid
             */
            try
            {
            
                var restaurant = db.Restaurants.SingleOrDefault(o => o.Id == checkIn.RestaurantId);

                if (restaurant == null)
                {
                    return Request.CreateResponse(System.Net.HttpStatusCode.NotFound, new Exception("Restaurant not found"));
                }
                


                // ---- CASE 1: We're checking in for a specific reservation
                if (checkIn.ReservationId != null)
                {
                    IEnumerable<Core.DatabaseModel.CheckIn> reservationCheck = restaurant.CheckIns.Where(chck => chck.Customer == customer && chck.Party != null && chck.Party.Id == checkIn.ReservationId);

                    if (reservationCheck.FirstOrDefault() != null)
                    {
                        //There is already a checkin for the reservation, so return the ID
                        return Request.CreateResponse(HttpStatusCode.OK, new Models.CheckIn(customer, reservationCheck.FirstOrDefault().Id));
                    }
                    else
                    {
                        //Check In for the reservation
                        if (checkIn.ReservationId.HasValue)
                        {
                            var reservation = db.Parties.SingleOrDefault(p => p.Id == checkIn.ReservationId.Value);

                            if (reservation == null)
                                return Request.CreateResponse(System.Net.HttpStatusCode.NotFound, new Exception("Reservation not found"));
                            else
                            {
                                reservation.ArrivedTime = DateTime.UtcNow;
                                checkIn.SetParty(reservation);
                            }
                        }
                    }
                }
                else
                {
                    // ---- CASE 2: Not a specific reservation, search for exisiting check in, which is seated in an active session
                    IEnumerable<Core.DatabaseModel.CheckIn> dinerCheck = restaurant.CheckIns.Where(chck => chck.Customer == customer && chck.Diner != null);

                    //That person has at some point been a diner
                    if (dinerCheck.FirstOrDefault() != null)
                    {
                        foreach (Core.DatabaseModel.CheckIn sessionCheck in dinerCheck)
                        {
                            //Iterate over the check ins finding any that are not yet closed
                            Core.DatabaseModel.Session sess = restaurant.Sessions.Where(chck => sessionCheck.Diner.SeatedSessionId == chck.Id).FirstOrDefault();
                            if (sess.ClosedTime == null)
                            {
                                // Session not closed so return Id
                                return Request.CreateResponse(HttpStatusCode.OK, new Models.CheckIn(customer, sessionCheck.Id));
                            }
                        }
                    }
                    {
                        // ---- CASE 3: Not a specific reservation and the diner is not currently in an active session, search for existing check in which is attached to an active party
                        IEnumerable<Core.DatabaseModel.CheckIn> partyCheck = restaurant.CheckIns.Where(chck => chck.Customer == customer && chck.Diner == null);

                        if (partyCheck.FirstOrDefault() != null)
                        {
                            DateTime minTime = DateTime.UtcNow.AddMinutes(-Settings.Setting<double>(restaurant.Id, "WalkinExpirationTime"));


                            foreach (Core.DatabaseModel.CheckIn p in partyCheck)
                            {
                                //Chech that party is not null and the session party has not been deleted
                                if (p.Party != null && !p.Party.Deleted)
                                {
                                    if ((p.Party.GetType() == typeof(epicuri.Core.DatabaseModel.WaitingList) && p.Party.CreatedTime > minTime)
                                        || (p.Party.GetType() == typeof(epicuri.Core.DatabaseModel.Reservation) && p.Party.ArrivedTime.HasValue && p.Party.ArrivedTime > minTime))
                                    {

                                        return Request.CreateResponse(HttpStatusCode.OK, new Models.CheckIn(customer, p.Id));
                                    }
                                }
                            }
                        }
                    }
                   
                    // ---- CASE 4: Not in a session, not in a party, search to see if the check in has not yet expired
                    DateTime min = DateTime.UtcNow.AddMinutes(-Settings.Setting<double>(restaurant.Id, "CheckinExpirationTime"));
                    IEnumerable<Core.DatabaseModel.CheckIn> pcheckins = restaurant.CheckIns.Where(chck => chck.Customer == customer && chck.Diner == null && chck.Party == null && chck.Time > min);

                    if (pcheckins.FirstOrDefault() != null)
                    {
                        return Request.CreateResponse(HttpStatusCode.OK, new Models.CheckIn(customer, pcheckins.FirstOrDefault().Id));
                    }
                }

                // ==== NONE OF THE ABOVE CASES APPLICABLE: Create a new check in

                // Set the customer on the checkin object
                checkIn.SetCustomer(customer);

                // Add the checkin to the database
                Core.DatabaseModel.CheckIn newObj = checkIn.ToCheckIn(); 
                restaurant.CheckIns.Add(newObj);

                //Commit to db
                db.SaveChanges();

                //Set the id on the model we created and return it
                return Request.CreateResponse(HttpStatusCode.Created, new Models.CheckIn(customer, newObj.Id));
           
            }
            catch (Exception e)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, e);
            }
            

        }


        [HttpGet]
        public HttpResponseMessage GetCheckIn(int id)
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

            // Get the check in from the database
            IEnumerable<Core.DatabaseModel.CheckIn> checkIns = db.CheckIns.Where(chck => chck.Customer.Id == customer.Id && chck.Id == id);
            Core.DatabaseModel.CheckIn checkIn = checkIns.FirstOrDefault();

            if (checkIn == null)
            {
                // checkin not found
                return Request.CreateResponse(HttpStatusCode.NotFound, "CheckIn Not Found");
            }
            else if(checkIn.Diner != null)
            {
                // Customer is a diner
                Core.DatabaseModel.Session sess = checkIn.Restaurant.Sessions.Where(chck => checkIn.Diner.SeatedSessionId == chck.Id).FirstOrDefault();
                return Request.CreateResponse(HttpStatusCode.OK, new Models.CheckIn(customer, checkIn.Id));
            }
            else if(checkIn.Party != null)
            {
                

                // customer is in a party
                if(checkIn.Party.GetType() == typeof(epicuri.Core.DatabaseModel.WaitingList))
                {
                    bool hasExpired = ((DateTime)checkIn.Party.CreatedTime).AddMinutes(Settings.Setting<double>(checkIn.Party.RestaurantId, "WalkinExpirationTime")) <= DateTime.UtcNow;

                    // CheckIn is of type WaitingList (as opposed to reservation)
                    if (!checkIn.Party.Deleted && !hasExpired)
                    {
                        // Customer is part of a party - return checkin id
                        return Request.CreateResponse(HttpStatusCode.OK, new Models.CheckIn(customer, checkIn.Id));
                    }
                    else
                    {
                        return Request.CreateResponse(HttpStatusCode.NotFound, "CheckIn Expired");
                    }

                }
                else
                {
                    //EP-263 CHECKED IN RESERVATIONS USES WALKIN EXPIRATION TIME AS OPPOSED TO CHECKIN EXPIRATION TIME - CHANGES REVERTED
                    bool hasExpired = ((DateTime)checkIn.Party.ArrivedTime).AddMinutes(Settings.Setting<double>(checkIn.Party.RestaurantId, "WalkinExpirationTime")) <= DateTime.UtcNow;

                    //bool hasExpired = ((DateTime)checkIn.Party.ArrivedTime).AddMinutes(Settings.Setting<double>(checkIn.Party.RestaurantId, "CheckinExpirationTime")) <= DateTime.UtcNow;

                    //CheckIn is of type reservation
                    if (!checkIn.Party.Deleted && !hasExpired)
                    {
                        // Customer is part of a reservation - return checkin id
                        return Request.CreateResponse(HttpStatusCode.OK, new Models.CheckIn(customer, checkIn.Id));
                    }
                    else
                    {
                        // reservation has been cancelled
                        return Request.CreateResponse(HttpStatusCode.NotFound, "CheckIn Expired");
                    }
                }
            } 
            else
            {
               // customer just has a checkin
                DateTime minTime = DateTime.UtcNow.AddMinutes(-Settings.Setting<double>(checkIn.Restaurant.Id, "CheckinExpirationTime"));
                
               if (checkIn.Time < minTime)
               {
                    return Request.CreateResponse(HttpStatusCode.NotFound, "CheckIn Expired");
               }

               return Request.CreateResponse(HttpStatusCode.OK, new Models.CheckIn(customer, checkIn.Id));
            }
        }



        [HttpDelete]
        public HttpResponseMessage DeleteCheckIn(int id)
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

            epicuri.Core.DatabaseModel.CheckIn checkin = db.CheckIns.FirstOrDefault(c => c.Id == id && c.Customer.Id == customer.Id);

            if (checkin == null)
            {
                return Request.CreateResponse(System.Net.HttpStatusCode.NotFound, new Exception("CheckIn not found")); 
            }

            // Delete the arrived time

            if (checkin.Party != null)
            {
                var reservation = db.Parties.SingleOrDefault(p => p.Id == checkin.Party.Id);
                if (reservation != null)
                    reservation.ArrivedTime = null;
            }

            db.CheckIns.DeleteObject(checkin);
            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.NoContent);

            /*
             * Create a new Model form the chekcin we got from db
             
            try
            {
                //

                epicuri.Core.DatabaseModel.CheckIn checkin = db.CheckIns.FirstOrDefault(c => c.Id == id && c.Customer == customer);

                if (checkin == null)
                {
                    return Request.CreateResponse(System.Net.HttpStatusCode.NotFound, new Exception("CheckIn not found"));
                }

                db.CheckIns.DeleteObject(checkin);
                db.SaveChanges();

                return Request.CreateResponse(HttpStatusCode.NoContent);
            }
            catch (Exception e)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, e);
            }*/


        }
    }
}

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
    public class ReservationController : Models.EpicuriApiController
    {

        [HttpGet]
        [ActionName("Single")]
        public HttpResponseMessage SingleReservation(int id)
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

            IQueryable Reservations = null;

           
            Reservations = from w in db.Parties.OfType<Reservation>()
                            orderby w.ReservationTime ascending
                            where w.Id == id
                            select w;           

            List<Models.Reservation> list = new List<Models.Reservation>();
            DateTime minTime = DateTime.UtcNow.AddMinutes(-Settings.Setting<double>(this.Restaurant.Id, "WalkinExpirationTime"));

            foreach (Party p in Reservations)
            {
                if (p.GetType() == typeof(Reservation))
                {
                    list.Add(new Models.Reservation((Reservation)p));
                }
            }

            return Request.CreateResponse<IEnumerable<Models.Reservation>>(HttpStatusCode.OK, list);
        }

        [HttpGet]
        [ActionName("Index")]
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

            if (fromTime == 0)
            {
                ft = DateTime.Today;
            }
            else
            {
                ft = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(fromTime);
            }

            if (toTime == 0)
            {
                toTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(ft.AddMonths(3));
            }

            DateTime tt = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(toTime);

            if (tt == null)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest);
            }


            IQueryable Reservations = null;

            if (pendingWaiterAction)
            {
                Reservations = from w in db.Parties.OfType<Reservation>()
                                   orderby w.ReservationTime ascending
                                   where w.RestaurantId == this.Restaurant.Id && w.Accepted == false && w.Rejected == false
                                       //&& (w.Session == null ? true : w.Session.Tables.Count() == 0)
                                   && w.Deleted == false
                                   && w.ReservationTime >= ft
                                   && w.ReservationTime <= tt
                                   && w.Rejected == false
                                   select w;
            }
            else
            {
               Reservations = from w in db.Parties.OfType<Reservation>()
                                   orderby w.ReservationTime ascending
                                   where w.RestaurantId == this.Restaurant.Id
                                       //&& (w.Session == null ? true : w.Session.Tables.Count() == 0)
                                   //&& w.Deleted == false
                                   && w.ReservationTime >= ft
                                   && w.ReservationTime <= tt
                                   && w.Rejected == false
                                   select w;
            }
            //

            // we need to add a new flag for "timed out" if the check-in for a reservaion has expired

            List<Models.Reservation> list = new List<Models.Reservation>();
            DateTime minTime = DateTime.UtcNow.AddMinutes(-Settings.Setting<double>(this.Restaurant.Id, "WalkinExpirationTime"));

            foreach (Party p in Reservations)
            {
                if (p.GetType() == typeof(Reservation))
                {
                    if (pendingWaiterAction)
                    {
                        CheckIn ci = (from c in db.CheckIns
                                     where c.Party.Id == p.Id
                                     select c).SingleOrDefault();

                        if (ci != null)
                        {
                        
                            if (ci.Time < minTime)
                            {
                                // Checkin has expired, do not add it to the list of reservations
                            }
                            else
                            {
                                list.Add(new Models.Reservation((Reservation)p));
                            }
                        }
                        else
                        {
                            list.Add(new Models.Reservation((Reservation)p));
                        }
                    }
                    else
                    {
                        list.Add(new Models.Reservation((Reservation)p));
                    }
                }
            }

            return Request.CreateResponse<IEnumerable<Models.Reservation>>(HttpStatusCode.OK, list);
        }


        [HttpPut]
        [ActionName("Accept")]
        public HttpResponseMessage PutAccept(int id)
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


            IEnumerable<Reservation> reservations;

            reservations = db.Parties.OfType<Reservation>().Where(p => p.Id == id && p.RestaurantId == this.Restaurant.Id);

            if (reservations.FirstOrDefault() == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Reservation not found"));
            }


            Core.DatabaseModel.Reservation s1 = reservations.FirstOrDefault();


            s1.Accepted = true;
            db.SaveChanges();



            return Request.CreateResponse(HttpStatusCode.OK, new Models.Reservation(s1));
        }

        [HttpPut]
        [ActionName("Reject")]
        public HttpResponseMessage PutReject(int id, Models.RejectionPayload payload)
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

            IEnumerable<Reservation> reservations;

            reservations = db.Parties.OfType<Reservation>().Where(p => p.Id == id && p.RestaurantId == this.Restaurant.Id);

            if (reservations.FirstOrDefault() == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Reservation not found"));
            }

            Core.DatabaseModel.Reservation s1 = reservations.FirstOrDefault();

            var associatedCheckIns = db.CheckIns.Where(c => c.Party.Id == s1.Id);

            foreach (CheckIn checkIn in associatedCheckIns)
            {
               db.CheckIns.DeleteObject(checkIn);
            }

            s1.Rejected = true;
            s1.RejectionNotice = payload.Notice;

            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.NoContent);
        }


        [HttpPut]
        [ActionName("Arrived")]
        public HttpResponseMessage PutArrived(int id)
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


            IEnumerable<Reservation> reservations;

            reservations = db.Parties.OfType<Reservation>().Where(p => p.Id == id && p.RestaurantId == this.Restaurant.Id);

            if (reservations.FirstOrDefault() == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Reservation not found"));
            }


            Core.DatabaseModel.Reservation s1 = reservations.FirstOrDefault();


            s1.ArrivedTime = DateTime.UtcNow;
            db.SaveChanges();



            return Request.CreateResponse(HttpStatusCode.OK, new Models.Reservation(s1));
        }


        [HttpPost]
        [ActionName("Index")]
        public HttpResponseMessage PostReservation(Models.Reservation reservation)
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

            Reservation r = new Reservation
            {

                ReservationTime = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(reservation.ReservationTime),
                Name = reservation.Name,
                NumberOfPeople = reservation.NumberOfPeople,
                Notes = reservation.Notes,

                CreatedTime = DateTime.UtcNow,
                RestaurantId = Restaurant.Id,
                Telephone = reservation.Telephone,
            };

            Customer customer = null;
            // Find existing customer
            if (reservation.LeadCustomerId != 0)
            {
                customer = db.Customers.FirstOrDefault(cust => cust.Id == reservation.LeadCustomerId);
                if (customer != null)
                {
                    r.LeadCustomer = customer;
                }
            }

            db.AddToParties(r);
            db.SaveChanges();

            reservation.Id = r.Id;
            reservation.Created = Core.Utils.Time.DateTimeToUnixTimestamp(r.CreatedTime);

            return Request.CreateResponse(HttpStatusCode.Created, reservation);
        }

        [HttpPost]
        [ActionName("ReservationCheck")]
        public HttpResponseMessage PostReservationCheck(Models.Reservation reservation)
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

            // Reservation checks
            Dictionary<string, object> ReturnDict = new Dictionary<string, object>();
            List<string> WarningMessages = new List<string>();
            
            if (!reservation.CheckMaxCoversPerReservation(Restaurant.Id))
            {
                //return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Party size too big, please call for group booking"));
                WarningMessages.Add("High number of covers (Max" + Core.Settings.Setting<string>(reservation.RestaurantId, "MaxCoversPerReservation") + ")"); 
            }

            if (!reservation.CheckMaxActiveReservations(Restaurant.Id, reservation.Id))
            {
                //return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant is fully booked for this time"));
                WarningMessages.Add("Restaurant floor is reaching its capacity for reservations during this time");
            }

            if (!reservation.CheckMaxActiveReservationsCovers(Restaurant.Id, reservation))
            {
                //return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant is fully booked for this time"));
                WarningMessages.Add("Restaurant floor is reaching its capacity for diners during this time");
            }
            
            var reservationMinimumTime = epicuri.Core.Settings.Setting<int>(Restaurant.Id, "ReservationMinimumTime");
            var rmtUnix = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(DateTime.UtcNow.AddMinutes(reservationMinimumTime));

            if (reservation.ReservationTime < rmtUnix)
            {
                WarningMessages.Add("Due within " + epicuri.Core.Settings.Setting<int>(Restaurant.Id, "ReservationMinimumTime") + " mins");
            }

            // EP-265
            Reservation partyRes = db.Parties.OfType<Reservation>().FirstOrDefault(r => r.Id == reservation.Id);
            if (partyRes != null && (partyRes.ReservationTime.AddMinutes(-Settings.Setting<double>(partyRes.RestaurantId, "ReservationLockWindow")) < DateTime.UtcNow))
            {
                WarningMessages.Add("Change to booking is very close to due time");
            }

            foreach (DateConstraint constraint in Restaurant.DateConstraints.Where(c => !c.TargetSession))
            {
                // First make a System.DateTime equivalent to the UNIX Epoch.
                System.DateTime reservationDateTime = new System.DateTime(1970, 1, 1, 0, 0, 0, 0);

                // Add the number of seconds in UNIX timestamp to be converted.
                reservationDateTime = reservationDateTime.AddSeconds(reservation.ReservationTime);

                TimeZoneInfo timezone = Restaurant.GetTimeZoneInfo();

                if (constraint.Matches(timezone, reservationDateTime))
                {
                    WarningMessages.Add("Blackout exists for this date/time"); 
                }
                /*
                if (constraint.Matches())
                {
                    WarningMessages.Add("Blackout exists for this date/time");
                }
                 */
            }

            Customer customerCheck = db.Customers.FirstOrDefault(cust => cust.Id == reservation.LeadCustomerId);
            if (customerCheck != null)
            {
                if (!epicuri.CPE.Models.Customer.OKToOrder(customerCheck))
                {
                    WarningMessages.Add("Customer has blackmarks. Please give careful attention");
                }
            }

            if (customerCheck != null)
            {
                var currentReservations = db.Parties.OfType<Reservation>().Where(cr => cr.LeadCustomer.Id == customerCheck.Id && cr.Deleted == false && cr.Rejected == false && cr.Session == null);
                var resTimeSlot = epicuri.Core.Settings.Setting<int>(Restaurant.Id, "ReservationTimeSlot");

                // First make a System.DateTime equivalent to the UNIX Epoch.
                System.DateTime resDateTime = new System.DateTime(1970, 1, 1, 0, 0, 0, 0);

                // Add the number of seconds in UNIX timestamp to be converted.
                resDateTime = resDateTime.AddSeconds(reservation.ReservationTime);
                bool conflictResTime = false;

                List<Reservation> currentRes = new List<Reservation>();


                foreach (Reservation res in currentReservations)
                {
                    if (res.Id != reservation.Id)
                    {
                        if (!conflictResTime)
                        {

                            //EP-259 A timed out reservation conflicts with new reservations

                            // Check to see if there is a checkin associated with this reservation
                            CheckIn ci = (from c in db.CheckIns
                                          where c.Party.Id == res.Id
                                          select c).SingleOrDefault();

                            if (ci != null)
                            {
                                // A checkin is associated with this reservation
                            }
                            else
                            {


                                //Check if they're the same time
                                if (res.ReservationTime == resDateTime)
                                {
                                    WarningMessages.Add("A reservation for this guest already exists during this date/time.");
                                    conflictResTime = true;
                                }

                                //Check the current reservation won't run over to the accepted reservation later (if any)
                                else if (resDateTime.AddMinutes(resTimeSlot) >= res.ReservationTime && resDateTime.AddMinutes(resTimeSlot) <= res.ReservationTime.AddMinutes(resTimeSlot))
                                {
                                    WarningMessages.Add("A reservation for this guest already exists during this date/time.");
                                    conflictResTime = true;
                                }

                                //Check the current reservation starts after the accepted reservation has finished (if any)
                                else if (res.ReservationTime.AddMinutes(resTimeSlot) >= resDateTime && res.ReservationTime.AddMinutes(resTimeSlot) <= resDateTime.AddMinutes(resTimeSlot))
                                {
                                    WarningMessages.Add("A reservation for this guest already exists during this date/time.");
                                    conflictResTime = true;
                                }
                            }
                        }
                    }

                }
            }

            ReturnDict.Add("Warning", WarningMessages);
            return Request.CreateResponse(HttpStatusCode.OK, ReturnDict);
        }



        [HttpPut]
        [ActionName("Single")]
        public HttpResponseMessage PutReservation(int id, Models.Reservation reservation)
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


            Reservation targetReservation;
            try
            {
                targetReservation = db.Parties.OfType<Reservation>().Single(res => res.Id == id && res.Deleted == false);

            }
            catch (Exception)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound);
            }

            if (targetReservation == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound);
            }

            if (targetReservation.RestaurantId != Restaurant.Id)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden);
            }

            targetReservation.ReservationTime = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(reservation.ReservationTime);
            targetReservation.Name = reservation.Name;
            targetReservation.NumberOfPeople = reservation.NumberOfPeople;
            targetReservation.Telephone = reservation.Telephone;
            targetReservation.Notes = reservation.Notes;

            // Only populate the lead customer if no previously set and if set by waiter app
            if (targetReservation.LeadCustomer == null && reservation.LeadCustomerId != 0)
            {
                 Customer customer = db.Customers.FirstOrDefault(cust => cust.Id == reservation.LeadCustomerId);
                 targetReservation.LeadCustomer = customer;
            }

            targetReservation.Accepted = true;

            db.SaveChanges();

            reservation.Id = id;
            reservation.Created = Core.Utils.Time.DateTimeToUnixTimestamp(targetReservation.CreatedTime);
            if (targetReservation.LeadCustomer != null)
            {
                reservation.LeadCustomer = new Models.Customer
                {
                    Id = targetReservation.LeadCustomer.Id,
                    Name = targetReservation.LeadCustomer.Name,
                    Address = targetReservation.LeadCustomer.Address,
                    PhoneNumber = targetReservation.LeadCustomer.PhoneNumber,
                    Email = targetReservation.LeadCustomer.Email
                };
            }
            return Request.CreateResponse(HttpStatusCode.OK);
        }

        [HttpDelete]
        [ActionName("Single")]
        public HttpResponseMessage DeleteReservation(int id, bool withPrejudice)
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

            Reservation targetReservation;
            try
            {
                targetReservation = db.Parties.OfType<Reservation>().Single(w => w.Id == id && w.Deleted == false);

            }
            catch (Exception)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound);
            }

            if (targetReservation == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound);
            }

            if (targetReservation.RestaurantId != Restaurant.Id)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden);
            }

            targetReservation.Deleted = true;

            /*
             * Add black mark if needs be
             */
            if (andBlackMark && targetReservation.LeadCustomer != null)
            {
                BlackMark bm = new BlackMark
                {
                    Reason = "Waiting List",
                    Expires = DateTime.UtcNow.AddMonths(1),
                    CustomerId = targetReservation.LeadCustomer.Id,
                    Added = DateTime.UtcNow,
                };
                db.AddToBlackMarks(bm);
            }

            var associatedCheckIns = db.CheckIns.Where(c => c.Party.Id == targetReservation.Id);

            foreach (CheckIn checkIn in associatedCheckIns)
            {
                db.CheckIns.DeleteObject(checkIn);
            }

            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.NoContent);
        }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using epicuri.Core.DatabaseModel;
using epicuri.Core;
using System.Data.Objects;


namespace epicuri.API.Controllers
{
    public class ReservationController : Support.APIController
    {
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
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state:" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }

            if (db.Restaurants.FirstOrDefault(rest => rest.Id == reservation.RestaurantId) == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Restaurant Not Found"));
            }
           
            /*
             * Check if model valid
             */

            bool rejections = true;

            string RejectionNotices = "";

            if (!reservation.CheckMaxCoversPerReservation())
            {
                //return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Party size too big, please call for group booking"));
                RejectionNotices = RejectionNotices +  "- Exceeds max number of covers (" + Core.Settings.Setting<string>(reservation.RestaurantId, "MaxCoversPerReservation") + ").";
                rejections = false;
            }

            if (!reservation.CheckMaxActiveReservations(reservation.Id))
            {
                if (!String.IsNullOrEmpty(RejectionNotices))
                    RejectionNotices = RejectionNotices + "\n";

                //return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant is fully booked for this time"));
                RejectionNotices = RejectionNotices + "- Restaurant floor is reaching its capacity for reservations during this time.";
                rejections = false;
            }

            if (!reservation.CheckMaxActiveReservationsCovers(reservation))
            {
                if (!String.IsNullOrEmpty(RejectionNotices))
                    RejectionNotices = RejectionNotices + "\n";

                //return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant is fully booked for this time"));
                RejectionNotices = RejectionNotices + "- Restaurant floor is reaching its capacity for diners during this time.";
                rejections = false;
            }

            var restaurant = db.Restaurants.Where(rest => rest.Id == reservation.RestaurantId).FirstOrDefault();

            var reservationMinimumTime = epicuri.Core.Settings.Setting<int>(restaurant.Id, "ReservationMinimumTime");
            var rmtUnix = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(DateTime.UtcNow.AddMinutes(reservationMinimumTime));

            if (reservation.ReservationTime < rmtUnix) {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Booking time too soon."));
            }

            TimeZoneInfo timezone = restaurant.GetTimeZoneInfo();

            foreach (DateConstraint constraint in restaurant.DateConstraints.Where(c => !c.TargetSession))

            {
                DateTime constraintCheck = Core.Utils.Time.UnixTimeStampToDateTime(reservation.ReservationTime);

                if (constraint.Matches(timezone, constraintCheck))
                {
                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant not accepting reservations for this time."));
                }

                /*
                if (!constraint.Matches())
                {
                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant not accepting reservations for this time."));
                }*/
            }


            if (String.IsNullOrWhiteSpace(reservation.Notes))
            {
                reservation.Notes = "";
            }
            Reservation r = new Reservation
            {
                ReservationTime = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(reservation.ReservationTime),
                Name = customer.Name.Firstname + " " + customer.Name.Surname,
                NumberOfPeople = reservation.NumberOfPeople,
                Notes = reservation.Notes,
                CreatedTime = DateTime.UtcNow,
                RestaurantId = reservation.RestaurantId,
                Telephone = customer.PhoneNumber,
                LeadCustomer = customer,
                Accepted = epicuri.API.Models.dbCustomer.OKToOrder(customer),
                InstantiatedFromId = reservation.InstantiatedFromId
            };

            if (!String.IsNullOrEmpty(RejectionNotices))
            {
                r.RejectionNotice = RejectionNotices;
            }

            if (reservation.ArrivedTime.HasValue) {
                r.ArrivedTime = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(reservation.ArrivedTime.Value);
            }

            //EP-321 Overlapping Reservation needs to only check for the restaurant the booking is for
            var currentReservations = db.Parties.OfType<Reservation>().Where(cr => cr.LeadCustomer.Id == customer.Id && cr.Deleted == false && cr.Rejected == false && cr.Session == null && cr.RestaurantId == reservation.RestaurantId);
            var resTimeSlot = epicuri.Core.Settings.Setting<int>(restaurant.Id, "ReservationTimeSlot");
            bool conflictResTime = false;


            foreach (Reservation res in currentReservations)
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
                        if (res.ReservationTime == r.ReservationTime)
                        {
                            //string returnDT = res.ReservationTime.ToString("dd/MM/yyyy HH:mm");

                            DateTime correctDateTime = TimeZoneInfo.ConvertTimeFromUtc(res.ReservationTime, timezone);
                            string returnDT = correctDateTime.ToString("dd/MM/yyyy HH:mm");


                            return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("A reservation already exists at this venue during this time (" + returnDT + ")."));

                            /*
                            if (!String.IsNullOrEmpty(r.RejectionNotice))
                                r.RejectionNotice = r.RejectionNotice + "\n";

                            //return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant is fully booked for this time"));
                            r.RejectionNotice = r.RejectionNotice + "- A reservation already exists at this venue during this time.";
                            rejections = false;
                            conflictResTime = true;
                             */
                        }

                        //Check the current reservation won't run over to the accepted reservation later (if any)
                        else if (r.ReservationTime.AddMinutes(resTimeSlot) >= res.ReservationTime && r.ReservationTime.AddMinutes(resTimeSlot) <= res.ReservationTime.AddMinutes(resTimeSlot))
                        {
                            //string returnDT = res.ReservationTime.ToString("dd/MM/yyyy HH:mm");

                            DateTime correctDateTime = TimeZoneInfo.ConvertTimeFromUtc(res.ReservationTime, timezone);
                            string returnDT = correctDateTime.ToString("dd/MM/yyyy HH:mm");

                            return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("A reservation already exists at this venue during this time (" + returnDT + ")."));

                            /*
                            if (!String.IsNullOrEmpty(r.RejectionNotice))
                                r.RejectionNotice = r.RejectionNotice + "\n";

                            //return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant is fully booked for this time"));
                            r.RejectionNotice = r.RejectionNotice + "- A reservation already exists at this venue during this time.";
                            rejections = false;
                            conflictResTime = true;
                             */
                        }

                        //Check the current reservation starts after the accepted reservation has finished (if any)
                        else if (res.ReservationTime.AddMinutes(resTimeSlot) >= r.ReservationTime && res.ReservationTime.AddMinutes(resTimeSlot) <= r.ReservationTime.AddMinutes(resTimeSlot))
                        {
                            //string returnDT = res.ReservationTime.ToString("dd/MM/yyyy HH:mm");

                            DateTime correctDateTime = TimeZoneInfo.ConvertTimeFromUtc(res.ReservationTime, timezone);
                            string returnDT = correctDateTime.ToString("dd/MM/yyyy HH:mm");

                            return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("A reservation already exists at this venue during this time (" + returnDT + ")."));

                            /*
                            if (!String.IsNullOrEmpty(r.RejectionNotice))
                                r.RejectionNotice = r.RejectionNotice + "\n";

                            //return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant is fully booked for this time"));
                            r.RejectionNotice = r.RejectionNotice + "- A reservation already exists at this venue during this time.";
                            rejections = false;
                            conflictResTime = true;
                             */
                        }
                    }

                }

            }

            if (!r.Accepted)
            {
                if (!String.IsNullOrEmpty(r.RejectionNotice))
                    r.RejectionNotice = r.RejectionNotice + "\n";

                r.RejectionNotice = r.RejectionNotice + "- Customer has blackmarks. Please give careful attention.";
                r.Accepted = false;
            }

            // If the rejections flag has been set - set accepeted to false (Party size too big, etc)
            if (!rejections)
            {
                r.Accepted = false;
            }

            db.AddToParties(r);
            db.SaveChanges();

            reservation.Id = r.Id;
            reservation.Telephone = r.Telephone;
            reservation.Accepted = r.Accepted;

            if (r.Accepted)
            {
                return Request.CreateResponse(HttpStatusCode.Created, reservation);
            }
            else
            {
                return Request.CreateResponse(HttpStatusCode.Accepted, reservation);
            }
        }

        
        public HttpResponseMessage DeleteReservation(int id)
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

            Reservation res = db.Parties.OfType<Reservation>().FirstOrDefault(r => r.Id == id);

            if (res == null)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Reservation doesnt exist"));
            }
            
            if (res.LeadCustomer != customer)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Cannot edit this reservation"));
            }

            if (res.ReservationTime < DateTime.UtcNow.AddMinutes(Settings.Setting<double>(res.RestaurantId, "ReservationLockWindow")))
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Reservation happens too soon to cancel online. Please contact the restaurant"));
            }

            res.Deleted = true;
            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.OK);

        }

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
                return Request.CreateResponse(HttpStatusCode.BadRequest);
            }

            Reservation res = db.Parties.OfType<Reservation>().FirstOrDefault(r => r.Id == id);

            if (res == null)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Reservation doesnt exist"));
            }

            if (res.LeadCustomer != customer)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Cannot edit this reservation"));
            }

            if (res.ReservationTime.AddMinutes(-Settings.Setting<double>(res.RestaurantId, "ReservationLockWindow")) > DateTime.UtcNow)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Reservation happens too soon. Please call"));
            }


            res.Notes = reservation.Notes;
            res.NumberOfPeople = reservation.NumberOfPeople;
            res.ReservationTime = Core.Utils.Time.UnixTimeStampToDateTime(reservation.ReservationTime);
            res.Accepted = epicuri.API.Models.dbCustomer.OKToOrder(customer);
            res.InstantiatedFromId = reservation.InstantiatedFromId;
            
            if (reservation.ArrivedTime.HasValue)
                res.ArrivedTime = Core.Utils.Time.UnixTimeStampToDateTime(reservation.ArrivedTime.Value);

            db.SaveChanges();

            reservation.Id = res.Id;
            reservation.Telephone = res.Telephone;
            reservation.RestaurantId = res.RestaurantId;

            if (res.Accepted)
            {
                return Request.CreateResponse(HttpStatusCode.Created, reservation);
            }
            else
            {
                return Request.CreateResponse(HttpStatusCode.Accepted, reservation);
            }
        }

        public HttpResponseMessage GetReservations()
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

            var Checkins = from checkin in db.CheckIns where checkin.Customer.Id == customer.Id select checkin;

            int FilterTime = int.Parse(db.DefaultSettings.FirstOrDefault(dfs => dfs.Key == "ReservationFilterTime(hours)").Value);

            var ReservationHistory = from reservation in db.Parties.OfType<Reservation>()
                                     where reservation.LeadCustomer.Id == customer.Id &&
                                           EntityFunctions.AddHours(reservation.ReservationTime, FilterTime) > DateTime.UtcNow
                                     join restaurant in db.Restaurants on reservation.RestaurantId equals restaurant.Id
                                     select new Models.Reservation
                                     {
                                         Id = reservation.Id,
                                        _Time = reservation.ReservationTime,
                                         Restaurant = new Models.Restaurant
                                         {
                                             Id = restaurant.Id,
                                             Name = restaurant.Name,
                                             Address = restaurant.Address,
                                             Description = restaurant.Description,
                                             Email = restaurant.PublicEmailAddress,
                                             PhoneNumber = restaurant.PhoneNumber,
                                             Position = restaurant.Position,
                                             EnabledForDiner = restaurant.Enabled,
                                             TakeawayMenuId = restaurant.TakeawayMenu.Id == null ? 0 : restaurant.TakeawayMenu.Id,
                                             MewsIntegration = restaurant.MewsIntegration,
                                             Currency = restaurant.ISOCurrency,
                                             Timezone = restaurant.IANATimezone
                                         },
                                        
                                         NumberOfPeople = reservation.NumberOfPeople,
                                         Notes = reservation.Notes,
                                         SessionId = reservation.Session == null ? 0 : reservation.Session.Id,
                                         Telephone = reservation.Telephone,
                                         RestaurantId = restaurant.Id,
                                         InstantiatedFromId = reservation.InstantiatedFromId,
                                         Deleted = reservation.Deleted,
                                         Accepted = reservation.Accepted,
                                         Rejected = reservation.Rejected,
                                         RejectionNotice = reservation.RejectionNotice
                                     };

            List<Models.Reservation> result = new List<Models.Reservation>();

            if (result != null)
            {
                foreach (var reservation in ReservationHistory)
                {
                    bool add = true;
                    foreach (var checkin in Checkins)
                    {
                        if (checkin.Party != null && checkin.Party.Id == reservation.Id)
                        {
                            add = false;
                        }

                    }

                    if (add)
                        result.Add(reservation);
                }
            }

            return Request.CreateResponse(HttpStatusCode.OK, result);
        }
        
        public HttpResponseMessage PutReservationCheck(Models.Reservation reservation)
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
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state:" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }

            if (db.Restaurants.FirstOrDefault(rest => rest.Id == reservation.RestaurantId) == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Restaurant Not Found"));
            }

            Dictionary<string, object> ReturnDict = new Dictionary<string, object>();
            List<string> WarningMessages = new List<String>();

            /*
             * Check if model valid
             */
            if (!reservation.CheckMaxCoversPerReservation())
            {
                //return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Party size too big, please call for group booking"));
                WarningMessages.Add("Exceeds max number of covers (" + Core.Settings.Setting<string>(reservation.RestaurantId, "MaxCoversPerReservation") + ")");
            }

            if (!reservation.CheckMaxActiveReservations(reservation.Id))
            {
                //return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant is fully booked for this time"));
                WarningMessages.Add("Restaurant floor is reaching its capacity for reservations during this time.");
            }

            if (!reservation.CheckMaxActiveReservationsCovers(reservation))
            {
                //return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant is fully booked for this time"));
                WarningMessages.Add("Restaurant floor is reaching its capacity for diners during this time.");
            }

            var Restaurant = db.Restaurants.Where(rest => rest.Id == reservation.RestaurantId).FirstOrDefault();

            var reservationMinimumTime = epicuri.Core.Settings.Setting<int>(Restaurant.Id, "ReservationMinimumTime");
            var rmtUnix = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(DateTime.UtcNow.AddMinutes(reservationMinimumTime));

            if (reservation.ReservationTime < rmtUnix)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Booking time too soon"));
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
                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant not accepting reservations for this time."));
                }

                /*
                if (!constraint.Matches())
                {
                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant not accepting reservations for this time."));
                }
                */
            }


            if (String.IsNullOrWhiteSpace(reservation.Notes))
            {
                reservation.Notes = "";
            }
            Reservation r = new Reservation
            {
                ReservationTime = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(reservation.ReservationTime),
                Name = customer.Name.Firstname + " " + customer.Name.Surname,
                NumberOfPeople = reservation.NumberOfPeople,
                Notes = reservation.Notes,
                CreatedTime = DateTime.UtcNow,
                RestaurantId = reservation.RestaurantId,
                Telephone = customer.PhoneNumber,
                LeadCustomer = customer,
                Accepted = epicuri.API.Models.dbCustomer.OKToOrder(customer),
                InstantiatedFromId = reservation.InstantiatedFromId,
            };

            if (!r.Accepted)
            {
               WarningMessages.Add("Request could not be approved online by the restaurant");
            }

            ReturnDict.Add("Warning", WarningMessages);
            return Request.CreateResponse(HttpStatusCode.OK, ReturnDict);
        }

    }
}

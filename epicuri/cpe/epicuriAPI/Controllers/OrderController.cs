using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using epicuri.Core.DatabaseModel;
using System.Data.Objects.SqlClient;
using System.Globalization;

namespace epicuri.API.Controllers
{
    public class OrderController : Support.APIController
    {
        [HttpPost]
        [ActionName("Takeaway")]
        public HttpResponseMessage PostOrder(Models.TakeAwayOrder order)
        {
            try
            {
                Authenticate();
            }
            catch(Exception e)
            {
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, e);
                response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                return response;
            }

            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state:" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }

            decimal surcharge = 0;
            bool badPostcode = false;

            if (order.Delivery)
            {
                bool orderAccepted = order.Accepted;

                if (!order.CheckDeliveryRadius())
                {
                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Exceeds max delivery radius (" + Core.Settings.Setting<string>(order.RestaurantId, "MaxDeliveryRadius") + " miles)"));
                }

                if (!order.Accepted)
                {
                    badPostcode = true;
                    order.Accepted = false;
                }
                /*
                if (badPostcode && !order.Accepted)
                {
                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Unrecognized Postcode"));
                }*/

                if (!order.CheckDeliverySurcharge())
                {
                    surcharge += Core.Settings.Setting<decimal>(order.RestaurantId, "DeliverySurcharge");
                }

                if (orderAccepted && order.Accepted)
                {
                    order.Accepted = true;
                }
            }


            var Restaurant = db.Restaurants.Where(r => r.Id == order.RestaurantId).FirstOrDefault();

            foreach (var constraint in Restaurant.DateConstraints.Where(c=>c.TargetSession))
            {
                System.DateTime reservationDateTime = new System.DateTime(1970, 1, 1, 0, 0, 0, 0);

                // Add the number of seconds in UNIX timestamp to be converted.
                reservationDateTime = reservationDateTime.AddSeconds(order.RequestedTime);

                TimeZoneInfo timezone = Restaurant.GetTimeZoneInfo();

                if (constraint.Matches(timezone, reservationDateTime))
                {
                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant not accepting takeways for this time."));
                }

                /*
                if (constraint.Matches())
                {
                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant not accepting takeways for this time."));
                }
                */
            }
            

            /*
             * Create takeaway session
             */
            epicuri.Core.DatabaseModel.TakeAwaySession takeaway = null;

            takeaway = new epicuri.Core.DatabaseModel.TakeAwaySession
            {
                Started = false,
                StartTime = DateTime.UtcNow,
                DeliveryAddress = new epicuri.Core.DatabaseModel.Address
                {
                    City = customer.Address.City,
                    PostCode = customer.Address.PostCode,
                    Town = customer.Address.Town,
                    Street = customer.Address.Street,
                },
                RestaurantId = order.RestaurantId,
                Delivery = order.Delivery,
                ExpectedTime = order.GetExpectedTime(),
                Accepted = epicuri.API.Models.dbCustomer.OKToOrder(customer),
                InstantiatedFromId = order.InstantiatedFromId,
                Message = order.Notes,
                Deleted = false,
                Telephone = !String.IsNullOrWhiteSpace(order.Telephone) ? order.Telephone : customer.PhoneNumber
            };

            if (order.Delivery)
            {
                takeaway.DeliveryAddress = new epicuri.Core.DatabaseModel.Address
                        {
                            City = order.Address.City,
                            PostCode = order.Address.PostCode,
                            Town = order.Address.Town,
                            Street = order.Address.Street,
                        };
            }
            var dd = new Diner
            {
                Customer = customer,
                IsTable=false,
                
            };
            takeaway.Diner = dd;

            DateTime batchStamp = DateTime.UtcNow;

            double minutesToSubtract = Core.Settings.Setting<double>(takeaway.RestaurantId, "TakeawayLockWindow");
            DateTime spoolTime = takeaway.ExpectedTime.AddMinutes(-minutesToSubtract);

            var printer = db.Printers.FirstOrDefault(p => p.Id == Restaurant.TakeawayPrinterId.Value);

            Batch thebatch = new Batch
            {
                OrderTime = takeaway.ExpectedTime,
                Ident = "Takeaway",
                PrinterId = Restaurant.TakeawayPrinterId.Value,
                SpoolTime = spoolTime,
            };

            // Mark batch printed if logical printer
            if (string.IsNullOrEmpty(printer.IP))
                thebatch.PrintedTime = DateTime.UtcNow;

            // Mark batch printed if logical printer
            if (string.IsNullOrEmpty(printer.IP))
                thebatch.PrintedTime = DateTime.UtcNow;

            //Restaurant.Batches.Add(batches[menuItem.PrinterId]);
            Restaurant.Batches.Add(thebatch);

             decimal runningTotal = 0;
            /*
             * For each order do validation
             */
                foreach (Models.Order orderPayload in order.Items)
                {


                    /*
                     * Check the Quantity is not 0
                     */
                    if (orderPayload.Quantity < 1)
                    {
                        return Request.CreateResponse(HttpStatusCode.NotAcceptable, new Exception("Quantity cannot be 0"));
                    }

                    /*
                     * Check Diner Exists
                     */
                    Diner diner = dd;
                    
                    /*
                     * Check Menu Item Exists
                     */
                    var menuItem = db.MenuItems.FirstOrDefault(item => item.Id == orderPayload.MenuItemId && item.RestaurantId == order.RestaurantId);

                    if (menuItem == null)
                    {
                        return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Menu item not found"));
                    }

                    /*
                     * Check Course Exists
                     */
                    List<Modifier> modifiers = new List<Modifier>();
                    if (orderPayload.Modifiers != null)
                    {

                        Dictionary<int, int> UsedGroups = new Dictionary<int, int>();


                        foreach (int Modifier in orderPayload.Modifiers)
                        {
                            /*
                             * Check if modifer exists
                             */

                            int match = 0;
                            foreach (var modGroup in menuItem.ModifierGroups)
                            {

                                if (modGroup.Modifiers.Count(mg => mg.Id == Modifier) == 1)
                                {
                                    match = 1;

                                    /*
                                     * Increment or create a dictionary entry for this group
                                     */
                                    if (UsedGroups.ContainsKey(modGroup.Id))
                                    {
                                        UsedGroups[modGroup.Id]++;
                                    }
                                    else
                                    {
                                        UsedGroups[modGroup.Id] = 1;
                                    }

                                    /*
                                     * Multiple items from this group - can we add some more
                                     */
                                    if (modGroup.UpperLimit >= UsedGroups[modGroup.Id])
                                    {
                                        modifiers.Add(db.Modifiers.Single(m => m.Id == Modifier));
                                    }
                                    else
                                    {
                                        return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Modifier exceeds limit in group"));
                                    }



                                    break;
                                }


                            }

                            if (match == 0)
                            {
                                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Modifier " + Modifier + " cannot be found in any of this item's modifier groups"));
                            }


                        }
                    }

                    
                    for (int i = 0; i < orderPayload.Quantity; i++)
                    {
                        if (String.IsNullOrWhiteSpace(orderPayload.Note))
                        {
                            orderPayload.Note = "";
                        }
                        
                        Order o = new Order
                        {
                            //CourseId hardcoded to the global takeaway course
                            CourseId = 1,
                            MenuItem = menuItem,
                            Note = orderPayload.Note,
                            SessionId = takeaway.Id,
                            PriceOverride = null,
                            BatchId = thebatch.Id,
                            Diner = dd,
                            InstantiatedFromId = orderPayload.InstantiatedFromId,
                            OrderTime = batchStamp
                        };
                        diner.Orders.Add(o);

                        foreach (Modifier mod in modifiers)
                        {
                            o.Modifiers.Add(mod);
                            runningTotal +=(decimal) mod.Cost;
                        }

                        runningTotal += (decimal)menuItem.Price;

                    }


                }

                order.ExpectedTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(takeaway.ExpectedTime);
                order.Accepted = takeaway.Accepted;
                order.Id = takeaway.Id;
                order.Total = runningTotal + surcharge;

                string RejectionNotices = null;

                if (!order.CheckMinPrice())
                {

                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Does not meet minimum order value (" + Core.Settings.Setting<decimal>(order.RestaurantId, "MinTakeawayValue").FormatCurrency(Restaurant.ISOCurrency)+")"));
                }

                var takeawayMinimumTime = epicuri.Core.Settings.Setting<int>(order.RestaurantId, "TakeawayMinimumTime");
                var rmtUnix = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(DateTime.UtcNow.AddMinutes(takeawayMinimumTime));

                if (order.RequestedTime < rmtUnix)
                {
                    if (!String.IsNullOrEmpty(RejectionNotices))
                        RejectionNotices = RejectionNotices + "\n";

                    RejectionNotices = RejectionNotices + "- Due within " + epicuri.Core.Settings.Setting<int>(order.RestaurantId, "TakeawayMinimumTime") + " mins";
                }

                //EP-313
                if (!takeaway.Accepted)
                {
                    takeaway.RejectionNotice = "\n";
                    if (!String.IsNullOrEmpty(RejectionNotices))
                        RejectionNotices = RejectionNotices + "\n";

                    //RejectionNotices = RejectionNotices + "- Request could not be approved online by the restaurant";
                    RejectionNotices = RejectionNotices + "- Customer has blackmarks. Please give careful attention.";
                    takeaway.Accepted = false;
                }

                if (!order.CheckMaxPrice())
                {
                    //return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Over value exceeds restaurant maximum"));
                    if (!String.IsNullOrEmpty(RejectionNotices))
                        RejectionNotices = RejectionNotices + "\n";

                    RejectionNotices = RejectionNotices + "- Exceeds max order value (" + Core.Settings.Setting<string>(order.RestaurantId, "MaxTakeawayValue") + " " + Restaurant.ISOCurrency + ")";
                    takeaway.Accepted = false;
                }
                

                if (badPostcode)
                {
                    if (!String.IsNullOrEmpty(RejectionNotices))
                        RejectionNotices = RejectionNotices + "\n";

                    RejectionNotices = RejectionNotices + "- Postcode unrecognised";
                    takeaway.Accepted = false;
                }

                if (!order.CheckMaxTakeawaysPerHour())
                {
                    if (!String.IsNullOrEmpty(RejectionNotices))
                        RejectionNotices = RejectionNotices + "\n";

                    RejectionNotices = RejectionNotices + "- Restaurant kitchen is reaching its capacity for takeaways during this time";
                    takeaway.Accepted = false;
                    
                }

                if (!String.IsNullOrEmpty(RejectionNotices))
                    takeaway.RejectionNotice = RejectionNotices;

                

                db.SaveChanges();

                return GetTakeaway(takeaway.Id);
        
        }

      
        [ActionName("Takeaway")]
        public HttpResponseMessage GetTakeaways()
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

            //var MaxTime = DateTime.UtcNow.AddHours(2);
            var OldTakeaways = DateTime.UtcNow.AddHours(-2);

            var SessionHistory = from session in db.Sessions.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>()
                                 where session.Diner.CustomerId == customer.Id
                                 && session.ExpectedTime > OldTakeaways
                                 join restaurant in db.Restaurants on session.RestaurantId equals restaurant.Id
                                 select new Models.Session
                                 {
                                     Id = session.Id,
                                     _Time = session.StartTime,
                                     _ExTime = session.ExpectedTime,
                                     _CTime = session.ClosedTime,
                                     Accepted = session.Accepted,
                                     Rejected = session.Rejected,
                                     Delivery = session.Delivery,
                                     Notes = session.Message,
                                     Deleted = session.Deleted,
                                     RejectionNotice = session.RejectionNotice,
                                     Restaurant = new Models.Restaurant
                                     {
                                         Id = restaurant.Id,
                                         Name = restaurant.Name,
                                         Address = restaurant.Address,
                                         Description = restaurant.Description,
                                         Email = restaurant.PublicEmailAddress,
                                         PhoneNumber = restaurant.PhoneNumber,
                                         Position = restaurant.Position,
                                         TakeawayMenuId = restaurant.TakeawayMenu.Id,
                                         EnabledForDiner = restaurant.Enabled && !(
                                                restaurant.TakeawayOffered == 0
                                                || (restaurant.TakeawayOffered == 1 && session.Delivery == false)
                                                || (restaurant.TakeawayOffered == 2 && session.Delivery == true)
                                        ),
                                        MewsIntegration = restaurant.MewsIntegration,
                                         Currency = restaurant.ISOCurrency,
                                         Timezone = restaurant.IANATimezone


                                        // EnabledForDiner works as follows:
                                        // Restaurant needs to have TakeawayOffered Enabled
                                        // Fail States:  restaurant doesn't offer takeaway
                                        //               Restaurant offers delivery, but customer asked for collection
                                        //               Restaurant offers collection, but cusomter wants delivery
                                     }
                                 };

            List<Models.Session> returnList = SessionHistory.ToList();


            foreach (Models.Session sess in returnList)
            {
                Core.DatabaseModel.TakeAwaySession session = db.Sessions.OfType<Core.DatabaseModel.TakeAwaySession>().FirstOrDefault(s => s.Id == sess.Id);

                if (session != null)
                {

                    CPE.Models.TakeawaySession ts = new CPE.Models.TakeawaySession(session);

                    ts.CalculateVals(session, ts.DeliveryCost ?? 0);

                    sess.TakeawayOrder = new Models.TakeAwayOrder()
                     {
                         Id = session.Id,
                         Notes = session.Message,
                         SubTotal = ts.SubTotal,
                         Total = ts.Total,
                         DeliveryCost = (ts.DeliveryCost ?? 0),
                         Discounts = ts.DiscountTotal,
                         ItemCount = ts.Orders.Count(),
                         Delivery = session.Delivery,
                         Accepted = session.Accepted,
                         ExpectedTime = Core.Utils.Time.DateTimeToUnixTimestamp(session.ExpectedTime),
                         RestaurantId = session.RestaurantId,
                         Deleted = session.Deleted,
                         Address = session.DeliveryAddress,
                         Items = from order in session.Orders
                                 select new Models.Order(order)

                     };

                    if (!sess.TakeawayOrder.Delivery)
                    {
                        sess.TakeawayOrder.DeliveryCost = null;
                    }
                    else
                    {
                        sess.TakeawayOrder.DeliveryCost = sess.TakeawayOrder.CheckDeliveryCost(sess.Restaurant.Id);
                    }

                    sess.Total = sess.TakeawayOrder.Total;
                    sess.SubTotal = sess.TakeawayOrder.SubTotal;
                    sess.Discounts = sess.TakeawayOrder.Discounts;
                }
            }

            return Request.CreateResponse(HttpStatusCode.OK, returnList);

        }

        [ActionName("Takeaway")]
        public HttpResponseMessage GetTakeaway(int id)
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

            Core.DatabaseModel.TakeAwaySession session = db.Sessions.OfType<Core.DatabaseModel.TakeAwaySession>().FirstOrDefault(s => s.Id == id);

            if (session == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Session not found"));
            }

            if (session.Diner.CustomerId != customer.Id)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("User not associated to this session"));
            }

            decimal runningTotal = 0;
            foreach (var order in session.Orders)
            {
                if (order.PriceOverride.HasValue)
                {
                    runningTotal += (decimal)order.PriceOverride.Value;
                }
                else
                {
                    runningTotal += (decimal) order.MenuItem.Price;

                    foreach (var mod in order.Modifiers)
                    {
                        runningTotal += (decimal)mod.Cost;
                    }
                }
            }

            CPE.Models.TakeawaySession ts = new CPE.Models.TakeawaySession(session);

            ts.CalculateVals(session, ts.DeliveryCost ?? 0);

            return Request.CreateResponse(HttpStatusCode.OK, new Models.TakeAwayOrder
            {
                Id = session.Id,
                Notes = session.Message,
                SubTotal = ts.SubTotal,
                Total = decimal.Round(ts.Total, 2, MidpointRounding.AwayFromZero),
                DeliveryCost = (ts.DeliveryCost??0),
                Discounts = ts.DiscountTotal,
                ItemCount = ts.Orders.Count(),
                Delivery = session.Delivery,
                Accepted = session.Accepted,
                ExpectedTime = Core.Utils.Time.DateTimeToUnixTimestamp(session.ExpectedTime),
                RestaurantId = session.RestaurantId,
                Deleted = session.Deleted,
                Address = session.DeliveryAddress,
                Items = from order in session.Orders
                        select new Models.Order(order)

            });
        }

        [ActionName("Takeaway")]
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

            if (session.Diner.CustomerId != customer.Id)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("User not associated to this session"));
            }

            var lockWindow = Core.Settings.Setting<double>(session.RestaurantId, "TakeawayLockWindow");

            if (session.ExpectedTime < DateTime.UtcNow.AddMinutes(lockWindow))
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Takeaway is due too soon to cancel online. Please contact the restaurant."));
            }

            // Set the deleted flag for the takeaaway
            session.Deleted = true;

            // Don't delete the order completely - just set the deleted flag 
            /*
            foreach (Order ord in session.Orders.ToList())
            {
                foreach (Modifier mod in ord.Modifiers.ToList())
                {
                    ord.Modifiers.Remove(mod);
                }
                
                db.Orders.DeleteObject(ord);
            }

            db.Sessions.DeleteObject(session);
            */

            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.OK);
        }

        [HttpPut]
        [ActionName("TakeawayCheck")]
        public HttpResponseMessage PutTakeawayCheck(Models.TakeAwayOrder order)
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
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state:" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }

            DateTime timeCheck = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(order.RequestedTime);

            var duplicateCheck = from session in db.Sessions.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>()
                                 where session.Diner.CustomerId == customer.Id &&
                                 session.ExpectedTime == timeCheck &&
                                 session.Rejected == false &&
                                 session.Deleted == false && 
                                 session.RestaurantId == order.RestaurantId
                                 select session;

            if (duplicateCheck.Count() > 0)
            {
                //EP-39
                //string returnDT = duplicateCheck.First().ExpectedTime.ToString("dd/MM/yyyy HH:mm");
                //EP-39 End

                Restaurant restaurant = db.Restaurants.FirstOrDefault(r => r.Id == order.RestaurantId);
                TimeZoneInfo timezone = restaurant.GetTimeZoneInfo();
                DateTime correctDateTime = TimeZoneInfo.ConvertTimeFromUtc(duplicateCheck.First().ExpectedTime, timezone);
                string returnDT = correctDateTime.ToString("dd/MM/yyyy HH:mm");

                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("A takeaway already exists at this venue during this time (" + returnDT + ")."));
            }


            Dictionary<string, object> DeliveryCost = new Dictionary<string, object>();
            List<string> WarningMessages = new List<String>();

            if (!order.CheckMaxTakeawaysPerHour())
            {
                //return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant is at capacity for Takeaways"));
                WarningMessages.Add("Restaurant kitchen is reaching its capacity for takeaways during this time");
            }

            var takeawayMinimumTime = epicuri.Core.Settings.Setting<int>(order.RestaurantId, "TakeawayMinimumTime");
            var rmtUnix = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(DateTime.UtcNow.AddMinutes(takeawayMinimumTime));

            if (order.RequestedTime < rmtUnix)
            {
                WarningMessages.Add("Due within " + epicuri.Core.Settings.Setting<int>(order.RestaurantId, "TakeawayMinimumTime") + " mins");
            }


            decimal surcharge = 0;
            bool badPostcode = false;

            if (order.Delivery)
            {
                bool orderAccepted = order.Accepted;

                if (!order.CheckDeliveryRadius())
                {
                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Exceeds max delivery radius (" + Core.Settings.Setting<string>(order.RestaurantId, "MaxDeliveryRadius") + " miles)"));
                }

                if (!order.Accepted)
                {
                    badPostcode = true;
                    order.Accepted = false;
                }

                if (badPostcode && !order.Accepted)
                {
                    //return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Unrecognized Postcode"));
                    WarningMessages.Add("Unrecognized Postcode");
                }

                if (!order.CheckDeliverySurcharge())
                {
                    surcharge += Core.Settings.Setting<decimal>(order.RestaurantId, "DeliverySurcharge");
                }

                if (orderAccepted && order.Accepted)
                {
                    order.Accepted = true;
                }
            }

            var Restaurant = db.Restaurants.Where(r => r.Id == order.RestaurantId).FirstOrDefault();

            foreach (var constraint in Restaurant.DateConstraints.Where(c => c.TargetSession))
            {
                System.DateTime reservationDateTime = new System.DateTime(1970, 1, 1, 0, 0, 0, 0);

                // Add the number of seconds in UNIX timestamp to be converted.
                reservationDateTime = reservationDateTime.AddSeconds(order.RequestedTime);

                TimeZoneInfo timezone = Restaurant.GetTimeZoneInfo();

                if (constraint.Matches(timezone, reservationDateTime))
                {
                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant not accepting takeways for this time."));
                }

                /*
                if (constraint.Matches())
                {
                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant not accepting takeways for this time."));
                }
                */
            }

            /*
             * Create takeaway session
             */
            epicuri.Core.DatabaseModel.TakeAwaySession takeaway = null;

            takeaway = new epicuri.Core.DatabaseModel.TakeAwaySession
            {
                Started = false,
                StartTime = DateTime.UtcNow,
                DeliveryAddress = new epicuri.Core.DatabaseModel.Address
                {
                    City = customer.Address.City,
                    PostCode = customer.Address.PostCode,
                    Town = customer.Address.Town,
                    Street = customer.Address.Street,
                },
                RestaurantId = order.RestaurantId,
                Delivery = order.Delivery,
                ExpectedTime = order.GetExpectedTime(),
                Accepted = epicuri.API.Models.dbCustomer.OKToOrder(customer),
                InstantiatedFromId = order.InstantiatedFromId,
                Deleted = false
            };

            if (order.Delivery)
            {
                takeaway.DeliveryAddress = new epicuri.Core.DatabaseModel.Address
                {
                    City = order.Address.City,
                    PostCode = order.Address.PostCode,
                    Town = order.Address.Town,
                    Street = order.Address.Street,
                };
            }
            var dd = new Diner
            {
                Customer = customer,
                IsTable = false,

            };
            takeaway.Diner = dd;

            Dictionary<int, Batch> batches = new Dictionary<int, Batch>();

            DateTime batchStamp = DateTime.UtcNow;

            decimal runningTotal = 0;
            /*
             * For each order do validation
             */
            foreach (Models.Order orderPayload in order.Items)
            {

                /*
                 * Check the Quantity is not 0
                 */
                if (orderPayload.Quantity < 1)
                {
                    return Request.CreateResponse(HttpStatusCode.NotAcceptable, new Exception("Quantity cannot be 0"));
                }

                /*
                 * Check Diner Exists
                 */
                Diner diner = dd;

                /*
                 * Check Menu Item Exists
                 */
                var menuItem = db.MenuItems.FirstOrDefault(item => item.Id == orderPayload.MenuItemId && item.RestaurantId == order.RestaurantId);

                if (menuItem == null)
                {
                    return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Menu item not found"));
                }

                try
                {
                    if (batches[menuItem.PrinterId] == null)
                    {
                        batches[menuItem.PrinterId] = new Batch
                        {
                            OrderTime = batchStamp,
                            Ident = "Takeaway order",
                            PrinterId = menuItem.PrinterId
                        };

                        // Mark batch printed if logical printer
                        var printer = db.Printers.FirstOrDefault(p => p.Id == menuItem.PrinterId);

                        if (string.IsNullOrEmpty(printer.IP))
                            batches[menuItem.PrinterId].PrintedTime = DateTime.UtcNow;

                        Restaurant.Batches.Add(batches[menuItem.PrinterId]);
                        //Restaurant.Batches.Add(batches[Restaurant.TakeawayPrinterId.Value]);

                    }
                }
                catch
                {

                    batches[menuItem.PrinterId] = new Batch
                    {

                        OrderTime = batchStamp,
                        Ident = "Takeaway order",
                        PrinterId = menuItem.PrinterId
                    };

                    // Mark batch printed if logical printer
                    var printer = db.Printers.FirstOrDefault(p => p.Id == menuItem.PrinterId);

                    if (string.IsNullOrEmpty(printer.IP))
                        batches[menuItem.PrinterId].PrintedTime = DateTime.UtcNow;

                    Restaurant.Batches.Add(batches[menuItem.PrinterId]);
                    //Restaurant.Batches.Add(batches[Restaurant.TakeawayPrinterId.Value]);
                }


                /*
                 * Check Course Exists
                 */
                List<Modifier> modifiers = new List<Modifier>();
                if (orderPayload.Modifiers != null)
                {

                    Dictionary<int, int> UsedGroups = new Dictionary<int, int>();


                    foreach (int Modifier in orderPayload.Modifiers)
                    {
                        /*
                         * Check if modifer exists
                         */

                        int match = 0;
                        foreach (var modGroup in menuItem.ModifierGroups)
                        {

                            if (modGroup.Modifiers.Count(mg => mg.Id == Modifier) == 1)
                            {
                                match = 1;

                                /*
                                 * Increment or create a dictionary entry for this group
                                 */
                                if (UsedGroups.ContainsKey(modGroup.Id))
                                {
                                    UsedGroups[modGroup.Id]++;
                                }
                                else
                                {
                                    UsedGroups[modGroup.Id] = 1;
                                }

                                /*
                                 * Multiple items from this group - can we add some more
                                 */
                                if (modGroup.UpperLimit >= UsedGroups[modGroup.Id])
                                {
                                    modifiers.Add(db.Modifiers.Single(m => m.Id == Modifier));
                                }
                                else
                                {
                                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Modifier exceeds limit in group"));
                                }



                                break;
                            }


                        }

                        if (match == 0)
                        {
                            return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Modifier " + Modifier + " cannot be found in any of this item's modifier groups"));
                        }


                    }
                }


                for (int i = 0; i < orderPayload.Quantity; i++)
                {
                    if (String.IsNullOrWhiteSpace(orderPayload.Note))
                    {
                        orderPayload.Note = "";
                    }

                    
                    Order o = new Order
                    {
                        //CourseId hardcoded to the global takeaway course
                        CourseId = 1,
                        MenuItem = menuItem,
                        Note = orderPayload.Note,
                        SessionId = takeaway.Id,
                        PriceOverride = null,
                        BatchId = batches[menuItem.PrinterId].Id,
                        Diner = dd
                    };
                    diner.Orders.Add(o);

                    foreach (Modifier mod in modifiers)
                    {
                        o.Modifiers.Add(mod);
                        runningTotal += (decimal) mod.Cost;
                    }

                    runningTotal += (decimal) menuItem.Price;
                }
            }

            order.ExpectedTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(takeaway.ExpectedTime);
            order.Accepted = takeaway.Accepted;
            order.Id = takeaway.Id;
            order.Total = runningTotal + surcharge;

            if (!order.CheckMinPrice())
            {
                // TODO Get Restaurant Defaults
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Does not meet minimum order value (" + Core.Settings.Setting<string>(order.RestaurantId, "MinTakeawayValue") + " " + Restaurant.ISOCurrency + ")"));
            }

            if (!order.CheckMaxPrice())
            {
                //return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Over value exceeds restaurant maximum"));

                // TODO Get Restaurant Defaults
                WarningMessages.Add("Exceeds max order value (" + Core.Settings.Setting<string>(order.RestaurantId, "MaxTakeawayValue") + " " + Restaurant.ISOCurrency + ")");
            }

            DeliveryCost.Add("Cost", surcharge);
            DeliveryCost.Add("Warning", WarningMessages);
            return Request.CreateResponse(HttpStatusCode.OK, DeliveryCost);

        }
    }

    public static class XCurrency
    {
        public static string FormatCurrency(this decimal amount, string currencyCode)
        {
            var culture = (from c in CultureInfo.GetCultures(CultureTypes.SpecificCultures)
                           let r = new RegionInfo(c.LCID)
                           where r != null
                           && r.ISOCurrencySymbol.ToUpper() == currencyCode.ToUpper()
                           select c).FirstOrDefault();

            if (culture == null)
                return amount.ToString("0.00");

            return string.Format(culture, "{0:C}", amount);
        }

    }
}
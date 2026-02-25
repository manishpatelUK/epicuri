using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Http;
using System.Net.Http;
using System.Net;
using epicuri.Core.DatabaseModel;

namespace epicuri.API.Controllers
{
    public class SessionController : Support.APIController
    {
        [HttpPost]
        [ActionName("ServiceRequest")]
        public HttpResponseMessage PostServiceRequest(int id)
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
             * Get diner's active session
             */
            epicuri.Core.DatabaseModel.SeatedSession sess = db.Sessions.OfType<epicuri.Core.DatabaseModel.SeatedSession>().FirstOrDefault(s => s.Id == id);

            if (sess == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Session not found"));
            }

            /*
             * Check that the diner is attached to this session
             */
            var diner = sess.Diners.FirstOrDefault(d => d.CustomerId == customer.Id);
            if (diner == null)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Customer not attached to this session"));
            }

            if (sess.AdhocNotifications.Count(n => n.Text == "Service call" && n.Created > DateTime.UtcNow.AddMinutes(-Core.Settings.Setting<int>(sess.RestaurantId, "MinTimeBetweenServiceRequests"))) > 0)
            {
                if (sess.RequestedBill)
                    return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Your bill has already been requested. Please wait " + Core.Settings.Setting<int>(sess.RestaurantId, "MinTimeBetweenServiceRequests") + " minutes before retrying"));
                else
                    return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Only one service call allowed every " + Core.Settings.Setting<int>(sess.RestaurantId, "MinTimeBetweenServiceRequests") + " minutes"));
            }

            /*
             * Create the adhoc notificaiton 
             */
            sess.AdhocNotifications.Add(new Core.DatabaseModel.AdhocNotification
            {
                Created = DateTime.UtcNow,
                Target = "waiter/action",
                Text = "Service call",

            });

            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.Created);
        }


        [HttpPost]
        [ActionName("BillRequest")]
        public HttpResponseMessage PostBillRequest(int id)
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
             * Get diner's active session
             */
            epicuri.Core.DatabaseModel.SeatedSession sess = db.Sessions.OfType<epicuri.Core.DatabaseModel.SeatedSession>().FirstOrDefault(s => s.Id == id);

            if (sess == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Session not found"));
            }

            /*
             * Check that the diner is attached to this session
             */
            var diner = sess.Diners.FirstOrDefault(d => d.CustomerId == customer.Id);
            if (diner == null)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Customer not attached to this session"));
            }

            if (sess.AdhocNotifications.Count(n => n.Text == "Requested Bill" && n.Created > DateTime.UtcNow.AddMinutes(-Core.Settings.Setting<int>(sess.RestaurantId, "MinTimeBetweenServiceRequests"))) > 0)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Your bill has already been requested. Please wait " + Core.Settings.Setting<int>(sess.RestaurantId, "MinTimeBetweenServiceRequests") + " minutes before retrying"));
            }


            /*
             * Create the adhoc notificaiton 
             */
            sess.AdhocNotifications.Add(new Core.DatabaseModel.AdhocNotification
            {
                Created = DateTime.UtcNow,
                Target = "waiter/action",
                Text = "Requested Bill",

            });

            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.Created);
        }



        [HttpPost]
        [ActionName("Order")]
        public HttpResponseMessage PostOrder(int id, Models.Order[] orders)
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
             * Get diner's active session
             */
            epicuri.Core.DatabaseModel.SeatedSession sess = db.Sessions.OfType<epicuri.Core.DatabaseModel.SeatedSession>().FirstOrDefault(s => s.Id == id);

            if (sess == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Session not found"));
            }

            // Check to see if the bill has been requested already
            if (sess.RequestedBill == true)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("The bill has been requested"));
            }

            /*
             * Check that the diner is attached to this session
             */
            var diner = sess.Diners.FirstOrDefault(d => d.CustomerId == customer.Id);
            if (diner == null)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Customer not attached to this session"));
            }

            if (orders.Length == 0)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Cannot order no items"));
            }


            var dt = DateTime.UtcNow;

            Dictionary<int, Batch> batches = new Dictionary<int, Batch>();
            foreach (Models.Order orderPayload in orders)
            {

                /*
                 * Check the Quantity is not 0
                 */
                if (orderPayload.Quantity < 1)
                {
                    return Request.CreateResponse(HttpStatusCode.NotAcceptable, new Exception("Quantity cannot be 0"));
                }




                /*
                 * Check Menu Item Exists
                 */
                var menuItem = db.MenuItems.FirstOrDefault(item => item.Id == orderPayload.MenuItemId && item.RestaurantId == sess.RestaurantId);

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

                            OrderTime = DateTime.UtcNow,
                            Ident = "Seated",
                            PrinterId = menuItem.PrinterId
                        };

                        // Mark batch printed if logical printer
                        var printer = db.Printers.FirstOrDefault(p => p.Id == menuItem.PrinterId);

                        if (string.IsNullOrEmpty(printer.IP))
                            batches[menuItem.PrinterId].PrintedTime = DateTime.UtcNow;

                        sess.Restaurant.Batches.Add(batches[menuItem.PrinterId]);

                    }
                }
                catch
                {

                    batches[menuItem.PrinterId] = new Batch
                    {

                        OrderTime = DateTime.UtcNow,
                        Ident = "Seated",
                        PrinterId = menuItem.PrinterId
                    };

                    // Mark batch printed if logical printer
                    var printer = db.Printers.FirstOrDefault(p => p.Id == menuItem.PrinterId);

                    if (string.IsNullOrEmpty(printer.IP))
                        batches[menuItem.PrinterId].PrintedTime = DateTime.UtcNow;

                    sess.Restaurant.Batches.Add(batches[menuItem.PrinterId]);
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

                DateTime batchStamp = DateTime.UtcNow;

                for (int i = 0; i < orderPayload.Quantity; i++)
                {
                    if (orderPayload.Note == null)
                    {
                        orderPayload.Note = "";
                    }

                    Order o = new Order
                    {
                        CourseId = null,
                        MenuItem = menuItem,
                        Note = orderPayload.Note,
                        SessionId = sess.Id,
                        PriceOverride = null,
                        BatchId = batches[menuItem.PrinterId].Id,
                        Diner = sess.Diners.Where(d => d.IsTable).FirstOrDefault(),
                        InstantiatedFromId = orderPayload.InstantiatedFromId,
                        OrderTime = batchStamp
                    };

                    foreach (Modifier mod in modifiers)
                    {
                        o.Modifiers.Add(mod);
                    }
                }
                db.SaveChanges();
            }

            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.Created);
        }


        [ActionName("Sessions")]
        public HttpResponseMessage GetSessions()
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
            var c = customer;
            var SessionHistory = from diner in db.Diners
                                 where diner.CustomerId == customer.Id
                                 join session in db.Sessions.OfType<epicuri.Core.DatabaseModel.SeatedSession>() on diner.SeatedSessionId equals session.Id
                                 join restaurant in db.Restaurants on session.RestaurantId equals restaurant.Id
                                 select new Models.Session
                                 {
                                     Id = session.Id,
                                     _Time = session.StartTime,
                                     RequestedBill = session.RequestedBill,
                                     _CTime = session.ClosedTime,
                                     Restaurant = new Models.Restaurant
                                     {
                                         Id=restaurant.Id,
                                         Name = restaurant.Name,
                                         Address = restaurant.Address,
                                         Description = restaurant.Description,
                                         Email = restaurant.PublicEmailAddress,
                                         PhoneNumber = restaurant.PhoneNumber,
                                         Position = restaurant.Position,
                                         TakeawayMenuId = restaurant.TakeawayMenu==null ? 0 : restaurant.TakeawayMenu.Id,
                                         MewsIntegration = restaurant.MewsIntegration,
                                         Currency = restaurant.ISOCurrency,
                                         Timezone = restaurant.IANATimezone
                                     }
                                 };

            return Request.CreateResponse(HttpStatusCode.OK, SessionHistory);
        }

        [ActionName("Takeaway")]
        public HttpResponseMessage GetTakeaway()
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

            var SessionHistory = from session in db.Sessions.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>()
                                 where session.Diner.CustomerId == customer.Id
                                 join restaurant in db.Restaurants on session.RestaurantId equals restaurant.Id
                                 select new Models.Session
                                 {
                                     Id = session.Id,
                                     _Time = session.StartTime,
                                     _CTime = session.ClosedTime,
                                     Accepted = session.Accepted,
                                     Rejected = session.Rejected,
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
                                         MewsIntegration = restaurant.MewsIntegration,
                                         Currency = restaurant.ISOCurrency,
                                         Timezone = restaurant.IANATimezone
                                     }
                                 };

            return Request.CreateResponse(HttpStatusCode.OK, SessionHistory);
        }

        public HttpResponseMessage GetSession(int id)
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

            Core.DatabaseModel.SeatedSession session = db.Sessions.OfType<Core.DatabaseModel.SeatedSession>().FirstOrDefault(s => s.Id == id);

            if (session == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Session not found"));
            }

            if (session.Diners.Count(d => d.CustomerId == customer.Id) == 0)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Session not found No Diner"));
            }
            var sf = session.Service.SelfServiceMenu;
            var dmid = session.Service.SelfServiceMenu == null ? 0 : session.Service.SelfServiceMenu.Id;

            double suggestedTip = 0;

            if (!session.TipTotal.HasValue)
            {
                suggestedTip = session.Diners.Count(diner => diner.IsTable == false) >= epicuri.Core.Settings.Setting<int>(session.RestaurantId, "CoversBeforeAutoTip") ?
                    epicuri.Core.Settings.Setting<double>(session.RestaurantId, "DefaultTipPercentage") : 0;
            }
            else
            {
                suggestedTip = session.TipTotal.Value;
            }


            double runningTotal = 0;
            foreach (var order in session.Orders)
            {
                if (order.PriceOverride.HasValue)
                {
                    runningTotal += order.PriceOverride.Value;
                }
                else
                {
                    runningTotal += order.MenuItem.Price;

                    foreach (var mod in order.Modifiers)
                    {
                        runningTotal += mod.Cost;
                    }
                }
            }

            CPE.Models.SeatedSession seated = new CPE.Models.SeatedSession(session);
            Models.Session returnSession = new Models.Session
            {
                Id = session.Id,
                Total = seated.Total,
                SubTotal = seated.SubTotal,
                Discounts = seated.DiscountTotal,
                //PriceOffset = session.PriceOffset,
                // Deprecated after Adjustments CR 12/09/14 A.M
                // PercentageOffset = session.PercentageOffset,
                SuggestedTip = seated.SuggestedTip,
                TipTotal = seated.TipTotal,
                Tips = seated.Tips,
                Restaurant = new Models.Restaurant
                    {
                        Id = session.Restaurant.Id,
                        Name = session.Restaurant.Name,
                        Address = session.Restaurant.Address,
                        Description = session.Restaurant.Description,
                        Email = session.Restaurant.PublicEmailAddress,
                        PhoneNumber = session.Restaurant.PhoneNumber,
                        Position = session.Restaurant.Position,
                        TakeawayMenuId = session.Restaurant.TakeawayMenu == null ? null : (int?) session.Restaurant.TakeawayMenu.Id,
                        MewsIntegration = session.Restaurant.MewsIntegration,
                        Currency = session.Restaurant.ISOCurrency,
                        Timezone = session.Restaurant.IANATimezone
                    },
                _CTime = session.ClosedTime,
                Diners = from diner in session.Diners
                         select new CPE.Models.Diner(diner, session),
                Time = Core.Utils.Time.DateTimeToUnixTimestamp(session.StartTime),
                SelfServiceMenuId = session.Service.MenuId1,
                RequestedBill = session.RequestedBill,
                MenuId = session.Service.MenuId,
                Orders = from order in session.Orders
                         select new Models.Order(order),
                Courses = from course in session.Service.Courses
                          select new CPE.Models.Course(course)
            };


            // Calculate total discount
            var adjustments = session.Adjustments.Where(aj => aj.AdjustmentType.Type == (int)Enums.AdjustmentTypeType.Discount && aj.Deleted == null).OrderBy(aj => aj.Created);
            returnSession.PriceOffset = returnSession.Discounts;


            var dinerList = returnSession.Diners.ToList();

            int dinerCount = 0;
            decimal sharedTotal = 0;

            foreach (CPE.Models.Diner diner in dinerList)
            {
                double dinerTotal = 0;

                foreach (int orderNum in diner.Orders)
                {
                    Models.Order order = returnSession.Orders.FirstOrDefault(o => o.Id == orderNum);

                    if (order.PriceOverride.HasValue)
                    {
                        dinerTotal += order.PriceOverride.Value;
                    }
                    else
                    {
                        dinerTotal += order.Item.Price;

                        foreach (int modNum in order.Modifiers)
                        {
                            var mod = order.ModifierDescriptions.FirstOrDefault(m => m.Id == modNum);
                            dinerTotal += mod.Price;
                        }
                    }
                }
                diner.SubTotal = (decimal)dinerTotal;
                if (!diner.IsTable)
                {
                    dinerCount++;
                }
                else
                {
                    sharedTotal = (decimal)dinerTotal;
                }
            }


            sharedTotal += returnSession.PriceOffset;
            // Deprecated after Adjustments CR 12/09/14 A.M
            // Conver the PercentageOffset to a decimal then multiply
            //sharedTotal += returnSession.Total * (returnSession.PercentageOffset / 100);

            decimal roundedAmount = Math.Floor(100 * sharedTotal / dinerCount) / 100;

            foreach (CPE.Models.Diner diner in dinerList)
            {
                if (!diner.IsTable)
                {
                    diner.SharedTotal = roundedAmount;
                }
            }

            dinerList[0].SharedTotal +=  (sharedTotal - (roundedAmount * dinerCount));

            // Return the tables for the session
            
            //epicuri.Core.DatabaseModel.CheckIn checkin = db.CheckIns.FirstOrDefault(c => c.Id == id);

            IEnumerable<int> Tables = Enumerable.Empty<int>();

            //if (checkin != null && checkin.Diner.SeatedSessionId.HasValue)
            //{
                Tables = db.Sessions.OfType<SeatedSession>()
                    .Single(s => s.Id == id)
                    .Tables
                    .Select(t => t.Id)
                    .AsEnumerable();
            //}

            returnSession.Diners = dinerList;
            returnSession.Tables = Tables;

            returnSession.ClosedMessage = Core.Settings.Setting<string>(returnSession.Restaurant.Id, "ClosedSessionMessage");
            returnSession.SocialMessage = Core.Settings.Setting<string>(returnSession.Restaurant.Id, "SocialMediaMessage");

            return Request.CreateResponse(HttpStatusCode.OK, returnSession);
            /*
            return Request.CreateResponse(HttpStatusCode.OK, new Models.Session
            {
                Id = session.Id,
                Total = runningTotal,
                PriceOffset = session.PriceOffset,
                PercentageOffset = session.PercentageOffset,
                SuggestedTip = suggestedTip,
                Restaurant = new Models.Restaurant
                    {
                        Id = session.Restaurant.Id,
                        Name = session.Restaurant.Name,
                        Address = session.Restaurant.Address,
                        Description = session.Restaurant.Description,
                        Email = session.Restaurant.PublicEmailAddress,
                        PhoneNumber = session.Restaurant.PhoneNumber,
                        Position = session.Restaurant.Position,
                        TakeawayMenuId = session.Restaurant.TakeawayMenu.Id
                    },
                _CTime = session.ClosedTime,
                Diners = from diner in session.Diners
                            select new CPE.Models.Diner(diner,session),
                Time = Core.Utils.Time.DateTimeToUnixTimestamp(session.StartTime),
                SelfServiceMenuId = session.Service.MenuId1,
                RequestedBill = session.RequestedBill,
                MenuId = session.Service.MenuId,
                Orders = from order in session.Orders
                         select new Models.Order(order),
                Courses = from course in session.Service.Courses
                          select new CPE.Models.Course(course),
            });*/
        }
    }
}
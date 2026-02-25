using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using epicuri.Core.DatabaseModel;

namespace epicuri.CPE.Controllers
{
    public class OrderController : Models.EpicuriApiController{
    
        public HttpResponseMessage GetOrders()
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



            var seated = from sess in this.Restaurant.Sessions.OfType<epicuri.Core.DatabaseModel.SeatedSession>()
                         where sess.ClosedTime == null
                         select new Models.OrderSession
                         {
                             KitchenIdentifiers = sess.Tables.First().Name,
                             Created = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(sess.StartTime),

                             Orders = (from order in sess.Orders
                                       select new Models.Order(order)).GroupBy(o => o.Course.Id)
                         };
            var takeaways = from sess in this.Restaurant.Sessions.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>()
                            where sess.ClosedTime == null
                            select new Models.OrderSession
                            {
                                KitchenIdentifiers = "Takeaway due: " + sess.ExpectedTime.Hour + ":" + sess.ExpectedTime.Minute,
                                Created = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(sess.StartTime),
                                Orders = (from order in sess.Orders
                                          select new Models.Order(order)).GroupBy(o => o.Course.Id)

                            };


            List<Models.OrderSession> orders = new List<Models.OrderSession>();
            foreach (Models.OrderSession order in seated)
            {
                orders.Add(order);
            }
            foreach (Models.OrderSession order in takeaways)
            {
                orders.Add(order);
            }

            return Request.CreateResponse(HttpStatusCode.OK, orders.OrderBy(k => k.Created));
        }

        [HttpPost]
        public HttpResponseMessage PostOrder(Models.OrderPayload[] Orders)
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
             * Check model state is ok
             */
            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.NotAcceptable, new Exception("Order model state is invalid" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }

            // Each CreateOrder returns a HttpResponseMessage. If there's an error this is returned.
            // Orders are created and added to the database within epicuri.cpe.models.Order in order to seperate model data and controller permissions
            HttpResponseMessage responseToReturn = Request.CreateResponse(HttpStatusCode.Created);

            Dictionary<int, epicuri.Core.DatabaseModel.Batch> batches = new Dictionary<int, epicuri.Core.DatabaseModel.Batch>();
           

            foreach (epicuri.CPE.Models.OrderPayload order in Orders)
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

           return responseToReturn;
            
        }

        [HttpPut]
        public HttpResponseMessage PutOrder(int id, Models.OrderPayload updateOrder)
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
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Model state is invalid"));
            }

            /*
             * Check order exists
             */
            Order oldOrder = db.Orders.FirstOrDefault(o => o.Id == id && o.MenuItem.RestaurantId == Restaurant.Id);

            if (oldOrder == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Order not found"));
            }

            if (oldOrder.Deleted)
            {
                return Request.CreateResponse(HttpStatusCode.Conflict, new Exception("Order is deleted"));
            }

            if (oldOrder.Locked)
            {
                return Request.CreateResponse(HttpStatusCode.Conflict, new Exception("Order is locked"));
            }

            /*
             * Check Diner Exists
             */
            Diner diner;
            try
            {
                diner = db.Diners.Single(d => d.Id == updateOrder.DinerId);
            }
            catch
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Diner not found"));
            }

            if (diner == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Diner not found"));
            }


            SeatedSession session = db.Sessions.OfType<SeatedSession>().Single(s => s.Id == diner.SeatedSessionId);
            if (session.RestaurantId != this.Restaurant.Id)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Cannot modifiy this diner"));
            }

            if (session.ClosedTime != null)
            {
                return Request.CreateResponse(HttpStatusCode.Conflict, new Exception("Session is closed"));
            }


            /*
             * Check Menu Item Exists
             */
            var menuItem = db.MenuItems.FirstOrDefault(item => item.Id == updateOrder.MenuItemId && item.RestaurantId == this.Restaurant.Id);

            if (menuItem == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Menu item not found"));
            }

            /*
             * Check Modifiers Exists
             */
            List<Modifier> modifiers = new List<Modifier>();
            if (updateOrder.Modifiers != null)
            {
                List<ModifierGroup> modGroups = new List<ModifierGroup>();
                Dictionary<int, int> UsedGroups = new Dictionary<int, int>();


                foreach (int Modifier in updateOrder.Modifiers)
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

                            modGroups.Add(modGroup);


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


                foreach (ModifierGroup g in menuItem.ModifierGroups)
                {
                    if (g.LowerLimit > 0)
                    {
                        if (!UsedGroups.ContainsKey(g.Id))
                        {
                            return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Modifier count is lower than lower limit in group"));
                        }
                    }
                }


                foreach (var modGroup in modGroups)
                {
                    if (UsedGroups[modGroup.Id] < modGroup.LowerLimit)
                    {
                        return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Modifier count is lower than lower limit in group"));
                    }
                }
            }


            db.SaveChanges();

            if (updateOrder.Note == null)
            {
                updateOrder.Note = "";
            }

            oldOrder.CourseId = updateOrder.CourseId;
            oldOrder.MenuItem = menuItem;
            oldOrder.Note = updateOrder.Note;
            oldOrder.SessionId = session.Id;
            oldOrder.PriceOverride = updateOrder.PriceOverride;


            oldOrder.Modifiers.Clear();

            foreach (Modifier mod in modifiers)
            {
                oldOrder.Modifiers.Add(mod);
            }


            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK);

        }

        [HttpPut]
        [ActionName("RemoveFromReports")]
        public HttpResponseMessage PutRemoveFromReports(int id)
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


            var order = Restaurant.Sessions.SelectMany(s => s.Orders).FirstOrDefault(o=>o.Id == id);

            if (order == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Order not found"));      
            }

            if (order.Session.ClosedTime == null)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Session is not closed"));
            }

            if (order.RemoveFromReports)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Order is already deleted"));
            }

            order.RemoveFromReports = true;

            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.NoContent);
        } 
          
        [HttpDelete]
        public HttpResponseMessage DeleteOrder(int id)
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
             * Check order exists
             */
            Order oldOrder = db.Orders.FirstOrDefault(o => o.Id == id && o.MenuItem.RestaurantId == Restaurant.Id);

            if (oldOrder == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Order not found"));
            }

            if (oldOrder.Locked)
            {
                return Request.CreateResponse(HttpStatusCode.Conflict, new Exception("Order is locked"));
            }

            oldOrder.Deleted = true;
            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.OK);
        }

        [HttpPut]
        [ActionName("RemoveOrderFromBill")]
        public HttpResponseMessage PutRemoveOrderFromBill(int id, Models.Removal removalReason)
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

            if (!db.AdjustmentTypes.Any(aj => aj.Id == removalReason.AdjustmentType))
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("AdjustmentType not found"));
            }

            /*
             * Check order exists
             */
            Order oldOrder = db.Orders.FirstOrDefault(o => o.Id == id && o.MenuItem.RestaurantId == Restaurant.Id);

            if (oldOrder == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Order not found"));
            }

            if (oldOrder.Locked)
            {
                return Request.CreateResponse(HttpStatusCode.Conflict, new Exception("Order is locked"));
            }
            oldOrder.AdjustmentType = db.AdjustmentTypes.Single(aj => aj.Id == removalReason.AdjustmentType);
            oldOrder.PriceOverride = 0;
            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.OK);
        }

        [HttpPut]
        [ActionName("RemoveAllOrdersFromSession")]
        public HttpResponseMessage PutRemoveAllOrdersFromSession(int id)
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
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Model state is invalid"));
            }


            // TODO: Remove the batch reference as well!


            List<Order> orders = this.Restaurant.Sessions.First(s => s.Id == id).Orders.ToList();
            List<int> batchIds = new List<int>();


            foreach (Order ord in orders)
            {
                if (!batchIds.Contains(ord.BatchId))
                {
                    batchIds.Add(ord.BatchId);
                }

                ord.Modifiers.Clear();
                db.Orders.DeleteObject(ord);
            }

            foreach (int i in batchIds)
            {
                Batch batch = Restaurant.Batches.First(b => b.Id == i);

                if (batch != null)
                {
                    db.Batches.DeleteObject(batch);
                }
            }

            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.OK);
        }
    }
}

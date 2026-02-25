using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using epicuri.Core.DatabaseModel;

namespace epicuri.CPE.Models
{
    public class Order
    {
        public int Id;
        public MenuItem MenuItem;
        public IEnumerable<Modifier> Modifiers;
        public string Note;
        public double Completed;
        public Course Course;
        public double? PriceOverride;
        public int SessionId;
        public int InstantiatedFromId;
        public String DiscountReason;


        public Staff Staff;
        public double VatRate;
        public String TaxName;
        public String TypeName;
        public DateTime OrderTime { get;  set; }

        /// <summary>
        /// Used to create an order and immediately add it to the database. Used when creating a party and immediately adding an order to avoid verification.
        /// </summary>
        /// <param name="db"></param>
        /// <param name="orderPayload"></param>
        /// <returns>Returns HttpResponseMessage to indicate success/issues</returns>

        public static HttpResponseMessage CreateOrder(HttpRequestMessage Request, 
                                                      epicuri.Core.DatabaseModel.epicuriContainer db, 
                                                      epicuri.CPE.Models.OrderPayload orderPayload,
                                                      epicuri.Core.DatabaseModel.Restaurant currentRestaurant,
                                                      epicuri.CPE.Models.Staff currentStaff,
                                                      Dictionary<int, epicuri.Core.DatabaseModel.Batch> batches)
        {

            DateTime batchStamp = DateTime.UtcNow;            

            if (orderPayload.Quantity < 1)
            {
                return Request.CreateResponse(HttpStatusCode.NotAcceptable, new Exception("Quantity cannot be 0"));
            }

            epicuri.Core.DatabaseModel.Diner diner;

            try
            {
                diner = db.Diners.Single(d => d.Id == orderPayload.DinerId);
            }
            catch
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Diner not found"));
            }

            if (diner == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Diner not found"));
            }

            epicuri.Core.DatabaseModel.Session session = db.Sessions.OfType<epicuri.Core.DatabaseModel.SeatedSession>().FirstOrDefault(s => s.Id == diner.SeatedSessionId);

            if (session == null)
            {
                session = db.Sessions.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>().FirstOrDefault(s => s.Diner.Id == diner.Id);
                if (session == null)
                {
                    return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("diner not found"));
                }
            }

            if (session.RestaurantId != currentRestaurant.Id)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Cannot modifiy this diner"));
            }

            if (session.ClosedTime != null)
            {
                return Request.CreateResponse(HttpStatusCode.Conflict, new Exception("Session is closed"));
            }

            var menuItem = db.MenuItems.FirstOrDefault(item => item.Id == orderPayload.MenuItemId && item.RestaurantId == currentRestaurant.Id);

            if (menuItem == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Menu item not found"));
            }

            string identifier = string.Empty;
            DateTime batchDT = new DateTime();

            if (session.GetType() == typeof(epicuri.Core.DatabaseModel.SeatedSession))
            {
                if (((epicuri.Core.DatabaseModel.SeatedSession)session).IsAdHoc)
                {
                    identifier = "AdHoc";

                    // Set Course to AdHoc
                    orderPayload.CourseId = -2;

                }
                else
                {
                    identifier = "Seated";
                }

                
                batchDT = batchStamp;
            }
            else
            {
                identifier = "Takeaway";
                batchDT = ((epicuri.Core.DatabaseModel.TakeAwaySession)session).ExpectedTime;
                menuItem.PrinterId = currentRestaurant.TakeawayPrinterId.Value;
            }

            // Check Course Exists

            List<epicuri.Core.DatabaseModel.Modifier> modifiers = new List<epicuri.Core.DatabaseModel.Modifier>();
            if (orderPayload.Modifiers != null)
            {

                Dictionary<int, int> UsedGroups = new Dictionary<int, int>();


                foreach (int Modifier in orderPayload.Modifiers)
                { 
                 
                    // Check if modifer exists

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
                                epicuri.Core.DatabaseModel.Modifier mod = db.Modifiers.Single(m => m.Id == Modifier);
                                modifiers.Add(mod);
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

            try
            {
                if (batches[menuItem.PrinterId] == null)
                {
                    batches[menuItem.PrinterId] = new epicuri.Core.DatabaseModel.Batch
                    {
                        OrderTime = batchDT,
                        Ident = identifier,
                        PrinterId = menuItem.PrinterId
                    };

                    // Mark batch printed if logical printer
                    var printer = db.Printers.FirstOrDefault(p => p.Id == menuItem.PrinterId);

                    if (string.IsNullOrEmpty(printer.IP))
                        batches[menuItem.PrinterId].PrintedTime = DateTime.UtcNow;

                    if (session.GetType() == typeof(SeatedSession))
                    {
                        currentRestaurant.Batches.Add(batches[menuItem.PrinterId]);
                    }
                    else
                    {
                        currentRestaurant.Batches.Add(batches[currentRestaurant.TakeawayPrinterId.Value]);
                    }

                    db.SaveChanges();

                }
                else
                {

                }
            }
            catch
            {

                batches[menuItem.PrinterId] = new epicuri.Core.DatabaseModel.Batch
                {
                    OrderTime = batchDT,
                    Ident = identifier,
                    PrinterId = menuItem.PrinterId
                };

                // Mark batch printed if logical printer
                var printer = db.Printers.FirstOrDefault(p => p.Id == menuItem.PrinterId);

                if (string.IsNullOrEmpty(printer.IP))
                    batches[menuItem.PrinterId].PrintedTime = DateTime.UtcNow;

                if (session.GetType() == typeof(epicuri.Core.DatabaseModel.SeatedSession))
                {
                    currentRestaurant.Batches.Add(batches[menuItem.PrinterId]);
                }
                else
                {
                    currentRestaurant.Batches.Add(batches[currentRestaurant.TakeawayPrinterId.Value]);
                }
                db.SaveChanges();
            }

            for (int i = 0; i < orderPayload.Quantity; i++)
            {
                if (orderPayload.Note == null)
                {
                    orderPayload.Note = "";
                }

                epicuri.Core.DatabaseModel.Order o = new epicuri.Core.DatabaseModel.Order
                {
                    CourseId = orderPayload.CourseId,
                    MenuItem = menuItem,
                    Note = orderPayload.Note,
                    SessionId = session.Id,
                    PriceOverride = orderPayload.PriceOverride,
                    BatchId = batches[menuItem.PrinterId].Id,
                    InstantiatedFromId = orderPayload.InstantiatedFromId,
                    OrderTime = DateTime.UtcNow,
                    StaffId = currentStaff.Id,
                };
                diner.Orders.Add(o);

                foreach (epicuri.Core.DatabaseModel.Modifier mod in modifiers)
                {
                    o.Modifiers.Add(mod);
                }
            }

            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.Created);

        }       

        public Order() { }
        public Order(Core.DatabaseModel.Order order)
        {
            Id = order.Id;
            Completed = order.Completed.HasValue ? epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(order.Completed.Value) : 0;
            Note = order.Note;

            //Course is null iff order type is self service
            Course = order.Course == null ? new Models.Course
            {
                Ordering=-1,
                Name="Self Service",
                Id=0,
                ServiceId = order.Session.GetType() == typeof(Core.DatabaseModel.SeatedSession) ? (order.Session as Core.DatabaseModel.SeatedSession).ServiceId : -1
            } : new Models.Course(order.Course);
            Modifiers = from mod in order.Modifiers
                        select new Models.Modifier(mod);
            
            MenuItem = new Models.MenuItem(order.MenuItem);
            PriceOverride = order.PriceOverride;
            SessionId = order.SessionId;
            InstantiatedFromId = order.InstantiatedFromId;
            VatRate = order.MenuItem.TaxType == null ? 0 : order.MenuItem.TaxType.Rate;
            TaxName = order.MenuItem.TaxType == null ? "" : order.MenuItem.TaxType.Name;
            
            //EP-476
            DiscountReason = null;
            if (order.AdjustmentType != null)
            {
                DiscountReason = order.AdjustmentType.Name;
            }
            OrderTime = order.OrderTime;


            if (order.Staff != null)
            {
                Staff = new Staff(order.Staff);
            }
        }


       

        public decimal OrderValue()
        {
            return (decimal) (PriceOverride.HasValue ? PriceOverride.Value : MenuItem.Price + Modifiers.Sum(m => m.Price));
        }


        public decimal OrderValueAfterAdjustment(Session Session)
        {

            var del = 0m;
            if (Session.GetType() == typeof(Models.TakeawaySession))
            {
                del += ((Models.TakeawaySession)Session).DeliveryCost ?? 0;
            }
            if (Session.SubTotal != 0)
                return (OrderValue() * (decimal)((Session.Total-(Session.Tips + del))/ Session.SubTotal));
            else
                return 0;

        }


        public decimal VATValue()
        {
            return (decimal)( PriceOverride.HasValue ? (PriceOverride.Value * (VatRate / (100 + VatRate))) : (MenuItem.Price * (VatRate / (100 + VatRate))) + Modifiers.Sum(m => m.Price * (m.taxrate / (100 + m.taxrate))));
        }


        public decimal VATValueAfterAdjustment(Session Session)
        {
            if (Session.SubTotal != 0)
                return (VATValue() * (decimal)((Session.Total-Session.Tips) / Session.SubTotal));
            else
                return 0;
        }
    }
}
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.Core.DatabaseModel;
using epicuri.CPE.Models;
using System.Data.Objects;

namespace epicuri.CPE.Controllers
{
    public class BusinessIntelligenceController : Models.EpicuriController
    {
        public enum FilterPeriod
        {
            Week = 0, 
            Month, 
            ThreeMonths, 
            Year,
            Forever
        };

        [RequiresAuth("")]
        public ActionResult Index(string Auth, int Period = -1, bool webView = false)
        {
            try
            {
                DoAuth(Auth);

                BusinessIntelligence bi = new BusinessIntelligence();

                bi.restaurant = this.Restaurant;

                //Dynamically changed from Query String -- TODO

                FilterPeriod period = (FilterPeriod)Period;
                int daysAdded = 0;

                switch (period)
                {
                    case FilterPeriod.Week:
                        {
                            daysAdded = 7;
                            bi.week = true;
                        }
                        break;
                    case FilterPeriod.Month:
                        {
                            daysAdded = 30;
                            bi.month = true;
                        }
                        break;
                    case FilterPeriod.ThreeMonths:
                        {
                            daysAdded = 90;
                            bi.threeMonths = true;
                        }
                        break;
                    case FilterPeriod.Year:
                        {
                            daysAdded = 365;
                            bi.year = true;
                        }
                        break;

                    case FilterPeriod.Forever:
                    default:
                        {
                            daysAdded = 1000000;
                            bi.forever = true;
                        }
                        break;
                }

                if (Period == -1)
                {
                    bi.showFilters = true;
                    bi.forever = false;
                }
                else
                {
                    bi.showFilters = false;
                }

                if (webView)
                {
                    bi.fromWebView = true;
                }
                else
                {
                    bi.fromWebView = false;
                }

                bi.filteredBy = "Filtered By: " + period.ToString();

                bi.Panel1 = new Dictionary<string, Dictionary<string, double>>();

                // Get the data for Panel 1
                foreach (DayOfWeek day in Enum.GetValues(typeof(DayOfWeek)))
                {
                    if (!bi.Panel1.ContainsKey(day.ToString()))
                        bi.Panel1.Add(day.ToString(), new Dictionary<string, double>());

                    //Average # Seated
                    // TODO 
                    int noSeated = this.Restaurant.Sessions.Count(sess =>
                        sess.StartTime.DayOfWeek == day &&
                        // EP-663
                        !sess.RemoveFromReports &&
                        sess.StartTime.AddDays(daysAdded) >= DateTime.UtcNow &&
                        sess.GetType() == typeof(Core.DatabaseModel.SeatedSession) &&
                        sess.Paid == true &&
                        sess.Orders.Count != 0
                        );

                    bi.Panel1[day.ToString()].Add("seated", noSeated);
                    
                    //Average # Collected
                    int noCollected = this.Restaurant.Sessions.Count(sess =>
                        sess.StartTime.DayOfWeek == day &&
                        // EP-663
                        !sess.RemoveFromReports &&
                        sess.StartTime.AddDays(daysAdded) >= DateTime.UtcNow &&
                        sess.Orders.Count != 0 &&
                        sess.Paid == true &&
                        sess.GetType() == typeof(Core.DatabaseModel.TakeAwaySession) &&
                        ((Core.DatabaseModel.TakeAwaySession)sess).Delivery == false
                        );

                    bi.Panel1[day.ToString()].Add("collected", noCollected);

                    //Average # Deliveries
                    int noDeliveries = this.Restaurant.Sessions.Count(sess =>
                        sess.StartTime.DayOfWeek == day &&
                        !sess.RemoveFromReports &&
                            // EP-663
                        sess.StartTime.AddDays(daysAdded) >= DateTime.UtcNow &&
                        sess.Orders.Count != 0 &&
                        sess.Paid == true &&
                        sess.GetType() == typeof(Core.DatabaseModel.TakeAwaySession) &&
                        ((Core.DatabaseModel.TakeAwaySession)sess).Delivery == true
                        );

                    bi.Panel1[day.ToString()].Add("deliveries", noDeliveries);
                }

                // Get the data for Panel 2

                bi.Panel2 = new Dictionary<string, List<KeyValuePair<Core.DatabaseModel.MenuItem, int>>>();
                // EP-663
                var orders = Restaurant.Sessions.Where(sess => !sess.RemoveFromReports && sess.StartTime.AddDays(daysAdded) >= DateTime.UtcNow && sess.Paid == true).SelectMany(sess => sess.Orders);

                var foodResult = from ord in orders
                                 where ord.MenuItem.MenuItemTypeId == (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Food
                                 group ord by ord.MenuItem into menuItems
                                 orderby menuItems.Count() descending
                                 select new KeyValuePair<Core.DatabaseModel.MenuItem, int>(menuItems.Key, menuItems.Count());

                var drinkResult = from ord in orders
                                  where ord.MenuItem.MenuItemTypeId == (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Drink
                                  group ord by ord.MenuItem into menuItems
                                  orderby menuItems.Count() descending
                                  select new KeyValuePair<Core.DatabaseModel.MenuItem, int>(menuItems.Key, menuItems.Count());

                bi.Panel2.Add("food", foodResult.Take(5).ToList());
                bi.Panel2.Add("drink", drinkResult.Take(5).ToList());

                // Get the data for Panel 3

                bi.Panel3 = new Dictionary<string, Dictionary<string, double>>();

                DateTime firstSunday = new DateTime(1753, 1, 7);

                foreach (DayOfWeek day in Enum.GetValues(typeof(DayOfWeek)))
                {
                    if (!bi.Panel3.ContainsKey(day.ToString()))
                        bi.Panel3.Add(day.ToString(), new Dictionary<string, double>());
                    // EP-663
                    var orders2 = Restaurant.Sessions
                                  .Where(sess => !sess.RemoveFromReports && sess.StartTime.DayOfWeek == day && sess.StartTime.AddDays(daysAdded) >= DateTime.UtcNow && sess.Paid == true)
                                  .SelectMany(sess => sess.Orders);

                    //   Calculating the Average Food
                    var food = from ord in orders2
                               where ord.MenuItem.MenuItemTypeId == (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Food
                               select ord;

                    int foodCount = food.Count();
                    double foodSum = food.Sum(f => f.MenuItem.Price);

                    bi.Panel3[day.ToString()].Add("food", Math.Round(foodSum/foodCount, 2));

                    // Calculation the Average Drink
                    var drink = from ord in orders2
                                where ord.MenuItem.MenuItemTypeId == (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Drink
                               select ord;

                    int drinkCount = drink.Count();
                    double drinkSum = drink.Sum(f => f.MenuItem.Price);

                    bi.Panel3[day.ToString()].Add("drink", Math.Round(drinkSum / drinkCount, 2));
                }

                // Get the data for Panel 4

                bi.Panel4 = new Dictionary<string, double>();

                // Number of Self Service orders
                var seatedSelfService = Restaurant.Sessions.Where(sess => !sess.RemoveFromReports && sess.GetType() == typeof(Core.DatabaseModel.SeatedSession) && sess.Paid == true && sess.StartTime.AddDays(daysAdded) >= DateTime.UtcNow).SelectMany(sess => sess.Orders);
                int noItems = seatedSelfService.Count(items => items.InstantiatedFromId == 1 || items.InstantiatedFromId == 2);

                // Number of Takeaway/Delviery orders
                var takeawaySelfService = from tss in Restaurant.Sessions
                                          where tss.GetType() == typeof(Core.DatabaseModel.TakeAwaySession) && (tss.InstantiatedFromId == 1 || tss.InstantiatedFromId == 2) &&
                                              // EP-663
                                          tss.StartTime.AddDays(daysAdded) >= DateTime.UtcNow &&
                                          tss.Paid == true
                                          select tss;
                int takeawayOrders = takeawaySelfService.Count();

                // Number of reservations created via the diner app -- only counts the number of reservations that have arrived (i.e ArrivedTime != Null)
                var reservationSelfService = from res in db.Parties
                                             where res.RestaurantId == Restaurant.Id &&
                                             res.ArrivedTime != null &&
                                                 // EP-663
                                             EntityFunctions.AddDays(res.Session.StartTime, daysAdded) >= DateTime.UtcNow &&
                                             (res.InstantiatedFromId == 1 || res.InstantiatedFromId == 2)
                                             select res;

                int reservations = reservationSelfService.Count();

                // Revenue via self service
                // Made up of three parts:
                //  - Session total if reservation has been made via the app
                //  - Session total if takeaway has been made via the app
                //  - Self-service Items if the session has not been made via the app

                double revenueTotal = 0;

                // Session totals for reservation/takeaway instantiated from app (parts 1 & 2)
                var restaurantSess = from sess in Restaurant.Sessions
                                     // EP-663
                                     where sess.StartTime.AddDays(daysAdded) >= DateTime.UtcNow &&
                                     !sess.RemoveFromReports &&
                                     sess.Paid == true &&
                                     sess.InstantiatedFromId != 0
                                     select sess;

                var sessArray = restaurantSess.ToArray();

                foreach (var sess in sessArray)
                {
                    // -- Get orders for that session
                    var revenueOrders = from ord in sess.Orders
                                        select ord;

                    // -- Iterated through records adding up values
                    foreach (var revenueOrd in revenueOrders)
                    {
                        if (revenueOrd.PriceOverride.HasValue)
                        {
                            revenueTotal += revenueOrd.PriceOverride.Value;
                        }
                        else
                        {
                            revenueTotal += revenueOrd.MenuItem.Price;

                            foreach (var mod in revenueOrd.Modifiers)
                            {
                                revenueTotal += mod.Cost;
                            }
                        }
                    }
                }

                // Self-service totals for reservations not made by the app

                var restaurantSess1 = from sess in Restaurant.Sessions
                                      // EP-663
                                     where sess.StartTime.AddDays(daysAdded) >= DateTime.UtcNow &&
                                     !sess.RemoveFromReports &&
                                     sess.Paid == true &&
                                     sess.InstantiatedFromId == 0 &&
                                     sess.GetType() == typeof(Core.DatabaseModel.SeatedSession)
                                     select sess;

                var sessArray1 = restaurantSess1.ToArray();

                foreach (var sess in sessArray1)
                {
                    // -- Get orders for that session
                    var revenueOrders = from ord in sess.Orders
                                        where ord.InstantiatedFromId == 1 || ord.InstantiatedFromId == 2
                                        select ord;

                    // -- Iterated through records adding up values
                    foreach (var revenueOrd in revenueOrders)
                    {
                        if (revenueOrd.PriceOverride.HasValue)
                        {
                            revenueTotal += revenueOrd.PriceOverride.Value;

                        }
                        else
                        {
                            revenueTotal += revenueOrd.MenuItem.Price;

                            foreach (var mod in revenueOrd.Modifiers)
                            {
                                revenueTotal += mod.Cost;
                            }
                        }
                    }
                }

                // Add information for the panels

                bi.Panel4.Add("Revenue via Self Service", Math.Round(revenueTotal, 2));
                bi.Panel4.Add("Menu Items via Self Service", noItems);
                bi.Panel4.Add("Takeaways/Delivery Orders", takeawayOrders);
                bi.Panel4.Add("No. of Reservations", reservations);

                return View(bi);

            }
            catch (Exception)
            {
                Console.Write("Broke");
            }

            throw new HttpException(403, "HTTP/1.1 403 Not Authorized");
        }
    }
}

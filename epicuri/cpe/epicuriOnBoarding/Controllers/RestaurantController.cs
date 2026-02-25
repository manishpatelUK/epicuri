using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.Core.DatabaseModel;
using epicuriOnBoarding.Filters;
using System.Collections;
using System.Reflection;
using System.Xml;
using System.IO;
using epicuriOnBoarding.Properties;
using Microsoft.VisualBasic.FileIO;
namespace epicuriOnBoarding.Controllers
{
    [Authorize]
    [InitializeSimpleMembership]
    public class RestaurantController : Controller
    {
        private epicuriContainer db = new epicuriContainer();

        private Dictionary<string, string> currencyList
        {
            get
            {
                XmlDocument xmlDoc = new XmlDocument();
                xmlDoc.LoadXml(Resources.ISOCurrencies);
                var currencies = xmlDoc.DocumentElement.SelectNodes("//CcyNtry").Cast<XmlNode>().Where(c => c.SelectSingleNode("CcyNm") != null && c.SelectSingleNode("Ccy") != null);

                var result = new Dictionary<string, string>();
                foreach (var currency in currencies)
                {
                    if (!result.ContainsKey(currency.SelectSingleNode("CcyNm").InnerText) && !String.IsNullOrEmpty(currency.SelectSingleNode("CcyNm").InnerText))
                        result.Add(currency.SelectSingleNode("CcyNm").InnerText, currency.SelectSingleNode("Ccy").InnerText);
                }
                return result;

            }
        }

        public Stream GenerateStreamFromString(string s)
        {
            MemoryStream stream = new MemoryStream();
            StreamWriter writer = new StreamWriter(stream);
            writer.Write(s);
            writer.Flush();
            stream.Position = 0;
            return stream;
        }

        private Dictionary<string, string> timezoneList
        {
            get
            {
                TextFieldParser parser = new TextFieldParser(GenerateStreamFromString(Resources.IANA));
                parser.TextFieldType = FieldType.Delimited;
                parser.SetDelimiters(",");
                int i = 0;
                var result = new Dictionary<string, string>();

                while (!parser.EndOfData)
                {
                    var currentRow = parser.ReadFields();

                    string key = "";
                    string value = "";

                    foreach (string currentField_loopVariable in currentRow)
                    {
                        switch (i)
                        {
                            case 0: key = currentField_loopVariable; break;
                            case 2: value = currentField_loopVariable; break;
                        }

                        i++;
                    }

                    if (!result.ContainsKey(key) && !string.IsNullOrEmpty(key))
                        result.Add(key, value);

                    i = 0;
                }

                return result;

            }
        }

        //
        // GET: /Restaurant/

        public ActionResult Index(int? page)
        {
            int pageSize = 25;
            IQueryable<epicuri.Core.DatabaseModel.Restaurant> restaurants = db.Restaurants.Where(r => r.Deleted == null).OrderBy(r => r.Name).Include("Category").Include("HeadOffice").Include("Country");
            return View(new Models.PaginatedList<Restaurant>(restaurants, page ?? 0, pageSize));
        }

        //
        // GET: /Restaurant/Details/5

        public ActionResult Details(int id = 0)
        {
            Restaurant restaurant = db.Restaurants.Single(r => r.Id == id);
            if (restaurant == null)
            {
                return HttpNotFound();
            }
            return View(restaurant);
        }

        //
        // GET: /Restaurant/Create

        public ActionResult Create()
        {
            ViewBag.CategoryId = new SelectList(db.Categories.OrderBy(c => c.Name), "Id", "Name");
            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name");
            ViewBag.ISOCurrency = new SelectList(currencyList.OrderBy(c => c.Key), "Value", "Key", "GBP");
            ViewBag.CountryId = new SelectList(db.Countries.OrderBy(c => c.Name), "Id", "Name");
            ViewBag.IANATimezone = new SelectList(timezoneList.OrderBy(c => c.Value), "Value", "Value", "Europe/London");

            return View();
        }

        //
        // POST: /Restaurant/Create

        [HttpPost]
        public ActionResult Create(Restaurant restaurant)
        {


            if (ModelState.IsValid)
            {

                if (restaurant.MewsIntegration && string.IsNullOrEmpty(restaurant.MewsAccessToken))
                {
                    ViewBag.StatusMessage = "Mews Access Token must be entered if enabled";

                    return View(restaurant);
                }



                db.Restaurants.AddObject(restaurant);

                if (Request.Files != null && Request.Files.Count > 0 && Request.Files[0].ContentLength > 0)
                {
                    // Handle upload of receipt image
                    try
                    {
                        System.IO.Directory.CreateDirectory(Server.MapPath("~/static/"));
                    }
                    catch { }

                    HttpPostedFileBase image = Request.Files[0];

                    Guid g = Guid.NewGuid();

                    System.IO.FileStream f = new System.IO.FileStream(Server.MapPath("~/static/") + g.ToString() + System.IO.Path.GetExtension(image.FileName), System.IO.FileMode.OpenOrCreate);
                    image.InputStream.CopyTo(f);
                    f.Close();
                    bool saved = false;

                    try
                    {

                        System.Drawing.Image img = System.Drawing.Image.FromFile(Server.MapPath("~/static/") + g.ToString() + System.IO.Path.GetExtension(image.FileName));

                        if (img.Height == 0)
                        {
                            throw new Exception("Receipt image corrupted");
                        }

                        if (img.Width > 384)
                        {
                            ViewBag.StatusMessage = "The image must not be more than 384px wide";
                            img.Dispose();

                            //EP-109
                            System.IO.File.Delete(f.Name);

                            ViewBag.MenuId = new SelectList(db.Menus.Where(r => r.RestaurantId == restaurant.Id), "Id", "MenuName", restaurant.MenuId);
                            ViewBag.CategoryId = new SelectList(db.Categories.OrderBy(c => c.Name), "Id", "Name", restaurant.CategoryId);
                            ViewBag.ISOCurrency = new SelectList(currencyList.OrderBy(c => c.Key), "Value", "Key", restaurant.ISOCurrency);
                            ViewBag.CountryId = new SelectList(db.Countries.OrderBy(c => c.Name), "Id", "Name", restaurant.CountryId);
                            ViewBag.IANATimezone = new SelectList(timezoneList.OrderBy(c => c.Value), "Value", "Value", restaurant.IANATimezone);

                            return View(restaurant);
                        }
                        else
                        {


                            img.Dispose();

                            // Save the restaurant so we get the Id
                            //db.SaveChanges();

                            Resource res = new Resource { CDNUrl = "http://cdn.epicuri.co.uk/" + g.ToString() + System.IO.Path.GetExtension(image.FileName), RestaurantId = restaurant.Id };
                            db.AddToResources(res);
                            // Save the resource so we get the Id
                            db.SaveChanges();

                            saved = true;

                            restaurant.ReceiptResourceId = res.Id;
                            restaurant.ReceiptResource = res;


                        }


                    }
                    catch (Exception)
                    {
                        ViewBag.StatusMessage = "There was an error saving the restaurant (due to the floorplan)";

                        // Delete the restaurant in this case
                        if (saved)
                        {
                            db.DeleteObject(restaurant);
                            db.SaveChanges();
                        }

                        // Clean up new file
                        System.IO.File.Delete(f.Name);

                        ViewBag.MenuId = new SelectList(db.Menus.Where(r => r.RestaurantId == restaurant.Id), "Id", "MenuName", restaurant.MenuId);
                        ViewBag.CategoryId = new SelectList(db.Categories.OrderBy(c => c.Name), "Id", "Name", restaurant.CategoryId);
                        ViewBag.ISOCurrency = new SelectList(currencyList.OrderBy(c => c.Key), "Value", "Key", restaurant.ISOCurrency);
                        ViewBag.CountryId = new SelectList(db.Countries.OrderBy(c => c.Name), "Id", "Name", restaurant.CountryId);
                        ViewBag.IANATimezone = new SelectList(timezoneList.OrderBy(c => c.Value), "Value", "Value", restaurant.IANATimezone);
                        ViewBag.TakeawayPrinterId = new SelectList(db.Printers.Where(p => p.RestaurantId == restaurant.Id).OrderBy(p => p.Name), "Id", "Name", restaurant.TakeawayPrinterId);
                        ViewBag.BillingPrinterId = new SelectList(db.Printers.Where(p => p.RestaurantId == restaurant.Id && !string.IsNullOrEmpty(p.IP)).OrderBy(p => p.Name), "Id", "Name", restaurant.BillingPrinterId);

                        return View(restaurant);
                    }
                }

                // --- CREATE 3 DUMMY MENUS ---

                // EP-344 Rename default Menus

                // ------ Takeaway Menu ------
                Menu takeawayMenu = new Menu();
                takeawayMenu.Active = true;
                takeawayMenu.MenuName = "Takeaway Menu";
                takeawayMenu.RestaurantId = restaurant.Id;
                takeawayMenu.LastUpdated = DateTime.UtcNow;
                db.Menus.AddObject(takeawayMenu);

                // ------ Main Menu ------
                Menu mainMenu = new Menu();
                mainMenu.Active = true;
                mainMenu.MenuName = "Main Menu";
                mainMenu.RestaurantId = restaurant.Id;
                mainMenu.LastUpdated = DateTime.UtcNow;
                db.Menus.AddObject(mainMenu);

                // ------ SelfService Menu ------
                Menu selfserviceMenu = new Menu();
                //EP-284 EP-300
                //selfserviceMenu.Active = true;
                selfserviceMenu.Active = false;

                selfserviceMenu.MenuName = "Self-Service Menu";
                selfserviceMenu.RestaurantId = restaurant.Id;
                selfserviceMenu.LastUpdated = DateTime.UtcNow;
                db.Menus.AddObject(selfserviceMenu);

                // ------ CREATE DUMMY TAKEAWAY SERVICE
                Service takeawayService = new Service
                {
                    ServiceName = "DummyTakeawayService",
                    IsTakeaway = true,
                    Notes = "This is a dummy takeaway service",
                    DefaultMenu = takeawayMenu,
                    Updated = DateTime.UtcNow
                };

                db.SaveChanges();
                return RedirectToAction("Index");
            }

            ViewBag.CategoryId = new SelectList(db.Categories, "Id", "Name", restaurant.CategoryId);
            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", restaurant.RestaurantId);
            ViewBag.ISOCurrency = new SelectList(currencyList.OrderBy(c => c.Key), "Value", "Key", restaurant.ISOCurrency);
            ViewBag.CountryId = new SelectList(db.Countries, "Id", "Name", restaurant.CountryId);
            ViewBag.IANATimezone = new SelectList(timezoneList.OrderBy(c => c.Value), "Value", "Value", restaurant.IANATimezone);
            ViewBag.TakeawayPrinterId = new SelectList(db.Printers.Where(p => p.RestaurantId == restaurant.Id), "Id", "Name", restaurant.TakeawayPrinterId);
            ViewBag.BillingPrinterId = new SelectList(db.Printers.Where(p => p.RestaurantId == restaurant.Id && !string.IsNullOrEmpty(p.IP)), "Id", "Name", restaurant.BillingPrinterId);

            return View(restaurant);
        }

        //
        // GET: /Restaurant/Edit/5

        public ActionResult Edit(int id = 0)
        {
            Restaurant restaurant = db.Restaurants.Single(r => r.Id == id);
            if (restaurant == null)
            {
                return HttpNotFound();
            }

            //EP-125
            ViewBag.MenuId = new SelectList(db.Menus.Where(r => r.RestaurantId == id && r.Deleted == null), "Id", "MenuName", restaurant.MenuId);
            ViewBag.CategoryId = new SelectList(db.Categories.OrderBy(c => c.Name), "Id", "Name", restaurant.CategoryId);
            ViewBag.RestaurantId = new SelectList(db.Restaurants.Where(r => id != r.Id), "Id", "Name", restaurant.RestaurantId);
            ViewBag.ISOCurrency = new SelectList(currencyList.OrderBy(c => c.Key), "Value", "Key", restaurant.ISOCurrency);
            ViewBag.CountryId = new SelectList(db.Countries.OrderBy(c => c.Name), "Id", "Name", restaurant.CountryId);
            ViewBag.IANATimezone = new SelectList(timezoneList.OrderBy(c => c.Value), "Value", "Value", restaurant.IANATimezone);
            ViewBag.TakeawayPrinterId = new SelectList(db.Printers.Where(p => p.RestaurantId == restaurant.Id).OrderBy(p => p.Name), "Id", "Name", restaurant.TakeawayPrinterId);
            ViewBag.BillingPrinterId = new SelectList(db.Printers.Where(p => p.RestaurantId == restaurant.Id && !string.IsNullOrEmpty(p.IP)).OrderBy(p => p.Name), "Id", "Name", restaurant.BillingPrinterId);

            if (restaurant.ReceiptResourceId != null)
            {
                string query = db.Resources.FirstOrDefault(r => r.Id == restaurant.ReceiptResourceId).CDNUrl;
                ViewData.Add("ImageURL", query);
            }

            return View(restaurant);
        }

        //
        // POST: /Restaurant/Edit/5

        [HttpPost]
        public ActionResult Edit(Restaurant restaurant)
        {
            if (ModelState.IsValid)
            {
                // Set the default ViewBag for responses
                ViewBag.MenuId = new SelectList(db.Menus.Where(r => r.RestaurantId == restaurant.Id), "Id", "MenuName", restaurant.MenuId);
                ViewBag.CategoryId = new SelectList(db.Categories.OrderBy(c => c.Name), "Id", "Name", restaurant.CategoryId);
                ViewBag.ISOCurrency = new SelectList(currencyList.OrderBy(c => c.Key), "Value", "Key", restaurant.ISOCurrency);
                ViewBag.CountryId = new SelectList(db.Countries.OrderBy(c => c.Name), "Id", "Name", restaurant.CountryId);
                ViewBag.IANATimezone = new SelectList(timezoneList.OrderBy(c => c.Value), "Value", "Value", restaurant.IANATimezone);
                ViewBag.TakeawayPrinterId = new SelectList(db.Printers.Where(p => p.RestaurantId == restaurant.Id).OrderBy(p => p.Name), "Id", "Name", restaurant.TakeawayPrinterId);
                ViewBag.BillingPrinterId = new SelectList(db.Printers.Where(p => p.RestaurantId == restaurant.Id && !string.IsNullOrEmpty(p.IP)).OrderBy(p => p.Name), "Id", "Name", restaurant.BillingPrinterId);

                if (restaurant.MewsIntegration && string.IsNullOrEmpty(restaurant.MewsAccessToken))
                {
                    ViewBag.StatusMessage = "Mews Access Token must be entered if enabled";

                    return View(restaurant);
                }

                if (restaurant.Enabled && !restaurant.EnabledForWaiter)
                {
                    ViewBag.StatusMessage = "You must enable the waiter before the diner";

                    return View(restaurant);
                }

                if (restaurant.TakeawayOffered != (int)epicuriOnBoarding.Models.TakeAwayServiceEnum.TakeAwayService.Not_Offered && !restaurant.TakeawayPrinterId.HasValue)
                {
                    ViewBag.StatusMessage = "You must select a takeaway printer if takeaway/collection is offered";

                    return View(restaurant);
                }

                if (restaurant.EnabledForWaiter)
                {
                    // Enabled for waiter is enabled - perform checks to see if has the requried information

                    var floorQuery = db.Floors.FirstOrDefault(f => f.RestaurantId == restaurant.Id);
                    var printerQuery = db.Printers.FirstOrDefault(p => p.RestaurantId == restaurant.Id);
                    var serviceQuery = db.Services.FirstOrDefault(s => s.RestaurantId == restaurant.Id);

                    string errorMessage = null;

                    if (floorQuery == null)
                    {
                        errorMessage = "There are no Floors";
                    }
                    if (printerQuery == null)
                    {
                        if (errorMessage != null)
                        {
                            errorMessage = errorMessage + ", ";
                        }
                        errorMessage = errorMessage + "There are no Printers";
                    }
                    if (serviceQuery == null)
                    {
                        if (errorMessage != null)
                        {
                            errorMessage = errorMessage + ", ";
                        }
                        errorMessage = errorMessage + "There are no Services";
                    }

                    if (floorQuery == null || printerQuery == null || serviceQuery == null)
                    {
                        ViewBag.StatusMessage = "The Restaurant is not ready for waiter release: (" + errorMessage + ")";

                        return View(restaurant);
                    }
                }

                if (!restaurant.EnabledForWaiter)
                {
                    // IF THE WAITER IS DISABLED, DISABLE THE ENABLED FOR DINER
                    restaurant.Enabled = false;

                    // IF THE WAITER IS DISABLED, REMOVE ALL STAFF AUTHKEYS FROM THE TABLE
                    var deleteStaffKeys = from sak in db.StaffAuthenticationKeys
                                          where sak.Restaurant.Id == restaurant.Id
                                          select sak;

                    foreach (var item in deleteStaffKeys)
                    {
                        db.StaffAuthenticationKeys.DeleteObject(item);
                    }
                }

                if (restaurant.TakeawayOffered == 0)
                {
                    restaurant.MenuId = null;
                }
                if (restaurant.MenuId == null)
                {
                    restaurant.TakeawayOffered = 0;
                }

                var original = db.Restaurants.FirstOrDefault(rest => rest.Id == restaurant.Id);

                db.Restaurants.Detach(original);

                db.Restaurants.Attach(restaurant);
                db.ObjectStateManager.ChangeObjectState(restaurant, EntityState.Modified);

                if (Request.Files != null && Request.Files.Count != 0 && Request.Files[0].ContentLength > 0)
                {
                    // Handle upload of receipt image
                    try
                    {
                        System.IO.Directory.CreateDirectory(Server.MapPath("~/static/"));
                    }
                    catch { }

                    HttpPostedFileBase image = Request.Files[0];

                    Guid g = Guid.NewGuid();

                    System.IO.FileStream f = new System.IO.FileStream(Server.MapPath("~/static/") + g.ToString() + System.IO.Path.GetExtension(image.FileName), System.IO.FileMode.OpenOrCreate);
                    image.InputStream.CopyTo(f);
                    f.Close();

                    try
                    {
                        System.Drawing.Image img = System.Drawing.Image.FromFile(Server.MapPath("~/static/") + g.ToString() + System.IO.Path.GetExtension(image.FileName));

                        if (img.Height == 0)
                        {
                            throw new Exception("file not image");
                        }

                        if (img.Width > 384)
                        {
                            //EP-109 Upload receipt image for restaurant doesn't work
                            //I think it does work - the images may be over 384px, and an error message isn't getting returned.


                            ViewBag.StatusMessage = "The image must not be more than 384px wide";
                            img.Dispose();

                            System.IO.File.Delete(f.Name);

                            return View(restaurant);
                        }
                        else
                        {

                            img.Dispose();

                            // Save the restaurant so we get the Id
                            //db.SaveChanges();

                            // Remove existing resource and file
                            if (original.ReceiptResourceId != null)
                            {
                                try
                                {
                                    // Removed the object from the resources table
                                    var resource = db.Resources.FirstOrDefault(r => r.Id == original.ReceiptResourceId);

                                    var imagePath = Server.MapPath("~/static/") + resource.CDNUrl.Replace("http://cdn.epicuri.co.uk/", "");

                                    db.Resources.DeleteObject(resource);

                                    //db.Resources.DeleteObject(original.ReceiptResource);

                                    System.IO.File.Delete(imagePath);
                                }
                                catch { }
                            }

                            Resource res = new Resource { CDNUrl = "http://cdn.epicuri.co.uk/" + g.ToString() + System.IO.Path.GetExtension(image.FileName), RestaurantId = restaurant.Id };
                            db.Resources.AddObject(res);

                            // Save the resource so we get the Id
                            db.SaveChanges();

                            restaurant.ReceiptResourceId = res.Id;
                            restaurant.ReceiptResource = res;

                            db.SaveChanges();
                            return RedirectToAction("Index");
                        }

                    }
                    catch (Exception e)
                    {
                        /*
                         * TODO file not image exception
                         */
                        var a = e;
                        System.IO.File.Delete(Server.MapPath("~/static/") + g.ToString() + System.IO.Path.GetExtension(image.FileName));
                    }

                }
                else
                {
                    restaurant.ReceiptResourceId = original.ReceiptResourceId;
                }

                db.SaveChanges();
                return RedirectToAction("Index");
            }

            if (restaurant.MenuId == null)
                restaurant.MenuId = 0;

            return View(restaurant);
        }
        //
        // GET: /Restaurant/Delete/5

        public ActionResult Delete(int id = 0)
        {
            Restaurant restaurant = db.Restaurants.Single(r => r.Id == id);
            if (restaurant == null)
            {
                return HttpNotFound();
            }
            return View(restaurant);
        }

        //
        // POST: /Restaurant/Delete/5

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            Restaurant restaurant = db.Restaurants.Single(r => r.Id == id);

            restaurant.Deleted = DateTime.UtcNow;

            var expireTokens = db.StaffAuthenticationKeys.Where(sak => sak.Restaurant.Id == id);

            foreach (StaffAuthenticationKey key in expireTokens)
            {
                if (key.Expires > DateTime.UtcNow)
                {
                    key.Expires = DateTime.UtcNow;
                }
            }

            //db.Restaurants.DeleteObject(restaurant);
            db.SaveChanges();
            return RedirectToAction("Index");
        }

        //
        // GET: /Restaurant/Floors/5

        public ActionResult Floors(int id)
        {
            Restaurant r = db.Restaurants.Single(a => a.Id == id);
            try
            {

                if (r == null)
                {
                    return HttpNotFound();
                }
            }
            catch { return HttpNotFound(); }

            var floors = db.Floors.Where(floor => floor.RestaurantId == id && floor.Deleted != true).Include("Restaurant");
            ViewData["id"] = id;
            ViewData["name"] = r.Name;
            return View(floors.ToList());
        }

        public ActionResult Printers(int id)
        {
            var Restaurant = db.Restaurants.Where(r => r.Id == id).FirstOrDefault();
            if (Restaurant == null)
            {
                return HttpNotFound();
            }
            else
            {

                var printers = Restaurant.Printers;
                ViewData["id"] = id;
                ViewData["name"] = Restaurant.Name;

                return View(printers.ToList());
            }
        }

        public ActionResult Services(int id)
        {
            ViewBag.RestaurantId = id;
            var services = db.Services.Where(s => s.RestaurantId == id && s.Deleted == null && s.IsTakeaway == false).Include("Restaurant").Include("DefaultMenu").Include("SelfServiceMenu");
            return View(services.ToList());
        }

        public ActionResult Notifications(int id)
        {
            ViewBag.RestaurantId = id;
            var Notifications = db.Notifications.Where(s => s.RestaurantId == id).Include("Restaurant");
            return View(Notifications.ToList());
        }

        [HttpPost]
        [ActionName("ClearOrders")]
        public ActionResult ClearOrders(int id)
        {
            var rest = db.Restaurants.Single(r => r.Id == id);

            var sessions = rest.Sessions.ToArray();
            var notificationacks = rest.Sessions.SelectMany(s => s.AdhocNotifications.SelectMany(a => a.AdhocNotificationAcks)).ToArray();

            foreach (var ack in notificationacks)
            {
                db.AdhocNotificationAcks.DeleteObject(ack);
            }


            foreach (var session in sessions)
            {

                var orders = session.Orders.ToArray();
                foreach (var order in orders)
                {
                    db.Orders.DeleteObject(order);

                    var modifiers = order.Modifiers.ToArray();
                    foreach (var mod in modifiers)
                    {
                        order.Modifiers.Remove(mod);
                    }

                }

                if (session.GetType() == typeof(epicuri.Core.DatabaseModel.SeatedSession))
                {
                    var diners = ((epicuri.Core.DatabaseModel.SeatedSession)session).Diners.ToArray();
                    foreach (var diner in diners)
                    {
                        db.Diners.DeleteObject(diner);
                    }

                    var tables = ((epicuri.Core.DatabaseModel.SeatedSession)session).Tables.ToArray();
                    foreach (var table in tables)
                    {
                        ((epicuri.Core.DatabaseModel.SeatedSession)session).Tables.Remove(table);

                    }


                    var party = ((epicuri.Core.DatabaseModel.SeatedSession)session).Party;
                    if (party != null)
                    {
                        db.Parties.DeleteObject(party);
                    }

                }

                var adjustments = session.Adjustments.ToArray();
                foreach (var adjustment in adjustments)
                {
                    db.Adjustment.DeleteObject(adjustment);
                }

                var nots = session.AdhocNotifications.ToArray();
                foreach (var adhoc in nots)
                {
                    db.AdhocNotifications.DeleteObject(adhoc);
                }

                var acks = session.NotificationAcks.ToArray();
                foreach (var ack in acks)
                {
                    db.NotificationAcks.DeleteObject(ack);
                }




                var sessacks = db.AdhocNotificationAcks.ToArray();

                //Put this back in
                var mods = db.Modifiers.Where(m => m.Orders.Any(o => o.MenuItem.RestaurantId == id)).ToArray();
                foreach (var mod in mods)
                { mod.Orders.Clear(); }
                

                var checkins = db.CheckIns.Where(c => c.Restaurant.Id == id);
                foreach (var ci in checkins)
                {
                    db.DeleteObject(ci);
                }




                if (session.CashUpDay != null)
                {
                    db.CashUpDay.DeleteObject(session.CashUpDay);
                }

                var payments = session.Payments.ToArray();
                foreach (var p in payments)
                {
                    db.Payments.DeleteObject(p);
                }

                db.Sessions.DeleteObject(session);

            }

            db.SaveChanges();
            ViewBag.Message = "All deleted!";
            return View(db.Restaurants.Where(r => r.Id == id).Single(r => r.Id == id));
        }

        [HttpGet]
        [ActionName("ClearOrders")]
        public ActionResult GetClearOrders(int id)
        {
            ViewBag.RestaurantId = id;
            return View(db.Restaurants.Single(r => r.Id == id));
        }


        public ActionResult Blackouts(int id)
        {
            var Restaurant = db.Restaurants.Where(r => r.Id == id).FirstOrDefault();
            if (Restaurant == null)
            {
                return HttpNotFound();
            }
            else
            {
                return View(new Models.BlackoutsViewModel(id, Restaurant.DateConstraints.ToList()));
            }

        }

        public ActionResult Settings(int id)
        {
            var Restaurant = db.Restaurants.Where(r => r.Id == id).FirstOrDefault();
            if (Restaurant == null)
            {
                return HttpNotFound();
            }

            var globalSettings = from d in db.DefaultSettings
                                 orderby d.SortId
                                 select d;
            var localSettings = db.Settings.Where(r => r.RestaurantId == id);

            Dictionary<String, Models.Setting> settings = new Dictionary<string, Models.Setting>();
            foreach (var globalSetting in globalSettings)
            {
                settings[globalSetting.Key] = new Models.Setting
                {
                    Value = globalSetting.Value,
                    local = false,
                    Default = globalSetting.Value,
                    Measure = globalSetting.Measure,
                    SettingDescription = globalSetting.SettingDescription
                };
            }



            foreach (var localSetting in localSettings)
            {
                settings[localSetting.Key] = new Models.Setting
                {
                    Value = localSetting.Value,
                    local = true,
                    Default = globalSettings.Where(g => g.Key == localSetting.Key).Single().Value,
                    Measure = globalSettings.Where(g => g.Key == localSetting.Key).Single().Measure,
                    SettingDescription = globalSettings.Where(g => g.Key == localSetting.Key).Single().SettingDescription
                };
            }

            return View(new Models.SettingsViewModel { Settings = settings });
        }

        [HttpPost]
        public ActionResult Settings(int id, [ModelBinder(typeof(DictionaryModelBinder))] Models.SettingsPayload Settings)
        {


            var Restaurant = db.Restaurants.Where(r => r.Id == id).FirstOrDefault();
            if (Restaurant == null)
            {
                return HttpNotFound();
            }

            var globalSettings = db.DefaultSettings;
            var localSettings = db.Settings.Where(r => r.RestaurantId == id);

            foreach (var localSetting in localSettings)
            {
                db.Settings.DeleteObject(localSetting);
            }

            foreach (var globalSetting in globalSettings)
            {
                if (Settings.Settings[globalSetting.Key].Value != null)
                {
                    if (Settings.Settings[globalSetting.Key].Value != globalSetting.Value)
                    {
                        db.Settings.AddObject(new Setting
                        {
                            Key = globalSetting.Key,
                            Value = Settings.Settings[globalSetting.Key].Value,
                            RestaurantId = id
                        });
                    }
                }
            }

            db.SaveChanges();

            return RedirectToAction("Index");




        }

        public ActionResult Staff(int id)
        {
            ViewBag.Id = id;
            var Restaurant = db.Restaurants.FirstOrDefault(r => r.Id == id);
            if (Restaurant == null)
            {
                return HttpNotFound();
            }


            return View(Restaurant.Staffs.Where(s => s.Deleted == false).Select(s => new Models.Staff(s)).ToList());
        }

        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }


    }


    public class DictionaryModelBinder : DefaultModelBinder
    {
        private static bool IsGenericDictionary(Type type)
        {
            return (type.IsGenericType && type.GetGenericTypeDefinition() == typeof(Dictionary<,>));
        }

        private void AddItemsToDictionary(IDictionary dictionary, Type dictionaryType, string modelName, ControllerContext controllerContext, ModelBindingContext bindingContext)
        {
            List<string> keys = new List<string>();
            HttpRequestBase request = controllerContext.HttpContext.Request;
            keys.AddRange(((IDictionary<string, object>)controllerContext.RouteData.Values).Keys.Cast<string>());
            keys.AddRange(request.QueryString.Keys.Cast<string>());
            keys.AddRange(request.Form.Keys.Cast<string>());

            Type dictionaryValueType = dictionaryType.GetGenericArguments()[1];
            IModelBinder dictionaryValueBinder = Binders.GetBinder(dictionaryValueType);

            foreach (string key in keys)
            {
                string dictItemKey = null;
                string valueModelName = null;

                if (!key.Equals("area", StringComparison.InvariantCultureIgnoreCase)
                    && !key.Equals("controller", StringComparison.InvariantCultureIgnoreCase)
                    && !key.Equals("action", StringComparison.InvariantCultureIgnoreCase))
                {
                    if (key.StartsWith(modelName + "[", StringComparison.InvariantCultureIgnoreCase))
                    {
                        int endIndex = key.IndexOf("]", modelName.Length + 1);
                        if (endIndex != -1)
                        {
                            dictItemKey = key.Substring(modelName.Length + 1, endIndex - modelName.Length - 1);
                            valueModelName = key.Substring(0, endIndex + 1);
                        }
                    }
                    else
                    {
                        dictItemKey = valueModelName = key;
                    }

                    if (dictItemKey != null && valueModelName != null && !dictionary.Contains(dictItemKey))
                    {
                        object dictItemValue = dictionaryValueBinder.BindModel(controllerContext,
                            new ModelBindingContext(bindingContext)
                            {
                                ModelName = valueModelName,
                                ModelMetadata = ModelMetadataProviders.Current.GetMetadataForType(() => null, dictionaryValueType)
                            });
                        if (dictItemValue != null)
                        {
                            dictionary.Add(dictItemKey, dictItemValue);
                        }
                    }
                }
            }
        }

        public override object BindModel(ControllerContext controllerContext, ModelBindingContext bindingContext)
        {
            object result = null;
            Type modelType = bindingContext.ModelType;
            string modelName = bindingContext.ModelName;

            if (IsGenericDictionary(modelType))
            {
                // The model itself is a generic dictionary.
                IDictionary dictionary = (IDictionary)CreateModel(controllerContext, bindingContext, modelType);
                AddItemsToDictionary(dictionary, modelType, modelName, controllerContext, bindingContext);
                result = dictionary;
            }
            else
            {
                // The model may contain properties that get or set generic dictionaries.
                result = base.BindModel(controllerContext, bindingContext);
                PropertyInfo[] properties = modelType.GetProperties();

                foreach (PropertyInfo property in properties)
                {
                    Type propertyType = property.PropertyType;
                    if (IsGenericDictionary(propertyType))
                    {
                        var dictionary = (IDictionary)Activator.CreateInstance(propertyType);
                        AddItemsToDictionary(dictionary, propertyType, modelName, controllerContext, bindingContext);
                        property.SetValue(result, dictionary, null);
                    }
                }
            }
            return result;
        }
    }
}
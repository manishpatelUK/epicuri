using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Data.Entity.Infrastructure;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web;
using System.Web.Http;
using epicuri.Core.DatabaseModel;

namespace epicuri.CPE.Controllers
{
    public class MenuItemController : Models.EpicuriApiController
    {
        [HttpPost]
        public HttpResponseMessage PostMenuItem(Models.MenuItem item)
        {
            try
            {
                Authenticate("Manager");
            }
            catch (RoleException e)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, e);
            }
            catch (Exception e)
            {
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, e);
                response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                return response;
            }


            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }

            if (db.TaxTypes.First(t => t.Id == item.TaxTypeId) == null)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Tax type not found")); 
            }


            var Printer = Restaurant.Printers.Where(p => p.Id == item.DefaultPrinter).FirstOrDefault();
            if (Printer == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Printer not found")); 
            }

            MenuItem m = new MenuItem
            {
                Name = item.Name,
                Price = item.Price,
                ImageURL = "",
                Description = item.Description,
                TaxTypeId = item.TaxTypeId,
                Printer = Printer,
                MenuItemTypeId = item.MenuItemTypeId,
                Unavailable = item.Unavailable
            };
            
            this.Restaurant.MenuItems.Add(m);

            /*
             * Check all modifier groups exist
             */
            foreach (int grp in item.ModifierGroups)
            {
                ModifierGroup mod = db.ModifierGroups.FirstOrDefault(g => g.RestaurantId == this.Restaurant.Id && g.Deleted == false && g.Id == grp);
                if (mod!=null)
                {
                    m.ModifierGroups.Add(mod);
                    
                }
                else
                {
                    return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Modifier group " + grp + " not found"));
                }

            }


            /*
             * Check all tags exist
             */
            foreach (int tag in item.TagIds)
            {
                MenuTag mt = db.MenuTags.FirstOrDefault(g => g.RestaurantId == this.Restaurant.Id && g.Id == tag);
                if (mt != null)
                {
                    m.MenuTags.Add(mt);
                }
                else
                {
                    return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Tag " + tag + " not found"));
                }

            }



            /*
             * Check all menu groups exist
             */
            foreach (int grp in item.MenuGroups)
            {
                MenuGroup mod = db.MenuGroups.FirstOrDefault(g => g.MenuCategory.Menu.RestaurantId == this.Restaurant.Id && g.Id == grp);
                if (mod!=null)
                {
                    m.MenuGroups.Add(mod);
                    mod.MenuCategory.Menu.LastUpdated = DateTime.UtcNow;
                }
                else
                {
                    return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Menu group " + grp + " not found"));
                }
                mod.MenuCategory.Menu.LastUpdated = DateTime.UtcNow;
            }

            

            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.Created, new Models.MenuItem(m));
        }


        [HttpPut]
        public HttpResponseMessage PutMenuItem(int id, Models.MenuItem item)
        {
            try
            {
                Authenticate("Manager");
            }
            catch (RoleException e)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, e);
            }
            catch (Exception e)
            {
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, e);
                response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                return response;
            }


            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }


            /*
             * Check item exists
             */
            var Item = db.MenuItems.Where(i=>i.Id == id && !i.Deleted).FirstOrDefault();
            if (Item == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Menuitem not found"));
            }


            /*
             * Check tax type
             */
            if (db.TaxTypes.First(t => t.Id == item.TaxTypeId) == null)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Tax type not found")); 
            }

            var Printer = Restaurant.Printers.Where(p => p.Id == item.DefaultPrinter).FirstOrDefault();
            if (Printer == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Printer not found"));
            }
            
            MenuItem m = new MenuItem
            {
                Name = item.Name,
                Price = item.Price,
                ImageURL = "",
                Description = item.Description,
                TaxTypeId = item.TaxTypeId,
                Printer = Printer,
                MenuItemTypeId = item.MenuItemTypeId,
                Unavailable = item.Unavailable
            };

            this.Restaurant.MenuItems.Add(m);

            /*
             * Check all modifier groups exist
             */
            foreach (int grp in item.ModifierGroups)
            {
                ModifierGroup mod = db.ModifierGroups.FirstOrDefault(g => g.RestaurantId == this.Restaurant.Id && g.Deleted == false && g.Id == grp);
                if (mod!=null)
                {
                    m.ModifierGroups.Add(mod);
                }
                else
                {
                    return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Modifier group " + grp + " not found"));
                }

            }


            /*
             * Check all tags exist
             */
            foreach (int tag in item.TagIds)
            {
                MenuTag mt = db.MenuTags.FirstOrDefault(g => g.RestaurantId == this.Restaurant.Id && g.Id == tag);
                if (mt != null)
                {
                    m.MenuTags.Add(mt);
                }
                else
                {
                    return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Tag " + tag + " not found"));
                }

            }



            /*
             * Check all menu groups exist
             */

            // Skip unless a parameter is provided
            if (item.MenuGroups != null && item.MenuGroups.Count() != 0)
            {
                foreach (int grp in item.MenuGroups)
                {
                    MenuGroup mod = db.MenuGroups.FirstOrDefault(g => g.MenuCategory.Menu.RestaurantId == this.Restaurant.Id && g.Id == grp);
                    if (mod != null)
                    {
                        m.MenuGroups.Add(mod);
                        mod.MenuCategory.Menu.LastUpdated = DateTime.UtcNow;
                    }
                    else
                    {
                        return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Menu group " + grp + " not found"));
                    }
                    mod.MenuCategory.Menu.LastUpdated = DateTime.UtcNow;
                }
            }
            else
            {
                // Set them equal to original set
                m.MenuGroups.Clear();
                
                foreach (var menuItem in Item.MenuGroups)
                {
                    m.MenuGroups.Add(menuItem);
                }

            }
            
            Item.Deleted = true;
            
            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.Created, new Models.MenuItem(m));
        }


        [HttpDelete]
        public HttpResponseMessage DeleteMenuItem(int id)
        {
            try
            {
                Authenticate("Manager");
            }
            catch (RoleException e)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, e);
            }
            catch (Exception e)
            {
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, e);
                response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                return response;
            }


            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }


            /*
             * Check item exists
             */
            var Item = db.MenuItems.Where(i => i.Id == id && !i.Deleted).FirstOrDefault();
            if (Item == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Menuitem not found"));
            }

            foreach (var grp in Item.MenuGroups)
            {
                grp.MenuCategory.Menu.LastUpdated = DateTime.UtcNow;
            }

            Item.Deleted = true;
            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK);
            

        }
        
        [HttpGet]
        public HttpResponseMessage GetMenuItems(bool orphaned = false)
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


            if (!orphaned) {
                return Request.CreateResponse(HttpStatusCode.OK, Restaurant.MenuItems.Where(m=>!m.Deleted).Select(m=>new Models.MenuItem(m)).ToList());
            }
            else {

                var result = from MenuItem in Restaurant.MenuItems
                         where MenuItem.MenuGroups.Count == 0
                         && !MenuItem.Deleted
                         select new Models.MenuItem(MenuItem);

                return Request.CreateResponse(HttpStatusCode.OK, result.ToList());
            }

            
        }
        

    }







}
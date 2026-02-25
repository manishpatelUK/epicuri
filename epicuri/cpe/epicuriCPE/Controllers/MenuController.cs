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
    public class MenuController  : Models.EpicuriApiController
    {

        //private epicuriContainer db = new epicuriContainer();

        // GET api/Menu
        [HttpGet]
        [ActionName("Menus")]
        public HttpResponseMessage GetMenus()
        {
            /*
             * Check the authentication token, resturn 401 if not authorized
             */
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
             * Get all meus for this restaurant
             */
            var menus = from menu in this.Restaurant.Menus
                        where menu.Active && menu.Deleted == null
                        select new Models.Menu(menu);

            return Request.CreateResponse(HttpStatusCode.OK, menus.AsEnumerable());
        }

        [HttpGet]
        [ActionName("AllMenus")]
        public HttpResponseMessage GetAllMenus()
        {
            /*
             * Check the authentication token, resturn 401 if not authorized
             */
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
             * Get all meus for this restaurant
             */
            var menus = from menu in this.Restaurant.Menus
                        where menu.Deleted == null
                        select new Models.Menu(menu);


            return Request.CreateResponse(HttpStatusCode.OK, menus.AsEnumerable());
        }


        // GET api/Menu/5
        [HttpGet]
        [ActionName("Menu")]
        public HttpResponseMessage GetMenu(int id)
        {
            /*
             * Check the authentication token, return a 401 if not authroized
             */
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
             * Get the menu from the database and throw 404 if its not found
             */
            var dbMenu = Restaurant.Menus.FirstOrDefault(m => m.Id == id);
            if (dbMenu == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Menu not found"));
            }


            /*
             * Create a new menu object using the info from db
             */
            var menu = new Models.Menu(dbMenu);

            return Request.CreateResponse(HttpStatusCode.OK, menu);
        }

        // POST api/Menu
        [ActionName("Menus")]
        public HttpResponseMessage PostMenu(Models.Menu menu)
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


            if (this.ModelState.IsValid)
            {
                /*
                 * Create a database menu object and add it 
                 */
                Menu m = menu.ToMenu();
                m.LastUpdated = DateTime.UtcNow;
                Restaurant.Menus.Add(m);
                db.SaveChanges();
                menu.Id = m.Id;
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Created, new Models.Menu(m));
                return response;
            }
            else
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, this.ModelState);
            }
        }

        // DELETE api/Menu/5
        [HttpDelete]
        [ActionName("Menu")]
        public HttpResponseMessage DeleteMenu(int id)
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


            Menu menu = Restaurant.Menus.Single(m => m.Id == id);
            if (menu == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound);
            }


            // Check for dependencies from the restaurant table - if in use return unable to delete message
            var RestaurantQuery = db.Restaurants.SingleOrDefault(r => r.Id == menu.RestaurantId && r.MenuId == id);
            
            // Check for dependencies from the services table - if in use return unable to delete message
            var ServicesQuery = db.Services.FirstOrDefault(s => s.RestaurantId == menu.RestaurantId && s.MenuId == id && s.Deleted == null);

       

            if (RestaurantQuery == null && ServicesQuery == null)
            {
                menu.Deleted = true;
                db.SaveChanges();
                return Request.CreateResponse(HttpStatusCode.OK);
            }

            return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Unable to delete the menu (menu currently in use)."));
                

        }

        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }


        // PUT api/Menu/5
        [HttpPut]
        [ActionName("Menu")]
        public HttpResponseMessage PutMenu(int id, Models.Menu menuModel)
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


            Menu menu = Restaurant.Menus.Single(m => m.Id == id);
            if (menu == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound);
            }

            menu.MenuName = menuModel.MenuName;
            menu.LastUpdated = DateTime.UtcNow;
            menu.Active = menuModel.Active;
            db.SaveChanges();
            

            return Request.CreateResponse(HttpStatusCode.OK, new Models.Menu(menu));
        }

        // PUT api/Menu/5
        [HttpPut]
        [ActionName("ChangeTakeawayMenu")]
        public HttpResponseMessage PutChangeTakeawayMenu(int id)
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

            var menu = Restaurant.Menus.FirstOrDefault(m => m.Id == id);

            if (menu == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound);
            }

            this.Restaurant.MenuId = id;

            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK);
        }

    }
}
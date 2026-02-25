using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.API.Controllers
{
    public class MenuController : Support.APIController
    {
        [ActionName("RestaurantMenus")]
        public HttpResponseMessage GetRestaurantMenus(int id)
        {
            epicuri.Core.DatabaseModel.Restaurant restaurant;
            try
            {
                restaurant = db.Restaurants.Single(m => m.Id == id);
            }
            catch(Exception)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Restaurant not found"));
            }

            var menus = restaurant.Menus.Where(m => m.RestaurantId == id && m.Active == true && m.Deleted == null);
            
            if (menus.Count() == 0)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("No Menus found"));
            }

            var results = from menu in menus
                          select new
                          {
                              MenuId = menu.Id,
                              MenuName = menu.MenuName,
                              TakeAwayMenu = (menu == restaurant.TakeawayMenu)
                          };

            return Request.CreateResponse(HttpStatusCode.OK, results);
        }

        [ActionName("TakeawayMenu")]
        public HttpResponseMessage GetTakeawayMenu(int id)
        {
            epicuri.Core.DatabaseModel.Menu menu_main;
            try
            {
                menu_main = db.Menus.Single(m => m.Id == id);
            }
            catch(Exception)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Menu not found"));
            }

            var menu = new Models.Menu(menu_main);
           
            return Request.CreateResponse(HttpStatusCode.OK, menu);
        }
    }
}

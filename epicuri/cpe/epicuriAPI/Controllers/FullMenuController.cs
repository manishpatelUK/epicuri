using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using System.Net.Http;
using System.Net;

namespace epicuri.API.Controllers
{
    public class FullMenuController : Support.APIController
    {
         
        [HttpGet]
        public HttpResponseMessage GetMenu(int id)
        {
           
            /*
             * Get the menu from the database and throw 404 if its not found
             */
            var dbMenu = db.Menus.FirstOrDefault(m => m.Id == id);
            if (dbMenu == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Menu not found"));
            }


            /*
             * Create a new menu object using the info from db
             */
            var menu = new CPE.Models.Menu(dbMenu);

            return Request.CreateResponse(HttpStatusCode.OK, menu);
        }


    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.Core.DatabaseModel;

namespace epicuri.CPE
{
    public class RequiresAuth : ActionFilterAttribute
    {
        private String authZone = "";


        protected epicuri.Core.DatabaseModel.Device Client;
        protected Restaurant Restaurant; 

        public RequiresAuth(String zone)
        {
            this.authZone = zone;
        }

        public RequiresAuth()
        {
        }

        public override void OnActionExecuting(ActionExecutingContext filterContext)
        {
            epicuriContainer db = new epicuriContainer();           

            String key = "";

            if (filterContext.ActionParameters.Keys.Contains("Auth")&&!String.IsNullOrWhiteSpace(filterContext.ActionParameters["Auth"] as string))
            {
                key = filterContext.ActionParameters["Auth"] as string;
                filterContext.HttpContext.Session["Auth"] = key;

                
               
            }
            else
            {
                key = filterContext.HttpContext.Session["Auth"] as string;
                filterContext.ActionParameters["Auth"] = key;
                if (string.IsNullOrWhiteSpace(filterContext.HttpContext.Session["Auth"] as string))
                {
                    redirectToLogin(filterContext);
                    return;
                }
            }


            


            /*
             * Get The Auth Key from the WWW-Auth header
             */
            string AuthKey = "";
            int AuthId = 0;
            try
            {
                AuthKey = key;
                AuthId = int.Parse(AuthKey.Split('-')[0]);
                AuthKey = AuthKey.Split('-')[1];
            }
            catch
            {
                throw new Exception("No Devicekey Sent");
            }
            /*
             * If auth key is null then throw an auth exception
             */
            if (string.IsNullOrWhiteSpace(AuthKey))
            {
                throw new Exception("Device Key is null");
            }

            /*
             * Check for devices with this key
             */
            epicuri.Core.DatabaseModel.StaffAuthenticationKey k = db.StaffAuthenticationKeys.Where(kx => kx.Id == AuthId).FirstOrDefault();
            if (k == null)
            {
                throw new Exception("Key Not Found");
            }
            if (k.Key != AuthKey)
            {
                throw new Exception("Hash doesn't match");
            }

            if (k.Expires <= DateTime.UtcNow)
            {
                throw new Exception("Authentication expired");
            }

            if(k.Staff.Deleted)
            {
                throw new Exception("User is deleted");
            }
         
            //this.Restaurant = db.Staffs.Where(s=>s.AuthenticationKeys.First(ak=>ak.Id == AuthId)).First().r
            this.Restaurant = k.Restaurant;

            if (!string.IsNullOrWhiteSpace(authZone))
            {
                var staff = Restaurant.Staffs.Where(s => s.StaffAuthenticationKeys.Contains(k)).FirstOrDefault();
                if (staff.Roles.Count(r => r.Name == authZone) == 0)
                {
                    throw new RoleException("User doesnt have this role");
                }

            }

            if(Restaurant.Deleted != null)
            {
                throw new Exception("Restaurant Unavailable");
            }

            if (!Restaurant.EnabledForWaiter)
            {
                throw new Exception("Restaurant disabled");
            }


            filterContext.ActionParameters["Restaurant"] = this.Restaurant;
            
        }

        private void redirectToLogin(ActionExecutingContext filterContext)
        {
            var controller = (Models.EpicuriController)filterContext.Controller;
            filterContext.Result = controller.RedirectToAction("Login", "Auth");
        }
    }
}

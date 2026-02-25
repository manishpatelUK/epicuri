using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.Core.DatabaseModel;

namespace epicuri.CPE.Models
{
    public class EpicuriController : Controller
    {
        protected epicuriContainer db = new epicuriContainer();
        protected Restaurant Restaurant;

        public new RedirectToRouteResult RedirectToAction(string action, string controller)
        {
            return base.RedirectToAction(action, controller);
        }
        protected void DoAuth(string Auth)
        {
            /*
            * Get The Auth Key from the WWW-Auth header
            */
            string AuthKey = "";
            int AuthId = 0;
            try
            {
                AuthKey = Auth;
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
            epicuri.Core.DatabaseModel.StaffAuthenticationKey k = db.StaffAuthenticationKeys.Where(key => key.Id == AuthId).FirstOrDefault();

            if (k.Key != AuthKey)
            {
                throw new Exception("Hash doesn't match");
            }

            this.Restaurant = k.Restaurant;
        }


    }
}

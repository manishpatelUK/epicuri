using System;
using System.Linq;
using System.Web.Http;
using epicuri.Core.DatabaseModel;

namespace epicuri.API.Support
{

    public class APIController : ApiController
    {
        public APIController()
        {

        }

        protected epicuriContainer db = new epicuriContainer();
        protected epicuri.Core.DatabaseModel.Customer customer;


        protected void Authenticate()
        { 

            /*
             * Get The Auth Key from the WWW-Auth header
             */
            string AuthKey = "";
            int AuthId = 0;
            try
            {
                AuthKey = Request.Headers.Authorization.Parameter;
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
            epicuri.Core.DatabaseModel.AuthenticationKey key = db.AuthenticationKeys.Where(k => k.Id == AuthId && k.Expires > DateTime.UtcNow).FirstOrDefault();

            if (key == null)
            {
                throw new Exception("No Key Found");
            }

            if (key.Key == AuthKey)
            {
                customer = db.Customers.Single(c => c.Id == key.CustomerId);
            }

            if (customer == null)
            {
                throw new Exception("Customer not found");
            }

         

        }

        protected override void Dispose(bool disposing)
        {
            
            base.Dispose(disposing);
        }


    }
}
using System;
using System.Linq;
using System.Web.Http;
using epicuri.Core.DatabaseModel;

namespace epicuri.CPE.Models
{

    public class EpicuriApiController : ApiController
    {
        public EpicuriApiController()
        {
            
        }

        public EpicuriApiController(ApiController c)
        {
            
        }

        protected epicuriContainer db = new epicuriContainer();
        protected epicuri.Core.DatabaseModel.Device Client;
        protected Restaurant Restaurant;
        protected Staff Staff;

        protected void Authenticate()
        {
            Authenticate("");
        }
        
        protected void Authenticate(String role)
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
            epicuri.Core.DatabaseModel.StaffAuthenticationKey k = db.StaffAuthenticationKeys.Where(key => key.Id == AuthId).FirstOrDefault();
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


            this.Restaurant = k.Restaurant;
            this.Staff = new Models.Staff(k.Staff);

            
            if (!string.IsNullOrWhiteSpace(role))
            {
                var staff = Restaurant.Staffs.Where(s => s.StaffAuthenticationKeys.Contains(k)).FirstOrDefault();
                if (staff.Roles.Count(r => r.Name == role) == 0)
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
        }

        

        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }
        

    }
}
namespace epicuri {
    public class RoleException : Exception {
        public RoleException(string message) : base(message) { }
    }
}
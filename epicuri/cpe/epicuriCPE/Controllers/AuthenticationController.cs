using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using epicuri.Core.DatabaseModel;
using System.ComponentModel.DataAnnotations;
using epicuri.CPE.Models;
using epicuri.API.Support;
using System.Security.Cryptography;

namespace epicuri.CPE.Controllers
{
    public class AuthenticationController : Models.EpicuriApiController
    {

  

        [HttpPost]
        [ActionName("Login")]
        public HttpResponseMessage PostLogin(AuthPayload p) 
        {

            epicuri.Core.DatabaseModel.Staff staff;
            try
            {
                var test = db.Restaurants.FirstOrDefault(r => r.Id == p.RestaurantId && r.EnabledForWaiter == true);

                if (test != null) 
                    staff = test.Staffs.FirstOrDefault(s =>s.Deleted==false && s.Username == p.Username);
                else
                    return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Not Authenticated"));
            }
            catch (Exception ex)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Restaurant Not Found", ex));
            }

            Restaurant currentRestaurant = db.Restaurants.SingleOrDefault(r => r.Id == p.RestaurantId);

            if (currentRestaurant.Deleted != null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Restaurant Not Found"));
            }

            if (staff == null)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden);
            }

            string Salt = staff.Salt;
            string Auth = Salt + p.Password + Salt;
            Auth = epicuri.API.Support.String.SHA1(Auth);


           // Auth = epicuri.API.Support.String.GetString(result);


            if (staff.Auth == Auth)
            {
                Restaurant = db.Restaurants.Single(r => r.Id == p.RestaurantId && r.Deleted == null);
                /*
                 * Create an auth and link it to the custoemr
                 */
                StaffAuthenticationKey k = new StaffAuthenticationKey
                {
                    Key = epicuri.API.Support.String.RandomString(24),
                    Expires = DateTime.UtcNow.AddDays(30),
                    Restaurant = Restaurant
                };
                staff.StaffAuthenticationKeys.Add(k);
                db.SaveChanges();


                return Request.CreateResponse(HttpStatusCode.OK, new Models.Staff(staff, k.Id + "-" + k.Key));
            }
            return Request.CreateResponse(HttpStatusCode.Forbidden);
        }

        
    }
}


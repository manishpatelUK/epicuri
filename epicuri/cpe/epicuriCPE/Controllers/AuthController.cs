using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.CPE.Models;
using epicuri.Core.DatabaseModel;

namespace epicuri.CPE.Controllers
{
    public class AuthController : Models.EpicuriController
    {
        //
        // GET: /Auth/
        [HttpGet]
        public ActionResult Login()
        {

            return View(new AuthPayload());
        }


        [HttpPost]
        [ActionName("Login")]
        public ActionResult PostLogin(AuthPayload p)
        {
            using (epicuri.Core.DatabaseModel.epicuriContainer db = new Core.DatabaseModel.epicuriContainer())
            {

                epicuri.Core.DatabaseModel.Staff staff = db.Staffs.SingleOrDefault(stf=>stf.RestaurantId == p.RestaurantId && stf.Username == p.Username && stf.Deleted == false);
                if (staff == null)
                {
                    return View(new AuthPayload());
                }



                Restaurant currentRestaurant = db.Restaurants.SingleOrDefault(r => r.Id == p.RestaurantId);

                if (currentRestaurant.Deleted != null)
                {
                    return View(new AuthPayload());
                }

             

                string Salt = staff.Salt;
                string Auth = Salt + p.Password + Salt;
                Auth = epicuri.API.Support.String.SHA1(Auth);

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
                    Session["Auth"] = k.Id+"-"+k.Key;


                    return RedirectToAction("Index", "Portal");
                }
                return View(new AuthPayload());
            }
        }

    }
}

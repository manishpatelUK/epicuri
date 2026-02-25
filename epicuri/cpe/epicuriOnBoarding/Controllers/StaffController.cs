using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.Core.DatabaseModel;

namespace epicuriOnBoarding.Controllers
{
    public class StaffController : Controller
    {
        //
        // GET: /Staff/
        private epicuriContainer db = new epicuriContainer();

        //
        // GET: /Staff/Create

        public ActionResult Create(int id)
        {
            ViewBag.Id = id;
            return View();
        }

        //
        // POST: /Staff/Create

        [HttpPost]
        public ActionResult Create(Models.Staff staff)
        {
            try
            {
                db.AddToStaffs(staff.GetUser());
                db.SaveChanges();
                return RedirectToAction("Staff", new { controller = "Restaurant", id = staff.RestaurantId });
            }
            catch
            {
                ViewBag.Id = staff.RestaurantId;
                return View();
            }
        }

        //
        // GET: /Staff/Edit/5

        public ActionResult Edit(int id)
        {
            var Staff = db.Staffs.FirstOrDefault(s => s.Deleted == false && s.Id == id);
            if (Staff == null)
            {
                return HttpNotFound();
            }
            return View(new Models.Staff(Staff));
        }

        //
        // POST: /Staff/Edit/5

        [HttpPost]
        public ActionResult Edit(int id, Models.Staff update)
        {
            var Staff = db.Staffs.FirstOrDefault(s => s.Deleted==false && s.Id == id);

            if (Staff == null)
            {
                return HttpNotFound();


            }
            try
            {
                if (update.PasswordChanged())
                {
                    Staff.Auth = update.GetUser().Auth;
                    Staff.Salt = update.GetUser().Salt;
                }
                Staff.Pin = update.GetUser().Pin;
                Staff.Name = update.GetUser().Name;
                Staff.Username = update.GetUser().Username;

                foreach (var role in Staff.Roles.ToList())
                {
                    Staff.Roles.Remove(role);

                }

                if (update.GetUser().Roles.Any(r => r.Name == "Manager"))
                {
                    Staff.Roles.Add(new Role
                    {
                        Name = "Manager",

                    });
                }

                db.SaveChanges();
                return RedirectToAction("Staff", new { controller = "Restaurant", id = Staff.RestaurantId });
            }
            catch(Exception e)
            {
                var b = e;
                return View(new Models.Staff(Staff));
            }
        }

        //
        // GET: /Staff/Delete/5
        [HttpGet]
        [ActionName("Delete")]
        public ActionResult Delete(int id)
        {
            var Staff = db.Staffs.FirstOrDefault(s => s.Id == id && s.Deleted ==false);
            if (Staff == null)
            {
                return HttpNotFound();
            }
       
                return View(new Models.Staff(Staff));
           
        }

        //
        // DELETE: /Staff/Delete/5

        [HttpPost]
        [ActionName("Delete")]
        public ActionResult ConfirmDelete(int id)
        {
            var Staff = db.Staffs.FirstOrDefault(s => s.Id == id && s.Deleted ==false);
            try
            {
                Staff.Deleted = true;
                db.SaveChanges();

                return RedirectToAction("Staff", new { controller = "Restaurant", id = Staff.RestaurantId });
            }
            catch(Exception e)
            {
                var b = e;
                return View();
            }
        }
    }
}

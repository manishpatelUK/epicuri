using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.Core.DatabaseModel;
using System.IO;
using System.Runtime.Serialization.Formatters.Binary;

namespace epicuriOnBoarding.Controllers
{
    public class AbsoluteDateConstraintController : Controller
    {
        private epicuriContainer db = new epicuriContainer();

        //
        // GET: /AbsoluteDateConstraint/

        public ActionResult Index()
        {
            var dateconstraints = db.DateConstraints.OfType<AbsoluteDateConstraint>().Include("Restaurant");
            return View(dateconstraints.ToList());
        }

        //
        // GET: /AbsoluteDateConstraint/Details/5

        public ActionResult Details(int id = 0)
        {
            AbsoluteDateConstraint absolutedateconstraint = db.DateConstraints.OfType<AbsoluteDateConstraint>().Single(a => a.Id == id);
            if (absolutedateconstraint == null)
            {
                return HttpNotFound();
            }
            return View(absolutedateconstraint);
        }

        //
        // GET: /AbsoluteDateConstraint/Create

        public IEnumerable<SelectListItem> GetHoursList()
        {
            var blackoutHours = new List<SelectListItem>();
            for (int i = 0; i <= 24; i++)
            {
                var list = new SelectListItem();
                list.Text = i.ToString();
                list.Value = i.ToString();
                blackoutHours.Add(list);
            }
            return blackoutHours;
        }

        public IEnumerable<SelectListItem> GetMinutesList()
        {
            var blackoutMinutes = new List<SelectListItem>();

            for (int i = 0; i <= 30; i+=30)
            {
                var list = new SelectListItem();
                list.Text = i.ToString();
                list.Value = i.ToString();
                blackoutMinutes.Add(list);
            }
            return blackoutMinutes;
        }

        public ActionResult Create(int id)
        {
            ViewBag.BlackoutHours = GetHoursList();
            ViewBag.BlackoutMinutes = GetMinutesList();
            ViewBag.RestaurantId = id;

            return View();
        }

        //
        // POST: /AbsoluteDateConstraint/Create

        [HttpPost]
        public ActionResult Create(AbsoluteDateConstraint absolutedateconstraint, bool isReservation, bool isTakeaway)
        {
            if (ModelState.IsValid)
            {
                if (absolutedateconstraint.BlackoutHours == 24 && absolutedateconstraint.BlackoutMinutes == 30)
                {
                    ViewBag.BlackoutHours = GetHoursList();
                    ViewBag.BlackoutMinutes = GetMinutesList();
                    ViewBag.RestaurantId = absolutedateconstraint.RestaurantId;
                    ViewBag.StatusMessage = "Duration must not exceed 24 hours";
                    return View();
                }

                if (absolutedateconstraint.BlackoutHours == 24)
                {
                    absolutedateconstraint.BlackoutHours = 23;
                    absolutedateconstraint.BlackoutMinutes = 59;
                }

                //Convert time given into UTC
                DateTime blackoutDateTime = new DateTime();
                blackoutDateTime = absolutedateconstraint.Date + absolutedateconstraint.StartTime;

                TimeZoneInfo timezone = absolutedateconstraint.Restaurant.GetTimeZoneInfo();

                absolutedateconstraint.StartTime = TimeZoneInfo.ConvertTimeToUtc(blackoutDateTime, timezone).TimeOfDay;
                absolutedateconstraint.EndTime = absolutedateconstraint.StartTime;


                int hoursCheck = absolutedateconstraint.EndTime.Hours + absolutedateconstraint.BlackoutHours;

                // Make sure the hours isn't over 24
                //if (hoursCheck + absolutedateconstraint.BlackoutHours > 24)
                
                if (hoursCheck >= 24)
                {
                    absolutedateconstraint.EndTime -= TimeSpan.FromHours(24);
                }

                absolutedateconstraint.EndTime += TimeSpan.FromHours(absolutedateconstraint.BlackoutHours);

                // Check the minutes doesn't push the hours over 24
                if (absolutedateconstraint.EndTime.Hours == 23 && absolutedateconstraint.EndTime.Minutes == 30 && absolutedateconstraint.BlackoutMinutes == 30)
                {
                    absolutedateconstraint.EndTime -= TimeSpan.FromHours(24);
                }

                absolutedateconstraint.EndTime += TimeSpan.FromMinutes(absolutedateconstraint.BlackoutMinutes);

                if (absolutedateconstraint.BlackoutMinutes == 59)
                {
                    absolutedateconstraint.EndTime += TimeSpan.FromSeconds(59);
                }

                // Make sure the hours isn't over 24
                if (absolutedateconstraint.EndTime.Days == 1)
                {
                    absolutedateconstraint.EndTime -= TimeSpan.FromHours(24);
                }

                if (isTakeaway && isReservation)
                {
                    absolutedateconstraint.TargetSession = true;
                    db.DateConstraints.AddObject(absolutedateconstraint);

                    AbsoluteDateConstraint newDateConstraint = new AbsoluteDateConstraint();

                    using (var ms = new MemoryStream())
                    {
                        var bf = new BinaryFormatter();
                        bf.Serialize(ms, absolutedateconstraint);
                        ms.Position = 0;
                        newDateConstraint = (AbsoluteDateConstraint)bf.Deserialize(ms);
                    }

                    db.DateConstraints.AddObject(newDateConstraint);

                    newDateConstraint.TargetSession = false;
                    db.SaveChanges();

                } else if (isTakeaway)
                {
                    absolutedateconstraint.TargetSession = true;
                    db.DateConstraints.AddObject(absolutedateconstraint);
                    db.SaveChanges();
                } else if (isReservation)
                {
                    absolutedateconstraint.TargetSession = false;
                    db.DateConstraints.AddObject(absolutedateconstraint);
                    db.SaveChanges();
                }

                return RedirectToAction("Blackouts", new {controller="Restaurant",id=absolutedateconstraint.RestaurantId});
            }

            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", absolutedateconstraint.RestaurantId);
            return View(absolutedateconstraint);
        }

        //
        // GET: /AbsoluteDateConstraint/Edit/5

        public ActionResult Edit(int id = 0)
        {
            AbsoluteDateConstraint absolutedateconstraint = db.DateConstraints.OfType<AbsoluteDateConstraint>().Single(a => a.Id == id);
            if (absolutedateconstraint == null)
            {
                return HttpNotFound();
            }

            absolutedateconstraint.StartTime = absolutedateconstraint.LocalTime;

            if (absolutedateconstraint.BlackoutHours == 23 && absolutedateconstraint.BlackoutMinutes == 59)
            {
                absolutedateconstraint.BlackoutHours = 24;
                absolutedateconstraint.BlackoutMinutes = 0;
            }

            ViewBag.BlackoutHours = new SelectList(GetHoursList(), "Text", "Value", absolutedateconstraint.BlackoutHours);
            ViewBag.BlackoutMinutes = new SelectList(GetMinutesList(), "Text", "Value", absolutedateconstraint.BlackoutMinutes);
            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", absolutedateconstraint.RestaurantId);
            return View(absolutedateconstraint);
        }

        //
        // POST: /AbsoluteDateConstraint/Edit/5

        [HttpPost]
        public ActionResult Edit(AbsoluteDateConstraint absolutedateconstraint)
        {
            if (ModelState.IsValid)
            {

                if (absolutedateconstraint.BlackoutHours == 24 && absolutedateconstraint.BlackoutMinutes == 30)
                {
                    ViewBag.BlackoutHours = new SelectList(GetHoursList(), "Text", "Value", absolutedateconstraint.BlackoutHours);
                    ViewBag.BlackoutMinutes = new SelectList(GetMinutesList(), "Text", "Value", absolutedateconstraint.BlackoutMinutes);
                    ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", absolutedateconstraint.RestaurantId);
                    ViewBag.StatusMessage = "Duration must not exceed 24 hours";
                    return View(absolutedateconstraint);
                }

                if (absolutedateconstraint.BlackoutHours == 24)
                {
                    absolutedateconstraint.BlackoutHours = 23;
                    absolutedateconstraint.BlackoutMinutes = 59;
                }

                //Convert time given into UTC
                //DateTime blackoutDateTime = Convert.ToDateTime(absolutedateconstraint.StartTime.ToString());
                DateTime blackoutDateTime = new DateTime(absolutedateconstraint.Date.Year, absolutedateconstraint.Date.Month, absolutedateconstraint.Date.Day, absolutedateconstraint.StartTime.Hours, absolutedateconstraint.StartTime.Minutes, 0);

                TimeZoneInfo lst = absolutedateconstraint.Restaurant.GetTimeZoneInfo();
               
                absolutedateconstraint.StartTime = TimeZoneInfo.ConvertTimeToUtc(blackoutDateTime, lst).TimeOfDay;

                absolutedateconstraint.EndTime = absolutedateconstraint.StartTime;

                int hoursCheck = absolutedateconstraint.EndTime.Hours + absolutedateconstraint.BlackoutHours;

                // Make sure the hours isn't over 24
                if (hoursCheck >= 24)
                {
                    absolutedateconstraint.EndTime = absolutedateconstraint.EndTime - TimeSpan.FromHours(24);
                }

                absolutedateconstraint.EndTime += TimeSpan.FromHours(absolutedateconstraint.BlackoutHours);

                // Check the minutes doesn't push the hours over 24
                if (absolutedateconstraint.EndTime.Hours == 23 && absolutedateconstraint.EndTime.Minutes == 30 && absolutedateconstraint.BlackoutMinutes == 30)
                {
                    absolutedateconstraint.EndTime = absolutedateconstraint.EndTime - TimeSpan.FromHours(24);
                }

                absolutedateconstraint.EndTime += TimeSpan.FromMinutes(absolutedateconstraint.BlackoutMinutes);

                // Make sure the hours isn't over 24
                if (absolutedateconstraint.EndTime.Days == 1)
                {
                    absolutedateconstraint.EndTime -= TimeSpan.FromHours(24);
                }

                db.DateConstraints.Attach(absolutedateconstraint);
                db.ObjectStateManager.ChangeObjectState(absolutedateconstraint, EntityState.Modified);
                db.SaveChanges();
                return RedirectToAction("Blackouts", new { controller = "Restaurant", id = absolutedateconstraint.RestaurantId });
            }
            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", absolutedateconstraint.RestaurantId);
            return View(absolutedateconstraint);
        }

        //
        // GET: /AbsoluteDateConstraint/Delete/5

        public ActionResult Delete(int id = 0)
        {
            AbsoluteDateConstraint absolutedateconstraint = db.DateConstraints.OfType<AbsoluteDateConstraint>().Single(a => a.Id == id);
            if (absolutedateconstraint == null)
            {
                return HttpNotFound();
            }
            return View(absolutedateconstraint);
        }

        //
        // POST: /AbsoluteDateConstraint/Delete/5

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            AbsoluteDateConstraint absolutedateconstraint = db.DateConstraints.OfType<AbsoluteDateConstraint>().Single(a => a.Id == id);
            db.DateConstraints.DeleteObject(absolutedateconstraint);
            db.SaveChanges();
            return RedirectToAction("Blackouts", new { controller = "Restaurant", id = absolutedateconstraint.RestaurantId });
        }

        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }
    }
}
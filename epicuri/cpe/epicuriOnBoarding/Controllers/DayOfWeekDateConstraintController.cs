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
    public class DayOfWeekDateConstraintController : Controller
    {
        private epicuriContainer db = new epicuriContainer();

        //
        // GET: /DayOfWeekDateConstraint/

        public ActionResult Index()
        {
            var dateconstraints = db.DateConstraints.OfType<DayOfWeekConstraint>().Include("Restaurant");
            return View(dateconstraints.ToList());
        }

        //
        // GET: /DayOfWeekDateConstraint/Details/5

        public ActionResult Details(int id = 0)
        {
            DayOfWeekConstraint dayofweekconstraint = db.DateConstraints.OfType<DayOfWeekConstraint>().Single(d => d.Id == id);
            if (dayofweekconstraint == null)
            {
                return HttpNotFound();
            }
            return View(dayofweekconstraint);
        
        }


        List<SelectListItem> AddDays()
        {
            List<SelectListItem> daysofweek = new List<SelectListItem>();
            daysofweek.Add(new SelectListItem { Value = "0", Text = "Monday" });
            daysofweek.Add(new SelectListItem { Value = "1", Text = "Tuesday" });
            daysofweek.Add(new SelectListItem { Value = "2", Text = "Wednesday" });
            daysofweek.Add(new SelectListItem { Value = "3", Text = "Thursday" });
            daysofweek.Add(new SelectListItem { Value = "4", Text = "Friday" });
            daysofweek.Add(new SelectListItem { Value = "5", Text = "Saturday" });
            daysofweek.Add(new SelectListItem { Value = "6", Text = "Sunday" });
            return daysofweek;
            
        }

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

            for (int i = 0; i <= 30; i += 30)
            {
                var list = new SelectListItem();
                list.Text = i.ToString();
                list.Value = i.ToString();
                blackoutMinutes.Add(list);
            }
            return blackoutMinutes;
        }

        //
        // GET: /DayOfWeekDateConstraint/Create
        
        public ActionResult Create(int id)
        {
            ViewBag.RestaurantId = id;
            AddDays();
            ViewBag.BlackoutHours = GetHoursList();
            ViewBag.BlackoutMinutes = GetMinutesList();
            ViewBag.DayOfWeek = new SelectList(AddDays(), "Value", "Text");
            return View();
        }

        //
        // POST: /DayOfWeekDateConstraint/Create

        [HttpPost]
        public ActionResult Create(DayOfWeekConstraint dayofweekconstraint, bool isReservation, bool isTakeaway)
        {
            if (ModelState.IsValid)
            {
                if (dayofweekconstraint.BlackoutHours == 24 && dayofweekconstraint.BlackoutMinutes == 30)
                {
                    ViewBag.BlackoutHours = GetHoursList();
                    ViewBag.BlackoutMinutes = GetMinutesList();
                    ViewBag.DayOfWeek = new SelectList(AddDays(), "Value", "Text");
                    ViewBag.RestaurantId = dayofweekconstraint.RestaurantId;
                    ViewBag.StatusMessage = "Duration must not exceed 24 hours";
                    return View();
                }

                if (dayofweekconstraint.BlackoutHours == 24)
                {
                    dayofweekconstraint.BlackoutHours = 23;
                    dayofweekconstraint.BlackoutMinutes = 59;
                }


                dayofweekconstraint.StartTime = dayofweekconstraint.StartTime;
                dayofweekconstraint.EndTime = dayofweekconstraint.StartTime;


                int hoursCheck = dayofweekconstraint.EndTime.Hours + dayofweekconstraint.BlackoutHours;
           
                // Make sure the hours isn't over 24
                if (hoursCheck >= 24)
                {
                    dayofweekconstraint.EndTime -= TimeSpan.FromHours(24);
                }

                dayofweekconstraint.EndTime += TimeSpan.FromHours(dayofweekconstraint.BlackoutHours);

                // Check the minutes doesn't push the hours over 24
                if (dayofweekconstraint.EndTime.Hours == 23 && dayofweekconstraint.EndTime.Minutes == 30 && dayofweekconstraint.BlackoutMinutes == 30)
                {
                    dayofweekconstraint.EndTime -= TimeSpan.FromHours(24);
                }

                dayofweekconstraint.EndTime += TimeSpan.FromMinutes(dayofweekconstraint.BlackoutMinutes);

                // Make sure the hours isn't over 24
                if (dayofweekconstraint.EndTime.Days == 1)
                {
                    dayofweekconstraint.EndTime -= TimeSpan.FromHours(24);
                }


                if (isTakeaway && isReservation)
                {
                    dayofweekconstraint.TargetSession = true;
                    db.DateConstraints.AddObject(dayofweekconstraint);

                    DayOfWeekConstraint newDateConstraint = new DayOfWeekConstraint();

                    using (var ms = new MemoryStream())
                    {
                        var bf = new BinaryFormatter();
                        bf.Serialize(ms, dayofweekconstraint);
                        ms.Position = 0;
                        newDateConstraint = (DayOfWeekConstraint)bf.Deserialize(ms);
                    }

                    db.DateConstraints.AddObject(newDateConstraint);

                    newDateConstraint.TargetSession = false;
                    db.SaveChanges();

                }
                else if (isTakeaway)
                {
                    dayofweekconstraint.TargetSession = true;
                    db.DateConstraints.AddObject(dayofweekconstraint);
                    db.SaveChanges();
                }
                else if (isReservation)
                {
                    dayofweekconstraint.TargetSession = false;
                    db.DateConstraints.AddObject(dayofweekconstraint);
                    db.SaveChanges();
                }
                //db.DateConstraints.AddObject(dayofweekconstraint);
                //db.SaveChanges();
                return RedirectToAction("Blackouts", new { controller = "Restaurant", id = dayofweekconstraint.RestaurantId });
            }

            ViewBag.DayOfWeek = new SelectList(AddDays(), "Value", "Text", dayofweekconstraint.DayOfWeek);
            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", dayofweekconstraint.RestaurantId);
            return View(dayofweekconstraint);
        }

        //
        // GET: /DayOfWeekDateConstraint/Edit/5

        public ActionResult Edit(int id = 0)
        {
            DayOfWeekConstraint dayofweekconstraint = db.DateConstraints.OfType<DayOfWeekConstraint>().Single(d => d.Id == id);
            if (dayofweekconstraint == null)
            {
                return HttpNotFound();
            }

            //dayofweekconstraint.StartTime = dayofweekconstraint.LocalTime;
            dayofweekconstraint.StartTime = dayofweekconstraint.StartTime;

            if (dayofweekconstraint.BlackoutHours == 23 && dayofweekconstraint.BlackoutMinutes == 59)
            {
                dayofweekconstraint.BlackoutHours = 24;
                dayofweekconstraint.BlackoutMinutes = 0;
            }

            ViewBag.BlackoutHours = new SelectList(GetHoursList(), "Text", "Value", dayofweekconstraint.BlackoutHours);
            ViewBag.BlackoutMinutes = new SelectList(GetMinutesList(), "Text", "Value", dayofweekconstraint.BlackoutMinutes);
            ViewBag.DayOfWeek = new SelectList(AddDays(), "Value", "Text",dayofweekconstraint.DayOfWeek);
            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", dayofweekconstraint.RestaurantId);
            return View(dayofweekconstraint);
        }

        //
        // POST: /DayOfWeekDateConstraint/Edit/5

        [HttpPost]
        public ActionResult Edit(DayOfWeekConstraint dayofweekconstraint)
        {
            if (ModelState.IsValid)
            {
                if (dayofweekconstraint.BlackoutHours == 24 && dayofweekconstraint.BlackoutMinutes == 30)
                {
                    ViewBag.BlackoutHours = GetHoursList();
                    ViewBag.BlackoutMinutes = GetMinutesList();
                    ViewBag.DayOfWeek = new SelectList(AddDays(), "Value", "Text");
                    ViewBag.RestaurantId = dayofweekconstraint.RestaurantId;
                    ViewBag.StatusMessage = "Duration must not exceed 24 hours";
                    return View(dayofweekconstraint);
                }

                if (dayofweekconstraint.BlackoutHours == 24)
                {
                    dayofweekconstraint.BlackoutHours = 23;
                    dayofweekconstraint.BlackoutMinutes = 59;
                }

                dayofweekconstraint.StartTime = dayofweekconstraint.StartTime;
                dayofweekconstraint.EndTime = dayofweekconstraint.StartTime;

                int hoursCheck = dayofweekconstraint.EndTime.Hours + dayofweekconstraint.BlackoutHours;

                // Make sure the hours isn't over 24
                if (hoursCheck >= 24)
                {
                    dayofweekconstraint.EndTime = dayofweekconstraint.EndTime - TimeSpan.FromHours(24);
                }

                dayofweekconstraint.EndTime += TimeSpan.FromHours(dayofweekconstraint.BlackoutHours);

                // Check the minutes doesn't push the hours over 24
                if (dayofweekconstraint.EndTime.Hours == 23 && dayofweekconstraint.EndTime.Minutes == 30 && dayofweekconstraint.BlackoutMinutes == 30)
                {
                    dayofweekconstraint.EndTime -= TimeSpan.FromHours(24);
                }

                dayofweekconstraint.EndTime += TimeSpan.FromMinutes(dayofweekconstraint.BlackoutMinutes);

                if (dayofweekconstraint.BlackoutMinutes == 59)
                {
                    dayofweekconstraint.EndTime += TimeSpan.FromSeconds(59);
                }

                // Make sure the hours isn't over 24
                if (dayofweekconstraint.EndTime.Days == 1)
                {
                    dayofweekconstraint.EndTime -= TimeSpan.FromHours(24);
                }

                db.DateConstraints.Attach(dayofweekconstraint);
                db.ObjectStateManager.ChangeObjectState(dayofweekconstraint, EntityState.Modified);
                db.SaveChanges();
                return RedirectToAction("Blackouts", new { controller = "Restaurant", id = dayofweekconstraint.RestaurantId });
            }
            ViewBag.DayOfWeek = new SelectList(AddDays(), "Value", "Text", dayofweekconstraint.DayOfWeek);
            return View(dayofweekconstraint);
        }

        //
        // GET: /DayOfWeekDateConstraint/Delete/5

        public ActionResult Delete(int id = 0)
        {
            DayOfWeekConstraint dayofweekconstraint = db.DateConstraints.OfType<DayOfWeekConstraint>().Single(d => d.Id == id);
            if (dayofweekconstraint == null)
            {
                return HttpNotFound();
            }
            return View(dayofweekconstraint);
        }

        //
        // POST: /DayOfWeekDateConstraint/Delete/5

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            DayOfWeekConstraint dayofweekconstraint = db.DateConstraints.OfType<DayOfWeekConstraint>().Single(d => d.Id == id);
            db.DateConstraints.DeleteObject(dayofweekconstraint);
            db.SaveChanges();
            return RedirectToAction("Blackouts", new { controller = "Restaurant", id = dayofweekconstraint.RestaurantId });
        }

        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }
    }
}
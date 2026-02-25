using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.Core.DatabaseModel;

namespace epicuriOnBoarding.Controllers
{
    public class RecurringScheduleItemController : Controller
    {
        private epicuriContainer db = new epicuriContainer();



        //
        // GET: /RecurringScheduleItem/Create

        public ActionResult Create(int id)
        {
            ViewBag.ServiceId = id;
            return View();
        }

        //
        // POST: /RecurringScheduleItem/Create

        [HttpPost]
        public ActionResult Create(int id, RecurringScheduleItem recurringscheduleitem)
        {
            ViewBag.ServiceId = id;
            if (ModelState.IsValid)
            {
                db.RecurringScheduleItems.AddObject(recurringscheduleitem);
                db.SaveChanges();
                return RedirectToAction("Recurring", new { id = recurringscheduleitem.ServiceId, controller = "Service" });
            }

            return View(recurringscheduleitem);
        }

        //
        // GET: /RecurringScheduleItem/Edit/5

        public ActionResult Edit(int id = 0)
        {
            RecurringScheduleItem recurringscheduleitem = db.RecurringScheduleItems.Single(r => r.Id == id);
            if (recurringscheduleitem == null)
            {
                return HttpNotFound();
            }
            ViewBag.ServiceId = recurringscheduleitem.ServiceId;
            return View(recurringscheduleitem);
        }

        //
        // POST: /RecurringScheduleItem/Edit/5

        [HttpPost]
        public ActionResult Edit(int id, RecurringScheduleItem recurringscheduleitem)
        {
            if (ModelState.IsValid)
            {
                db.RecurringScheduleItems.Attach(recurringscheduleitem);
                db.ObjectStateManager.ChangeObjectState(recurringscheduleitem, EntityState.Modified);
                db.SaveChanges();
                return RedirectToAction("Recurring", new { id = recurringscheduleitem.ServiceId, controller = "Service" });
            }
            ViewBag.ServiceId = recurringscheduleitem.ServiceId;
            return View(recurringscheduleitem);
        }

        public ActionResult Notifications(int id)
        {
            ViewBag.RecurringScheduleItemId = id;
            RecurringScheduleItem scheduleitem = db.RecurringScheduleItems.Single(s => s.Id == id);
            if (scheduleitem == null)
            {
                return HttpNotFound();
            }

            var notifications = scheduleitem.Notifications.ToList();
            ViewBag.ServiceId = scheduleitem.ServiceId;
            return View(notifications);

        }


        public ActionResult Associate(int id)
        {
            ViewBag.RecurringScheduleItemId = id;

            var notifications = db.Services.Where(s =>
                s.Id == (db.RecurringScheduleItems.Where(si =>
                    si.Id == id).FirstOrDefault().ServiceId)).FirstOrDefault().Restaurant.Notifications.ToList();
            ViewBag.Notifications = new SelectList(notifications, "Id", "Text");
            return View();
        }

        [HttpPost]
        public ActionResult Associate(int id, FormCollection c)
        {
            int notificationid = Convert.ToInt32(c["Notifications"]);
            var notification = db.Notifications.Where(n => n.Id == notificationid).FirstOrDefault();

            var scheduleitem = db.RecurringScheduleItems.Where(si =>
                    si.Id == id).FirstOrDefault();

            scheduleitem.Notifications.Add(notification);
            db.SaveChanges();


            ViewBag.RecurringScheduleItemId = id;
            return RedirectToAction("Recurring", new { id = scheduleitem.ServiceId, controller = "Service" });

        }

        public ActionResult Remove(int id, int notification)
        {
            var not = db.Notifications.Where(n => n.Id == notification).FirstOrDefault();

            var scheduleitem = db.RecurringScheduleItems.Where(si =>
                    si.Id == id).FirstOrDefault();

            scheduleitem.Notifications.Remove(not);
            db.SaveChanges();


            ViewBag.RecurringScheduleItemId = id;
            return RedirectToAction("Recurring", new { id = scheduleitem.ServiceId, controller = "Service" });

        }


        //
        // GET: /RecurringScheduleItem/Delete/5

        public ActionResult Delete(int id = 0)
        {
            RecurringScheduleItem recurringscheduleitem = db.RecurringScheduleItems.Single(r => r.Id == id);

            if (recurringscheduleitem == null)
            {
                return HttpNotFound();
            }
            ViewBag.ServiceId = recurringscheduleitem.ServiceId;
            return View(recurringscheduleitem);
        }

        //
        // POST: /RecurringScheduleItem/Delete/5

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            RecurringScheduleItem recurringscheduleitem = db.RecurringScheduleItems.Single(r => r.Id == id);

            if (recurringscheduleitem.Notifications.Count != 0)
            {
                ViewBag.ServiceId = recurringscheduleitem.ServiceId;
                ViewBag.StatusMessage = "Remove notifications for this recurring schedule item before deleting";
                return View(recurringscheduleitem);
            }

            db.RecurringScheduleItems.DeleteObject(recurringscheduleitem);
            db.SaveChanges();
            return RedirectToAction("Recurring", new { id = recurringscheduleitem.ServiceId, controller = "Service" });
        }

        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }
    }
}
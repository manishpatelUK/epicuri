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
    public class ScheduleItemController : Controller
    {
        private epicuriContainer db = new epicuriContainer();

        //
        // GET: /ScheduleItem/

        public ActionResult Index()
        {
            return View(db.ScheduleItems.ToList());
        }

        //
        // GET: /ScheduleItem/Details/5

        public ActionResult Details(int id = 0)
        {
            ScheduleItem scheduleitem = db.ScheduleItems.Single(s => s.Id == id);
            if (scheduleitem == null)
            {
                return HttpNotFound();
            }
            return View(scheduleitem);
        }

        //
        // GET: /ScheduleItem/Create

        public ActionResult Create(int id)
        {
            ViewBag.ServiceId = id;
            return View();
        }

        //
        // POST: /ScheduleItem/Create

        [HttpPost]
        public ActionResult Create(int id, ScheduleItem scheduleitem)
        {
            if (ModelState.IsValid)
            {
                db.ScheduleItems.AddObject(scheduleitem);
                db.SaveChanges();
                return RedirectToAction("Schedule", new { id=scheduleitem.ServiceId, controller="Service"});
            }
            ViewBag.ServiceId = id;
            return View(scheduleitem);
        }

        //
        // GET: /ScheduleItem/Edit/5

        public ActionResult Edit(int id = 0)
        {
            ScheduleItem scheduleitem = db.ScheduleItems.Single(s => s.Id == id);
            ViewBag.ServiceId = scheduleitem.ServiceId;
            if (scheduleitem == null)
            {
                return HttpNotFound();
            }
            return View(scheduleitem);
        }

        //
        // POST: /ScheduleItem/Edit/5

        [HttpPost]
        public ActionResult Edit(int id, ScheduleItem scheduleitem)
        {
            if (ModelState.IsValid)
            {
                db.ScheduleItems.Attach(scheduleitem);
                db.ObjectStateManager.ChangeObjectState(scheduleitem, EntityState.Modified);
                db.SaveChanges();
                return RedirectToAction("Schedule", new { id = scheduleitem.ServiceId, controller = "Service" });
            }
            ViewBag.ServiceId = scheduleitem.ServiceId;
            return View(scheduleitem);
        }

        //
        // GET: /ScheduleItem/Delete/5

        public ActionResult Delete(int id = 0)
        {
            ScheduleItem scheduleitem = db.ScheduleItems.Single(s => s.Id == id);
            if (scheduleitem == null)
            {
                return HttpNotFound();
            }
            ViewBag.ServiceId = scheduleitem.ServiceId;
            return View(scheduleitem);
        }


        public ActionResult Notifications(int id)
        {
            ViewBag.ScheduleItemId = id;
            ScheduleItem scheduleitem = db.ScheduleItems.Single(s => s.Id == id);
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
            ViewBag.ScheduleItemId = id;

            var notifications = db.Services.Where(s => 
                s.Id == (db.ScheduleItems.Where(si =>
                    si.Id == id).FirstOrDefault().ServiceId)).FirstOrDefault().Restaurant.Notifications.ToList();
            ViewBag.Notifications = new SelectList(notifications, "Id", "Text");
            return View();
        }

        [HttpPost]
        public ActionResult Associate(int id, FormCollection c)
        {

            int notificationid = Convert.ToInt32(c["Notifications"]);
            var notification = db.Notifications.Where(n => n.Id == notificationid).FirstOrDefault();

            var scheduleitem = db.ScheduleItems.Where(si =>
                    si.Id == id).FirstOrDefault();

            var test = scheduleitem.Notifications.FirstOrDefault(n => n.Id == notificationid);

            if (test != null)
            {
                ViewBag.StatusMessage = "There is already a Notification of this type";
                ViewBag.ScheduleItemId = id;
                var notifications = db.Services.Where(s =>
                s.Id == (db.ScheduleItems.Where(si =>
                    si.Id == id).FirstOrDefault().ServiceId)).FirstOrDefault().Restaurant.Notifications.ToList();
                ViewBag.Notifications = new SelectList(notifications, "Id", "Text");
                return View();
            }
            else
            {

                scheduleitem.Notifications.Add(notification);
                db.SaveChanges();


                ViewBag.ScheduleItemId = id;
                ViewBag.ServiceId = scheduleitem.ServiceId;
                return RedirectToAction("Notifications", new { id = scheduleitem.Id, controller = "ScheduleItem" });
            }
            
        }

        public ActionResult Remove(int id, int notification)
        {
            var not = db.Notifications.Where(n => n.Id == notification).FirstOrDefault();

            var scheduleitem = db.ScheduleItems.Where(si =>
                    si.Id == id).FirstOrDefault();

            scheduleitem.Notifications.Remove(not);
            db.SaveChanges();


            ViewBag.ScheduleItemId = id;
            ViewBag.ServiceId = scheduleitem.ServiceId;
            return RedirectToAction("Notifications", new { id = scheduleitem.Id, controller = "ScheduleItem" });

        }



        //
        // POST: /ScheduleItem/Delete/5

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            ScheduleItem scheduleitem = db.ScheduleItems.Single(s => s.Id == id);

            if (scheduleitem.Notifications.Count != 0)
            {
                ViewBag.ServiceId = scheduleitem.ServiceId;
                ViewBag.StatusMessage = "Remove notifications for this schedule item before deleting";
                return View(scheduleitem);
            }

            db.ScheduleItems.DeleteObject(scheduleitem);
            db.SaveChanges();
            return RedirectToAction("Schedule", new { id = scheduleitem.ServiceId, controller = "Service" });
        }

        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }
    }
}
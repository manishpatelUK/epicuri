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
    public class NotificationController : Controller
    {
        private epicuriContainer db = new epicuriContainer();

        //
        // GET: /Notification/

        public ActionResult Index()
        {
            var notifications = db.Notifications.Include("Restaurant");
            return View(notifications.ToList());
        }

        //
        // GET: /Notification/Details/5

        public ActionResult Details(int id = 0)
        {
            Notification notification = db.Notifications.Single(n => n.Id == id);
            if (notification == null)
            {
                return HttpNotFound();
            }
            return View(notification);
        }

        //
        // GET: /Notification/Create

        public ActionResult Create(int id)
        {
            ViewBag.RestaurantId = id;
            return View();
        }

        //
        // POST: /Notification/Create

        [HttpPost]
        public ActionResult Create(Notification notification)
        {
            if (ModelState.IsValid)
            {
                db.Notifications.AddObject(notification);
                db.SaveChanges();
                return RedirectToAction("Notifications", new { controller = "Restaurant", id = notification.RestaurantId });
            }

            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", notification.RestaurantId);
            return View(notification);
        }

        //
        // GET: /Notification/Edit/5

        public ActionResult Edit(int id = 0)
        {
            Notification notification = db.Notifications.Single(n => n.Id == id);
            if (notification == null)
            {
                return HttpNotFound();
            }
            ViewBag.RestaurantId = new SelectList(db.Restaurants.Where(r => r.Deleted == null), "Id", "Name", notification.RestaurantId);
            return View(notification);
        }

        //
        // POST: /Notification/Edit/5

        [HttpPost]
        public ActionResult Edit(Notification notification)
        {
            if (ModelState.IsValid)
            {
                db.Notifications.Attach(notification);
                db.ObjectStateManager.ChangeObjectState(notification, EntityState.Modified);
                db.SaveChanges();
                return RedirectToAction("Notifications", new { controller = "Restaurant", id = notification.RestaurantId });
            }
            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", notification.RestaurantId);
            return View(notification);
        }

        //
        // GET: /Notification/Delete/5

        public ActionResult Delete(int id = 0)
        {
            Notification notification = db.Notifications.Single(n => n.Id == id);
            if (notification == null)
            {
                return HttpNotFound();
            }
            return View(notification);
        }

        //
        // POST: /Notification/Delete/5

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            Notification notification = db.Notifications.Single(n => n.Id == id);

            var scheduleItems = db.ScheduleItems.Where(i => i.Notifications.Count(n => n.Id == notification.Id) > 0);
            var recurringScheduleItems = db.RecurringScheduleItems.Where(i => i.Notifications.Count(n => n.Id == notification.Id) > 0);

            if (scheduleItems.ToList().Count != 0)
            {
                ViewBag.StatusMessage = "Unable to delete this notification (Notification is assigned to ScheduleItems)";
                ViewBag.RestaurantId = notification.RestaurantId;
                return View(notification);
            }

            if (recurringScheduleItems.ToList().Count != 0)
            {
                ViewBag.StatusMessage = "Unable to delete this notification (Notification is assigned to RecurringScheduleItems)";
                ViewBag.RestaurantId = notification.RestaurantId;
                return View(notification);
            }

            foreach (var item in notification.NotificationAcks.ToList())
            {
                db.NotificationAcks.DeleteObject(item);
            }
            db.SaveChanges();
            
            foreach (var item in db.ScheduleItems.Where(i => i.Notifications.Count(n=>n.Id == notification.Id)>0).ToList())
            {
                item.Notifications.Remove(notification);
            }
            db.SaveChanges();

            foreach (var item in db.RecurringScheduleItems.Where(i => i.Notifications.Count(n => n.Id == notification.Id) > 0).ToList())
            {
                item.Notifications.Remove(notification);
            }

            db.SaveChanges();
            db.Notifications.DeleteObject(notification);

            db.SaveChanges();
            return RedirectToAction("Notifications", new { controller = "Restaurant", id = notification.RestaurantId });
        }

        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }
    }
}
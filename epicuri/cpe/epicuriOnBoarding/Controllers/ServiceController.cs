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
    public class ServiceController : Controller
    {
        private epicuriContainer db = new epicuriContainer();

        //
        // GET: /Service/

        public ActionResult Index()
        {
            var services = db.Services.Include("Restaurant").Include("DefaultMenu").Include("SelfServiceMenu").Where(s => s.Deleted == null);
            return View(services.ToList());
        }

        

        //
        // GET: /Service/Details/5

        public ActionResult Details(int id = 0)
        {
            Service service = db.Services.Single(s => s.Id == id);
            if (service == null)
            {
                return HttpNotFound();
            }
            return View(service);
        }

        //
        // GET: /Service/Create

        public ActionResult Create(int id)
        {
            var Restaurant = db.Restaurants.First(r => r.Id == id);
            ViewBag.RestaurantId = id;

            //EP-339
            ViewBag.MenuId = new SelectList(db.Menus.OrderBy(m => m.MenuName).Where(m => m.RestaurantId == id && m.Deleted == null), "Id", "MenuName");
            ViewBag.MenuId1 = new SelectList(db.Menus.OrderBy(m => m.MenuName).Where(m => m.RestaurantId == id && m.Deleted == null), "Id", "MenuName");

            //ViewBag.MenuId = new SelectList(Restaurant.Menus.OrderBy(m => m.MenuName).Where(m => m.RestaurantId == id), "Id", "MenuName");
            //ViewBag.MenuId1 = new SelectList(Restaurant.Menus.OrderBy(m => m.MenuName).Where(m => m.RestaurantId == id), "Id", "MenuName");
            return View();
        }

        //
        // POST: /Service/Create

        [HttpPost]
        public ActionResult Create(int id,Service service)
        {
            Restaurant Restaurant = db.Restaurants.First(r => r.Id == id);
            if (ModelState.IsValid)
            {
                var test = db.Services.Where(s => !s.Deleted.HasValue).Where(s => s.ServiceName.ToLower().Trim() == service.ServiceName.ToLower().Trim() && s.RestaurantId == service.RestaurantId && !s.Deleted.HasValue);
   
                if (test != null && test.Count() > 0)
                {
                    ViewBag.StatusMessage = "A service with this name already exists for this service";
                    ViewBag.RestaurantId = id;
                    ViewBag.MenuId = new SelectList(Restaurant.Menus, "Id", "MenuName", service.MenuId);
                    ViewBag.MenuId1 = new SelectList(Restaurant.Menus, "Id", "MenuName", service.MenuId1);
                    return View(service);
                }

                var isTakeawayCheck = db.Services.Where(s => s.IsTakeaway == true && s.RestaurantId == service.RestaurantId);

                if (isTakeawayCheck.Count() > 1)
                {
                    ViewBag.StatusMessage = "Only one service per restaurant can be a Takeaway Service";
                    ViewBag.RestaurantId = id;
                    ViewBag.MenuId = new SelectList(Restaurant.Menus, "Id", "MenuName", service.MenuId);
                    ViewBag.MenuId1 = new SelectList(Restaurant.Menus, "Id", "MenuName", service.MenuId1);
                    return View(service);
                }

                service.Updated = DateTime.UtcNow;
                db.Services.AddObject(service);
                db.SaveChanges();
                return RedirectToAction("Services", new { controller = "Restaurant", id = service.RestaurantId });
            }

            ViewBag.RestaurantId = id;
            ViewBag.MenuId = new SelectList(Restaurant.Menus, "Id", "MenuName", service.MenuId);
            ViewBag.MenuId1 = new SelectList(Restaurant.Menus, "Id", "MenuName", service.MenuId1);
            return View(service);
        }

        //
        // GET: /Service/Edit/5

        public ActionResult Edit(int id = 0)
        {
            Service service = db.Services.Single(s => s.Id == id);
            if (service == null)
            {
                return HttpNotFound();
            }
            ViewBag.RestaurantId = service.RestaurantId;
            //EP-125
            ViewBag.MenuId = new SelectList(db.Menus.OrderBy(m => m.MenuName).Where(m => m.RestaurantId == service.RestaurantId && m.Deleted == null), "Id", "MenuName", service.MenuId);
            ViewBag.MenuId1 = new SelectList(db.Menus.OrderBy(m => m.MenuName).Where(m => m.RestaurantId == service.RestaurantId && m.Deleted == null), "Id", "MenuName", service.MenuId1);
            return View(service);
        }

        //
        // POST: /Service/Edit/5

        [HttpPost]
        public ActionResult Edit(int id, Service service)
        {
            if (ModelState.IsValid)
            {
                Service currentService = db.Services.Where(s => s.Id == service.Id).Single();
                db.Services.Detach(currentService);

                var test = db.Services.Where(s => !s.Deleted.HasValue).Where(s => s.ServiceName.ToLower().Trim() == service.ServiceName.ToLower().Trim());

                if (test != null && test.Count() > 0 && service.ServiceName != currentService.ServiceName)
                {
                    ViewBag.StatusMessage = "A service with this name already exists for this service";
                    var returnService = db.Services.First(r => r.Id == id);
                    ViewBag.RestaurantId = returnService.RestaurantId;
                    ViewBag.MenuId = new SelectList(returnService.Restaurant.Menus, "Id", "MenuName", service.MenuId);
                    ViewBag.MenuId1 = new SelectList(returnService.Restaurant.Menus, "Id", "MenuName", service.MenuId1);
                    return View(service);
                }

                var isTakeawayCheck = db.Services.Where(s => s.IsTakeaway == true && s.RestaurantId == service.RestaurantId && s.Id != service.Id);

                if (isTakeawayCheck.Count() > 1)
                {
                    ViewBag.StatusMessage = "Only one service per restaurant can be a Takeaway Service";
                    var returnService = db.Services.First(r => r.Id == id);
                    ViewBag.RestaurantId = returnService.RestaurantId;
                    ViewBag.MenuId = new SelectList(returnService.Restaurant.Menus, "Id", "MenuName", service.MenuId);
                    ViewBag.MenuId1 = new SelectList(returnService.Restaurant.Menus, "Id", "MenuName", service.MenuId1);
                    return View(service);
                }

                var serviceCourses = db.Courses.Where(c => c.ServiceId == service.Id).FirstOrDefault();

                if (serviceCourses == null && service.Active)
                {
                    ViewBag.StatusMessage = "A service cannot become active until a course is associated with it";
                    var returnService = db.Services.First(r => r.Id == id);
                    ViewBag.RestaurantId = returnService.RestaurantId;
                    ViewBag.MenuId = new SelectList(returnService.Restaurant.Menus, "Id", "MenuName", service.MenuId);
                    ViewBag.MenuId1 = new SelectList(returnService.Restaurant.Menus, "Id", "MenuName", service.MenuId1);
                    return View(service);
                }


                db.Services.Attach(service);
                service.Updated = DateTime.UtcNow;
                db.ObjectStateManager.ChangeObjectState(service, EntityState.Modified);
                db.SaveChanges();
                return RedirectToAction("Services",new {controller="Restaurant",id=service.RestaurantId});
            }
            var Service = db.Services.First(r => r.Id == id);
            ViewBag.RestaurantId = Service.RestaurantId;
            ViewBag.MenuId = new SelectList(Service.Restaurant.Menus, "Id", "MenuName", service.MenuId);
            ViewBag.MenuId1 = new SelectList(Service.Restaurant.Menus, "Id", "MenuName", service.MenuId1);
            return View(service);
        }

        //
        // GET: /Service/Delete/5

        public ActionResult Delete(int id = 0)
        {
            Service service = db.Services.Single(s => s.Id == id);
            if (service == null)
            {
                return HttpNotFound();
            }

            ViewBag.RestaurantId = service.RestaurantId;
            ViewBag.MenuId = new SelectList(db.Menus.OrderBy(m => m.MenuName).Where(m => m.RestaurantId == service.RestaurantId), "Id", "MenuName", service.MenuId);
            ViewBag.MenuId1 = new SelectList(db.Menus.OrderBy(m => m.MenuName).Where(m => m.RestaurantId == service.RestaurantId), "Id", "MenuName", service.MenuId1);
            return View(service);
        }

        //
        // POST: /Service/Delete/5

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            Service service = db.Services.Single(s => s.Id == id);

            service.Deleted = DateTime.Today;
            service.Active = false;
            
            //db.Services.DeleteObject(service);
            int rid = service.RestaurantId;
            db.SaveChanges();
            return RedirectToAction("Services", new { controller = "Restaurant", id = rid });
        }

        public ActionResult Schedule(int id)
        {
            ViewBag.ServiceId = id;
            //EP-342
            //return View(db.ScheduleItems.Where(s=>s.ServiceId == id).OrderBy(s=>s.Order).ToList());
            return View(db.ScheduleItems.Where(s => s.ServiceId == id).ToList());
        }

        public ActionResult Recurring(int id)
        {
            ViewBag.ServiceId = id;
            return View(db.RecurringScheduleItems.Where(s => s.ServiceId == id).ToList());
        }

        //
        // GET: /Course/

        public ActionResult Courses(int id)
        {
            
            ViewBag.ServiceId = id;
            Service service = db.Services.Single(s => s.Id == id);
            ViewBag.RestaurantId = service.RestaurantId;
            return View(service.Courses.OrderBy(c=>c.Ordering).ToList());
        }

        public ActionResult OrderingUpdate(int orderIndex, int itemId)
        {
            Course course = db.Courses.Single(c => c.Id == itemId);
            course.Ordering = (Int16)orderIndex;

            db.SaveChanges();

            return null;
        }

        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }
    }
}
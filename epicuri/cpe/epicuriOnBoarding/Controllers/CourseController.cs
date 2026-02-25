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
    public class CourseController : Controller
    {
        private epicuriContainer db = new epicuriContainer();

        

      
        //
        // GET: /Course/Create

        public ActionResult Create(int id)
        {
            ViewBag.ServiceId = id;

            var Service = db.Services.First(s => s.Id == id);
            ViewBag.RestaurantId = Service.RestaurantId;
            return View();
        }

        //
        // POST: /Course/Create

        [HttpPost]
        public ActionResult Create(int id, Course course)
        {
            ViewBag.ServiceId = id;

            var Service = db.Services.First(s => s.Id == id);
            ViewBag.RestaurantId = Service.RestaurantId;
            if (ModelState.IsValid)
            {
                var test = db.Courses.Where(c => c.Name.ToLower().Trim() == course.Name.ToLower().Trim() 
                    && c.RestaurantId == course.RestaurantId 
                    && c.ServiceId == course.ServiceId);

                if (test != null && test.Count() > 0)
                {
                    ViewBag.StatusMessage = "A course with this name already exists for this service";
                    ViewBag.ServiceId = course.ServiceId;
                    ViewBag.RestaurantId = course.RestaurantId;
                    return View(course);
                }

                var orderCheck = db.Courses.Where(c => c.RestaurantId == course.RestaurantId
                    && c.ServiceId == course.ServiceId);

                //EP-107
                if (course.Ordering == null)
                {
                    // No order number sent up, so set it to the highest + 1
                    course.Ordering = (Int16)(orderCheck.Max(c => c.Ordering) + 1);
                }
                else
                {
                    // Order number has been sent up - check that it's not a duplicate

                    bool orderNumExists = false;

                    foreach (Course c in orderCheck)
                    {
                        if (c.Ordering == course.Ordering)
                            orderNumExists = true;
                    }
                    if (orderNumExists)
                    {
                        ViewBag.StatusMessage = "The ordering number " + course.Ordering.ToString() + " already exists";
                        ViewBag.ServiceId = course.ServiceId;
                        ViewBag.RestaurantId = course.RestaurantId;
                        return View(course);
                    }
                }

                /*
                // TO-DO Check max order value
                int nextOrderNumber = 0;
                if (orderCheck.Count() != 0)
                    nextOrderNumber = orderCheck.Max(t => t.Ordering) + 1;

                if (course.Ordering != nextOrderNumber)
                {
                    ViewBag.StatusMessage = "The next ordering number needs to be " + nextOrderNumber.ToString();
                    ViewBag.ServiceId = course.ServiceId;
                    ViewBag.RestaurantId = course.RestaurantId;
                    return View(course);
                }
                */

                db.Courses.AddObject(course);
                db.SaveChanges();
                return RedirectToAction("Courses", new { id = course.ServiceId, controller = "Service" });
            }

            return View(course);
        }

        //
        // GET: /Course/Edit/5

        public ActionResult Edit(int id = 0)
        {
            Course course = db.Courses.Single(c => c.Id == id);
            if (course == null)
            {
                return HttpNotFound();
            }

            ViewBag.ServiceId = course.ServiceId;
            ViewBag.RestaurantId = course.RestaurantId;

            return View(course);
        }

        //
        // POST: /Course/Edit/5

        [HttpPost]
        public ActionResult Edit(Course course)
        {
            if (ModelState.IsValid)
            {
                Course currentRecord = db.Courses.Where(c => c.Id == course.Id).Single();
                //db.Courses.Detach(currentRecord);

                var test = db.Courses.Where(c => c.Name.ToLower().Trim() == course.Name.ToLower().Trim()
                   && c.RestaurantId == course.RestaurantId
                   && c.ServiceId == course.ServiceId);

                if (test != null && test.Count() > 0 && course.Name != currentRecord.Name)
                {
                    ViewBag.StatusMessage = "A course with this name already exists for this service";
                    ViewBag.ServiceId = course.ServiceId;
                    ViewBag.RestaurantId = course.RestaurantId;
                    return View(course);
                }

                var orderCheck = db.Courses.Where(c => c.RestaurantId == course.RestaurantId
                    && c.ServiceId == course.ServiceId && c.Ordering == course.Ordering);

                var orderingValueUsed = orderCheck.FirstOrDefault();

                //EP-107
                if (orderingValueUsed != null && course.Ordering != currentRecord.Ordering)
                {
                    ViewBag.StatusMessage = "The ordering number needs to either remain the same (" + currentRecord.Ordering + "), or be an avaliable ordering number.";
                    ViewBag.ServiceId = course.ServiceId;
                    ViewBag.RestaurantId = course.RestaurantId;
                    return View(course);
                }

                currentRecord.Name = course.Name;
                currentRecord.Ordering = course.Ordering;

                db.SaveChanges();
                return RedirectToAction("Courses", new { id = course.ServiceId, controller = "Service" });
            }
            return View(course);
        }

        //
        // GET: /Course/Delete/5

        public ActionResult Delete(int id = 0)
        {
            Course course = db.Courses.Single(c => c.Id == id);
            if (course == null)
            {
                return HttpNotFound();
            }
            return View(course);
        }

        //
        // POST: /Course/Delete/5

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            Course course = db.Courses.Single(c => c.Id == id);

            foreach (var menucat in db.MenuCategories)
            {
                if (menucat.Courses.Contains(course))
                {
                    menucat.Courses.Remove(course);
                }
            }

            db.Courses.DeleteObject(course);
            db.SaveChanges();
            return RedirectToAction("Courses", new { id = course.ServiceId, controller = "Service" });
        }

        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }
    }
}
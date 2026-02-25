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
    public class DayOfYearDateConstraintController : Controller
    {
        private epicuriContainer db = new epicuriContainer();

        //
        // GET: /DayOfYearDateConstraint/

        public ActionResult Index()
        {
            var dateconstraints = db.DateConstraints.OfType<DayOfYearConstraint>().Include("Restaurant");
            return View(dateconstraints.ToList());
        }

        //
        // GET: /DayOfYearDateConstraint/Details/5

        public ActionResult Details(int id = 0)
        {
            DayOfYearConstraint dayofyearconstraint = db.DateConstraints.OfType<DayOfYearConstraint>().Single(d => d.Id == id);
            if (dayofyearconstraint == null)
            {
                return HttpNotFound();
            }
            return View(dayofyearconstraint);
        }

        //
        // GET: /DayOfYearDateConstraint/Create

        public ActionResult Create()
        {
            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name");
            return View();
        }

        //
        // POST: /DayOfYearDateConstraint/Create

        [HttpPost]
        public ActionResult Create(DayOfYearConstraint dayofyearconstraint)
        {
            if (ModelState.IsValid)
            {
                db.DateConstraints.AddObject(dayofyearconstraint);
                db.SaveChanges();
                return RedirectToAction("Index");
            }

            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", dayofyearconstraint.RestaurantId);
            return View(dayofyearconstraint);
        }

        //
        // GET: /DayOfYearDateConstraint/Edit/5

        public ActionResult Edit(int id = 0)
        {
            DayOfYearConstraint dayofyearconstraint = db.DateConstraints.OfType<DayOfYearConstraint>().Single(d => d.Id == id);
            if (dayofyearconstraint == null)
            {
                return HttpNotFound();
            }
            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", dayofyearconstraint.RestaurantId);
            return View(dayofyearconstraint);
        }

        //
        // POST: /DayOfYearDateConstraint/Edit/5

        [HttpPost]
        public ActionResult Edit(DayOfYearConstraint dayofyearconstraint)
        {
            if (ModelState.IsValid)
            {
                db.DateConstraints.Attach(dayofyearconstraint);
                db.ObjectStateManager.ChangeObjectState(dayofyearconstraint, EntityState.Modified);
                db.SaveChanges();
                return RedirectToAction("Index");
            }
            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", dayofyearconstraint.RestaurantId);
            return View(dayofyearconstraint);
        }

        //
        // GET: /DayOfYearDateConstraint/Delete/5

        public ActionResult Delete(int id = 0)
        {
            DayOfYearConstraint dayofyearconstraint = db.DateConstraints.OfType<DayOfYearConstraint>().Single(d => d.Id == id);
            if (dayofyearconstraint == null)
            {
                return HttpNotFound();
            }
            return View(dayofyearconstraint);
        }

        //
        // POST: /DayOfYearDateConstraint/Delete/5

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            DayOfYearConstraint dayofyearconstraint = db.DateConstraints.OfType<DayOfYearConstraint>().Single(d => d.Id == id);
            db.DateConstraints.DeleteObject(dayofyearconstraint);
            db.SaveChanges();
            return RedirectToAction("Index");
        }

        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }
    }
}
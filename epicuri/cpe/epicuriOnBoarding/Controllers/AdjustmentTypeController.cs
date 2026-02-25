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
    public class AdjustmentTypeController : Controller
    {
        private epicuriContainer db = new epicuriContainer();

        //
        // GET: /AdjustmentType/

        public ActionResult Index()
        {
            return View(db.AdjustmentTypes.ToList());
        }

        //
        // GET: /AdjustmentType/Details/5

        public ActionResult Details(int id = 0)
        {
            AdjustmentType adjustmenttype = db.AdjustmentTypes.Single(a => a.Id == id);
            if (adjustmenttype == null)
            {
                return HttpNotFound();
            }
            return View(adjustmenttype);
        }

        //
        // GET: /AdjustmentType/Create

        public ActionResult Create()
        {
            return View();
        }

        //
        // POST: /AdjustmentType/Create

        [HttpPost]
        [ValidateAntiForgeryToken]
        public ActionResult Create(AdjustmentType adjustmenttype)
        {
            if (ModelState.IsValid)
            {
                db.AdjustmentTypes.AddObject(adjustmenttype);
                db.SaveChanges();
                return RedirectToAction("Index");
            }

            return View(adjustmenttype);
        }

        //
        // GET: /AdjustmentType/Edit/5

        public ActionResult Edit(int id = 0)
        {
            AdjustmentType adjustmenttype = db.AdjustmentTypes.Single(a => a.Id == id);
            if (adjustmenttype == null)
            {
                return HttpNotFound();
            }
            return View(adjustmenttype);
        }

        //
        // POST: /AdjustmentType/Edit/5

        [HttpPost]
        [ValidateAntiForgeryToken]
        public ActionResult Edit(AdjustmentType adjustmenttype)
        {
            if (ModelState.IsValid)
            {
                db.AdjustmentTypes.Attach(adjustmenttype);
                db.ObjectStateManager.ChangeObjectState(adjustmenttype, EntityState.Modified);
                db.SaveChanges();
                return RedirectToAction("Index");
            }
            return View(adjustmenttype);
        }

        //
        // GET: /AdjustmentType/Delete/5

        public ActionResult Delete(int id = 0)
        {
            AdjustmentType adjustmenttype = db.AdjustmentTypes.Single(a => a.Id == id);
            if (adjustmenttype == null)
            {
                return HttpNotFound();
            }
            return View(adjustmenttype);
        }

        //
        // POST: /AdjustmentType/Delete/5

        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public ActionResult DeleteConfirmed(int id)
        {
            AdjustmentType adjustmenttype = db.AdjustmentTypes.Single(a => a.Id == id);
            adjustmenttype.Deleted = DateTime.UtcNow;
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
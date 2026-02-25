using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.Core.DatabaseModel;
using epicuriOnBoarding.Filters;
namespace epicuriOnBoarding.Controllers
{
    [Authorize]
    [InitializeSimpleMembership]
    public class TaxController : Controller
    {
        private epicuriContainer db = new epicuriContainer();

        //
        // GET: /Tax/

        public ActionResult Index()
        {
            var taxtypes = db.TaxTypes.Where(t => t.Deleted == null);
            return View(taxtypes.ToList());
        }

        //
        // GET: /Tax/Details/5

        public ActionResult Details(int id = 0)
        {
            TaxType taxtype = db.TaxTypes.Single(t => t.Id == id);
            if (taxtype == null)
            {
                return HttpNotFound();
            }
            return View(taxtype);
        }

        //
        // GET: /Tax/Create

        public ActionResult Create()
        {
            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name");
            ViewBag.CountryId = new SelectList(db.Countries.OrderBy(c => c.Name), "Id", "Name");
            return View();
        }

        //
        // POST: /Tax/Create

        [HttpPost]
        public ActionResult Create(TaxType taxtype)
        {
            if (ModelState.IsValid)
            {
                var test = db.TaxTypes.Where(tt => tt.Name.ToLower().Trim() == taxtype.Name.ToLower().Trim() && tt.CountryId == taxtype.CountryId && tt.Deleted == null);
                
                if (test != null && test.Count() > 0)
                {
                    ViewBag.StatusMessage = "A Tax Type with this name already exists for this country";
                    ViewBag.CountryId = new SelectList(db.Countries.OrderBy(c => c.Name), "Id", "Name");
                    return View(taxtype);
                }

                db.TaxTypes.AddObject(taxtype);
                db.SaveChanges();
                return RedirectToAction("Index");
            }


            ViewBag.CountryId = new SelectList(db.Countries, "Id", "Name");
            return View(taxtype);
        }

        //
        // GET: /Tax/Edit/5

        public ActionResult Edit(int id = 0)
        {
            TaxType taxtype = db.TaxTypes.Single(t => t.Id == id);
            if (taxtype == null)
            {
                return HttpNotFound();
            }

            ViewBag.CountryId = new SelectList(db.Countries.OrderBy(c => c.Name), "Id", "Name", taxtype.CountryId);
            return View(taxtype);
        }

        //
        // POST: /Tax/Edit/5

        [HttpPost]
        public ActionResult Edit(TaxType taxtype)
        {
            if (ModelState.IsValid)
            {
                TaxType currentRecord = db.TaxTypes.Where(tt => tt.Id == taxtype.Id).Single();
                db.TaxTypes.Detach(currentRecord);

                var test = db.TaxTypes.Where(tt => tt.Name.ToLower().Trim() == taxtype.Name.ToLower().Trim() && tt.CountryId == taxtype.CountryId && tt.Deleted == null);
                
                if (test != null && test.Count() > 0 && taxtype.Name != currentRecord.Name)
                {
                    ViewBag.StatusMessage = "A Tax Type with this name already exists for this country";
                    ViewBag.CountryId = new SelectList(db.Countries.OrderBy(c => c.Name), "Id", "Name");
                    return View(taxtype);
                }

                db.TaxTypes.Attach(taxtype);
                db.ObjectStateManager.ChangeObjectState(taxtype, EntityState.Modified);
                db.SaveChanges();
                return RedirectToAction("Index");
            }
            
            ViewBag.CountryId = new SelectList(db.Countries, "Id", "Name");
            return View(taxtype);
        }

        //
        // GET: /Tax/Delete/5

        public ActionResult Delete(int id = 0)
        {
            TaxType taxtype = db.TaxTypes.Single(t => t.Id == id);
            if (taxtype == null)
            {
                return HttpNotFound();
            }
            return View(taxtype);
        }

        //
        // POST: /Tax/Delete/5

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            TaxType taxtype = db.TaxTypes.Single(t => t.Id == id);
            taxtype.Deleted = DateTime.Today;
            
            //db.TaxTypes.DeleteObject(taxtype);
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
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
    public class CountryController : Controller
    {
        private epicuriContainer db = new epicuriContainer();

        //
        // GET: /Country/

        public ActionResult Index()
        {
            return View(db.Countries.OrderBy(c => c.Name).ToList());
        }

        //
        // GET: /Country/Details/5

        public ActionResult Details(int id = 0)
        {
            Country country = db.Countries.Single(c => c.Id == id);
            if (country == null)
            {
                return HttpNotFound();
            }
            return View(country);
        }

        //
        // GET: /Country/Create

        public ActionResult Create()
        {
            return View();
        }

        //
        // POST: /Country/Create

        [HttpPost]
        public ActionResult Create(Country country)
        {
            if (ModelState.IsValid)
            {
                var test = db.Countries.Where(ctry => ctry.Name.ToLower().Trim() == country.Name.ToLower().Trim() || ctry.Acronym == country.Acronym);
                if (test != null && test.Count() > 0)
                {
                    ViewBag.StatusMessage = "A Country with this name or acronym already exists";
                    return View(country);
                }
                
                db.Countries.AddObject(country);
                db.SaveChanges();
                return RedirectToAction("Index");
            }

            return View(country);
        }

        //
        // GET: /Country/Edit/5

        public ActionResult Edit(int id = 0)
        {
            Country country = db.Countries.Single(c => c.Id == id);
            if (country == null)
            {
                return HttpNotFound();
            }
            return View(country);
        }

        //
        // POST: /Country/Edit/5

        [HttpPost]
        public ActionResult Edit(Country country)
        {
            if (ModelState.IsValid)
            {
                db.Countries.Attach(country);
                db.ObjectStateManager.ChangeObjectState(country, EntityState.Modified);
                db.SaveChanges();
                return RedirectToAction("Index");
            }
            return View(country);
        }

        //
        // GET: /Country/Delete/5

        public ActionResult Delete(int id = 0)
        {
            Country country = db.Countries.Single(c => c.Id == id);
            if (country == null)
            {
                return HttpNotFound();
            }
            return View(country);
        }

        //
        // POST: /Country/Delete/5

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            Country country = db.Countries.Single(c => c.Id == id);
            db.Countries.DeleteObject(country);
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
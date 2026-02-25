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
    public class CategoryController : Controller
    {
        private epicuriContainer db = new epicuriContainer();

        //
        // GET: /Category/

        public ActionResult Index()
        {
            return View(db.Categories.OrderBy(cat => cat.Name).ToList());
        }

        //
        // GET: /Category/Details/5

        public ActionResult Details(int id = 0)
        {
            Category category = db.Categories.Single(c => c.Id == id);
            if (category == null)
            {
                return HttpNotFound();
            }
            return View(category);
        }

        //
        // GET: /Category/Create

        public ActionResult Create()
        {
            return View();
        }

        //
        // POST: /Category/Create

        [HttpPost]
        public ActionResult Create(Category category)
        {
            if (ModelState.IsValid)
            {
                var test = db.Categories.Where(ctgy => ctgy.Name.ToLower().Trim() == category.Name.ToLower().Trim());
                if (test != null && test.Count() > 0)
                {
                    ViewBag.StatusMessage = "A Cuisine with this name already exists";
                    return View(category);
                }

                db.Categories.AddObject(category);
                db.SaveChanges();
                return RedirectToAction("Index");
            }

            return View(category);
        }

        //
        // GET: /Category/Edit/5

        public ActionResult Edit(int id = 0)
        {
            Category category = db.Categories.Single(c => c.Id == id);
            if (category == null)
            {
                return HttpNotFound();
            }
            return View(category);
        }

        //
        // POST: /Category/Edit/5

        [HttpPost]
        public ActionResult Edit(Category category)
        {
            if (ModelState.IsValid)
            {
                var test = db.Categories.Where(ctgy => ctgy.Name.ToLower().Trim() == category.Name.ToLower().Trim());
                if (test != null && test.Count() > 0)
                {
                    ViewBag.StatusMessage = "A Cuisine with this name already exists";
                    return View(category);
                }
                db.Categories.Attach(category);
                db.ObjectStateManager.ChangeObjectState(category, EntityState.Modified);
                db.SaveChanges();
                return RedirectToAction("Index");
            }
            return View(category);
        }

        //
        // GET: /Category/Delete/5

        public ActionResult Delete(int id = 0)
        {
            Category category = db.Categories.Single(c => c.Id == id);
            if (category == null)
            {
                return HttpNotFound();
            }
            return View(category);
        }

        //
        // POST: /Category/Delete/5

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            Category category = db.Categories.Single(c => c.Id == id);

                db.Categories.DeleteObject(category);
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
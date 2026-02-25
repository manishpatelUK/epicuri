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
    public class DefaultController : Controller
    {
        private epicuriContainer db = new epicuriContainer();

        //
        // GET: /Default/

        public ActionResult Index()
        {
            var defaultList = from d in db.DefaultSettings
                              orderby d.SortId
                              select d;
          
            return View(defaultList.ToList());
        }

       
       
      
        //
        // GET: /Default/Edit/5

        public ActionResult Edit(int id = 0)
        {
            DefaultSetting defaultsetting = db.DefaultSettings.Single(d => d.Id == id);
            if (defaultsetting == null)
            {
                return HttpNotFound();
            }
            return View(defaultsetting);
        }

        //
        // POST: /Default/Edit/5

        [HttpPost]
        public ActionResult Edit(int id , DefaultSetting defaultsetting)
        {
            if (ModelState.IsValid)
            {
                DefaultSetting oldDefaultsetting = db.DefaultSettings.Single(d => d.Id == id);
                if (defaultsetting == null)
                {
                    return HttpNotFound();
                }

                oldDefaultsetting.Value = defaultsetting.Value;
                oldDefaultsetting.SortId = defaultsetting.SortId;
               
                
                
                db.SaveChanges();
                return RedirectToAction("Index");
            }
            return View(defaultsetting);
        }

      
        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }
    }
}
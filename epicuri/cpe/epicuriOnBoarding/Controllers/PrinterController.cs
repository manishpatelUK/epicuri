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
    public class PrinterController : Controller
    {
        private epicuriContainer db = new epicuriContainer();



        //
        // GET: /Printer/Create

        public ActionResult Create(string id)
        {
            int rid = int.Parse(id);


            Restaurant r = db.Restaurants.Single(a => a.Id == rid);

            try
            {

                if (r == null)
                {
                    return HttpNotFound();
                }
            }
            catch { return HttpNotFound(); }

            ViewBag.RestaurantId = r.Id;
            ViewData["name"] = r.Name;
            return View();
        }

        //
        // POST: /Printer/Create

        [HttpPost]
        public ActionResult Create(int id, Printer printer)
        {

            Restaurant r = db.Restaurants.Single(a => a.Id == id);
            if (r == null)
            {
                return HttpNotFound();
            }


            if (ModelState.IsValid)
            {
                // EP-751 clean out IP if it is a screen printer
                if (string.IsNullOrEmpty(printer.IP))
                    printer.IP = null;

                r.Printers.Add(printer);
                db.SaveChanges();
                return RedirectToAction("Printers", new { id = printer.RestaurantId, controller = "Restaurant" });
                //return RedirectToAction("Index","Restaurant");
            }

            ViewBag.RestaurantId = r.Id;
            return View(printer);
        }

        //
        // GET: /Printer/Edit/5

        public ActionResult Edit(int id = 0)
        {
            Printer printer = db.Printers.Single(p => p.Id == id);
            if (printer == null)
            {
                return HttpNotFound();
            }
            ViewBag.RestaurantId = new SelectList(db.Restaurants.Where(r => r.Deleted == null), "Id", "Name", printer.RestaurantId);
            ViewBag.ReturnToRestaurant = printer.RestaurantId;
            return View(printer);
        }

        //
        // POST: /Printer/Edit/5

        [HttpPost]
        public ActionResult Edit(Printer printer)
        {
            if (ModelState.IsValid)
            {
                // EP-751 clean out IP if it is a screen printer
                if (string.IsNullOrEmpty(printer.IP))
                    printer.IP = null;

                db.Printers.Attach(printer);
                db.ObjectStateManager.ChangeObjectState(printer, EntityState.Modified);
                db.SaveChanges();
                
                //EP-108
                return RedirectToAction("Printers", new { id = printer.RestaurantId, controller = "Restaurant" });

                //return RedirectToAction("Index","Restaurant");
            }
            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", printer.RestaurantId);
            return View(printer);
        }

        //
        // GET: /Printer/Delete/5

        public ActionResult Delete(int id = 0)
        {
            Printer printer = db.Printers.Single(p => p.Id == id);
            if (printer == null)
            {
                return HttpNotFound();
            }

            var printerQuery = db.Printers.Where(p => p.RestaurantId == printer.RestaurantId);

            if (printerQuery.ToList().Count == 1)
            {
                ViewBag.StatusMessage = "WARNING: Deleting the last printer will disable the Restaurant for the Waiter App";
            }

            ViewBag.RestaurantId = printer.RestaurantId;
            return View(printer);
        }

        //
        // POST: /Printer/Delete/5

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            Printer printer = db.Printers.Single(p => p.Id == id);

            var printerQuery = db.Printers.Where(p => p.RestaurantId == printer.RestaurantId);

            if (printerQuery.ToList().Count == 1)
            {
                var Restaurant = db.Restaurants.Single(r => r.Id == printer.RestaurantId);
                Restaurant.EnabledForWaiter = false;
            }

            var PrinterInUse = db.MenuItems.Where(mi => mi.RestaurantId == printer.RestaurantId && mi.PrinterId == printer.Id);

            if (PrinterInUse.ToList().Count != 0)
            {
                ViewBag.StatusMessage = "Unable to delete this printer (MenuItems are assigned to this printer)";
                ViewBag.RestaurantId = printer.RestaurantId;
                return View(printer);
            }

            var restPrinter = db.Restaurants.Single(r => r.Id == printer.RestaurantId);

            if (restPrinter.TakeawayPrinterId == id || restPrinter.BillingPrinterId == id)
            {
                ViewBag.StatusMessage = "Unable to delete this printer (This is the takeaway/billing printer)";
                ViewBag.RestaurantId = printer.RestaurantId;
                return View(printer);
            }


            db.Printers.DeleteObject(printer);
            db.SaveChanges();
           
            return RedirectToAction("Printers", new { controller = "Restaurant", id = printer.RestaurantId });
            //return RedirectToAction("Index");
        }

        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }
    }
}
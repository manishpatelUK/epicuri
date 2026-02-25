using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.Core.DatabaseModel;
using epicuri.CPE.Models;

namespace epicuri.CPE.Controllers
{

    public class KitchenController : Models.EpicuriController
    {
        //
        // GET: /Kitchen/
        [RequiresAuth("")]
        public ActionResult Index(Restaurant restaurant, String Auth, int Printer=0)
        {
            ViewData["auth"] = Auth;


            if (Printer > 0)
            {
                if(restaurant.Printers.Any(p=>p.Id==Printer))
                {
                    var print = restaurant.Printers.Single(p => p.Id == Printer);
                    return View(new KitchenViewModel(print.Id,print.Name));
                }
            
            }

            return RedirectToAction("List");            
        }

        [RequiresAuth("")]
        [ActionName("List")]
        public ActionResult List(Restaurant restaurant, String Auth)
        {
            IEnumerable<epicuri.CPE.Models.Printer> printers = restaurant.Printers.Select(p => new epicuri.CPE.Models.Printer(p));
            return View(printers);
        }

        public ActionResult MarkDone(int id, string Auth)
        {
            ViewData["auth"] = Auth;
            if (string.IsNullOrWhiteSpace(Auth))
            {
                throw new HttpException(403, "HTTP/1.1 403 Not Authorized");
            }
            try
            {
                var order = db.Orders.Where(o => o.Id == id).FirstOrDefault();

                if (order == null)
                {
                    return RedirectToAction("Index");
                }

                order.Completed = DateTime.UtcNow;
                var batch = db.Batches.Where(b => b.Id == order.BatchId).First();
                db.SaveChanges();

                return RedirectToAction("Index", new {Auth=Auth, Printer = batch.PrinterId});
            }
            catch
            {
                   
            }

            throw new HttpException(403, "HTTP/1.1 403 Not Authorized");
        }


        public ActionResult MarkAllDone(int id, string Auth)
        {
            ViewData["auth"] = Auth;
            if (string.IsNullOrWhiteSpace(Auth))
            {
                throw new HttpException(403, "HTTP/1.1 403 Not Authorized");
            }
            try
            {
                var Session = db.Sessions.Where(o => o.Id == id).FirstOrDefault();

                if (Session == null)
                {
                    return RedirectToAction("Index");
                }

                epicuri.Core.DatabaseModel.Batch batch = null;

                foreach(var order in Session.Orders.ToArray())
                {
                    order.Completed = DateTime.UtcNow;
                    
                    batch = db.Batches.Where(b => b.Id == order.BatchId).First();
                }
                db.SaveChanges();


                return RedirectToAction("Index", new { Auth = Auth, Printer = batch.PrinterId });
            }
            catch
            {

            }

            throw new HttpException(403, "HTTP/1.1 403 Not Authorized");
        }

   
    }
}

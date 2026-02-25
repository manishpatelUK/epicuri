using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.CPE.Models.Reporting;
using epicuri.CPE.Model.Reporting;
using System.Text;
using System.IO;
namespace epicuri.CPE.Controllers
{
    public class ReportingCentreController : Models.EpicuriController
    {

        //
        // GET: /ReportingCentre/
        [RequiresAuth("")]
        public ActionResult Index(Core.DatabaseModel.Restaurant restaurant)
        {
           

            ReportFactory rf = new ReportFactory(restaurant);
            return View(rf.ReportTypes());
        }


        [RequiresAuth("")]
        [ActionName("CreateReport")]
        public ActionResult GetCreateReport(Core.DatabaseModel.Restaurant restaurant, string id)
        {
           
            ReportFactory rf = new ReportFactory(restaurant);
            if (rf.ReportTypesShort().Contains(id))
            {
                ViewBag.Name = id;
                var rep = rf.GetReport(id);
                return View(rep);
            }
            throw new HttpException(403, "HTTP/1.1 404 Not Found"); 
        }


        [RequiresAuth("")]
        [HttpPost]
        [ActionName("CreateReport")]
        public ActionResult PostCreateReport(Core.DatabaseModel.Restaurant restaurant, string id)
        {
            ReportFactory rf = new ReportFactory(restaurant);
            if (rf.ReportTypesShort().Contains(id))
            {
                var report = rf.GetReport(id);

                try
                {
                    report.Configure(Request.Form);
                }
                catch (InvalidOperationException ex)
                {
                    ViewBag.Error = ex.Message;
                    return View(report);
                }

                var stream = new MemoryStream(Encoding.UTF8.GetBytes(report.ReportBody(DateTime.MinValue, DateTime.MaxValue)));
          
                return File(stream, "text/csv", id + ".csv");

            }
            throw new HttpException(403, "HTTP/1.1 404 Not Found");
        }

    }
}

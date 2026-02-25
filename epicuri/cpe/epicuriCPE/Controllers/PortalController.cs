using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.Core.DatabaseModel;

namespace epicuri.CPE.Controllers
{
    public class PortalController : Models.EpicuriController
    {
        //
        // GET: /Auth/
        [RequiresAuth("")]
        public ActionResult Index(Restaurant restaurant)
        {
            IEnumerable<epicuri.CPE.Models.Printer> printers = restaurant.Printers.Select(p => new epicuri.CPE.Models.Printer(p));
            return View(printers);
        }


    }
}

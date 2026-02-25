using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.Core.DatabaseModel;
using System.Diagnostics;

namespace epicuriOnBoarding.Controllers
{
    public class ArchiveController : Controller
    {
        //
        // GET: /Archive/

        private epicuriContainer db = new epicuriContainer();

        public ActionResult Index(int? page)
        {
            int pageSize = 25;
            IQueryable<epicuri.Core.DatabaseModel.Archive> archives = db.Archives.OrderBy(a => a.Id);
            Debug.Write(archives);

            return View(new Models.PaginatedList<Archive>(archives, page ?? 0, pageSize));
        }

    }
}

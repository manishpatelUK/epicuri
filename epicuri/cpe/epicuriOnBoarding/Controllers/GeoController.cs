using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;

namespace epicuriOnBoarding.Controllers
{
    public class GeoController : Controller
    {
        //
        // GET: /Geo/

        public ActionResult Lookup(string id)
        {
            Tuple<double,double> latlong = new Tuple<double,double>(0,0);
            try
            {
                latlong = epicuri.Core.Utils.GeoCoding.LatLongFromPostCode(id);
            }
            catch
            {
                
            }
            return View(latlong);
        }

    }
}

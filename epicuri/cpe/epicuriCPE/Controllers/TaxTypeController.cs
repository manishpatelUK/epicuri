using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using System.Net.Http;
using System.Net;
namespace epicuri.CPE.Controllers
{
    public class TaxTypeController : Models.EpicuriApiController
    {
        //
        // GET: /TaxType/

        public HttpResponseMessage Get()
        {
            try
            {
                Authenticate();
            }
            catch (Exception e)
            {
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, e);
                response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                return response;
            }

            return Request.CreateResponse(HttpStatusCode.OK, db.TaxTypes.Where(tt => tt.CountryId == this.Restaurant.CountryId).Select(o=>new {Id=o.Id, o.Rate, Name=o.Name}));
        }

    }
}

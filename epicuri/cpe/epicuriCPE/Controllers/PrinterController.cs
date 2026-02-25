using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.CPE.Controllers
{
    public class PrinterController : Models.EpicuriApiController
    {
        [HttpGet]
        [ActionName("Printer")]
        public HttpResponseMessage GetPrinters()
        {
            try
            {
                Authenticate();
            }
            catch { return Request.CreateResponse(HttpStatusCode.Unauthorized);  }
            return Request.CreateResponse(HttpStatusCode.OK, Restaurant.Printers.Select(p => new Models.Printer(p)));
        }

        [HttpPut]
        [ActionName("Redirect")]
        public HttpResponseMessage Redirect(Dictionary<string, int> redirection)
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

            int printerFromId = (from v in redirection where string.Compare(v.Key, "From", true) == 0 select v.Value).FirstOrDefault();
            int printerToId = (from v in redirection where string.Compare(v.Key, "To", true) == 0 select v.Value).FirstOrDefault();

            Core.DatabaseModel.Printer setRedirection = (from p in Restaurant.Printers
                                            where p.Id == printerFromId
                                            && p.RestaurantId == Restaurant.Id
                                            select p).SingleOrDefault();

            if (setRedirection == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Printer " + printerFromId + " not found"));
            }

            Core.DatabaseModel.Printer setRedirectionPrinter = (from p in Restaurant.Printers
                                                         where p.Id == printerToId
                                                         && p.RestaurantId == Restaurant.Id
                                                         select p).SingleOrDefault();

            if (setRedirectionPrinter == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Printer " + printerToId + " not found"));
            }

            setRedirection.RedirectPrinterId = printerToId;

            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK);
        }

        [ActionName("RedirectedPrinters")]
        public HttpResponseMessage GetRedirect()
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

            var redirectedPrinters = (from rp in Restaurant.Printers
                                     where rp.RestaurantId == Restaurant.Id
                                     && rp.RedirectPrinterId != null
                                     select rp).ToList();

            if (redirectedPrinters.Count == 0)
            {
                return Request.CreateResponse(HttpStatusCode.OK, redirectedPrinters);
            }

            List<Dictionary<string, object>> currentRedirections = new List<Dictionary<string, object>>();

            foreach (Core.DatabaseModel.Printer printer in redirectedPrinters)
            {
                Dictionary<string, object> redirection = new Dictionary<string, object>();

                redirection.Add("Id", printer.Id);
                redirection.Add("From", Restaurant.Printers.Select(p => new Models.Printer(p)).Where(p => p.Id == printer.Id).SingleOrDefault());
                redirection.Add("To", Restaurant.Printers.Select(p => new Models.Printer(p)).Where(p => p.Id == printer.RedirectPrinterId).SingleOrDefault());

                currentRedirections.Add(redirection);
            }

            return Request.CreateResponse(HttpStatusCode.OK, currentRedirections);
        }

        [ActionName("Redirect")]
        public HttpResponseMessage DeleteRedirect(int id)
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

            Core.DatabaseModel.Printer removeRedirection = (from p in Restaurant.Printers
                                                         where p.Id == id
                                                         && p.RestaurantId == Restaurant.Id
                                                         select p).SingleOrDefault();

            removeRedirection.RedirectPrinterId = null;

            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK);
        }

    }
}
 
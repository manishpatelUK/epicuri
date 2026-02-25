using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Net;
using System.Net.Http;
using epicuri.Core.DatabaseModel;
namespace epicuri.CPE.Controllers
{
    public class PaymentController : Models.EpicuriApiController
    {
        public HttpResponseMessage PostPayment(Models.Payment payment)
        {
            // Deprecated after Adjustments CR 12/09/14 A.M
            return Request.CreateResponse(HttpStatusCode.Gone, new Exception("DEPRECATED METHOD: Use POST Adjustment instead"));
        }
    }
}
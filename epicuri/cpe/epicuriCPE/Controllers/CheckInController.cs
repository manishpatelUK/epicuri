using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using epicuri.Core;
using epicuri.Core.DatabaseModel;
namespace epicuri.CPE.Controllers
{
    public class CheckInController : Models.EpicuriApiController
    {
        // GET api/<controller>
        public HttpResponseMessage Get()
        {
            try
            {
                Authenticate();
            }
            catch(Exception e)
            {
                return Request.CreateResponse(HttpStatusCode.Unauthorized,e);
            }

            DateTime minTime = DateTime.UtcNow.AddMinutes(-Settings.Setting<double>(Restaurant.Id,"CheckinExpirationTime"));
            var CheckIns = from checkin in Restaurant.CheckIns
                           where checkin.Time > minTime && checkin.Diner == null && checkin.Party == null
                           select new Models.CheckIn(checkin);

            return Request.CreateResponse(HttpStatusCode.OK, CheckIns.ToList<Models.CheckIn>());
        }
    }
}
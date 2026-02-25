using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.CPE.Controllers
{
    public class AdhocAcknowledgementController : Models.EpicuriApiController
    {
        [HttpPost]
        public HttpResponseMessage PostAdhocAcknowledgementController(int id, Models.NotifySessionId sesInfo)
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

            var session = Restaurant.Sessions.Where(s => s.Id == sesInfo.SessionId).FirstOrDefault();
            if (session == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Session Not Found"));
            }


            var not = session.AdhocNotifications.FirstOrDefault(n => n.Id == id);
            if (not == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Adhoc Notification Not Found"));
            }

            var ak = new Core.DatabaseModel.AdhocNotificationAck
            {
                Time = DateTime.UtcNow
            };
            not.AdhocNotificationAcks.Add(ak);
            db.SaveChanges();


            return Request.CreateResponse(HttpStatusCode.Created, new { Id = ak.Id, Time = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(ak.Time)});
        }
    }
}

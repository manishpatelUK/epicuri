using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.CPE.Controllers
{
    public class AcknowledgementController : Models.EpicuriApiController
    {
        [HttpPost]
        public HttpResponseMessage PostAcknowledgement(int id, Models.NotifySessionId sesInfo)
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

            var session = Restaurant.Sessions.OfType<Core.DatabaseModel.SeatedSession>().Where(ses => ses.Id == sesInfo.SessionId).FirstOrDefault();
            if (session == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Session Not Found"));
            }

            var s = new Models.SeatedSession(session);


            var ak = new Core.DatabaseModel.NotificationAck
            {
                NotificationId = id,
                SessionId = session.Id,
                Time = DateTime.UtcNow,
            };
            session.NotificationAcks.Add(ak);
            db.SaveChanges();

            var Notification = new Models.Notification(ak.Notification, session);

            var items = s.ScheduleItems.Where(si => si.Notifications.Count(sin=>sin.Id == ak.Notification.Id)==1);

            foreach (var item in items)
            {
                if (item != null)
                {
                    session.Delay = int.Parse(((Core.Utils.Time.DateTimeToUnixTimestamp(DateTime.UtcNow) - Core.Utils.Time.DateTimeToUnixTimestamp(session.StartTime)) - item.Delay).ToString());
                }
            }

            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.Created, new { Id = ak.Id, NotificationId = id, SessionId = session.Id, Time = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(ak.Time) });
        }
    }
}

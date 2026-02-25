using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.CPE.Controllers
{
    public class NotificationController : Models.EpicuriApiController
    {
        [HttpGet]
        public HttpResponseMessage GetNotifications()
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

            var nots = Restaurant.Notifications.Select(n => new Models.Notification(n));
            return Request.CreateResponse(HttpStatusCode.OK, nots);
        }

        [HttpPut]
        public HttpResponseMessage PutNotification(int id, Models.Notification notification)
        {
            try
            {
                Authenticate("Manager");
            }
            catch (RoleException e)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, e);
            }
            catch (Exception e)
            {
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, e);
                response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                return response;
            }

            var Notification = db.Notifications.FirstOrDefault(n => n.Id == id);
            if (Notification == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Notification Not Found"));
            }

            Notification.Target = notification.Target;
            Notification.Text = notification.Text;

            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK, new Models.Notification(Notification));

        }

        [HttpPost]
        public HttpResponseMessage PostNotification(Models.Notification notification)
        {
            try
            {
                Authenticate("Manager");
            }
            catch (RoleException e)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, e);
            }
            catch (Exception e)
            {
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, e);
                response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                return response;
            }


            var Notification = notification.ToNotification();
            Restaurant.Notifications.Add(Notification);

            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.Created, new Models.Notification(Notification));
        }

        [HttpDelete]
        public HttpResponseMessage DeleteNotification(int id)
        {
            try
            {
                Authenticate("Manager");
            }
            catch (RoleException e)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, e);
            }
            catch (Exception e)
            {
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, e);
                response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                return response;
            }

            var Notification = db.Notifications.FirstOrDefault(n => n.Id == id);
            if (Notification == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Notification Not Found"));
            }


            db.DeleteObject(Notification);
            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.OK);
        }
    }
}

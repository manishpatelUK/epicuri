using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.CPE.Controllers
{
    public class ScheduleItemController : Models.EpicuriApiController
    {
        public HttpResponseMessage PostScheduleItem(Models.ScheduleItem scheduleItem)
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

            var Service = Restaurant.Services.FirstOrDefault(s => s.Id == scheduleItem.ServiceId);
            if (Service == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Service Not Found"));
            }

            var ScheduleItem = scheduleItem.ToScheduleItem();

            foreach(var notification in scheduleItem.Notifications)
            {
                var Notification = Restaurant.Notifications.FirstOrDefault(n => n.Id == notification.Id);
                if (Notification == null)
                {
                    return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Notification not found"));
                }
                ScheduleItem.Notifications.Add(Notification);
            }

            Service.ScheduleItems.Add(ScheduleItem);
            Service.Updated = DateTime.UtcNow;
            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.Created, new Models.ScheduleItem(ScheduleItem));
        }



        public HttpResponseMessage PutScheduleItem(int id, Models.ScheduleItem scheduleItem)
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

            var ScheduleItem = db.ScheduleItems.FirstOrDefault(s => s.Id == id);
            if (ScheduleItem == null) 
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Schedule Item Not Found"));
            }

            var OldService = Restaurant.Services.FirstOrDefault(s => s.Id == ScheduleItem.ServiceId);
            if (OldService == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Schedule Item Not Found"));
            }



            var Service = Restaurant.Services.FirstOrDefault(s => s.Id == scheduleItem.ServiceId);
            if (Service == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Service Not Found"));
            }

            //EP-342
           // ScheduleItem.Order = Convert.ToInt16(scheduleItem.Order);
            ScheduleItem.Delay = scheduleItem.Delay;
            ScheduleItem.Comment = "";

            foreach (var notification in ScheduleItem.Notifications.ToList())
            {
                ScheduleItem.Notifications.Remove(notification);
            }

            foreach (var notification in scheduleItem.Notifications)
            {
                var Notification = Restaurant.Notifications.FirstOrDefault(n => n.Id == notification.Id);
                if (Notification == null)
                {
                    return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Notification not found"));
                }
                ScheduleItem.Notifications.Add(Notification);
            }

            
            Service.Updated = DateTime.UtcNow;
            OldService.Updated = DateTime.UtcNow;

            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.OK, new Models.ScheduleItem(ScheduleItem));
        }


        public HttpResponseMessage DeleteScheduleItem(int id)
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

            var ScheduleItem = db.ScheduleItems.FirstOrDefault(s => s.Id == id);
            if (ScheduleItem == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Schedule Item Not Found"));
            }

            var OldService = Restaurant.Services.FirstOrDefault(s => s.Id == ScheduleItem.ServiceId);
            if (OldService == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Schedule Item Not Found"));
            }

            db.DeleteObject(ScheduleItem);

            
            OldService.Updated = DateTime.UtcNow;

            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.OK);
        }

    }
}

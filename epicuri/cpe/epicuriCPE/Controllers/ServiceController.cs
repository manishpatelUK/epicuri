using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using epicuri.Core.DatabaseModel;

namespace epicuri.CPE.Controllers
{
    public class ServiceController : Models.EpicuriApiController
    {
        [HttpGet]
        public HttpResponseMessage GetServices()
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

            // Return all services that are not AdHoc

            var services = from service in this.Restaurant.Services
                           where service.Active && !service.ServiceName.Equals("AdHocService")
                           select new Models.Service(service);

            return Request.CreateResponse(HttpStatusCode.OK, services);
        }


        [HttpGet]
        public HttpResponseMessage GetService(int id)
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



            var services = from service in this.Restaurant.Services
                           where service.Id == id
                           select new Models.Service(service);


            var outService = services.FirstOrDefault();

            if (outService == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound);
            }
            return Request.CreateResponse<Models.Service>(HttpStatusCode.OK, outService);
        }


        [HttpPost]
        public HttpResponseMessage PostService(Models.Service service)
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


            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest);
            }

            /*
             * Check menus not null
             */
            if (service.SelfServiceMenuId!=0 && Restaurant.Menus.FirstOrDefault(m => m.Id == service.SelfServiceMenuId) == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Self service menu not found"));
            }

            if (Restaurant.Menus.FirstOrDefault(m => m.Id == service.MenuId) == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Main menu not found"));
            }

            Service newServce = service.ToService();
            Restaurant.Services.Add(newServce);
             
            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.Created, new Models.Service(newServce));
        }



        
        [HttpPut]
        public HttpResponseMessage PutService(int id, Models.Service service)
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


            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest);
            }

            var dbService = Restaurant.Services.FirstOrDefault(s => s.Id == id);
            if (dbService == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Service not found"));
            }

            /*
             * Check menus not null
             */
            if (service.SelfServiceMenuId != 0 && Restaurant.Menus.FirstOrDefault(m => m.Id == service.SelfServiceMenuId) == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Self service menu not found"));
            }

            if (Restaurant.Menus.FirstOrDefault(m => m.Id == service.MenuId) == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Main menu not found"));
            }


            dbService.Notes = service.Notes;
            dbService.ServiceName = service.ServiceName;
            dbService.MenuId = service.MenuId;
            dbService.MenuId1 = service.SelfServiceMenuId == 0 ? new int?() : service.SelfServiceMenuId;
            dbService.Updated = DateTime.UtcNow;

            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.OK, new Models.Service(dbService));
        }

        

        [HttpDelete]
        public HttpResponseMessage DeleteService(int id)
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


            var dbService = Restaurant.Services.FirstOrDefault(s => s.Id == id);
            if (dbService == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Service not found"));
            }

            if (dbService.IsTakeaway)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Unable to delete service (service is the current takeaway menu)"));
            }

            dbService.IsTakeaway = false;

            dbService.Active = false;
            dbService.Deleted = DateTime.UtcNow;
            dbService.Updated = DateTime.UtcNow;

            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.OK);
        }
        
    }
}

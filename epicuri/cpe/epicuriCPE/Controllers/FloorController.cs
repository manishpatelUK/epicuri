using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Data.Entity.Infrastructure;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web;
using System.Web.Http;
using epicuri.Core.DatabaseModel;

namespace epicuri.CPE.Controllers
{
    public class FloorController : Models.EpicuriApiController
    {

        // GET api/Floor
        public HttpResponseMessage GetFloors()
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


            var floors = from f in db.Floors
                         where  f.RestaurantId == this.Restaurant.Id && f.Deleted != true
                         select new Models.Floor
                         {
                             Capacity = f.Capacity,
                             Name = f.Name,
                             ImageURL = f.Resource.CDNUrl,
                             Id = f.Id,
                             Layout = f.ActiveLayout.Id
                         };
                         
            return Request.CreateResponse(HttpStatusCode.OK, floors.AsEnumerable());
        }

        // GET api/Floor/5
        public HttpResponseMessage GetFloor(int id)
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


            var floors = from f in db.Floors
                         where f.RestaurantId == this.Restaurant.Id &&
                                 f.Id == id &&
                                 f.Deleted != true
                         select new Models.Floor
                         {
                             Capacity = f.Capacity,
                             Name = f.Name,
                             ImageURL = f.Resource.CDNUrl,
                             Id = f.Id,
                             Scale = f.Scale,
                             Layout = f.ActiveLayout.Id,
                             Layouts = (from l in db.Layouts
                                       where l.FloorId == id && l.Temporary == false
                                       select l).AsEnumerable().Select(l=>
                                       new Models.Layout
                                       {
                                           _Updated = l.LastModified,
                                           Id= l.Id,
                                           Name = l.Name,
                                           Floor = f.Id,
                                           Scale = f.Scale,
                                       })
                         };

            var floor = floors.FirstOrDefault();
            if (floor == null)
            {
                throw new HttpResponseException(Request.CreateResponse(HttpStatusCode.NotFound));
            }

            return Request.CreateResponse(HttpStatusCode.OK, floor);
        }

        [HttpPut]
        public HttpResponseMessage PutFloor(int id, Models.Layout layout)
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

            var floors = from f in db.Floors
                         where f.RestaurantId == this.Restaurant.Id &&
                                 f.Id == id
                         select f;

            Floor floor = floors.FirstOrDefault();
            if (floor == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound,new Exception("Floor not found"));
            }

            var layouts = from l in db.Layouts
                          where l.Id == layout.Id && l.Floor.RestaurantId == this.Restaurant.Id
                                 
                         select l;

            if (layouts.FirstOrDefault() == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Layout not found"));
            }
            
            floor.LayoutId = layout.Id;
            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK);
        }
       

        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }
    }
}
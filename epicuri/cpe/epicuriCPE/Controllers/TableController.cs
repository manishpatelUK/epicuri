using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.CPE.Controllers
{
    public class TableController : Models.EpicuriApiController
    {

        public HttpResponseMessage GetTables()
        {
            try
            {
                Authenticate();
            }
            catch
            {
                return Request.CreateResponse(HttpStatusCode.Unauthorized);
            }


            var tables = from table in Restaurant.Tables
                         select new Models.Table
                         {
                             Name = table.Name,
                             Id = table.Id,
                             DefaultCovers = table.DefaultCovers,
                             Shape = table.Shape,
                             
                         };

            return Request.CreateResponse(HttpStatusCode.OK, tables.ToList());
        }

        
        public HttpResponseMessage PostTable(Models.Table table)
        {
            try
            {
                Authenticate();
            }
            catch
            {
                return Request.CreateResponse(HttpStatusCode.Unauthorized);
            }

            var dbtable = new Core.DatabaseModel.Table();

            dbtable.Name = table.Name;
            dbtable.DefaultCovers = table.DefaultCovers;
            dbtable.Shape = table.Shape;
            
            Restaurant.Tables.Add(dbtable);

            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK, new Models.Table(dbtable));

        }

        public HttpResponseMessage PutTable(int id, Models.Table table)
        {
            try
            {
                Authenticate();
            }
            catch
            {
                return Request.CreateResponse(HttpStatusCode.Unauthorized);
            }

            var dbtable = Restaurant.Tables.Where(t => t.Id == id).FirstOrDefault();

            if (dbtable == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Table not found"));

            }


            dbtable.Name = table.Name;
            dbtable.DefaultCovers = table.DefaultCovers;
            dbtable.Shape = table.Shape;

            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK, new Models.Table(dbtable));
        }

        public HttpResponseMessage DeleteTable(int id)
        {
            try
            {
                Authenticate();
            }
            catch
            {
                return Request.CreateResponse(HttpStatusCode.Unauthorized);
            }

            var table = Restaurant.Tables.Where(t => t.Id == id).FirstOrDefault();

            if (table == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Table not found"));

            }

            Restaurant.Tables.Remove(table);
            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.NoContent);
        }
    }
}

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
    public class LayoutController : Models.EpicuriApiController
    {


        // GET api/Layout/5
        public HttpResponseMessage GetLayouts(int id)
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


            var layouts = (from l in db.Layouts
                         where l.Id == id
                         select l).AsEnumerable().Select(l=> new Models.Layout
                         {
                             Id = l.Id,
                             Name = l.Name,
                             _Updated = l.LastModified,
                             Floor = l.FloorId,
                             Scale = l.Floor.Scale,
                             Temporary = l.Temporary,
                             Tables =  from t in l.Tables
                                       select new Models.Table
                                       {
                                           Position = t.Position,
                                           Id = t.Table.Id,
                                           Name = t.Table.Name,
                                           DefaultCovers = t.Table.DefaultCovers,
                                           Shape = t.Table.Shape,
                                           
                                       }
                         });

            var layout = layouts.FirstOrDefault();
            if (layout == null)
            {
                throw new HttpResponseException(Request.CreateResponse(HttpStatusCode.NotFound));
            }

            return Request.CreateResponse(HttpStatusCode.OK, layout);
        }

        public HttpResponseMessage PostLayout(Models.Layout newLayout)
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
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Model state is invalid"));
            }

            /*
             * Check floor exists
             */
            try
            {
                Restaurant.Floors.Single(f => f.Id == newLayout.Floor);
            }
            catch
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Floor not found"));
            }


            /*
             * Create new layout object
             */
            Layout layout = new Layout
            {
                Name = newLayout.Name,
                LastModified = DateTime.UtcNow,
                FloorId = newLayout.Floor,
                Temporary = newLayout.Temporary

            };
          

            foreach (Models.Table table in newLayout.Tables)
            {
                Table tab;

                /*
                 * Check each table exists
                 */
                try
                {
                    tab = db.Tables.Single(f => f.Id == table.Id);
                }
                catch
                {
                    return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Table not found"));
                }


                layout.Tables.Add(new TableLayout
                {
                    Position = table.Position,
                    TableId= tab.Id,
                });
               


            }
            db.AddToLayouts(layout);
            db.SaveChanges();




            return Request.CreateResponse(HttpStatusCode.Created, new { Id = layout.Id });
        }

        public HttpResponseMessage PutLayout(int id, Models.Layout newLayout)
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
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Model state is invalid"));
            }


            Layout layout = db.Layouts.Where(l => l.Id == id && l.Floor.RestaurantId == Restaurant.Id).FirstOrDefault();

            if (layout == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Layout not found"));
            }
            var Tables = layout.Tables.ToList();
            layout.Name = newLayout.Name;
            layout.LastModified = DateTime.UtcNow;
            layout.Temporary = newLayout.Temporary;
          

            /*
             * Remove tables where there are no sessions
             */
            foreach(var table in Tables)
            {
                if (newLayout.Tables.Count(t => t.Id == table.TableId) > 0)
                {
                    layout.Tables.Remove(table);
                    db.TableLayouts.DeleteObject(table);
                }
                else
                {
                    if (Restaurant.Sessions.OfType<SeatedSession>().Where(s => s.ClosedTime == null && s.Tables.Contains(table.Table)).Count() == 0)
                    {
                        layout.Tables.Remove(table);
                        db.TableLayouts.DeleteObject(table);
                    }
                }
            }


            foreach (Models.Table table in newLayout.Tables)
            {
                Table tab;

                /*
                 * Check each table exists
                 */
                try
                {
                    tab = db.Tables.Single(f => f.Id == table.Id);
                }
                catch
                {
                    return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Table not found"));
                }


                layout.Tables.Add(new TableLayout
                {
                    Position = table.Position,
                    TableId = tab.Id,
                    
                });



            }


            db.SaveChanges();




            return Request.CreateResponse(HttpStatusCode.Created, new { Id = layout.Id });
        }

    }
}
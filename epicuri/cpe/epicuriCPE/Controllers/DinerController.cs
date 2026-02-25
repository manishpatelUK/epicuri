using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using epicuri.Core.DatabaseModel;
using epicuri.Core;

namespace epicuri.CPE.Controllers
{
    public class DinerController : Models.EpicuriApiController
    {
        public HttpResponseMessage PostDiner(Models.Diner diner)
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



            /*
             * Check that the session matches the restaurant and is allowed
             */
            Session sess = db.Sessions.FirstOrDefault(s => s.Id == diner.SessionId && s.RestaurantId == Restaurant.Id);
            if (sess == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Session not found"));
            }

            if (sess.ClosedTime != null)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Session is closed"));
            }

            Diner dbDiner = diner.ToDiner();


            /*
             * Check that the epicuri user is OK
             */
            if (diner.EpicuriUser != null && diner.EpicuriUser.Id !=  0)
            {
                DateTime min = DateTime.UtcNow.AddHours(-12);
                CheckIn c = db.CheckIns.Where(ci => ci.Time > min && ci.Restaurant.Id == this.Restaurant.Id && ci.Customer.Id == diner.EpicuriUser.Id).FirstOrDefault();
                if (c == null)
                {
                    return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("User not found or has not checked in recently"));
                }

                dbDiner.CustomerId = c.Customer.Id;
                
                
            } 
            
            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.Created);

        }

        public HttpResponseMessage PutDiner(int id, Models.Diner diner)
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

            


            /*
             * Retrieve diner from the db
             */
            Diner dbDiner = db.Diners.FirstOrDefault(d => d.Id == id);
            if (dbDiner == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Diner not found"));
            }


            /*
             * Check that the session matches the restaurant and is allowed
             */
            Session sess = db.Sessions.FirstOrDefault(s => s.Id == dbDiner.SeatedSessionId && s.RestaurantId == Restaurant.Id);
            if (sess == null)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Session belongs to a different restaurant"));
            }

            if (sess.ClosedTime != null)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Session is closed"));
            }

            /*
             * Check that the epicuri user is OK
             */
            if (diner.EpicuriUser != null)
            {
                DateTime min = DateTime.UtcNow.AddMinutes(- Settings.Setting<double>(Restaurant.Id, "CheckinExpirationTime"));
                CheckIn c = db.CheckIns.Where(ci => ci.Time > min && ci.Restaurant.Id == this.Restaurant.Id && ci.Customer.Id == diner.EpicuriUser.Id).FirstOrDefault();
                if (c == null)
                {
                    return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("User not found or has not checked in recently"));
                }

                dbDiner.CustomerId = c.Customer.Id;
                c.Diner = dbDiner;
                db.SaveChanges();
                return Request.CreateResponse(HttpStatusCode.NoContent);
            }

            return Request.CreateResponse(HttpStatusCode.NotModified);
            
        }

        [HttpDelete]
        [ActionName("DeleteDiner")]
        public HttpResponseMessage DeleteDiner(int id)
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




            /*
             * Retrieve diner from the db
             */
            Diner dbDiner = db.Diners.FirstOrDefault(d => d.Id == id);
            if (dbDiner == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Diner not found"));
            }


            /*
             * Check that the session matches the restaurant and is allowed
             */
            Session sess = db.Sessions.OfType<SeatedSession>().FirstOrDefault(s => s.Id == dbDiner.SeatedSessionId && s.RestaurantId == Restaurant.Id);
            if (sess == null)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Session belongs to a different restaurant"));
            }

            if (sess.ClosedTime != null)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Session is closed"));
            }

            if (dbDiner.IsTable)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Cannot delete table object"));
            }

            if (dbDiner.Orders.Count>0)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Diner has orders, cannot remove"));
            }


            db.Diners.DeleteObject(dbDiner);
            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.NoContent);
        }

        [HttpDelete]
        [ActionName("DisassociateCheckIn")]
        public HttpResponseMessage DeleteDisassociateCheckIn(int id)
        {
            //Takes a dinerId and removes the customerId and the checkIn associated
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

            Diner dbDiner = db.Diners.FirstOrDefault(d => d.Id == id);
            if (dbDiner == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Diner not found"));
            }


            if (dbDiner.Customer == null)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Diner is not associated with a checkin"));
            }
            CheckIn checkIn = dbDiner.Customer.CheckIns.Where(ci=>ci.Diner != null && ci.Diner.Id==id).Single();

            dbDiner.Customer = null;
            dbDiner.CustomerId = null;



            db.CheckIns.DeleteObject(checkIn);
        



            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.NoContent);
        }

    }
}
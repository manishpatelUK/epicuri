using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Net;
using System.Net.Http;
using epicuri.Core.DatabaseModel;
using System.Web.Mvc;

namespace epicuri.CPE.Controllers
{
    public class AdjustmentController : Models.EpicuriApiController
    {
        
       
        public HttpResponseMessage PostAdjustment(Models.Adjustment adjustment)
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
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state"));
            }

            Session session = db.Sessions.FirstOrDefault(s => s.Id == adjustment.SessionId);

            //Get the adjustment type and ensure it exists
            var adjustmentType = db.AdjustmentTypes.FirstOrDefault(at=>at.Id==adjustment.TypeId);
            if(adjustmentType == null)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Adjustment type does not exist"));
            }


            //If the adjustment is a discount, prevent the value of the adjustment from making the session balance negative
            if ((Core.DatabaseModel.Enums.AdjustmentTypeType)adjustmentType.Type == Core.DatabaseModel.Enums.AdjustmentTypeType.Discount) 
            {
                if ((Core.DatabaseModel.Enums.NumericalTypeType)adjustment.NumericalTypeId == Core.DatabaseModel.Enums.NumericalTypeType.Percentage)
                {
                    adjustment.Value = Math.Min(100,adjustment.Value);
                    adjustment.Value = Math.Max(0, adjustment.Value);
                }

                if ((Core.DatabaseModel.Enums.NumericalTypeType)adjustment.NumericalTypeId == Core.DatabaseModel.Enums.NumericalTypeType.Percentage && new Models.Session(session).Total <= 0)
                {
                    adjustment.Value = 0;
                }

                if ((Core.DatabaseModel.Enums.NumericalTypeType)adjustment.NumericalTypeId == Core.DatabaseModel.Enums.NumericalTypeType.Price && new Models.Session(session).Total < adjustment.Value)
                {
                    adjustment.Value = new Models.Session(session).Total;
                }
            }
            else
            {
                if ((Core.DatabaseModel.Enums.NumericalTypeType)adjustment.NumericalTypeId == Core.DatabaseModel.Enums.NumericalTypeType.Percentage && new Models.Session(session).RemainingTotal <= 0)
                {
                   
                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("You cannot add a percentage payment to a session that has been fully paid."));
            
                }
            }

           
            Adjustment a = new Adjustment
            {
                SessionId = session.Id,
                AdjustmentTypeId = (int)adjustment.TypeId,
                NumericalType = (int)adjustment.NumericalTypeId,
                Value = (double) adjustment.Value,
                Reference = adjustment.Reference,
                Created = DateTime.UtcNow,
                StaffId=Staff.Id
            };

            session.Adjustments.Add(a);

            //db.AddToAdjustments(a);
            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK); 
            


        }

        [HttpDelete]
        public HttpResponseMessage DeleteAdjustment(int id)
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


            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }


            /*
             * Check item exists
             */
            var Item = db.Adjustment.Where(i => i.Id == id && !i.Deleted.HasValue).FirstOrDefault();
            if (Item == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Adjustment not found"));
            }


            Item.Deleted = DateTime.UtcNow;
            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK);


        }
        
    }
}
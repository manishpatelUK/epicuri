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
    public class MewsController : Models.EpicuriApiController
    {
        public HttpResponseMessage GetCustomers(string name = null, string room = null)
        {
            try
            {
                Authenticate();
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

            if (!this.Restaurant.MewsIntegration || string.IsNullOrEmpty(this.Restaurant.MewsAccessToken))
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Mews is not configured for this restaurant"));
            }

            Mews.MewsResponse result = Mews.MewsAPI.SearchCustomer(this.Restaurant.MewsAccessToken, name, room);

            if (result.Code == HttpStatusCode.OK)
            {

                var customers = result.Result;

                //TODO: create appropriate reponse for displaying to waiter
                return Request.CreateResponse(HttpStatusCode.OK, customers);
            }
            else
            {
                return Request.CreateResponse(result.Code, new Exception("There was an error communicating with Mews (" + result.Code + "): " + result.Result.ToString()));
            }
            
            
        }

        [HttpPost]
        public HttpResponseMessage PostAdjustment(Mews.MewsPayload adjustment)
        {
            try
            {
                Authenticate("Manager");
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

            var dbSession = db.Sessions.FirstOrDefault(s => s.Id == adjustment.SessionId);

            if (dbSession == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Session does not exist"));
            }

            // Check restaurant supports Mews
            if (!this.Restaurant.MewsIntegration || string.IsNullOrEmpty(this.Restaurant.MewsAccessToken))
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Restaurant does not support Mews payments or the Access Token has not been set"));
            }

            if (dbSession.Adjustments.Count(a => a.AdjustmentType.Type == (int)Enums.AdjustmentTypeType.Payment && a.NumericalType == (int)Enums.NumericalTypeType.Price && !a.Deleted.HasValue) != 0)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Mews charge cannot be made as monetary adjustments have already been made against the session"));
            }

            Models.Session session = dbSession.GetType() == typeof(Core.DatabaseModel.SeatedSession) ? (Models.Session)new Models.SeatedSession((Core.DatabaseModel.SeatedSession)dbSession) : (Models.Session)new Models.TakeawaySession((Core.DatabaseModel.TakeAwaySession)dbSession);

            // Submit the charge to Mews
            Mews.MewsCharge charge = new Mews.MewsCharge();

            if (session.Paid || session.Total != adjustment.PaymentAmount)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("The session has already been paid, or the payment amount doesn't match the session total"));
            }

            charge.Customer = adjustment.Customer;
            charge.Notes = "Session Id: " + session.Id;

            charge.Items = new List<Mews.MewsChargeItem>();

            foreach (var order in session.Orders)
            {
                string category = null;

                switch ((Enums.MenuItemType)order.MenuItem.MenuItemTypeId)
                {
                    case Enums.MenuItemType.Food:
                        category = "FOOD";
                        break;
                    case Enums.MenuItemType.Drink:
                        category = "DRINK";
                        break;
                    default:
                        category = "OTHER";
                        break;
                }

            
                charge.Items.Add(new Mews.MewsChargeItem()
                {
                    Name = order.MenuItem.Name,
                    UnitCount = 1,
                    UnitCost = new Mews.MewsChargeItemUnitCost()
                    {
                        Amount = order.OrderValueAfterAdjustment(session),
                        Currency = this.Restaurant.ISOCurrency,
                        Tax = order.VatRate/100
                    },
                    Category = new Mews.MewsChargeItemCategory()
                    {
                        Code = category,
                        Name = category
                    }
                });
            }

            if (session.Tips > 0)
            {
                charge.Items.Add(new Mews.MewsChargeItem()
                {
                    Name = "TIP",
                    UnitCount = 1,
                    UnitCost = new Mews.MewsChargeItemUnitCost()
                    {
                        Amount = session.Tips,
                        Currency = Restaurant.ISOCurrency,
                        Tax = 0,
                    },
                    Category = new Mews.MewsChargeItemCategory

                    {
                        Code="TIP",
                        Name="TIP"
                    }
                });
            }
            

            var result = Mews.MewsAPI.ChargeCustomer(this.Restaurant.MewsAccessToken, charge);

            if (result.Code == HttpStatusCode.OK)
            {
                // Save the adjustment to the database
                MewsAdjustment a = new MewsAdjustment
                {
                    AdjustmentTypeId = -1, // Hardcoded default value for Mews paymetns
                    SessionId = session.Id,
                    NumericalType = (int)Enums.NumericalTypeType.Price,
                    Value = (double)adjustment.PaymentAmount,
                    Reference = (string)result.Result,
                    Created = DateTime.UtcNow,
                    FirstName = adjustment.Customer.FirstName,
                    LastName = adjustment.Customer.LastName,
                    RoomNo = adjustment.Customer.RoomNumber,
                    ChargeId = (string)result.Result,
                    StaffId = this.Staff.Id
                };

                db.Adjustment.AddObject(a);

                // Flag session as paid
                dbSession.Paid = true;

                db.SaveChanges();

                return Request.CreateResponse(HttpStatusCode.OK);
            }
            else
            {
                return Request.CreateResponse(result.Code, new Exception("There was an error charging the payment to the customer "+ result.Code + " " + result.Result.ToString()));
            }
        }

    }
}
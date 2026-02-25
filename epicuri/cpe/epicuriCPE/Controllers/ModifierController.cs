using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.CPE.Controllers
{
    public class ModifierController : Models.EpicuriApiController
    {
        [HttpPost]
        public HttpResponseMessage PostModifier(Models.Modifier modifier)
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
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid Model State" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }


            var NewModGroup = Restaurant.ModifierGroups.Where(g => g.Id == modifier.ModifierGroupId).FirstOrDefault();
            if (NewModGroup == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Modifier Group Not Found"));
            }



            var mm = new Core.DatabaseModel.Modifier
            {
                Cost = modifier.Price,
                ModifierValue = modifier.ModifierValue,
                TaxType = db.TaxTypes.Where(t => t.Id == modifier.TaxTypeId).FirstOrDefault(),
                ModifierGroupId = modifier.ModifierGroupId
            };


            NewModGroup.Modifiers.Add(mm);
            db.SaveChanges();


            return Request.CreateResponse(HttpStatusCode.OK, new Models.Modifier(mm));

            
        }


        [HttpPut]
        public HttpResponseMessage PutModifier(int id, Models.Modifier modifier)
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
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid Model State: " + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }


            var Modifier = db.Modifiers.FirstOrDefault(m => m.Id == id);
            if (Modifier == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Not found"));
            }

            Modifier.Deleted = true;

            var NewModGroup = db.ModifierGroups.Where(g => g.Id == modifier.ModifierGroupId).FirstOrDefault();
            if (NewModGroup == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Modifier Group Not Found"));
            }



            var mm = new Core.DatabaseModel.Modifier
            {
                Cost = modifier.Price,
                ModifierValue = modifier.ModifierValue,
                TaxType = db.TaxTypes.Where(t=>t.Id == modifier.TaxTypeId).FirstOrDefault(),
                ModifierGroupId = modifier.ModifierGroupId
            };


            NewModGroup.Modifiers.Add(mm);
            db.SaveChanges();


            return Request.CreateResponse(HttpStatusCode.OK, new Models.Modifier(mm));
        }



        [HttpDelete]
        public HttpResponseMessage DeleteModifier(int id)
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
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid Model State" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }

            var Modifier = db.Modifiers.FirstOrDefault(m => m.Id == id && !m.Deleted);
            if (Modifier == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Not found"));
            }

            Modifier.Deleted = true;
            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK);

        }

    }
}

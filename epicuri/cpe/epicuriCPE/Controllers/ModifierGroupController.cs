using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.CPE.Controllers
{
    public class ModifierGroupController : Models.EpicuriApiController
    {
        [HttpGet]
        public HttpResponseMessage GetModifierGroups()
        {
            try
            {
                Authenticate();
            }
            catch
            {
                return Request.CreateResponse(HttpStatusCode.Unauthorized, new Exception("Unauthorized"));
            }

            var Groups = Restaurant.ModifierGroups.Where(m => m.Deleted == false).Select(m => new Models.ModifierGroup(m));

            return Request.CreateResponse(HttpStatusCode.OK,Groups);
        }

        public HttpResponseMessage GetModifierGroup(int id)
        {
            try
            {
                Authenticate();
            }
            catch
            {
                return Request.CreateResponse(HttpStatusCode.Unauthorized, new Exception("Unauthorized"));
            }

            var Group = Restaurant.ModifierGroups.FirstOrDefault(g=>g.Id==id);

            return Request.CreateResponse(HttpStatusCode.OK, new Models.ModifierGroup(Group));
        }

        [HttpPost]
        public HttpResponseMessage PostModifierGroup(Models.ModifierGroup group)
        {
            try
            {
                Authenticate();
            }
            catch
            {
                return Request.CreateResponse(HttpStatusCode.Unauthorized, new Exception("Unauthorized"));
            }


            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest);
            }



            Core.DatabaseModel.ModifierGroup Group = new Core.DatabaseModel.ModifierGroup
            {
                GroupName = group.GroupName,
                UpperLimit = Convert.ToInt16(group.UpperLimit),
                LowerLimit = Convert.ToInt16(group.LowerLimit)
               
            };

            Restaurant.ModifierGroups.Add(Group);
            db.SaveChanges();


            return Request.CreateResponse(HttpStatusCode.OK, new Models.ModifierGroup(Group));
        }



        [HttpPut]
        public HttpResponseMessage PutModifierGroup(int id, Models.ModifierGroup group)
        {
            try
            {
                Authenticate();
            }
            catch
            {
                return Request.CreateResponse(HttpStatusCode.Unauthorized, new Exception("Unauthorized"));
            }


            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest);
            }

            var dbGroup = Restaurant.ModifierGroups.FirstOrDefault(g => g.Id == id);
            if (dbGroup == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Group not found"));
            }

            dbGroup.GroupName = group.GroupName;
            dbGroup.UpperLimit = Convert.ToInt16(group.UpperLimit);
            dbGroup.LowerLimit = Convert.ToInt16(group.LowerLimit);

            db.SaveChanges();


            return Request.CreateResponse(HttpStatusCode.OK, new Models.ModifierGroup(dbGroup));
        }



        [HttpDelete]
        public HttpResponseMessage DeleteModifierGroup(int id)
        {
            try
            {
                Authenticate();
            }
            catch
            {
                return Request.CreateResponse(HttpStatusCode.Unauthorized, new Exception("Unauthorized"));
            }


            var dbGroup = Restaurant.ModifierGroups.FirstOrDefault(g => g.Id == id);
            if (dbGroup == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Group not found"));
            }

            dbGroup.Deleted = true;
            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.OK);
        }





    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.CPE.Controllers
{
    public class StaffController : Models.EpicuriApiController
    {
        // GET api/<controller>
        public HttpResponseMessage Get()
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


            var allStaff = from staff in this.Restaurant.Staffs
                           select new Models.Staff(staff);

            return Request.CreateResponse(HttpStatusCode.OK, allStaff);
        }


        public HttpResponseMessage PostStaff(Models.Staff staff)
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

            if(Restaurant.Staffs.Count(s=>s.Username == staff.Username)>0)
            {
                return Request.CreateResponse(HttpStatusCode.Conflict, new Exception("Duplicate user exists"));
            }

            var Staff = new Core.DatabaseModel.Staff
            {
                Name = staff.Name,
                Username = staff.Username,
                Pin = staff.Pin,
                Phone = "",
            };

            if (staff.Manager)
            {
                Staff.Roles.Add(new Core.DatabaseModel.Role
                {
                    Name = "Manager"
                });
            }

            string Salt = API.Support.Salt.GetSalt();
            string Auth = Salt + staff.Password + Salt;
            Auth = epicuri.API.Support.String.SHA1(Auth);
            Staff.Auth = Auth;
            Staff.Salt = Salt;

            Restaurant.Staffs.Add(Staff);
            db.SaveChanges();


            return Request.CreateResponse(HttpStatusCode.Created, new Models.Staff(Staff));
        }


        public HttpResponseMessage PutStaff(int id, Models.Staff staff)
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

            var Staff = Restaurant.Staffs.Where(s=>s.Id == id).FirstOrDefault();

            if (Staff == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Not found"));
            }

            Staff.Roles.Clear();
            /*
            foreach (var role in Staff.Roles.ToList())
            {
                Staff.Roles.Remove(role);
            }
             */

            if (staff.Manager)
            {
                Staff.Roles.Add(new Core.DatabaseModel.Role
                {
                    Name = "Manager"
                });
            }

            Staff.Name = staff.Name;
            Staff.Username = staff.Username;
            Staff.Phone = "";

            if (!string.IsNullOrEmpty(staff.Pin))
            {
                Staff.Pin = staff.Pin;
            }
            
            if (!string.IsNullOrEmpty(staff.Password))
            {
                string Salt = API.Support.Salt.GetSalt();
                string Auth = Salt + staff.Password + Salt;
                Auth = epicuri.API.Support.String.SHA1(Auth);
                Staff.Auth = Auth;
                Staff.Salt = Salt;
            }

            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.OK, new Models.Staff(Staff));
        }

        public HttpResponseMessage DeleteStaff(int id)
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

            var Staff = Restaurant.Staffs.Where(s => s.Id == id).FirstOrDefault();

            if (Staff == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Not found"));
            }

            foreach (var role in Staff.Roles.ToList())
            {
                Staff.Roles.Remove(role);
            }

            Restaurant.Staffs.Remove(Staff);
            db.DeleteObject(Staff);
            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.OK);
        }
    }
}
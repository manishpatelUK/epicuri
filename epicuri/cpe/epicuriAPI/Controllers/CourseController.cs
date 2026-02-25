using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using System.Net.Http;
using System.Net;

namespace epicuri.API.Controllers
{
    public class CourseController : Support.APIController
    {
        //
        // GET: /Course/

        public HttpResponseMessage Index(int Id)
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


            var courses = from course in db.Courses
                          where course.RestaurantId ==Id
                          orderby course.Ordering ascending
                          select new CPE.Models.Course(course);
            return Request.CreateResponse(HttpStatusCode.OK, courses);
        }

      
    }
}

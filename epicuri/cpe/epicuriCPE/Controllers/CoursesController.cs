using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.CPE.Controllers
{
    public class CourseController : Models.EpicuriApiController
    {
        public HttpResponseMessage GetCourses()
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


            var courses = from course in this.Restaurant.Courses
                          orderby course.Ordering ascending
                          select new Models.Course(course);
            return Request.CreateResponse(HttpStatusCode.OK, courses);

        }

        [HttpPost]
        public HttpResponseMessage PostCourse(Models.Course course)
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
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }

            var Service = Restaurant.Services.Where(s => s.Id == course.ServiceId).FirstOrDefault();

            if (Service == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Service not found"));
            }


            var Course = course.ToCourse();
            Service.Courses.Add(Course);
            Restaurant.Courses.Add(Course);
            Service.Updated = DateTime.UtcNow;
            db.SaveChanges();


            return Request.CreateResponse(HttpStatusCode.Created, new Models.Course(Course));

        }

        [HttpPut]
        public HttpResponseMessage PutCourse(int id, Models.Course course)
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
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }

            var Course = Restaurant.Courses.Where(s => s.Id == id).FirstOrDefault();

            if (Course == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Course not found"));
            }


            var Service = Restaurant.Services.Where(s => s.Id == course.ServiceId).FirstOrDefault();

            if (Service == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Service not found"));
            }



            Course.ServiceId = course.ServiceId;
            Course.Name = course.Name;
            Course.Ordering = Convert.ToInt16(course.Ordering);
            
            
            Service.Updated = DateTime.UtcNow;
            db.SaveChanges();


            return Request.CreateResponse(HttpStatusCode.OK, new Models.Course(Course));


        }

        public HttpResponseMessage DeleteCourse(int id)
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
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }

            var Course = Restaurant.Courses.Where(s => s.Id == id).FirstOrDefault();

            if (Course == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Course not found"));
            }


            db.DeleteObject(Course);

            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.OK);
        }

    }
}

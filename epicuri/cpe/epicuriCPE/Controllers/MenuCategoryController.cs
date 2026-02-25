using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.CPE.Controllers
{
    public class MenuCategoryController : Models.EpicuriApiController
    {
        [HttpPost]
        public HttpResponseMessage PostCategory(Models.MenuCategory category)
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


            /*
             * Check menu exists
             */
            var Menu = Restaurant.Menus.Where(m => m.Id == category.MenuId).FirstOrDefault();
            if (Menu == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Menu not found"));
            }




            /*
             * Add category to menu
             */
            var Cat = new Core.DatabaseModel.MenuCategory
            {
                CategoryName = category.CategoryName,
                Order = category.Order
            };
            Menu.MenuCategories.Add(Cat);

            /*
             * Add all courses to category
             */
            foreach (int courseId in category.DefaultCourseIds)
            {
                var dbCourse = db.Courses.FirstOrDefault(c => c.Id == courseId && c.RestaurantId == Restaurant.Id);
                if (dbCourse != null)
                {
                    Cat.Courses.Add(dbCourse);
                }
                else
                {
                    return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Course " + courseId + " not found"));
                }
            }

            /*
             * Set menu as updated
             */
            Menu.LastUpdated = DateTime.UtcNow;



            /*
             * Save
             */
            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.Created, new Models.MenuCategory(Cat));
        }



        [HttpPut]
        public HttpResponseMessage PutCategory(int id, Models.MenuCategory cat)
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


            /*
             * Check menu exists
             */
            var Menu = Restaurant.Menus.Where(m => m.Id == cat.MenuId).FirstOrDefault();
            if (Menu == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Menu not found"));
            }


            /*
             * Check category exists
             */
            var Category = db.MenuCategories.Where(c => c.Id == id).FirstOrDefault();
            if (Category == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Category not found"));
            }
            Category.Order = cat.Order;



            Category.CategoryName = cat.CategoryName;
            Category.Menu = Menu;

            /*
             * Remove all courses
             */
            foreach (var course in Category.Courses.ToList())
            {
                Category.Courses.Remove(course);
            }

            /*
             * Update courses
             */
            foreach (int courseId in cat.DefaultCourseIds)
            {
                var dbCourse = db.Courses.FirstOrDefault(c => c.Id == courseId && c.RestaurantId == Restaurant.Id);
                if (dbCourse != null)
                {
                    Category.Courses.Add(dbCourse);
                }
                else
                {
                    return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Course " + courseId + " not found"));
                }
            }

            /*
             * Set menu as updated
             */
            Menu.LastUpdated = DateTime.UtcNow;

            /*
             * Save
             */
            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.OK, new Models.MenuCategory(Category));

        }




        [HttpDelete]
        public HttpResponseMessage DeleteCategory(int id)
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

            var Category = db.MenuCategories.Where(c => c.Id == id).FirstOrDefault();

            if (Category == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Category not found"));
            }

            Category.Deleted = true;

            var deleteMenuGroups = db.MenuGroups.Where(mg => mg.MenuCategoryId == id);

            foreach (var mg in deleteMenuGroups)
            {
                foreach (var menuItem in mg.MenuItems.ToList())
                {
                    menuItem.MenuGroups.Remove(mg);
                }

                db.MenuGroups.DeleteObject(mg);
            }

            
            //db.MenuCategories.DeleteObject(Category);
            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK);

        }

    }
}

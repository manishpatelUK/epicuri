using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.CPE.Controllers
{
    public class MenuGroupController : Models.EpicuriApiController
    {
        // This method was added but it is never used (might be useful at some point)
        public HttpResponseMessage GetMenuGroups(Models.MenuCategory category)
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
                HttpResponseMessage responseMessage = Request.CreateResponse(HttpStatusCode.Unauthorized, e);
                responseMessage.Headers.Add("WWW-Authenticate", "Basic");
                return responseMessage;
            }

            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid Model State" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }


            /*
             * Check category exists
             */
            var Category = db.MenuCategories.Where(c => c.Id == category.Id && c.Menu.Restaurant.Id == Restaurant.Id).FirstOrDefault();
            if (Category == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Category not found"));
            }

            var response = Category.MenuGroups.Select(mg => new Models.MenuGroup(mg, Category)).ToList();

            return Request.CreateResponse(HttpStatusCode.Created, response); 
        }

        [HttpPost]
        public HttpResponseMessage PostGroup(Models.MenuGroup group)
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
            var Category = db.MenuCategories.Where(c => c.Id == group.MenuCategoryId && c.Menu.Restaurant.Id == Restaurant.Id).FirstOrDefault();
            if (Category == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Category not found"));
            }


            
            /*
             * Add category to menu
             */
            var Group = new Core.DatabaseModel.MenuGroup
            {
                GroupName= group.GroupName,
                Order = group.Order
            };
            Category.MenuGroups.Add(Group);

          

            /*
             * Set menu as updated
             */
            Category.Menu.LastUpdated = DateTime.UtcNow;

            /*
             * Save
             */
            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.Created, new Models.MenuGroup(Group,Category));
        }



        [HttpPut]
        public HttpResponseMessage PutGroup(int id, Models.MenuGroup group)
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
             * Check Group exists
             */
            var Group = db.MenuGroups.Where(c => c.Id == id && c.MenuCategory.Menu.Restaurant.Id == Restaurant.Id).FirstOrDefault();
            if (Group == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Group not found"));
            }


            /*
             * Check Category exists
             */
            var Category = db.MenuCategories.Where(c => c.Id == group.MenuCategoryId && c.Menu.Restaurant.Id == Restaurant.Id).FirstOrDefault();
            if (Category == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Category not found"));
            }


            /*
             * Remove all courses
             */
            foreach (var menuitem in Group.MenuItems.ToList())
            {
                Group.MenuItems.Remove(menuitem);
            }

            foreach (var menuitem in group.MenuItemIds)
            {
                Group.MenuItems.Add(Restaurant.MenuItems.FirstOrDefault(i => i.Id == menuitem));
            }


            /*
             * Update Group
             */
            Group.GroupName = group.GroupName;
            Group.MenuCategory = Category;

            Group.Order = group.Order;

            /*
             * Set menu as updated
             */
            Category.Menu.LastUpdated = DateTime.UtcNow;

            /*
             * Save
             */
            db.SaveChanges();
            return Request.CreateResponse(HttpStatusCode.Created, new Models.MenuGroup(Group, Category));

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


            /*
             * Check Group exists
             */
            var Group = db.MenuGroups.Where(c => c.Id == id && c.MenuCategory.Menu.Restaurant.Id == Restaurant.Id).FirstOrDefault();
            if (Group == null)
            {
                return Request.CreateResponse(HttpStatusCode.NotFound, new Exception("Group not found"));
            }


            /*
             * Remove all courses
             */
            foreach (var menuitem in Group.MenuItems.ToList())
            {
                Group.MenuItems.Remove(menuitem);
            }

            db.MenuGroups.DeleteObject(Group);
            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK);


        }

    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    public class MenuCategory : DataModel
    {
        public int Id;
        [Required]
        public string CategoryName;
        public IEnumerable<int> DefaultCourseIds;
        public IEnumerable<Models.Course> DefaultCourses;
        public IEnumerable<MenuGroup> MenuGroups;
        public int Order;
        [Required]
        public int MenuId;


        public MenuCategory() {}
        public MenuCategory(Core.DatabaseModel.MenuCategory cat)
        {
            Init();
            MenuId = cat.MenuId;
            Id = cat.Id;
            CategoryName = cat.CategoryName;
            DefaultCourses = cat.Courses.Select(course => new Models.Course(course));
            MenuGroups =    from grp in cat.MenuGroups
                            orderby grp.Order
                            select new Models.MenuGroup(grp, cat);
            Order = cat.Order;
        }



    }
}
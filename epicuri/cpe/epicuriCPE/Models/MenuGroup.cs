using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    public class MenuGroup
    {
        public int Id;
        [Required]
        public string GroupName;
        [Required]
        public int MenuCategoryId;
        public IEnumerable<MenuItem> MenuItems;
        public IEnumerable<int> MenuItemIds;
        //public IEnumerable<Course> DefaultCourses;
        public int Order;
        public MenuGroup() { }
        public MenuGroup(Core.DatabaseModel.MenuGroup grp, Core.DatabaseModel.MenuCategory cat)
        {
            Id = grp.Id;
            MenuCategoryId = grp.MenuCategoryId;
            GroupName = grp.GroupName;
            //DefaultCourses = cat.Courses.Select(course => new Models.Course(course));
            MenuItems = from item in grp.MenuItems
                        where item.Deleted == false
                        select new Models.MenuItem(item,cat);
            MenuItemIds = from item in grp.MenuItems
                          where item.Deleted == false
                          select item.Id;
            Order = grp.Order;
        }
    }
}
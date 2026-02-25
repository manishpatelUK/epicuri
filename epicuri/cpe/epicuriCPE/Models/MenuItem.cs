using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    public class MenuItem
    {
        public int Id;
        [Required]
        public int DefaultPrinter { get; set; }
        public MenuItem() { }
        public String TypeName;
        public MenuItem(Core.DatabaseModel.MenuItem item, Core.DatabaseModel.MenuCategory cat)
        {
            Id = item.Id;
            Name = item.Name;
            MenuItemTypeId = item.MenuItemTypeId;
            Price = item.Price;
            Description = item.Description;
            ImageUrl = item.ImageURL;
            DefaultCourses = cat.Courses.Select(course => new Models.Course(course));
            TaxTypeId = item.TaxTypeId;
            ModifierGroups =    from mgrp in item.ModifierGroups
                                where mgrp.Deleted == false
                                select mgrp.Id;

            Tags = from tag in item.MenuTags
                   select new MenuTag(tag);
            DefaultPrinter = item.PrinterId;
            Unavailable = item.Unavailable;
            TypeName = ((Core.DatabaseModel.Enums.MenuItemType)item.MenuItemTypeId).ToString();
        }


        public MenuItem(Core.DatabaseModel.MenuItem item)
        {
            Id = item.Id;
            Name = item.Name;
            MenuItemTypeId = item.MenuItemTypeId;
            Price = item.Price;
            Description = item.Description;
            ImageUrl = item.ImageURL;
            TaxTypeId = item.TaxTypeId;
            ModifierGroups = from mgrp in item.ModifierGroups
                             where mgrp.Deleted == false
                             select mgrp.Id;
            DefaultPrinter = item.PrinterId;
            Tags = from tag in item.MenuTags
                   select new MenuTag(tag);
            TagIds = from tag in item.MenuTags
                     select tag.Id;
            Unavailable = item.Unavailable;
            TypeName = ((Core.DatabaseModel.Enums.MenuItemType)item.MenuItemTypeId).ToString();
        }

        [Required]
        public string Name { get; set; }
        [Required]
        public double Price { get; set; }

        public string Description { get; set; }
        public string ImageUrl { get; set; }
        public IEnumerable<Course> DefaultCourses { get; set; }
        [Required]
        public IEnumerable<int> ModifierGroups { get; set; }
        [Required]
        public IEnumerable<int> MenuGroups { get; set; }
        [Required]
        public int TaxTypeId { get; set; }
        public IEnumerable<MenuTag> Tags { get; set; }
        public IEnumerable<int> TagIds { get; set; }

        [Required]
        public int MenuItemTypeId { get; set; }

        public bool Unavailable { get; set; }
    }
}
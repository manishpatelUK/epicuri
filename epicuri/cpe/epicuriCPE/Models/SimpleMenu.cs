using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Data.Entity;
namespace epicuri.CPE.Models
{
    public class Menu : DataModel
    {
        public Menu() { }
        public Menu(epicuri.Core.DatabaseModel.Menu m)
        {
            Init();

            Id = m.Id;
            MenuName = m.MenuName;
            MenuCategories = new List<Models.MenuCategory>();
            ModifierGroups = new List<Models.ModifierGroup>();
            MenuTags = new List<Models.MenuTag>();
            Active = m.Active;

            LastUpdated = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(m.LastUpdated);
            RestaurantId = m.RestaurantId;

            var tmpMenuTags = db.MenuTags.Where(tag => tag.RestaurantId == m.RestaurantId);
            var tmpMenuCategories = from cat in db.MenuCategories
                                    where cat.MenuId == Id && cat.Deleted == false
                                    orderby cat.Order
                                    select cat;

            var tmpModGroups = from modgrp in m.Restaurant.ModifierGroups
                               select modgrp;

            foreach (var modgrp in tmpModGroups)
            {
                if (!modgrp.Deleted)
                {
                    ModifierGroups.Add(new Models.ModifierGroup(modgrp));
                }
            }

            foreach (var tag in tmpMenuTags)
            {
                MenuTags.Add(new Models.MenuTag(tag));
            }

            foreach (var cat in tmpMenuCategories)
            {
                MenuCategories.Add(new Models.MenuCategory(cat));
            }
        }



        public int Id{ get; set; }

        [Required] 
        public string MenuName{ get; set; }
        public int RestaurantId { get; set; }
        public bool Active;
        private double lastUpdated;
        public double LastUpdated
        {
            get
            {
                
                return lastUpdated;

            }

            set
            {
                lastUpdated = value;
            }
        }

        public List<MenuCategory> MenuCategories { get; set; }
        public List<ModifierGroup> ModifierGroups { get; set; }
        public List<MenuTag> MenuTags { get; set; }


        public Core.DatabaseModel.Menu ToMenu()
        {
            return new Core.DatabaseModel.Menu
            {
                MenuName = MenuName,
                Active = this.Active
            };
        }
    }
}
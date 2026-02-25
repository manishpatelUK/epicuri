using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.API.Models
{
    public class Menu : CPE.Models.DataModel
    {
        public int Id { get; set; }
        public string MenuName { get; set; }
        public int RestaurantId { get; set; }
        private double lastUpdated;
        


        public Menu() { }


        public Menu(Core.DatabaseModel.Menu menu)
        {
            Init();


            Id = menu.Id;
            MenuName = menu.MenuName;
            LastUpdated = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(menu.LastUpdated);
            RestaurantId = menu.RestaurantId;

            /*
             * This is a flat menu, categories are not included, so we compress this information out by doing selectmany
             */
            var tmpGroups = db.MenuCategories.Where(cat => cat.MenuId == menu.Id).SelectMany(cat => cat.MenuGroups);
            var listGrp = from grp in tmpGroups
                          select grp;

            MenuGroups = new List<CPE.Models.MenuGroup>();
            foreach (var grp in listGrp)
            {
                MenuGroups.Add(new epicuri.CPE.Models.MenuGroup(grp,grp.MenuCategory));

            }
            
            /*
             * Get tags and groups like normal
             */
            var tmpMenuTags = from tag in db.MenuTags
                       where tag.RestaurantId == menu.RestaurantId
                       select tag;

            MenuTags = new List<CPE.Models.MenuTag>();

            foreach (var tag in tmpMenuTags)
            {
                MenuTags.Add(new epicuri.CPE.Models.MenuTag(tag));

            }

            var tmpModifierGroups = from modgrp in db.ModifierGroups
                                    where modgrp.RestaurantId == menu.RestaurantId
                                    select modgrp;

            ModifierGroups = new List<CPE.Models.ModifierGroup>();

            foreach (var mod in tmpModifierGroups)
            {
                ModifierGroups.Add(new epicuri.CPE.Models.ModifierGroup(mod));

            }
                                  
        }
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

        public List<epicuri.CPE.Models.MenuGroup> MenuGroups { get; set; }
        public List<epicuri.CPE.Models.ModifierGroup> ModifierGroups { get; set; }
        public List<epicuri.CPE.Models.MenuTag> MenuTags { get; set; }
    }
}
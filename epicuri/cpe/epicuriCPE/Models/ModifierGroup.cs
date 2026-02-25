using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;
namespace epicuri.CPE.Models
{
    public class ModifierGroup
    {
        public int Id;
        [Required]
        public String GroupName;
        [Required]
        public int LowerLimit;
        [Required]
        public int UpperLimit;
        public IEnumerable<Modifier> Modifiers;

        public ModifierGroup(){}
        
        public ModifierGroup(Core.DatabaseModel.ModifierGroup modgrp)
        {
            Id = modgrp.Id;

            GroupName = modgrp.GroupName;

            UpperLimit = modgrp.UpperLimit;

            LowerLimit = modgrp.LowerLimit;
            Modifiers = from mod in modgrp.Modifiers
                        where mod.Deleted == false
                        select new Models.Modifier(mod);                 
        }

    }
}
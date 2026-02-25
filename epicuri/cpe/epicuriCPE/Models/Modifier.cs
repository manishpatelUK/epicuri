using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class Modifier
    {

        public int Id;
        public string ModifierValue;
        public double Price;
        public int ModifierGroupId;
        public int TaxTypeId;

        public double taxrate;

        public Modifier() { }
        public Modifier(Core.DatabaseModel.Modifier mod)
        {
            ModifierValue = mod.ModifierValue;
            Id = mod.Id;
            Price = mod.Cost;
            TaxTypeId = mod.TaxType.Id;
            ModifierGroupId = mod.ModifierGroup.Id;
            taxrate = mod.TaxType.Rate;
        }
        
    }
}
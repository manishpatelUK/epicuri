using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.API.Models
{
    public class Order : CPE.Models.DataModel
    {
        public Order() { }
        public Order(Core.DatabaseModel.Order order)
        {
            Init();
            Id = order.Id;
     //       Completed = order.Completed.HasValue ? epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(order.Completed.Value) : 0;
            Note = order.Note;
            Modifiers = from mod in order.Modifiers
                        select mod.Id;
            ModifierDescriptions = from mod in order.Modifiers
                                   select new CPE.Models.Modifier(mod);
            Item = new CPE.Models.MenuItem(order.MenuItem);
            PriceOverride = order.PriceOverride;
            if (order.Course != null)
                Course = new CPE.Models.Course(order.Course);
            InstantiatedFromId = order.InstantiatedFromId;
            
        }
        [Required]
        public int MenuItemId { get; set; }
        //[Required]
        //public int DinerId { get; set; }
        public IEnumerable<int> Modifiers { get; set; }
        public String Note { get; set; }
        public int Quantity { get; set; }
        public double? PriceOverride{ get; set; }
        public epicuri.CPE.Models.MenuItem Item { get; set; }
        public IEnumerable<epicuri.CPE.Models.Modifier> ModifierDescriptions { get; set; }
        public int Id;
        public CPE.Models.Course Course {get;set;}
        public int InstantiatedFromId { get; set; }
    }
}
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    public class OrderPayload
    {
        [Required]
        public int MenuItemId { get; set; }
        [Required]
        public int DinerId { get; set; }
        [Required]
        public int CourseId { get; set; }
        public IEnumerable<int> Modifiers { get; set; }
        public String Note { get; set; }
        public Nullable<double> PriceOverride { get; set; }
        public int Quantity { get; set; }
        public int InstantiatedFromId { get; set; }
    }
}
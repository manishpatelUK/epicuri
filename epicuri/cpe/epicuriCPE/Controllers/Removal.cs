using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Text;

namespace epicuri.CPE.Models
{
    public class Removal
    {
        [Required]
        public int AdjustmentType { get; set; }
    }
}

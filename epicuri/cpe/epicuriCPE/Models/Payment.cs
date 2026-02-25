using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    // Deprecated after Adjustments CR 12/09/14 A.M

    public class Payment
    {
        public int Id;
        [Required]
        public int SessionId { get; set; }
        [Required]
        public double Amount {get;set;}

    }
}
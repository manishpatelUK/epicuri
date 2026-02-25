using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    public class ForceDelete
    {
        [Required]
        public bool WithPrejudice { get; set; }
    }
}
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    public class TablePayload
    {
        [Required]
        public IEnumerable<int> Tables { get; set; }

    }
}
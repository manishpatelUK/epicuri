using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class BatchPayload
    {
        [Required]
        public IEnumerable<int> batchId { get; set; }
    }
}
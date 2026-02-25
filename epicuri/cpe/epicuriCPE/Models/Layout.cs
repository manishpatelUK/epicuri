using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    public class Layout
    {
        
        public int Id { get; set; }
        [Required]
        public String Name { get; set; }
        [Required]
        public IEnumerable<Table> Tables;
        public DateTime _Updated { set { updated = value; } }
        private DateTime updated;
        public double Updated { get
        {
            return epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(updated);
        } }
        [Required]
        public int Floor { get; set; }

        public bool Temporary { get; set; }
        
        public double Scale { get; set; }
    }
}
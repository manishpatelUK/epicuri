using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    public class DietaryPreference
    {
        [Required]
        public int Id;
        [Required]
        public string Name { get; set; }

        public DietaryPreference()
        {
        }

        public DietaryPreference(int id, string name)
        {
            this.Id = id;
            this.Name = name;
        }
    }
}
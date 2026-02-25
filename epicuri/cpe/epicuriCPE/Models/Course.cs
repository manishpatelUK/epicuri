using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    public class Course
    {
        public int Id;
        [Required]
        public string Name;
        public int? Ordering;
        [Required]
        public int? ServiceId;

        public Course() { }
        public Course(Core.DatabaseModel.Course course)
        {
            Id = course.Id;
            Name = course.Name == null ? "Self Service" : course.Name;
            Ordering = course.Ordering;
            ServiceId = course.ServiceId;
        }


        public Core.DatabaseModel.Course ToCourse()
        {
            return new Core.DatabaseModel.Course
            {
                ServiceId = this.ServiceId,
                Name = this.Name,
                Ordering = Convert.ToInt16(this.Ordering)
            };
        }
       
    }
}
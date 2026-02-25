using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Http.Validation.Validators;
using System.Web.Http.Validation.Providers;
using System.ComponentModel.DataAnnotations;


namespace epicuri.CPE.Models
{
    public abstract class Party : DataModel
    {
        public int Id {get; set;}
        public short NumberOfPeople{get; set;}
        [Required]
        public string Name{get; set;}
        public double Created{get; set;}
        public int SessionId;
        
    }
}
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class MenuTag
    {
        public int Id { get; set; }
        public String Tag { get; set; }


        public MenuTag() { }
        public MenuTag(Core.DatabaseModel.MenuTag tag)
        {
            Id = tag.Id;
            Tag = tag.Tag;
        }
        
    }
}
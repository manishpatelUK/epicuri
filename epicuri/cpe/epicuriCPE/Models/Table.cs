using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    public class Table
    {
        public int Id;
        [Required]
        public String Name;
        public epicuri.Core.DatabaseModel.Position Position;
        public short DefaultCovers;
        public short Shape;

        public Table() { }
        public Table(Core.DatabaseModel.Table tab)
        {
            this.Id = tab.Id;
            this.Shape = tab.Shape;
            this.DefaultCovers = tab.DefaultCovers;
            this.Name = tab.Name;
            
        }
    }
}
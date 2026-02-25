using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class Printer
    {
        public int Id;
        public String Name;
        public String IP;
        public Printer() { }
        public Printer(Core.DatabaseModel.Printer printer)
        {
            Id = printer.Id;
            Name = printer.Name;
            IP = printer.IP ?? "";
        }
    }
}
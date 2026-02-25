using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class Floor
    {
        public int Id;
        public String Name;
        public short Capacity;
        public String ImageURL;
        public int? Layout;
        public double Scale;
        public IEnumerable<Layout> Layouts;
    }
}
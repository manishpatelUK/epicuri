using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class Event
    {
        public int Id;
        public int Session;
        public double Due;
        public string Text;
        public string Target;
        public string Type;
        public int Delay;
    }
}
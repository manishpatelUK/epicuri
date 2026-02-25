using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.API.Models
{
    public class PositionSquare
    {
        public double BottomLeftLongitude { get; set; }
        public double BottomLeftLatitude { get; set; }

        public double TopRightLongitude { get; set; }
        public double TopRightLatitude { get; set; }
    }
}
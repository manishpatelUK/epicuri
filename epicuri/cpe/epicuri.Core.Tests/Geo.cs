using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace epicuri.Core.Tests
{
    [TestClass]
    public class Geo
    {
        [TestMethod]
        public void GetLatLongForPostCode()
        {
            
            var expt = new Tuple<double, double>(53.948051, -1.030865);
            var res = Core.Utils.GeoCoding.LatLongFromPostCode("YO105GH");
            Assert.AreEqual(expt.Item1, res.Item1, 0.1);
            Assert.AreEqual(expt.Item2, res.Item2,0.1);
        }
    }
}

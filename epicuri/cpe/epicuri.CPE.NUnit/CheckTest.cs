using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework;
namespace epicuri.CPE.NUnit
{
    [TestFixture]
    public class CheckTest
    {
        [TestCase]
        public void SanityCheck()
        {
            Assert.AreEqual(1, 1);
        }
    }
}

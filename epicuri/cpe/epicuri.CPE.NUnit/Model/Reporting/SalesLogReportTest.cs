using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework;
using epicuri.CPE.Models;
using System.Data.Objects.DataClasses;
using epicuri.CPE.Models.Reporting;
namespace epicuri.CPE.NUnit.Model.Reporting
{
    [TestFixture]
    public class SalesLogReportTest
    {
        private Core.DatabaseModel.Restaurant restaurant;
        private SalesLogReport slr;
        [SetUp]
        public void SetUp()
        {
            var sessions = new EntityCollection<Core.DatabaseModel.Session>();
            var orders1 = new EntityCollection<Core.DatabaseModel.Order>();
            var orders2 = new EntityCollection<Core.DatabaseModel.Order>();

            sessions.Add(new Core.DatabaseModel.Session
            {
                Orders = orders1,
                Id = 1,
            });

            sessions.Add(new Core.DatabaseModel.Session
            {
                Orders = orders2,
                Id = 2,
            });



            orders1.Add(new Core.DatabaseModel.Order
            {
                Staff = new Core.DatabaseModel.Staff
                {
                    Id = 1,
                    Name = "a"
                },
                MenuItem = new Core.DatabaseModel.MenuItem
                {
                    Id=1,
                    Name="a",
                    Price=1
                },
                OrderTime = DateTime.MinValue,
                Session = new Core.DatabaseModel.Session
                {
                    Id=1,
                }

            });

            orders2.Add(new Core.DatabaseModel.Order
            {
                Staff = new Core.DatabaseModel.Staff
                {
                    Id = 2,
                    Name = "b"
                },
                MenuItem = new Core.DatabaseModel.MenuItem
                {
                    Id = 2,
                    Name = "b",
                    Price = 2
                },
                OrderTime=DateTime.MinValue,
                Session = new Core.DatabaseModel.Session
                {
                    Id = 2,
                }

            });

            

           
            restaurant = new Core.DatabaseModel.Restaurant
            {
                Sessions = sessions
            };

            slr = new SalesLogReport(restaurant);
        }


        [Test]
        public void TestReportCount2()
        {
            Assert.AreEqual(2, slr.GetReport(null, null).Count());
        }

        [Test]
        public void TestFirstMenuItemId()
        {
            var rl = (CPE.Models.Reporting.Line.SalesLogLine)slr.GetReport(null, null).First();
            Assert.AreEqual(1, rl.MenuItemId);
        }

        [Test]
        public void TestSecondMenuItemId()
        {
            var rl = (CPE.Models.Reporting.Line.SalesLogLine)slr.GetReport(null, null).Last();
            Assert.AreEqual(2, rl.MenuItemId);
        }

        [Test]
        public void TestFirstMenuItemName()
        {
            var rl = (CPE.Models.Reporting.Line.SalesLogLine)slr.GetReport(null, null).First();
            Assert.AreEqual("a", rl.MenuItemName);
        }

        [Test]
        public void TestSecondMenuItemName()
        {
            var rl = (CPE.Models.Reporting.Line.SalesLogLine)slr.GetReport(null, null).Last();
            Assert.AreEqual("b", rl.MenuItemName);
        }

        [Test]
        public void TestFirstSessionId()
        {
            var rl = (CPE.Models.Reporting.Line.SalesLogLine)slr.GetReport(null, null).First();
            Assert.AreEqual(1, rl.SessionId);
        }


        [Test]
        public void TestSecondSessionId()
        {
            var rl = (CPE.Models.Reporting.Line.SalesLogLine)slr.GetReport(null, null).Last();
            Assert.AreEqual(2, rl.SessionId);
        }

        [Test]
        public void TestFirstMenuItemPrice()
        {
            var rl = (CPE.Models.Reporting.Line.SalesLogLine)slr.GetReport(null, null).First();
            Assert.AreEqual(1, rl.SalesPrice);
        }

        [Test]
        public void TestSecondMenuItemPrice()
        {
            var rl = (CPE.Models.Reporting.Line.SalesLogLine)slr.GetReport(null, null).Last();
            Assert.AreEqual(2, rl.SalesPrice);
        }
    }
}

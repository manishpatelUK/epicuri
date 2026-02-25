
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework;
using epicuri.CPE.Models.Reporting;
using epicuri.CPE.Models;

namespace epicuri.CPE.NUnit.Model.Reporting
{
    [TestFixture]
    public class SessionRevenueReportTest
    {
        SessionRevenueReport srr;
        [SetUp]
        public void SetUp()
        {
            var sessions = new System.Data.Objects.DataClasses.EntityCollection<Core.DatabaseModel.Session>();
            var orders = new System.Data.Objects.DataClasses.EntityCollection<Core.DatabaseModel.Order>();
            var adjustments = new System.Data.Objects.DataClasses.EntityCollection<Core.DatabaseModel.Adjustment>();



            orders.Add(new Core.DatabaseModel.Order
            {
                MenuItem = new Core.DatabaseModel.MenuItem
                {
                    Price = 10
                }
            });

            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                },
                Value = 2,
                NumericalType = 0
            });

            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0
                },
                Value = 4,
                NumericalType = 0
            });



            sessions.Add(new Core.DatabaseModel.Session
            {
                Orders = orders,
                Adjustments = adjustments,
                ClosedTime = DateTime.MinValue,
                StartTime = DateTime.MinValue
            });

            Core.DatabaseModel.Restaurant rest = new Core.DatabaseModel.Restaurant
            {
                Sessions = sessions
            };
            srr = new SessionRevenueReport(rest);
        }


        [Test]
        public void TestSessionRevenueReportCount()
        {
            Assert.AreEqual(1, srr.GetReport(null, null).Count());
        }

        [Test]
        public void TestSessionRevenueStartTime()
        {
            var rl = (CPE.Models.Reporting.Line.SessionRevenueLine)srr.GetReport(null, null).First();
            Assert.AreEqual(DateTime.MinValue, rl.StartTime);
        }

        [Test]
        public void TestSessionRevenueEndTime()
        {
            var rl = (CPE.Models.Reporting.Line.SessionRevenueLine)srr.GetReport(null,null).First();
            Assert.AreEqual(DateTime.MinValue, rl.ClosedTime);
        }

        [Test]
        public void TestSessionRevenueSubtotal()
        {
            var rl = (CPE.Models.Reporting.Line.SessionRevenueLine)srr.GetReport(null, null).First();
            Assert.AreEqual(10, rl.SubTotal);
        }

        [Test]
        public void TestSessionRevenueTotal()
        {
            var rl = (CPE.Models.Reporting.Line.SessionRevenueLine)srr.GetReport(null, null).First();
            Assert.AreEqual(8, rl.Total);
        }

        [Test]
        public void TestSessionRevenuePaymentsTotal()
        {
            var rl = (CPE.Models.Reporting.Line.SessionRevenueLine)srr.GetReport(null, null).First();
            Assert.AreEqual(4, rl.Payments);
        }
    }
}

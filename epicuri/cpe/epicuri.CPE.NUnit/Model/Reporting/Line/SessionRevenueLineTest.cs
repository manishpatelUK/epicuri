using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework;
using epicuri.CPE.Models.Reporting.Line;
using epicuri.CPE.Models;
using System.Data.Objects.DataClasses;
namespace epicuri.CPE.NUnit.Model.Reporting.Line
{
    [TestFixture]
    public class SessionRevenueLineTest
    {
        SessionRevenueLine srl;
        Core.DatabaseModel.Session session;
        [SetUp]
        public void SetUp()
        {

            var orders = new EntityCollection<Core.DatabaseModel.Order>();
            var adjustments = new EntityCollection<Core.DatabaseModel.Adjustment>();

            orders.Add(new Core.DatabaseModel.Order
            {
                MenuItem = new Core.DatabaseModel.MenuItem
                {
                    Price=10
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


            session = new Core.DatabaseModel.Session
            {
                StartTime = DateTime.MinValue,
                ClosedTime = DateTime.MinValue,
                Id=1,
                Orders = orders,
                Adjustments = adjustments,
            };
            srl = new SessionRevenueLine(new Session(session));

        }

        [Test]
        public void TestSessionRevenueStartTime()
        {
            Assert.AreEqual(DateTime.MinValue, srl.StartTime);
        }

        [Test]
        public void TestSessionRevenueEndTime()
        {
            Assert.AreEqual(DateTime.MinValue, srl.ClosedTime);
        } 

        [Test]
        public void TestSessionRevenueSubtotal()
        {
            Assert.AreEqual(10, srl.SubTotal);
        }

        [Test]
        public void TestSessionRevenueTotal()
        {
            Assert.AreEqual(8, srl.Total);
        }

        [Test]
        public void TestSessionRevenuePaymentsTotal()
        {
            Assert.AreEqual(4, srl.Payments);
        }

    }
}

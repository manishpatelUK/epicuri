using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Data.Objects.DataClasses;
using NUnit.Framework;
using epicuri.CPE.Models.Reporting;
namespace epicuri.CPE.NUnit.Model.Reporting
{
    [TestFixture]
    public class PaymentDetailsReportTest
    {
        Core.DatabaseModel.Restaurant restaurant;
        PaymentDetailsReport pdr;

        [SetUp]
        public void SetUp()
        {
            var sessions = new EntityCollection<Core.DatabaseModel.Session>();
            var adjustments1 = new EntityCollection<Core.DatabaseModel.Adjustment>();
            var adjustments2 = new EntityCollection<Core.DatabaseModel.Adjustment>();

            adjustments1.Add(new Core.DatabaseModel.Adjustment
            {
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Name = "a",
                    SupportsChange = 0,
                    Type = 0
                },
                Staff = new Core.DatabaseModel.Staff{
                    Id=1,
                    Name="a"
                },
                Value = 1,
                NumericalType = 0,
                
            });

            adjustments1.Add(new Core.DatabaseModel.Adjustment
            {
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Name = "a",
                    SupportsChange = 0,
                    Type = 0
                },
                Staff = new Core.DatabaseModel.Staff
                {
                    Id = 1,
                    Name = "a"
                },
                Value = 1,
                NumericalType = 0
            });

            adjustments2.Add(new Core.DatabaseModel.Adjustment
            {
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Name = "a",
                    SupportsChange = 0,
                    Type = 1
                },
                Staff = new Core.DatabaseModel.Staff
                {
                    Id = 1,
                    Name = "a"
                },
                Value = 1,
                NumericalType = 0
            });

            adjustments2.Add(new Core.DatabaseModel.Adjustment
            {
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Name = "a",
                    SupportsChange = 0,
                    Type = 1
                },
                Staff = new Core.DatabaseModel.Staff
                {
                    Id = 1,
                    Name = "a"
                },
                Value = 1,
                NumericalType = 0
            });

            sessions.Add(new Core.DatabaseModel.Session
            {
                Adjustments = adjustments1,
                Id=1,
            });

            sessions.Add(new Core.DatabaseModel.Session
            {
                Adjustments = adjustments2,
                Id=1
            });

            restaurant = new Core.DatabaseModel.Restaurant
            {
                Sessions = sessions
            };

            pdr = new PaymentDetailsReport(restaurant);
        }


        [Test]
        public void TestPaymentDetailsReportContainsFourPaymentsFromFourAdjustments()
        {
            Assert.AreEqual(4, pdr.GetReport(null, null).Count());
        }

        [Test]
        public void TestFirstPDRLineMethod()
        {
            var rl = (CPE.Models.Reporting.Line.PaymentDetailsLine)pdr.GetReport(null, null).First();
            Assert.AreEqual("a", rl.Method);
        }

        [Test]
        public void TestFirstPDRLineDate()
        {
            var rl = (CPE.Models.Reporting.Line.PaymentDetailsLine)pdr.GetReport(null, null).First();
            Assert.AreEqual(DateTime.MinValue, rl.Date);
        }

        [Test]
        public void TestFirstPDRLineStaffName()
        {
            var rl = (CPE.Models.Reporting.Line.PaymentDetailsLine)pdr.GetReport(null, null).First();
            Assert.AreEqual("a", rl.StaffName);
        }

        [Test]
        public void TestFirstPDRLineStaffId()
        {
            var rl = (CPE.Models.Reporting.Line.PaymentDetailsLine)pdr.GetReport(null, null).First();
            Assert.AreEqual(1, rl.StaffId);
        }

        [Test]
        public void TestFirstPDRLineSessionId()
        {
            var rl = (CPE.Models.Reporting.Line.PaymentDetailsLine)pdr.GetReport(null, null).First();
            Assert.AreEqual(1, rl.SessionId);
        }
    }
}

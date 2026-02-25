using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework;
using epicuri.CPE.Models;
using epicuri.CPE.Models.Reporting.Line;
namespace epicuri.CPE.NUnit.Model.Reporting.Line
{
    [TestFixture]
    public class PaymentDetailsLineTest
    {
        SessionValueModification svm;
        PaymentDetailsLine line;
        [SetUp]
        public void SetUp()
        {
            svm = new SessionValueModification
            {
                AbsAdjustment = 1,
                AdjustmentType = "A",
                Date = DateTime.MinValue,
                Name = "B",
                SessionId=1,
                SessionType="",
                Staff = new Staff
                {
                    Id = 1,
                    Name = "C"
                }
            };

            line = new PaymentDetailsLine(svm);
        }

        [TestCase]
        public void TestAdjustmentValue()
        {
            Assert.AreEqual(-1, line.Value);
        }

        [TestCase]
        public void TestAdjustmentType()
        {
            Assert.AreEqual("A", line.AdjustmentType);
        }

        [TestCase]
        public void TestAdjustmentDate()
        {
            Assert.AreEqual(DateTime.MinValue, line.Date);
        }

        public void TestAdjustmentName()
        {
            Assert.AreEqual("B", line.Method);
        }

        public void TestAdjustmentSessionId()
        {
            Assert.AreEqual(1, line.SessionId);
        }

        public void TestAdjustmentStaffId()
        {
            Assert.AreEqual(1, line.StaffId);
        }

        public void TestAdjustmentStaffName()
        {
            Assert.AreEqual("C", line.StaffName);
        }
    }
}

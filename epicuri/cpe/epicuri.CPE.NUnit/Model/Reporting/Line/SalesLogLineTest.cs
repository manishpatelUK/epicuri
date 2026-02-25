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
    public class SalesLogLineTest
    {
        Order seatedOrder;
        Order seatedOrderVoid;
        Order seatedOrderPriceOverride;
		Order takeawayOrder;
        SalesLogLine salesLineSeated;
        SalesLogLine salesLineVoidSeated;
        SalesLogLine salesLineSeatedPriceOverride;
        SalesLogLine salesLineTakeaway;

		[SetUp]
        public void SetUp()
        {
            seatedOrder = new Order
            {
				Id=1,
				OrderTime = DateTime.MinValue,
				SessionId =1,
				MenuItem = new MenuItem {
					Id=1,
					Name = "a",
					Price=1,
				},
                Modifiers = new List<Modifier>(),
                Staff = new Staff
                {
                    Name = "s",
                    Id = 1
                }
            };


            seatedOrderVoid = new Order
            {
                Id = 1,
                OrderTime = DateTime.MinValue,
                SessionId=1,
                MenuItem = new MenuItem
                {
                    Id = 1,
                    Name = "a",
                    Price = 1,
                },
				PriceOverride = 0,
				DiscountReason = "b",
                Modifiers = new List<Modifier>(),
                Staff = new Staff
                {
					Name = "s",
					Id=1
                }
            };

            seatedOrderPriceOverride = new Order
            {
				Id=1,
                OrderTime = DateTime.MinValue,
                SessionId =1,
                MenuItem = new MenuItem
                {
                    Name = "a",
                    Price = 1,
                },
                Modifiers = new List<Modifier>(),
				PriceOverride = 2,
                Staff = new Staff
                {
                    Name = "s",
                    Id = 1
                }
            };
            
            var session = new Models.Session { Id = 1,SessionType="Seated" };
            salesLineVoidSeated = new SalesLogLine(session,seatedOrderVoid);
            salesLineSeated = new SalesLogLine(session,seatedOrder);
            salesLineSeatedPriceOverride = new SalesLogLine(session,seatedOrderPriceOverride);
        }

		[Test]
        public void TestSeatedOrderDateTimeCorrect()
        {
            Assert.AreEqual(DateTime.MinValue, salesLineSeated.Date);
        }


        [Test]
        public void TestSeatedOrderSessionTypeCorrect()
        {
            Assert.AreEqual("Seated", salesLineSeated.SessionType);
        }

        [Test]
        public void TestSeatedOrderSessionPriceCorrect()
        {
            Assert.AreEqual(1, salesLineSeated.SalesPrice);
        }

        [Test]
        public void TestSeatedOrderMenuItemIdCorrect()
        {
            Assert.AreEqual(1, salesLineSeated.MenuItemId);
        }

        [Test]
        public void TestSeatedOrderSessionIdCorrect()
        {
            Assert.AreEqual(1, salesLineSeated.SessionId);
        }

        [Test]
        public void TestSeatedOrderVoidReasonBlankCorrect()
        {
            Assert.AreEqual("", salesLineSeated.VoidReason);
        }


        [Test]
        public void TestVoidSeatedOrderVoidReasonCorrect()
        {
            Assert.AreEqual("b", salesLineVoidSeated.VoidReason);
        }



        [Test]
        public void TestSeatedOrderMenuNameCorrect()
        {
            Assert.AreEqual("a", salesLineSeated.MenuItemName);
        }

        [Test]
        public void TestSeatedOrderPriceOverrideSessionPriceCorrect()
        {
            Assert.AreEqual(2, salesLineSeatedPriceOverride.SalesPrice);
        }
    }

}

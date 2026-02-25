using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework;
using System.Data.Objects.DataClasses;
namespace epicuri.CPE.NUnit.Model
{
    [TestFixture]
    public class SessionDeliveryTest
    {
        epicuri.Core.DatabaseModel.Session s;
        epicuri.Core.DatabaseModel.TaxType t;
        EntityCollection<epicuri.Core.DatabaseModel.Adjustment> adjustmentsNoPayments;
        EntityCollection<epicuri.Core.DatabaseModel.Adjustment> paymentsNoAdjustments;
        EntityCollection<epicuri.Core.DatabaseModel.Adjustment> paymentsAndAdjustments;
        [SetUp]
        public void SetUp()
        {

            adjustmentsNoPayments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustmentsNoPayments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                },
                Value = 10,
                NumericalType = 0
            });


            paymentsNoAdjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            paymentsNoAdjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0
                },
                Value = 10,
                NumericalType = 0
            });


            paymentsAndAdjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            paymentsAndAdjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                },
                Value = 10,
                NumericalType = 0
            });

            paymentsAndAdjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0
                },
                Value = 10,
                NumericalType = 0
            });



            t = new epicuri.Core.DatabaseModel.TaxType();

            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();
            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 100,
                    TaxType = t,
                    MenuItemTypeId = (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Food
                },

            };
            orders.Add(order);

            var order2 = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 100,
                    TaxType = t,
                    MenuItemTypeId = (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Drink
                },
            };

            orders.Add(order2);

            s = new epicuri.Core.DatabaseModel.Session
            {
                Orders = orders
            };

        }


        [Test]
        public void TestRemainingTotalWithNoDeliveryCost()
        {

            epicuri.CPE.Models.Session session = new Models.Session(s);
            session.CalculateVals(s, 0);
            Assert.AreEqual(200, session.RemainingTotal);
        }

        [Test]
        public void TestRemainingTotalWithDeliveryCost()
        {

            epicuri.CPE.Models.Session session = new Models.Session(s);
            session.CalculateVals(s, 10);
            Assert.AreEqual(210, session.RemainingTotal);
        }


        [Test]
        public void TestRemainingTotalWithDeliveryCostAndPartialPayment()
        {
            

            var cashAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Cash",
                Type = 0,
            };

            s.Adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = cashAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 10
            });

            epicuri.CPE.Models.Session session = new Models.Session(s);
            session.CalculateVals(s, 10);
            Assert.AreEqual(200, session.RemainingTotal);
        }

        [Test]
        public void TestRemainingTotalZeroWithDeliveryCostAndFullPayment()
        {


            var cashAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Cash",
                Type = 0,
            };

            s.Adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = cashAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 210
            });

            epicuri.CPE.Models.Session session = new Models.Session(s);
            session.CalculateVals(s, 10);
            Assert.AreEqual(0, session.RemainingTotal);
        }

     
        [Test]
        public void TestRemainingTotalZeroWithDeliveryCostAndOverPayment()
        {


            var cashAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Cash",
                Type = 0,
                SupportsChange = 1,
            };

            s.Adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = cashAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 220
            });

            epicuri.CPE.Models.Session session = new Models.Session(s);
            session.CalculateVals(s, 10);
            Assert.AreEqual(-10, session.RemainingTotal);
        }

        [Test]
        public void TestNoChangeWithDeliveryCostAndOverPayment()
        {


            var cashAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Cash",
                Type = 0,
                SupportsChange = 0
            };

            s.Adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = cashAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 220
            });

            epicuri.CPE.Models.Session session = new Models.Session(s);
            session.CalculateVals(s, 10);
            Assert.AreEqual(0, session.Change);
        }

        [Test]
        public void TestChangeWithDeliveryCostAndOverPaymentCash()
        {


            var cashAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Cash",
                Type = 0,
                SupportsChange = 1
            };

            s.Adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = cashAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 220
            });

            epicuri.CPE.Models.Session session = new Models.Session(s);
            session.CalculateVals(s, 10);
            Assert.AreEqual(10, session.Change);
        }




        [Test]
        public void TestNoOverpaymentWithDeliveryCostAnChange()
        {


            var cashAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Cash",
                Type = 0,
                SupportsChange = 1
            };

            s.Adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = cashAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 220
            });

            epicuri.CPE.Models.Session session = new Models.Session(s);
            session.CalculateVals(s, 10);
            Assert.AreEqual(0, session.OverPayments);
        }

        [Test]
        public void TestOverPaymentWithDeliveryCostAndOverPaymentCard()
        {


            var cashAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Card",
                Type = 0,
                SupportsChange = 0
            };

            s.Adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = cashAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 220
            });

            epicuri.CPE.Models.Session session = new Models.Session(s);
            session.CalculateVals(s, 10);
            Assert.AreEqual(10, session.OverPayments);
        }
    }

}

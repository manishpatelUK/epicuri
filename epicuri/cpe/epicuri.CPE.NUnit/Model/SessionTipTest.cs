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
    public class SessionTipTest
    {
        Core.DatabaseModel.Service service;
        EntityCollection<epicuri.Core.DatabaseModel.Order> orders;
        [SetUp]
        public void SetUp()
        {
            Core.DatabaseModel.TaxType t = new Core.DatabaseModel.TaxType
            {
                Rate = 0,
                Name = "Tax"
            };


            orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();
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

            service = new Core.DatabaseModel.Service
            {
                ServiceName = "",
            };

        }
        [Test]
        public void TestRemainingTotalWithoutTipsOrAdjustments()
        {
            Core.DatabaseModel.SeatedSession ss = new Core.DatabaseModel.SeatedSession()
            {
                Orders = orders,
                Service = service
            };

            CPE.Models.SeatedSession seated = new Models.SeatedSession(ss);
            Assert.AreEqual(200,seated.RemainingTotal);
        }

        [Test]
        public void TestRemainingTotalWithTipsNoAdjustments()
        {
            Core.DatabaseModel.SeatedSession ss = new Core.DatabaseModel.SeatedSession()
            {
                Orders = orders,
                Service = service,
                TipTotal=10
            };

            CPE.Models.SeatedSession seated = new Models.SeatedSession(ss);
            Assert.AreEqual(220, seated.RemainingTotal);
        }

        [Test]
        public void TestRemainingTotalWithTipsAdjustments()
        {
            EntityCollection<Core.DatabaseModel.Adjustment> adj = new EntityCollection<Core.DatabaseModel.Adjustment>();
            adj.Add(new Core.DatabaseModel.Adjustment
            {
                NumericalType = (int)Core.DatabaseModel.Enums.NumericalTypeType.Price,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Name="",
                    SupportsChange = 0,
                    Type= (int)Core.DatabaseModel.Enums.AdjustmentTypeType.Discount
                },
                Value=100
            });
            Core.DatabaseModel.SeatedSession ss = new Core.DatabaseModel.SeatedSession()
            {
                Orders = orders,
                Service = service,
                Adjustments = adj,
                TipTotal = 10
            };

            CPE.Models.SeatedSession seated = new Models.SeatedSession(ss);
            Assert.AreEqual(110, seated.RemainingTotal);
        }

        [Test]
        public void TestRemainingTotalWithTipsAdjustmentsAndPayment()
        {
            EntityCollection<Core.DatabaseModel.Adjustment> adj = new EntityCollection<Core.DatabaseModel.Adjustment>();
            adj.Add(new Core.DatabaseModel.Adjustment
            {
                NumericalType = (int)Core.DatabaseModel.Enums.NumericalTypeType.Price,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Name = "",
                    SupportsChange = 0,
                    Type = (int)Core.DatabaseModel.Enums.AdjustmentTypeType.Discount
                },
                Value = 100
            });
            adj.Add(new Core.DatabaseModel.Adjustment
            {
                NumericalType = (int)Core.DatabaseModel.Enums.NumericalTypeType.Price,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Name = "",
                    SupportsChange = 0,
                    Type = (int)Core.DatabaseModel.Enums.AdjustmentTypeType.Payment
                },
                Value = 100
            });
            Core.DatabaseModel.SeatedSession ss = new Core.DatabaseModel.SeatedSession()
            {
                Orders = orders,
                Service = service,
                Adjustments = adj,
                TipTotal = 10
            };

            CPE.Models.SeatedSession seated = new Models.SeatedSession(ss);
            Assert.AreEqual(10, seated.RemainingTotal);
        }



        [Test]
        public void TestRemainingTotalWithTipsPaymentAndNoAdjustments()
        {
            EntityCollection<Core.DatabaseModel.Adjustment> adj = new EntityCollection<Core.DatabaseModel.Adjustment>();
            adj.Add(new Core.DatabaseModel.Adjustment
            {
                NumericalType = (int)Core.DatabaseModel.Enums.NumericalTypeType.Price,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Name = "",
                    SupportsChange = 0,
                    Type = (int)Core.DatabaseModel.Enums.AdjustmentTypeType.Payment
                },
                Value = 100
            });

            Core.DatabaseModel.SeatedSession ss = new Core.DatabaseModel.SeatedSession()
            {
                Orders = orders,
                Service = service,
                Adjustments = adj,
                TipTotal = 10
            };

            CPE.Models.SeatedSession seated = new Models.SeatedSession(ss);
            Assert.AreEqual(120, seated.RemainingTotal);
        }

      
    }
}

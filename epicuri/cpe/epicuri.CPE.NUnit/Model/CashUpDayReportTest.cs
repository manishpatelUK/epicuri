using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework;
using epicuri.CPE.Models;
using Moq;
using System.Data.Objects.DataClasses;

namespace epicuri.CPE.NUnit.Model
{
    [TestFixture]
    public class CashUpDayReportTest
    {
        Mock<IQueryable<epicuri.Core.DatabaseModel.Session>> SessionsMock;
        [SetUp]
        public void SetUp()
        {
            Mock<epicuri.Core.DatabaseModel.Session> session1 = new Mock<epicuri.Core.DatabaseModel.Session>();

            var sessions = new List<Session>();

            CashUpDay cashupDay = new CashUpDay();
            SessionsMock = new Mock<IQueryable<epicuri.Core.DatabaseModel.Session>>();
            SessionsMock.SetupIQueryable<IQueryable<epicuri.Core.DatabaseModel.Session>>(sessions.AsQueryable());

        }

        [Test]
        public void TestAdjustmentReportEmptyWhenNoSessions()
        {
            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            Assert.IsEmpty(CashUpDay.CreateAdjustmentReport(sessions.AsQueryable()));
        }

        [Test]
        public void TestAdjustmentReportEmptyWhenSessionNotPaid()
        {
            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = false,
                ClosedTime = DateTime.MinValue
            };
            

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.IsEmpty(CashUpDay.CreateAdjustmentReport(sessions.AsQueryable()));
        }

        [Test]
        public void TestAdjustmentReportEmptyWhenSessionNotClosed()
        {
            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid=true,
                ClosedTime=null
            };
            


            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.IsEmpty(CashUpDay.CreateAdjustmentReport(sessions.AsQueryable()));
            
        }

        [Test]
        public void TestAdjustmentReportEmptyWhenSessionHasNoAdjustments()
        {
            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue
            };



            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.IsEmpty(CashUpDay.CreateAdjustmentReport(sessions.AsQueryable()));

        }


        [Test]
        public void TestAdjustmentReportEmptyWithPaymentOption()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var cashAdj =  new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name="Cash",
                Type=0,
            };

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = cashAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value=1
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.IsEmpty(CashUpDay.CreateAdjustmentReport(sessions.AsQueryable()));
        }

        [Test]
        public void TestAdjustmentReportNotEmptyWithAdjustmentOption()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Error",
                Type = 1
            };

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 1
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.IsNotEmpty(CashUpDay.CreateAdjustmentReport(sessions.AsQueryable()));
        }

        [Test]
        public void TestAdjustmentReportFirstKeyIsAdjTypeName()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Error",
                Type = 1
            };

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 1
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual("Error",CashUpDay.CreateAdjustmentReport(sessions.AsQueryable()).Keys.First());
        }


        [Test]
        public void TestAdjustmentReportValueIsZeroWhenNumericalType0AndNoOrders()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Error",
                Type = 1
            };

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 1
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(0, CashUpDay.CreateAdjustmentReport(sessions.AsQueryable())["Error"]);
        }


        [Test]
        public void TestAdjustmentReportValueIsCalculatedWhenAdjustmentDeleted()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Error",
                Type = 1
            };

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 1,
                Deleted=DateTime.MinValue
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,

            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.IsEmpty(CashUpDay.CreateAdjustmentReport(sessions.AsQueryable()));
        }


        [Test]
        public void TestAdjustmentReportValueIsCalculatedWhenNumericalType1()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();
            
            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name="test",
                    Price=100
                }
            };

            orders.Add(order);

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Error",
                Type = 1
            };

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
               
                NumericalType = 1,
                Value = 1
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(1, CashUpDay.CreateAdjustmentReport(sessions.AsQueryable())["Error"]);
        }

        [Test]
        public void TestAdjustmentReportValueIsCalculatedWhenNumericalType1WithPriceOverride()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Error",
                Type = 1
            };



            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();

            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 10,

                },
                PriceOverride = 100,
                AdjustmentType=errAdj
            };

            orders.Add(order);

            

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 1
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(1, CashUpDay.CreateAdjustmentReport(sessions.AsQueryable())["Error"]);
        }

        [Test]
        public void TestAdjustmentReportValueIsCalculatedWhenNumericalType1WithPriceOverrideAndModifiers()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Error",
                Type = 1
            };

            var modifiers = new EntityCollection<epicuri.Core.DatabaseModel.Modifier>();
            modifiers.Add(new epicuri.Core.DatabaseModel.Modifier{
                Cost=50
            });

            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();

            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 10,

                },
            
                Modifiers = modifiers,
                PriceOverride = 100,
                AdjustmentType = errAdj
            };

            orders.Add(order);



            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 1
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(1, CashUpDay.CreateAdjustmentReport(sessions.AsQueryable())["Error"]);
        }


        [Test]
        public void TestAdjustmentReportValueIsCalculatedWhenNumericalType1WithModifiers()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Error",
                Type = 1
            };



            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();
            var modifiers =  new EntityCollection<epicuri.Core.DatabaseModel.Modifier>();
            modifiers.Add(new epicuri.Core.DatabaseModel.Modifier{
                Cost=50
            });
            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 50,

                },
                
                Modifiers = modifiers
            };

            orders.Add(order);



            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 1
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(1, CashUpDay.CreateAdjustmentReport(sessions.AsQueryable())["Error"]);
        }


        [Test]
        public void TestAdjustmentReportMultiAdjustment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "First",
                Type = 1
            };

            var errAdj2 = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Second",
                Type = 1
            };



            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();
           
            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 1000,
                },
            };

            orders.Add(order);

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 100
            });

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj2,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 50
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(100, CashUpDay.CreateAdjustmentReport(sessions.AsQueryable())["First"]);
            Assert.AreEqual(450, CashUpDay.CreateAdjustmentReport(sessions.AsQueryable())["Second"]);
        }


        [Test]
        public void TestItemAdjustmentReportEmptyWhenNoSessions()
        {
            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            Assert.IsEmpty(CashUpDay.CreateItemAdjustmentReport(sessions.AsQueryable()));
        }

        [Test]
        public void TestItemAdjustmentReportEmptyWhenSessionNotPaid()
        {
            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = false,
                ClosedTime = DateTime.MinValue
            };


            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.IsEmpty(CashUpDay.CreateItemAdjustmentReport(sessions.AsQueryable()));
        }

        [Test]
        public void TestItemAdjustmentReportEmptyWhenSessionNotClosed()
        {
            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = null
            };



            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.IsEmpty(CashUpDay.CreateItemAdjustmentReport(sessions.AsQueryable()));

        }

        [Test]
        public void TestItemAdjustmentReportEmptyWhenSessionHasNoAdjustments()
        {
            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue
            };



            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.IsEmpty(CashUpDay.CreateItemAdjustmentReport(sessions.AsQueryable()));
        }

        [Test]
        public void TestItemAdjustmentReportValueIsCalculatedWhenPriceOverriden()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Error",
                Type = 1
            };



            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();

            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 10,
                },
                PriceOverride = 100,
                AdjustmentType = errAdj

            };

            orders.Add(order);
            
            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Orders=orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.AreEqual(100,CashUpDay.CreateItemAdjustmentReport(sessions.AsQueryable())["Error"]);

        }


        [Test]
        public void TestItemAdjustmentReportVEmptyWhenNoPriceOverriden()
        {
            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();

            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 10,
                }

            };

            orders.Add(order);

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.IsEmpty(CashUpDay.CreateItemAdjustmentReport(sessions.AsQueryable()));
        }

        [Test]
        public void TestItemLossAdjustmentReportEmptyWhenNoSessions()
        {
            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            Assert.IsEmpty(CashUpDay.CreateItemAdjustmentLossReport(sessions.AsQueryable()));
        }

        [Test]
        public void TestItemLossAdjustmentReportEmptyWhenSessionNotPaid()
        {
            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = false,
                ClosedTime = DateTime.MinValue
            };


            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.IsEmpty(CashUpDay.CreateItemAdjustmentLossReport(sessions.AsQueryable()));
        }

        [Test]
        public void TestItemLossAdjustmentReportEmptyWhenSessionNotClosed()
        {
            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = null
            };



            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.IsEmpty(CashUpDay.CreateItemAdjustmentLossReport(sessions.AsQueryable()));

        }

        [Test]
        public void TestItemAdjustmentLossReportEmptyWhenSessionHasNoAdjustments()
        {
            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue
            };



            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.IsEmpty(CashUpDay.CreateItemAdjustmentLossReport(sessions.AsQueryable()));
        }

        [Test]
        public void TestItemAdjustmentLossReportValueIsCalculatedWhenPriceOverriden()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Error",
                Type = 1
            };



            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();

            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 100,
                },
                PriceOverride = 90,
                AdjustmentType = errAdj

            };

            orders.Add(order);

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.AreEqual(10, CashUpDay.CreateItemAdjustmentLossReport(sessions.AsQueryable())["Error"]);

        }


        public void TestItemAdjustmentLossReportValueIsCalculatedWhenPriceOverridenWithModifier()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Error",
                Type = 1
            };

            var modifiers = new EntityCollection<epicuri.Core.DatabaseModel.Modifier>();
            modifiers.Add(new epicuri.Core.DatabaseModel.Modifier
            {
                Cost = 50
            });

            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();

            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 50,
                },
                PriceOverride = 90,
                AdjustmentType = errAdj,
                Modifiers=modifiers
            };

            orders.Add(order);

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.AreEqual(10, CashUpDay.CreateItemAdjustmentLossReport(sessions.AsQueryable())["Error"]);

        }


        [Test]
        public void TestItemAdjustmentLossReportVEmptyWhenNoPriceOverriden()
        {
            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();

            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 10,
                }

            };

            orders.Add(order);

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.IsEmpty(CashUpDay.CreateItemAdjustmentLossReport(sessions.AsQueryable()));
        }







        [Test]
        public void TestPaymentReportEmptyWhenNoSessions()
        {
            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            Assert.IsEmpty(CashUpDay.CreatePaymentReport(sessions.AsQueryable()));
        }

        [Test]
        public void TestPaymentReportEmptyWhenSessionNotPaid()
        {
            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = false,
                ClosedTime = DateTime.MinValue
            };


            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.IsEmpty(CashUpDay.CreatePaymentReport(sessions.AsQueryable()));
        }

        [Test]
        public void TestPaymentReportEmptyWhenSessionNotClosed()
        {
            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = null
            };



            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.IsEmpty(CashUpDay.CreatePaymentReport(sessions.AsQueryable()));

        }

        [Test]
        public void TestPaymentReportEmptyWhenSessionHasNoAdjustments()
        {
            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue
            };



            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            Assert.IsEmpty(CashUpDay.CreatePaymentReport(sessions.AsQueryable()));

        }


        [Test]
        public void TestPaymentReportEmptyWithPaymentOption()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var cashAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Error",
                Type = 1,
            };

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = cashAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 1
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.IsEmpty(CashUpDay.CreatePaymentReport(sessions.AsQueryable()));
        }

        [Test]
        public void TestPaymentReportNotEmptyWithAdjustmentOption()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Payment",
                Type = 0
            };

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 1
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.IsNotEmpty(CashUpDay.CreatePaymentReport(sessions.AsQueryable()));
        }

        [Test]
        public void TestPaymentReportFirstKeyIsAdjTypeName()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Payment",
                Type = 0
            };

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 1
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual("Payment", CashUpDay.CreatePaymentReport(sessions.AsQueryable()).Keys.First());
        }


        [Test]
        public void TestPaymentReportValueIsSameWhenNumericalType0()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Payment",
                Type = 0
            };

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 1
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(1, CashUpDay.CreatePaymentReport(sessions.AsQueryable())["Payment"]);
        }


        [Test]
        public void TestPaymentReportValueIsCalculatedWhenAdjustmentDeleted()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Payment",
                Type = 0
            };

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 1,
                Deleted = DateTime.MinValue
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,

            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.IsEmpty(CashUpDay.CreatePaymentReport(sessions.AsQueryable()));
        }


        [Test]
        public void TestPaymentReportValueIsCalculatedWhenNumericalType1()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();

            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 100
                }
            };

            orders.Add(order);

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Payment",
                Type = 0
            };

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,

                NumericalType = 1,
                Value = 1
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(1, CashUpDay.CreatePaymentReport(sessions.AsQueryable())["Payment"]);
        }

        [Test]
        public void TestPaymentReportValueIsCalculatedWhenNumericalType1WithPriceOverride()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Payment",
                Type = 0
            };



            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();

            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 10,

                },
                PriceOverride = 100,
                AdjustmentType = errAdj
            };

            orders.Add(order);



            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 1
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(1, CashUpDay.CreatePaymentReport(sessions.AsQueryable())["Payment"]);
        }

        [Test]
        public void TestPaymentReportValueIsCalculatedWhenNumericalType1WithPriceOverrideAndModifiers()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Payment",
                Type = 0
            };

            var modifiers = new EntityCollection<epicuri.Core.DatabaseModel.Modifier>();
            modifiers.Add(new epicuri.Core.DatabaseModel.Modifier
            {
                Cost = 50
            });

            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();

            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 10,

                },

                Modifiers = modifiers,
                PriceOverride = 100,
                AdjustmentType = errAdj
            };

            orders.Add(order);



            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 1
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(1, CashUpDay.CreatePaymentReport(sessions.AsQueryable())["Payment"]);
        }


        [Test]
        public void TestPaymentReportValueIsCalculatedWhenNumericalType1WithModifiers()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Payment",
                Type = 0
            };



            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();
            var modifiers = new EntityCollection<epicuri.Core.DatabaseModel.Modifier>();
            modifiers.Add(new epicuri.Core.DatabaseModel.Modifier
            {
                Cost = 50
            });
            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 50,

                },

                Modifiers = modifiers
            };

            orders.Add(order);



            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 1
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(1, CashUpDay.CreatePaymentReport(sessions.AsQueryable())["Payment"]);
        }


        [Test]
        public void TestPaymentReportMultiAdjustment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "First",
                Type = 0
            };

            var errAdj2 = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Second",
                Type = 0
            };



            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();

            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 1000,
                },
            };

            orders.Add(order);

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 100
            });

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj2,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 50
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(100, CashUpDay.CreatePaymentReport(sessions.AsQueryable())["First"]);
            Assert.AreEqual(450, CashUpDay.CreatePaymentReport(sessions.AsQueryable())["Second"]);
        }



        [Test]
        public void TestPaymentReportMultiAdjustmentName()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "First",
                Type = 1
            };

            var errAdj2 = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "First",
                Type = 1
            };



            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();

            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 1000,
                },
            };

            orders.Add(order);

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 100
            });

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj2,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 50
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(550, CashUpDay.CreateAdjustmentReport(sessions.AsQueryable())["First"]);

        }




        [Test]
        public void TestPaymentReportMultiAdjustmentL()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "First",
                Type = 1
            };

            var errAdj2 = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Second",
                Type = 1
            };



            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();

            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 1000,
                },
            };

            orders.Add(order);

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 100
            });

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 50
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(550, CashUpDay.CreateAdjustmentReport(sessions.AsQueryable())["First"]);
          
        }


        [Test]
        public void TestPaymentReportMultiAdjustmentM()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "First",
                Type = 1
            };

            var errAdj2 = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Second",
                Type = 0
            };



            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();

            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 1000,
                },
            };

            orders.Add(order);

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 100
            });

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj2,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 50
            });

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(100, CashUpDay.CreateAdjustmentReport(sessions.AsQueryable())["First"]);

        }



        [Test]
        public void TestPaymentReportMultiAdjustmentO()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "First",
                Type = 1
            };

            var errAdj2 = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Second",
                Type = 1
            };



            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();

            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 1000,
                },
            };

            orders.Add(order);

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 50
            });

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj2,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 50
            });

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 50
            });

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj2,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 50
            });




            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(625, CashUpDay.CreateAdjustmentReport(sessions.AsQueryable())["First"]);

        }




        [Test]
        public void TestPaymentReportMultiAdjustmentP()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "First",
                Type = 1
            };

            var errAdj2 = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Second",
                Type = 1
            };



            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();

            var order = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 1000,
                },
            };

            orders.Add(order);

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 50
            });

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj2,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 50
            });

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 50
            });

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj2,
                Created = DateTime.MinValue,
                NumericalType = 1,
                Value = 50
            });




            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,
                Orders = orders
            };

            var sessions = new List<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);

            Assert.AreEqual(312.5m, CashUpDay.CreateAdjustmentReport(sessions.AsQueryable())["Second"]);

        }


       


    }



}

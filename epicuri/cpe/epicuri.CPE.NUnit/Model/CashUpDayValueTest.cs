using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework;
using System.Data.Objects.DataClasses;
using epicuri.CPE.Models;
namespace epicuri.CPE.NUnit.Model
{
    [TestFixture]
    public class CashUpDayValueTest
    {
        EntityCollection<epicuri.Core.DatabaseModel.Session> sessions;

        epicuri.Core.DatabaseModel.AdjustmentType payAdj = new epicuri.Core.DatabaseModel.AdjustmentType
        {
            Name = "Pay",
            Type = 0
        };

        [SetUp]
        public void SetUp()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            var adjustments2  = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            var adjustments3 = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            var adjustments4 = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();

            var errAdj = new epicuri.Core.DatabaseModel.AdjustmentType
            {
                Name = "Err",
                Type = 1
            };

            

            epicuri.Core.DatabaseModel.TaxType t = new Core.DatabaseModel.TaxType
            {
                Rate = 20
            };


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


            var orders2 = new EntityCollection<epicuri.Core.DatabaseModel.Order>();
            var order3 = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 100,
                    TaxType = t,
                    MenuItemTypeId = (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Food
                },

            };
            orders2.Add(order3);

            var order4 = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 100,
                    TaxType = t,
                    MenuItemTypeId = (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Other
                },
            };
            orders2.Add(order4);

            var orders3 = new EntityCollection<epicuri.Core.DatabaseModel.Order>();
            var order5 = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 100,
                    TaxType = t
                    
                },

            };
            orders3.Add(order5);

            var order6 = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 100,
                    TaxType = t
                },
            };
            orders3.Add(order6);


            var orders4 = new EntityCollection<epicuri.Core.DatabaseModel.Order>();
            var order7 = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 100,
                    TaxType = t
                },

            };
            orders4.Add(order7);

            var order8 = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 100,
                    TaxType = t
                },
            };
            orders4.Add(order8);

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 50
            });

            adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = payAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 150
            });


            adjustments2.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 50
            });

            adjustments2.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = payAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 150
            });


            adjustments3.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 50
            });

            adjustments3.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = payAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 150
            });


            adjustments4.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = errAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 50
            });

            adjustments4.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType = payAdj,
                Created = DateTime.MinValue,
                NumericalType = 0,
                Value = 150
            });


            var diners = new EntityCollection<epicuri.Core.DatabaseModel.Diner>();
            diners.Add(new epicuri.Core.DatabaseModel.Diner
            {
                IsTable=false
            });

            diners.Add(new epicuri.Core.DatabaseModel.Diner
            {
                IsTable = true
            });

            diners.Add(new epicuri.Core.DatabaseModel.Diner
            {
                IsTable = false
            });

            var diners2 = new EntityCollection<epicuri.Core.DatabaseModel.Diner>();
            diners2.Add(new epicuri.Core.DatabaseModel.Diner
            {
                IsTable = false
            });

            diners2.Add(new epicuri.Core.DatabaseModel.Diner
            {
                IsTable = true
            });

            diners2.Add(new epicuri.Core.DatabaseModel.Diner
            {
                IsTable = false
            });

            var service = new epicuri.Core.DatabaseModel.Service
            {
                ServiceName = "s",
                Id = 1
            };

            var session1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments,
                Orders = orders,
                Diners= diners,
                Service=service,
                Party = new Core.DatabaseModel.Party {
                    
                }
            };

            var session2 = new epicuri.Core.DatabaseModel.TakeAwaySession
            {
                Paid = true,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments2,
                Orders = orders2,
               
            };

            var voidsession1 = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Paid = false,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments3,
                Orders = orders3,
                Diners = diners2,
                Service=service,
                Party = new Core.DatabaseModel.Party {
                    
                }
            };

            var voidsession2 = new epicuri.Core.DatabaseModel.TakeAwaySession
            {
                Paid = false,
                ClosedTime = DateTime.MinValue,
                Adjustments = adjustments4,
                Orders = orders4,
                
            };

            sessions = new EntityCollection<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(session1);
            sessions.Add(session2);
            sessions.Add(voidsession1);
            sessions.Add(voidsession2);
        }

        [Test]
        public void TestSeatedSessionsCount()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(1, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["SeatedSessionsCount"]);
        }

        [Test]
        public void TestSeatedSessionsValue()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(200, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["SeatedSessionsValue"]);
        }

        [Test]
        public void TestTakeawaySessionsCount()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(1, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["TakeawaySessionsCount"]);
        }

        [Test]
        public void TestTakeawaySessionsValue()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(200, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["TakeawaySessionsValue"]);
        }


        [Test]
        public void TestSessionGrossValue()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime=DateTime.MinValue,
                EndTime=DateTime.MaxValue,
                Sessions=sessions,
                
            };
            Assert.AreEqual(300, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(),null)["GrossValue"]);
        }

        [Test]
        public void TestCoversCount()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime=DateTime.MinValue,
                EndTime=DateTime.MaxValue,
                Sessions=sessions,
                
            };
            Assert.AreEqual(2, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["CoversCount"]);
        }
        
    
        [Test]
        public void TestVoidCount() 
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(2, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["VoidCount"]);
        }

        [Test]
        public void TestVoidValue() 
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(400, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["VoidValue"]);
        }

        [Test]

        public void TestVoidSeatedSessionsCount()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(1, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["VoidSeatedSessionCount"]);
        }


        [Test]

        public void TestVoidSeatedSessionsValue()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(200, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["VoidSeatedSessionValue"]);
        }


        [Test]

        public void TestVoidTakeawaySessionsCount()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(1, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["VoidTakeawaySessionCount"]);
        }


        [Test]

        public void TestVoidTakeawaySessionsValue()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(200, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["VoidTakeawaySessionValue"]);
        }

        [Test]
        public void TestFoodValue()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(200, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["FoodValue"]);
        }

        [Test]
        public void TestFoodVAT()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(33.34m, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["FoodVAT"]);
        }

        [Test]
        public void TestFoodCount()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(2, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["FoodCount"]); 
        }


        [Test]
        public void TestDrinkValue()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(100, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["DrinkValue"]);
        }

        [Test]
        public void TestDrinkVAT()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(16.67m, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["DrinkVAT"]);
        }

        [Test]
        public void TestDrinkCount()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(1, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["DrinkCount"]);
        }

        [Test]
        public void TestOtherValue()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(100, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["OtherValue"]);
        }

        [Test]
        public void TestOtherVAT()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(16.67m, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["OtherVAT"]);
        }

        [Test]
        public void TestOtherCount()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(1, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["OtherCount"]);
        }

        [Test]
        public void TestTipsWithNoOverPayment()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(0, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["OverPayments"]);
        }

        [Test]
        public void TestTipsWithOverPayment()
        {
            sessions.First().Adjustments.Add(new epicuri.Core.DatabaseModel.Adjustment
            {
                AdjustmentType=payAdj,
                Value=10,
                NumericalType=0
            });
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(10, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["OverPayments"]);
        }

        [Test]
        public void TestGuests()
        {
            
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(2, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["Guests"]);
        }

        [Test]
        public void TestVATValue()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(50, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["VATValue"]);
        }

        [Test]
        public void TestNetValue()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(250, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["NetValue"]);
        }

        [Test]
        public void TestTotalSales()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };
            Assert.AreEqual(400, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["TotalSales"]);
        }
    }
}

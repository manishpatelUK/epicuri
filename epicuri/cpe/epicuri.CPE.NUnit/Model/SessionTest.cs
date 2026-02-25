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
    public class SessionTest
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
                Value=10,
                NumericalType=0
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
        public void TestSubTotalWithNoAdjustmentsOrPayments()
        {
            
            epicuri.CPE.Models.Session session = new Models.Session(s);
            Assert.AreEqual(200, session.SubTotal);
        }

        [Test]
        public void TestTotalBeforeAdjustmentsWithNoAdjustmentsOrPayments()
        {

            epicuri.CPE.Models.Session session = new Models.Session(s);
            Assert.AreEqual(200, session.TotalBeforeAdjustments);
        }

        [Test]
        public void TestTotalWithNoAdjustmentsOrPayments()
        {

            epicuri.CPE.Models.Session session = new Models.Session(s);
            Assert.AreEqual(200, session.Total);
        }

        [Test]
        public void TestRemainingTotalWithNoAdjustmentsOrPayments()
        {

            epicuri.CPE.Models.Session session = new Models.Session(s);
            Assert.AreEqual(200, session.RemainingTotal);
        }

        [Test]
        public void TestTotalWithTipNoAdjustmentsOrPayments()
        {
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

         


            epicuri.CPE.Models.SeatedSession session = new Models.SeatedSession(new epicuri.Core.DatabaseModel.SeatedSession
            {
                Orders = orders,
                Party = new Core.DatabaseModel.Party
                {
                    Name="a"
                },
                Service = new Core.DatabaseModel.Service
                {
                    ServiceName="a"
                },
                TipTotal = 10,
            });
            Assert.AreEqual(220, session.Total);
        }



        [Test]
        public void TestSeatedSessionRemainingTotalWithTipNoAdjustmentsOrPayments()
        {
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




            epicuri.CPE.Models.SeatedSession session = new Models.SeatedSession(new epicuri.Core.DatabaseModel.SeatedSession
            {
                Orders = orders,
                Party = new Core.DatabaseModel.Party
                {
                    Name = "a"
                },
                Service = new Core.DatabaseModel.Service
                {
                    ServiceName = "a"
                },
                TipTotal = 10,
            });
            //200+20 Includes 10% for tip
            Assert.AreEqual(220, session.RemainingTotal);
        }




        [Test]
        public void TestSubTotalWithAdjustmentsNoPayments()
        {
            s.Adjustments = adjustmentsNoPayments;
            epicuri.CPE.Models.Session session = new Models.Session(s);
            Assert.AreEqual(200, session.SubTotal);
        }

        [Test]
        public void TestTotalWithAdjustmentNoPayments()
        {
            s.Adjustments = adjustmentsNoPayments;
            epicuri.CPE.Models.Session session = new Models.Session(s);
            Assert.AreEqual(200, session.TotalBeforeAdjustments);
        }

        [Test]
        public void TestTotalWithAdjustmentsNoPayments()
        {
            s.Adjustments = adjustmentsNoPayments;
            epicuri.CPE.Models.Session session = new Models.Session(s);
            Assert.AreEqual(190, session.Total);
        }

        [Test]
        public void TestRemainingTotalWithAdjustmentsNoPayments()
        {
            s.Adjustments = adjustmentsNoPayments;
            epicuri.CPE.Models.Session session = new Models.Session(s);
            Assert.AreEqual(190, session.RemainingTotal);
        }


        [Test]
        public void TestTotalWithTipAdjustmentsNoPayments()
        {
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


            epicuri.CPE.Models.SeatedSession session = new Models.SeatedSession(new epicuri.Core.DatabaseModel.SeatedSession
                {
                    Adjustments = adjustmentsNoPayments,
                    Orders=orders,
                    Party = new Core.DatabaseModel.Party
                    {
                        Name = "a"
                    },
                    Service = new Core.DatabaseModel.Service
                    {
                        ServiceName = "a"
                    },
                    TipTotal = 10
                });

            //200 - 10=190. 190+10% = 209
            Assert.AreEqual(209, session.Total);
        }




        [Test]
        public void TestSeatedSessionRemainingTotalWithTipAdjustmentsNoPayments()
        {
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


            epicuri.CPE.Models.SeatedSession session = new Models.SeatedSession(new epicuri.Core.DatabaseModel.SeatedSession
            {
                Adjustments = adjustmentsNoPayments,
                Orders = orders,
                Party = new Core.DatabaseModel.Party
                {
                    Name = "a"
                },
                Service = new Core.DatabaseModel.Service
                {
                    ServiceName = "a"
                },
                TipTotal = 10
            });

            //Session total is 200, 190 after adjustments add 10% tips = 190+19 = 209
            Assert.AreEqual(209, session.RemainingTotal);
        }







        [Test]
        public void TestSubTotalWithPaymentsNoAdjustments()
        {
            s.Adjustments = paymentsNoAdjustments;
            epicuri.CPE.Models.Session session = new Models.Session(s);
            Assert.AreEqual(200, session.SubTotal);
        }

        [Test]
        public void TestTotalBeforeAdjustmentsWithPaymentsNoAdjustments()
        {
            s.Adjustments = paymentsNoAdjustments;
            epicuri.CPE.Models.Session session = new Models.Session(s);
            Assert.AreEqual(200, session.TotalBeforeAdjustments);
        }

        [Test]
        public void TestTotalWithPaymentsNoAdjustments()
        {
            s.Adjustments = paymentsNoAdjustments;
            epicuri.CPE.Models.Session session = new Models.Session(s);
            Assert.AreEqual(200, session.Total);
        }

        [Test]
        public void TestRemainingTotalWithPaymentsNoAdjustments()
        {
            s.Adjustments = paymentsNoAdjustments;
            epicuri.CPE.Models.Session session = new Models.Session(s);
            Assert.AreEqual(190, session.RemainingTotal);
        }


        [Test]
        public void TestTotalWithTipPaymentsNoAdjustments()
        {
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

            epicuri.CPE.Models.Session session = new Models.SeatedSession(new epicuri.Core.DatabaseModel.SeatedSession{
                Adjustments=paymentsNoAdjustments,
                Orders=orders,
                
                Party = new Core.DatabaseModel.Party
                {
                    Name="a"
                },
                Service = new Core.DatabaseModel.Service
                {
                    ServiceName="a"
                }, TipTotal = 10
            });
            Assert.AreEqual(220, session.Total);
        }

        [Test]
        public void TestRemainingTotalWithTipPaymentsNoAdjustments()
        {
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

            epicuri.CPE.Models.Session session = new Models.SeatedSession(new epicuri.Core.DatabaseModel.SeatedSession
            {
                Adjustments = paymentsNoAdjustments,
                Orders = orders,

                Party = new Core.DatabaseModel.Party
                {
                    Name = "a"
                },
                Service = new Core.DatabaseModel.Service
                {
                    ServiceName = "a"
                },
                TipTotal = 10
            });
            //Add 10% tip to the 200 session total, subtract -10 adjustments
            Assert.AreEqual(210, session.RemainingTotal);
        }













        [Test]
        public void TestSubTotalWithPaymentsAndAdjustments()
        {
            s.Adjustments = paymentsAndAdjustments;
            epicuri.CPE.Models.Session session = new Models.Session(s);
            Assert.AreEqual(200, session.SubTotal);
        }

        [Test]
        public void TestTotalBeforeAdjustmentsWithPaymentsAndAdjustments()
        {
            s.Adjustments = paymentsAndAdjustments;
            epicuri.CPE.Models.Session session = new Models.Session(s);
            Assert.AreEqual(200, session.TotalBeforeAdjustments);
        }

        [Test]
        public void TestTotalWithPaymentsAndAdjustments()
        {
            s.Adjustments = paymentsAndAdjustments;
            epicuri.CPE.Models.Session session = new Models.Session(s);
            Assert.AreEqual(190, session.Total);
        }

        [Test]
        public void TestRemainingTotalWithPaymentsAndAdjustments()
        {
            s.Adjustments = paymentsAndAdjustments;
            epicuri.CPE.Models.Session session = new Models.Session(s);
            Assert.AreEqual(180, session.RemainingTotal);
        }


        [Test]
        public void TestTotalWithTipPaymentsAndAdjustments()
        {
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

            epicuri.CPE.Models.Session session = new Models.SeatedSession(new epicuri.Core.DatabaseModel.SeatedSession
            {
                Adjustments = paymentsAndAdjustments,
                Orders = orders,

                Party = new Core.DatabaseModel.Party
                {
                    Name = "a"
                },
                Service = new Core.DatabaseModel.Service
                {
                    ServiceName = "a"
                },
                TipTotal = 10
            });
            Assert.AreEqual(209, session.Total);
        }

        [Test]
        public void TestSeatedSessionRemainingTotalWithTipPaymentsAndAdjustments()
        {
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

            epicuri.CPE.Models.Session session = new Models.SeatedSession(new epicuri.Core.DatabaseModel.SeatedSession
            {
                Adjustments = paymentsAndAdjustments,
                Orders = orders,

                Party = new Core.DatabaseModel.Party
                {
                    Name = "a"
                },
                Service = new Core.DatabaseModel.Service
                {
                    ServiceName = "a"
                },
                TipTotal = 10
            });
            //200-10(adj) => 190+(10% TIP) 19, => 209-10(pay)
            Assert.AreEqual(199, session.RemainingTotal);
        }





        [Test]
        public void TestRealAdjustmentsEmpty()
        {
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.IsEmpty(sess.RealAdjustments);
        }

        [Test]
        public void TestRealPaymentsEmpty()
        {
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.IsEmpty(sess.RealPayments);
        }

        [Test]
        public void TestRealAdjustmentContainsSumOfAbsAdjustments()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
                {
                    Value = 1,
                    NumericalType = 0,
                    AdjustmentType = new Core.DatabaseModel.AdjustmentType
                    {
                        Type = 1
                    }

                });

            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(1,sess.RealAdjustments.Sum(a=>a.AbsAdjustment));
        }

        [Test]
        public void TestRealPaymentsContainsSumOfAbsPayments()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 1,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(1, sess.RealPayments.Sum(a => a.AbsAdjustment));
        }




        [Test]
        public void TestRealAdjustmentsWith2AdjustmentsContainsSumOfAbsAdjustments()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 1,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 1,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });

            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(2, sess.RealAdjustments.Sum(a => a.AbsAdjustment));
        }

        [Test]
        public void TestRealPaymentsWith2PaymentsContainsSumOfAbsPayments()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 1,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0
                }

            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 1,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(2, sess.RealPayments.Sum(a => a.AbsAdjustment));
        }




        [Test]
        public void TestRealAdjustmentsWith1Adjustment1PaymentContainsSumOfAbsAdjustments()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 1,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 1,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0
                }

            });

            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(1, sess.RealAdjustments.Sum(a => a.AbsAdjustment));
        }

        [Test]
        public void TestRealPaymentsWith1Payment1AdjustmentContainsSumOfAbsPayments()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 1,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0
                }

            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 1,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(1, sess.RealPayments.Sum(a => a.AbsAdjustment));
        }





        [Test]
        public void TestRealAdjustmentContainsSumOfRelativeAdjustments()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
                {
                    Value = 10,
                    NumericalType = 1,
                    AdjustmentType = new Core.DatabaseModel.AdjustmentType
                    {
                        Type = 1
                    }

                });

            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(20,sess.RealAdjustments.Sum(a=>a.AbsAdjustment));
        }

        [Test]
        public void TestRealPaymentsContainsSumOfRelativePayments()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 10,
                NumericalType = 1,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(20, sess.RealPayments.Sum(a => a.AbsAdjustment));
        }




        [Test]
        public void TestRealAdjustmentsWith2AdjustmentsContainsSumOfRelativeAdjustments()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 50,
                NumericalType = 1,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 10,
                NumericalType = 1,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });

            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(110, sess.RealAdjustments.Sum(a => a.AbsAdjustment));
        }

        [Test]
        public void TestRealPaymentsWith2PaymentsContainsSumOfRelativePayments()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 50,
                NumericalType = 1,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0
                }

            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 10,
                NumericalType = 1,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(110, sess.RealPayments.Sum(a => a.AbsAdjustment));
        }




        [Test]
        public void TestRealAdjustmentsWith1Adjustment1PaymentContainsSumOfRelativeAdjustments()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 50,
                NumericalType = 1,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 10,
                NumericalType = 1,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0
                }

            });

            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(100, sess.RealAdjustments.Sum(a => a.AbsAdjustment));
        }

        [Test]
        public void TestRealPaymentsWith1Payment1AdjustmentContainsSumOfRelativePayments()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 50,
                NumericalType = 1,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0
                }

            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 10,
                NumericalType = 1,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(90, sess.RealPayments.Sum(a => a.AbsAdjustment));
        }





        [Test]
        public void TestPaymentDoesNotAffectSessionTotal()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 1,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(sess.TotalBeforeAdjustments, sess.Total);
        }

        [Test]
        public void TestAdjustmentValueEqualToSessionTotal()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 200,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(0,sess.Total);
        }


        [Test]
        public void TestAdjustmentValueEqualToSessionRemainingTotal()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 200,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(0, sess.RemainingTotal);
        }


        [Test]
        public void TestAdjustmentValueEqualToSessionAdjustmentVal()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 200,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(200, sess.RealAdjustments.Sum(a=>a.AbsAdjustment));
        }


        [Test]
        public void TestAdjustmentValueEqualToSessionAdjustmentValWithOverAdjustment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(200, sess.RealAdjustments.Sum(a => a.AbsAdjustment));
        }



        [Test]
        public void TestAdjustmentValueEqualToSessionTotalWithOverAdjustment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(0, sess.Total);
        }


        [Test]
        public void TestAdjustmentValueEqualToSessionRemainingTotalWithOverAdjustment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(0, sess.RemainingTotal);
        }



        [Test]
        public void TestChangeValueEqualToSessionRemainingTotalWithOverAdjustment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }
                

            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(100, sess.Change);
        }


        [Test]
        public void TestOverPaymentsZeroWithOverAdjustment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }
                

            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(0, sess.OverPayments);
        }

        [Test]
        public void TestRemainingTotalNegativeWithOverAdjustment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(-100, sess.RemainingTotal);
        }





        [Test]
        public void TestChangeValueEqualToSessionRemainingTotalWithPartOverAdjustment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(100, sess.Change);
        }


        [Test]
        public void TestOverPaymentsZeroWithPartOverAdjustment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(0, sess.OverPayments);
        }

        [Test]
        public void TestRemainingTotalNegativeWithPartOverAdjustment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(-100, sess.RemainingTotal);
        }




        [Test]
        public void TestChangeValueEqualToSessionRemainingTotalWithPartNAOverAdjustment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(0, sess.Change);
        }


        [Test]
        public void TestOverPaymentsZeroWithPartNAOverAdjustment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(100, sess.OverPayments);
        }

        [Test]
        public void TestRemainingTotalNegativeWithPartNAOverAdjustment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 150,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(-100, sess.RemainingTotal);
        }




        [Test]
        public void TestLimitedChangeRemainingTotal()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 199,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 2,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(-1, sess.RemainingTotal);
        }


        [Test]
        public void TestLimitedChangeChange()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 199,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 2,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(1, sess.Change);
        }


        [Test]
        public void TestLimitedChangeOverPayment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 199,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 2,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(0, sess.OverPayments);
        }


        [Test]
        public void TestLimitedCardRemainingTotal()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 210,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 2,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(-12, sess.RemainingTotal);
        }


        [Test]
        public void TestLimitedCardChange()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 210,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 2,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(2, sess.Change);
        }


        [Test]
        public void TestLimitedCardOverPayment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 210,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 2,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(10, sess.OverPayments);
        }


        [Test]
        public void TestLimitedDoubleCardOverPayment()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 210,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 2,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(12, sess.OverPayments);
        }




        [Test]
        public void TestLimitedCardOverPaymentCorrectCardVal()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 210,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 2,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(210, sess.RealPayments.First().AbsAdjustment);
        }


        [Test]
        public void TestLimitedCardOverPaymentCorrectCashVal()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 210,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 2,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(2, sess.RealPayments.Last().AbsAdjustment);
        }


        [Test]
        public void TestLimitedCardOverPaymentCorrectCashValWith3Payments()
        {
            var adjustments = new EntityCollection<epicuri.Core.DatabaseModel.Adjustment>();
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 210,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 0
                }


            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 2,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }

            });
            adjustments.Add(new Core.DatabaseModel.Adjustment
            {
                Value = 2,
                NumericalType = 0,
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Type = 0,
                    SupportsChange = 1
                }

            });
            s.Adjustments = adjustments;
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(2, sess.RealPayments.Last().AbsAdjustment);
        }




    }
}

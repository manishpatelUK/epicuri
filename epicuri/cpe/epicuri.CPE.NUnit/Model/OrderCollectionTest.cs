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
    public class OrderCollectionTest
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
        public void TestNoAdjustmentsMakesTotalSameAsGrossValue()
        {
            epicuri.CPE.Models.Session sess = new Models.Session(s);
            Assert.AreEqual(sess.Total, sess.Orders.Sum(o=>o.OrderValueAfterAdjustment(sess)));
        }

   

        [Test]
        public void TestRealAdjustmentReducesGrossValue()
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
            Assert.AreEqual(sess.Total, sess.Orders.Sum(o => o.OrderValueAfterAdjustment(sess)));
        }

        [Test]
        public void TestRealAdjustmentDoesNotAffectTotalBeforeAdjustment()
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
            Assert.AreEqual(sess.TotalBeforeAdjustments - 1, sess.Orders.Sum(o => o.OrderValueAfterAdjustment(sess)));
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
            Assert.AreEqual(sess.TotalBeforeAdjustments, sess.Orders.Sum(o=>o.OrderValueAfterAdjustment(sess)));
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
            Assert.AreEqual(198, sess.Orders.Sum(a => a.OrderValueAfterAdjustment(sess)));
        }


        [Test]
        public void TestRealAdjustmentsWith2AdjustmentsContainsSumOfAbsAdjustmentsTotal()
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
            Assert.AreEqual(198, sess.Total);
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


        
        /// <summary>
        /// Add an adjustment to the order which reduces the value by 1. 
        /// Add a payment to the order which reduces the balance due by 1
        /// 
        /// Verify that the value of the order is 1 lower than the original price
        /// </summary>
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
            Assert.AreEqual(199, sess.Orders.Sum(o=>o.OrderValueAfterAdjustment(sess)));
        }



        [Test]
        public void TestRealAdjustmentsWith1Adjustment1PaymentContainsSumOfAbsAdjustmentsTotal()
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
            Assert.AreEqual(199, sess.Total);
        }

        [Test]
        public void TestRealAdjustmentsWith1Adjustment1PaymentContainsSumOfAbsAdjustmentTotalBefore()
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
            Assert.AreEqual(200, sess.TotalBeforeAdjustments);
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
            Assert.AreEqual(180, sess.Orders.Sum(o=>o.OrderValueAfterAdjustment(sess)));
        }

         [Test]
        public void TestRealAdjustmentContainsSumOfRelativeAdjustmentsTotal()
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
            Assert.AreEqual(180, sess.Total);
        }

        /*

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
           Assert.AreEqual(100, sess.RealPayments.Sum(a => a.AbsAdjustment));
       }
       */
    }
}

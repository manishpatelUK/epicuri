using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using epicuri.CPE.Models.Reporting;
using NUnit.Framework;
using System.Data.Objects.DataClasses;
namespace epicuri.CPE.NUnit.Model.Reporting
{
    [TestFixture]
    public class CustomerReportTest
    {


        private EntityCollection<Core.DatabaseModel.Order> singleOrderSet()
        {
            var ec = new EntityCollection<Core.DatabaseModel.Order>();
            ec.Add(new Core.DatabaseModel.Order
            {
                MenuItem = new Core.DatabaseModel.MenuItem
                {
                    Price = 10

                },
                Modifiers = new EntityCollection<Core.DatabaseModel.Modifier>()
            });
            return ec;
        }

        private EntityCollection<Core.DatabaseModel.Diner> dinerOb(Core.DatabaseModel.Customer d1, Core.DatabaseModel.Customer d2 = null)
        {
            var diners = new EntityCollection<Core.DatabaseModel.Diner>();

            diners.Add(new Core.DatabaseModel.Diner
            {
                Customer = d1,
                Orders = singleOrderSet()
            });

            if (d2 != null)
            {
                diners.Add(new Core.DatabaseModel.Diner
                {
                    Customer = d2,
                    Orders = singleOrderSet()
                });
            }
            return diners;
        }


        CustomerReport cr;
        [SetUp]
        public void SetUp()
        {
            var parties = new EntityCollection<Core.DatabaseModel.Party>();
            var takeaways = new EntityCollection<Core.DatabaseModel.Session>();


            var customer1 = new Core.DatabaseModel.Customer {
                Id=1
            };

            var customer2 = new Core.DatabaseModel.Customer {
                Id=2
            };

            takeaways.Add(new Core.DatabaseModel.TakeAwaySession
            {
                PrimaryCustomer = customer1,
                Orders= singleOrderSet()
            });

            takeaways.Add(new Core.DatabaseModel.TakeAwaySession
            {
                PrimaryCustomer = customer1,
                Orders = singleOrderSet()
            });

            takeaways.Add(new Core.DatabaseModel.TakeAwaySession
            {
                PrimaryCustomer = customer2,
                Orders = singleOrderSet()
            });




            parties.Add(new Core.DatabaseModel.Party
            {
                LeadCustomer = customer1,
                Session = new Core.DatabaseModel.SeatedSession {
                    Orders = singleOrderSet(),
                    Diners = dinerOb(customer1)
                }
            });

            parties.Add(new Core.DatabaseModel.Party
            {
                LeadCustomer = customer2,
                Session = new Core.DatabaseModel.SeatedSession
                {
                    Orders = singleOrderSet(),
                    Diners = dinerOb(customer2)
                }
            });

            parties.Add(new Core.DatabaseModel.Party
            {
                LeadCustomer = customer1,
                Session = new Core.DatabaseModel.SeatedSession
                {
                    Orders = singleOrderSet(),
                    Diners = dinerOb(customer1,customer2)
                }
            });

            parties.Add(new Core.DatabaseModel.Party
            {
                LeadCustomer = customer2,
                Session = new Core.DatabaseModel.SeatedSession
                {
                    Orders = singleOrderSet(),
                    Diners = dinerOb(customer1,customer2)
                }
            });




            Core.DatabaseModel.Restaurant r = new Core.DatabaseModel.Restaurant
            {
                Parties = parties,
                Sessions = takeaways
            };



            cr = new CustomerReport(r);
            

        }



        [Test]
        public void TestReportLengthEqualNumberOfCustomers()
        {

            /*
             * Customer report length 2 - showing 2 customers who have dined on more than 2 occasions
             */
            Assert.AreEqual(2,cr.GetReport(null,null).Count());
        }

        [Test]
        public void TestFirstCustomerTakeawayCount()
        {
            var rl = (CPE.Models.Reporting.Line.CustomerLine)cr.GetReport(null, null).First();
            Assert.AreEqual(2, rl.TakeawayInteractions);
        }


        [Test]
        public void TestSecondCustomerTakeawayCount()
        {
            var rl = (CPE.Models.Reporting.Line.CustomerLine)cr.GetReport(null, null).Last();
            Assert.AreEqual(1, rl.TakeawayInteractions);
        }


        [Test]
        public void TestFirstCustomerSeatedCount()
        {
            var rl = (CPE.Models.Reporting.Line.CustomerLine)cr.GetReport(null, null).First();
            Assert.AreEqual(3, rl.SeatedInteractions);
        }


        [Test]
        public void TestSecondCustomerSeatedCount()
        {
            var rl = (CPE.Models.Reporting.Line.CustomerLine)cr.GetReport(null, null).Last();
            Assert.AreEqual(3, rl.SeatedInteractions);
        }

        [Test]
        public void TestFirstCustomerIndividualValue()
        {
            var rl = (CPE.Models.Reporting.Line.CustomerLine)cr.GetReport(null, null).First();
            Assert.AreEqual(20 + 10, rl.IndividualValue);
        }

        [Test]
        public void TestSecondCustomerIndividualValue()
        {
            var rl = (CPE.Models.Reporting.Line.CustomerLine)cr.GetReport(null, null).Last();
            Assert.AreEqual(10 + 10, rl.IndividualValue);
        }


        [Test]
        public void TestFirstCustomerGroupValue()
        {
            var rl = (CPE.Models.Reporting.Line.CustomerLine)cr.GetReport(null, null).First();
            Assert.AreEqual(20, rl.TotalGroupValue);
        }

        [Test]
        public void TestSecondCustomerGroupValue()
        {
            var rl = (CPE.Models.Reporting.Line.CustomerLine)cr.GetReport(null, null).Last();
            Assert.AreEqual(20, rl.TotalGroupValue);
        }

        [Test]
        public void TestFirstCustomerTotalValue()
        {
            var rl = (CPE.Models.Reporting.Line.CustomerLine)cr.GetReport(null, null).First();
            Assert.AreEqual(45, rl.ActualValue);
        }

        [Test]
        public void TestSecondCustomerTotalValue()
        {
            var rl = (CPE.Models.Reporting.Line.CustomerLine)cr.GetReport(null, null).Last();
            Assert.AreEqual(35, rl.ActualValue);
        }
    }
}

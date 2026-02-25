using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework;
using epicuri.CPE.Models;
using epicuri.CPE.Models.Reporting.Line;
using System.Data.Objects.DataClasses;
namespace epicuri.CPE.NUnit.Model.Reporting.Line
{
    [TestFixture]
    public class CustomerReportLineTest
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
        CustomerLine cl;
        [SetUp]
        public void SetUp()
        {
            var interactions = new EntityCollection<Core.DatabaseModel.Diner>();

            Core.DatabaseModel.Customer cust = new Core.DatabaseModel.Customer
            {
                Name= new Core.DatabaseModel.Name{
                    Firstname="a",Surname="b"
                },
                Email="a",
                PhoneNumber = "a",

            };

            Core.DatabaseModel.Customer othercust = new Core.DatabaseModel.Customer
            {
                
            };


            var parties = new EntityCollection<Core.DatabaseModel.Party>();
            var diners1 = new EntityCollection<Core.DatabaseModel.Diner>();
            var diners2 = new EntityCollection<Core.DatabaseModel.Diner>();
            var diners3 = new EntityCollection<Core.DatabaseModel.Diner>();

            diners1.Add(new Core.DatabaseModel.Diner
            {
                Customer = cust
                
            });
             diners3.Add(new Core.DatabaseModel.Diner {
                 Customer = cust,
                 Orders = singleOrderSet()
             });
            

            parties.Add(new Core.DatabaseModel.WaitingList
            {
                LeadCustomer = cust,
                Session = new Core.DatabaseModel.SeatedSession
                {
                    Diners = diners1,
                    Orders = this.singleOrderSet()
                }
                
            });



            parties.Add(new Core.DatabaseModel.WaitingList
            {
                LeadCustomer = othercust,
                Session = new Core.DatabaseModel.SeatedSession
                {
                    Diners = diners3,
                    Orders = this.singleOrderSet()
                }
            });




            var takeaways = new List<Core.DatabaseModel.TakeAwaySession>();
            takeaways.Add(new Core.DatabaseModel.TakeAwaySession
            {
                StartTime = DateTime.MinValue,
                Orders = this.singleOrderSet(),

            });
            
            cl = new CustomerLine(cust,DateTime.MinValue,DateTime.MaxValue, takeaways, parties);
        }


        [Test]
        public void TestCustomerNameCorrect()
        {
            Assert.AreEqual("a b", cl.Name);
        }

        [Test]
        public void TestCustomerEmailCorrect()
        {
            Assert.AreEqual("a", cl.Email);
        }

        [Test]
        public void TestCustomerPhoneCorrect()
        {
            Assert.AreEqual("a", cl.Telephone);
        }

        [Test]
        public void TestCustomerSeatedSessionsCount()
        {
            Assert.AreEqual(2, cl.SeatedInteractions);
        }

        [Test]
        public void TestCustomerTakeawaySessionsCount()
        {
            Assert.AreEqual(1, cl.TakeawayInteractions);
        }

        [Test]
        public void TestCustomerFirstInteractionDate()
        {
            Assert.AreEqual(DateTime.MinValue, cl.FirstInteraction);
        }

        [Test]
        public void TestCustomerLastInteractionDate()
        {
            Assert.AreEqual(DateTime.MinValue, cl.LastInteraction);
        }

        /*
         *  10 from lead
         *  10 from takeaway
         */
        [Test]
        public void TestCustomerIndividualValue()
        {
            Assert.AreEqual(20, cl.IndividualValue);
        }

        [Test]
        public void TestCustomerGroupValue()
        {
            Assert.AreEqual(10,cl.TotalGroupValue);
        }

        [Test]
        public void TestCustomerActualValue()
        {
            Assert.AreEqual(30, cl.ActualValue);
        }
    }
}

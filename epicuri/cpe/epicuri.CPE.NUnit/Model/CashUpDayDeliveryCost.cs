using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework;
using System.Data.Objects.DataClasses;
using epicuri.CPE.Models;
using epicuri.Core;
namespace epicuri.CPE.NUnit.Model
{
    [TestFixture]
    public class CashUpDayDeliveryCost
    {
        
        EntityCollection<epicuri.Core.DatabaseModel.Session> sessions =  new EntityCollection<epicuri.Core.DatabaseModel.Session>();
        EntityCollection<epicuri.Core.DatabaseModel.Session> sessionsVat = new EntityCollection<Core.DatabaseModel.Session>();

        [SetUp]
        public void SetUp()
        {
            epicuri.Core.DatabaseModel.TaxType t = new Core.DatabaseModel.TaxType
            {
                Rate = 0
            };
            epicuri.Core.DatabaseModel.TakeAwaySession sess = new Core.DatabaseModel.TakeAwaySession();
            var orders4 = new EntityCollection<epicuri.Core.DatabaseModel.Order>();
            var order7 = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 100,
                    TaxType = t
                },
                Session = sess

            };
            orders4.Add(order7);

            var order8 = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 100,
                    TaxType = t,
                    
                },
                Session = sess
            };


            orders4.Add(order8);
            
            sess.Orders = orders4;
            sess.Delivery = true;
            sess.Restaurant = new Core.DatabaseModel.Restaurant();
            sess.Restaurant.Position = new Core.DatabaseModel.LatLongPair();
            sess.Restaurant.Position.Latitude = 53.9552309;
            sess.Restaurant.Position.Longitude = -1.0835643;
            sess.Accepted = true;
            sess.Rejected = false;
            sess.ClosedTime = DateTime.MinValue;
            sess.Paid = true;
            sess.Restaurant.Id = 0;
            sess.DeliveryAddress = new Core.DatabaseModel.Address();
            sess.DeliveryAddress.PostCode = "YO10 5AA";
            Settings.DefineForSingleUse<String>(0, "FreeDeliveryRadius", "0");
            Settings.DefineForSingleUse<String>(0, "DeliverySurcharge", "20");
            sessions = new EntityCollection<epicuri.Core.DatabaseModel.Session>();
            sessions.Add(sess);







            epicuri.Core.DatabaseModel.TaxType t2 = new Core.DatabaseModel.TaxType
            {
                Rate = 20
            };
            epicuri.Core.DatabaseModel.TakeAwaySession sess1 = new Core.DatabaseModel.TakeAwaySession();
            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();
            var order1 = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 100,
                    TaxType = t2
                },
                Session = sess1

            };
            orders.Add(order1);

            var order2 = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 100,
                    TaxType = t2,

                },
                Session = sess1
            };


            orders.Add(order2);

            sess1.Orders = orders;
            sess1.Delivery = true;
            sess1.Restaurant = new Core.DatabaseModel.Restaurant();
            sess1.Restaurant.Position = new Core.DatabaseModel.LatLongPair();
            sess1.Restaurant.Position.Latitude = 53.9552309;
            sess1.Restaurant.Position.Longitude = -1.0835643;
            sess1.Accepted = true;
            sess1.Rejected = false;
            sess1.ClosedTime = DateTime.MinValue;
            sess1.Paid = true;
            sess1.Restaurant.Id = 0;
            sess1.DeliveryAddress = new Core.DatabaseModel.Address();
            sess1.DeliveryAddress.PostCode = "YO10 5AA";
            Settings.DefineForSingleUse<String>(0, "FreeDeliveryRadius", "0");
            Settings.DefineForSingleUse<String>(0, "DeliverySurcharge", "20");
            sessionsVat = new EntityCollection<epicuri.Core.DatabaseModel.Session>();
            sessionsVat.Add(sess1);

        }
        [TestCase]
        public void TestSingleDeliveryCost()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };

            Assert.AreEqual(20,CashUpDay.CreateCashUpReport(cud,sessions.AsQueryable(),null)["TotalDelivery"]);
        }


        [TestCase]
        public void TestNet()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };

            Assert.AreEqual(220, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["NetValue"]);
        }


        [TestCase]
        public void TestVAT()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };

            Assert.AreEqual(0, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["VATValue"]);
        }


        [TestCase]
        public void TestGross()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessions,

            };

            Assert.AreEqual(220, CashUpDay.CreateCashUpReport(cud, sessions.AsQueryable(), null)["GrossValue"]);

        }







        [TestCase]
        public void TestSingleDeliveryCost1()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessionsVat,

            };

            Assert.AreEqual(20, CashUpDay.CreateCashUpReport(cud, sessionsVat.AsQueryable(), null)["TotalDelivery"]);
        }


        [TestCase]
        public void TestNet1()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessionsVat,

            };

            //Total cost - 220 inc del.
            //

            Assert.AreEqual(183.34m, CashUpDay.CreateCashUpReport(cud, sessionsVat.AsQueryable(), null)["NetValue"]);
        }


        [TestCase]
        public void TestVAT1()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessionsVat,

            };

            Assert.AreEqual(36.66m, CashUpDay.CreateCashUpReport(cud, sessionsVat.AsQueryable(), null)["VATValue"]);
        }


        [TestCase]
        public void TestGross1()
        {
            var cud = new epicuri.Core.DatabaseModel.CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MaxValue,
                Sessions = sessionsVat,

            };

            Assert.AreEqual(220, CashUpDay.CreateCashUpReport(cud, sessionsVat.AsQueryable(), null)["GrossValue"]);

        }
    }

}

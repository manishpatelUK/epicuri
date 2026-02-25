using epicuri.Core;
using epicuri.CPE.Models;
using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.Data.Objects.DataClasses;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace epicuri.CPE.NUnit.Model
{
    [TestFixture]
    public class TakeawaySessionTest
    {
        [TestCase]
        public void DeliveryVATRateNone()
        {

          

            epicuri.Core.DatabaseModel.TaxType t2 = new Core.DatabaseModel.TaxType
            {
                Rate = 0
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

            TakeawaySession ts = new TakeawaySession(sess1);
            Assert.AreEqual(0, ts.GetDeliveryVAT(ts));
        }

        [TestCase]
        public void DeliveryVATRateProportional()
        {

            epicuri.Core.DatabaseModel.TaxType t1 = new Core.DatabaseModel.TaxType
            {
                Rate = 50
            };

            epicuri.Core.DatabaseModel.TaxType t2 = new Core.DatabaseModel.TaxType
            {
                Rate = 0
            };
            epicuri.Core.DatabaseModel.TakeAwaySession sess1 = new Core.DatabaseModel.TakeAwaySession();
            var orders = new EntityCollection<epicuri.Core.DatabaseModel.Order>();
            var order1 = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = new epicuri.Core.DatabaseModel.MenuItem
                {
                    Name = "test",
                    Price = 100,
                    TaxType = t1
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

            TakeawaySession ts = new TakeawaySession(sess1);
            Assert.AreEqual(4, ts.GetDeliveryVAT(ts));
        }

        [TestCase]
        public void DeliveryVATRateAll()
        {
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

            TakeawaySession ts = new TakeawaySession(sess1);
            Assert.AreEqual(3.33, ts.GetDeliveryVAT(new Models.Session(sess1)));

        }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestTools.UnitTesting;

using epicuri.CPE;

namespace epicuri.CPE.Tests
{
    [TestClass]
    public class MewsTest
    {
        [TestMethod]
        public void GetCustomer()
        {
            var result = Mews.MewsAPI.SearchCustomer("Smith");

            Assert.AreNotEqual((List<Mews.MewsCustomer>)result.Result, 0);
        }

        [TestMethod]
        public void ChargeCustomer()
        {
            Mews.MewsCharge charge = new Mews.MewsCharge();

            charge.Customer = new Mews.MewsCustomer() { Id = "b36664dc-57b5-4c2f-8e2f-301810cc3a2f" };

            charge.Items = new List<Mews.MewsChargeItem>();
            
            charge.Items.Add(new Mews.MewsChargeItem(){
               Name = "Beer",
               UnitCount = 10,
               UnitCost = new Mews.MewsChargeItemUnitCost()
               {
                   Amount = 2.50m,
                   Currency = "GBP",
                   Tax = 0.2
               },
               Category = new Mews.MewsChargeItemCategory()
               {
                   Code = "ABC",
                   Name = "Alcoholic Beverage"
               }
            });


            var customers = Mews.MewsAPI.ChargeCustomer("2BEC1AC810DB4983BA996174827BB259-85AEFF6419BAF4BE76E0270A9FA1E20", charge);

            Assert.IsNotNull(customers);
        }
    }
}

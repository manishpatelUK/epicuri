using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace epicuri.Core.DatabaseTests
{
    [TestClass]
    public class TestCreateRestaurant
    {
        [TestMethod]
        public void CreateRestaurant()
        {
            epicuri.Core.DatabaseModel.epicuriContainer db = new epicuri.Core.DatabaseModel.epicuriContainer();

            var r = new epicuri.Core.DatabaseModel.Restaurant
            {
                Name = "Test Restaurant",
                Category = db.Categories.First(cat => cat.Name == "Mexican"),
                Currency = db.Currencies.First(cur => cur.CurrencyName == "Pound"),
                Country = db.Countries.First(country => country.Name == "United Kingdom"),
                Description = "My Mexican Restaurant",
                Address = new DatabaseModel.Address { City = "Southampton", PostCode = "SO16 1AA", Street = "High Street", Town = "Milbrook" }
            };

            db.AddToRestaurants(r);
            db.SaveChanges();

            Assert.AreNotEqual(0, r.Id);
            int id = r.Id;
            db.Restaurants.DeleteObject(r);
            db.SaveChanges();

            Assert.IsNull(db.Restaurants.FirstOrDefault(row => row.Id == id));
        }

        
    }
}

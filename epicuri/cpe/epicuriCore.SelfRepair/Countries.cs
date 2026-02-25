using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace epicuri.Core.SelfRepair
{
    [TestClass]
    public class Countries : SRSUtils
    {
        protected static List<epicuri.Core.DatabaseModel.Country> ExpectedCountries;

        [ClassInitialize]
        static public void SetUpCountries(Microsoft.VisualStudio.TestTools.UnitTesting.TestContext tc)
        {
            Connect(tc);
            ExpectedCountries = new List<DatabaseModel.Country>();

            ExpectedCountries.Add(new DatabaseModel.Country { Name = "United States", Acronym = "US" });
            ExpectedCountries.Add(new DatabaseModel.Country { Name = "United Kingdom", Acronym = "UK" });
            ExpectedCountries.Add(new DatabaseModel.Country { Name = "Belgium", Acronym ="BE" });
            ExpectedCountries.Add(new DatabaseModel.Country { Name = "South Africa", Acronym = "ZA" });
            ExpectedCountries.Add(new DatabaseModel.Country { Name = "Uruguay", Acronym = "UY" });
            ExpectedCountries.Add(new DatabaseModel.Country { Name = "Turkey", Acronym = "TR" });


        }

        [TestMethod]
        public void Examine()
        {
            foreach (DatabaseModel.Country Country in ExpectedCountries)
            {
                var qry = from b in db.Countries
                          where b.Name == Country.Name
                          select b;

                try
                {
                    Assert.IsNotNull(qry.FirstOrDefault());
                }
                catch (AssertFailedException)
                {
                    db.AddToCountries(Country);
                    db.SaveChanges();
                }

               
                

            }
        }
    }
}

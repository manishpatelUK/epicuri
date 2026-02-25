using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace epicuri.Core.SelfRepair
{
    [TestClass]
    public class Taxes : SRSUtils
    {

        protected static List<epicuri.Core.DatabaseModel.TaxType> ExpectedTaxes;

        [ClassInitialize]
        static public void SetUp(Microsoft.VisualStudio.TestTools.UnitTesting.TestContext tc)
        {
            Connect(tc);
            ExpectedTaxes = new List<DatabaseModel.TaxType>();

            ExpectedTaxes.Add(new DatabaseModel.TaxType { Name = "UK VAT 20%", Rate = 20, Country = db.Countries.Single(c => c.Name == "United Kingdom") });
            ExpectedTaxes.Add(new DatabaseModel.TaxType { Name = "UK VAT Zero Rated", Rate = 0, Country= db.Countries.Single(c=>c.Name == "United Kingdom") });


        }

        [TestMethod]
        public void Examine()
        {
            foreach (DatabaseModel.TaxType Tax in ExpectedTaxes)
            {
                var qry = from b in db.TaxTypes
                          where b.Name == Tax.Name
                          select b;

                try
                {
                    Assert.IsNotNull(qry.FirstOrDefault());
                }
                catch (AssertFailedException)
                {
                    db.AddToTaxTypes(Tax);
                    db.SaveChanges();
                }




            }
        }
    }
}

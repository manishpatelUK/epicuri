using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace epicuri.Core.SelfRepair
{
    [TestClass]
    public class Allergies : SRSUtils
    {
        protected static List<epicuri.Core.DatabaseModel.Allergy> allergies;

        [ClassInitialize]
        static public void SetUpCountries(Microsoft.VisualStudio.TestTools.UnitTesting.TestContext tc)
        {
            Connect(tc);
            allergies = new List<DatabaseModel.Allergy>();

            allergies.Add(new DatabaseModel.Allergy { Name = "Additives" });
            allergies.Add(new DatabaseModel.Allergy { Name = "Dairy"});
            allergies.Add(new DatabaseModel.Allergy { Name = "Nuts" });
            allergies.Add(new DatabaseModel.Allergy { Name = "Seafood"});
            allergies.Add(new DatabaseModel.Allergy { Name = "Soya"});
            allergies.Add(new DatabaseModel.Allergy { Name = "Wheat" });
            allergies.Add(new DatabaseModel.Allergy { Name = "Gluten" });


        }

        [TestMethod]
        public void Examine()
        {
            foreach (DatabaseModel.Allergy allergy in allergies)
            {
                var qry = from b in db.Allergies
                          where b.Name == allergy.Name
                          select b;

                try
                {
                    Assert.IsNotNull(qry.FirstOrDefault());
                }
                catch (AssertFailedException)
                {
                    db.AddToAllergies(allergy);
                    db.SaveChanges();
                }




            }
        }
    }
}

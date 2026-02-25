using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace epicuri.Core.SelfRepair
{
    [TestClass]
    public class DietaryRequirements : SRSUtils
    {
        protected static List<epicuri.Core.DatabaseModel.DietaryRequirement> requirements;

        [ClassInitialize]
        static public void SetUpCountries(Microsoft.VisualStudio.TestTools.UnitTesting.TestContext tc)
        {
            Connect(tc);
            requirements = new List<DatabaseModel.DietaryRequirement>();

            requirements.Add(new DatabaseModel.DietaryRequirement { Name = "Kosher" });
            requirements.Add(new DatabaseModel.DietaryRequirement { Name = "Halal"});
            requirements.Add(new DatabaseModel.DietaryRequirement { Name = "Vegan"});
            requirements.Add(new DatabaseModel.DietaryRequirement { Name = "Vegetarian" });
   
           

        }

        [TestMethod]
        public void Examine()
        {
            foreach (DatabaseModel.DietaryRequirement req in requirements)
            {
                var qry = from b in db.DietaryRequirements
                          where b.Name == req.Name
                          select b;

                try
                {
                    Assert.IsNotNull(qry.FirstOrDefault());
                }
                catch (AssertFailedException)
                {
                    db.AddToDietaryRequirements(req);
                    db.SaveChanges();
                }




            }
        }
    }
}

using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace epicuri.Core.SelfRepair
{
    [TestClass]
    public class FoodPreferences : SRSUtils
    {
        protected static List<epicuri.Core.DatabaseModel.FoodPreference> prefs;

        [ClassInitialize]
        static public void SetUpCountries(Microsoft.VisualStudio.TestTools.UnitTesting.TestContext tc)
        {
            Connect(tc);
            prefs = new List<DatabaseModel.FoodPreference>();

            prefs.Add(new DatabaseModel.FoodPreference { Name = "Bitter" });
            prefs.Add(new DatabaseModel.FoodPreference { Name = "Salty" });
            prefs.Add(new DatabaseModel.FoodPreference { Name = "Savoury" });
            prefs.Add(new DatabaseModel.FoodPreference { Name = "Sour" });
            prefs.Add(new DatabaseModel.FoodPreference { Name = "Spicy" });
            prefs.Add(new DatabaseModel.FoodPreference { Name = "Sweet" });
           
        }

        [TestMethod]
        public void Examine()
        {
            foreach (DatabaseModel.FoodPreference pref in prefs)
            {
                var qry = from b in db.FoodPreferences
                          where b.Name == pref.Name
                          select b;

                try
                {
                    Assert.IsNotNull(qry.FirstOrDefault());
                }
                catch (AssertFailedException)
                {
                    db.AddToFoodPreferences(pref);
                    db.SaveChanges();
                }




            }
        }
    }
}

using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace epicuri.Core.SelfRepair
{
    [TestClass]
    public class Categories : SRSUtils
    {
        protected static List<epicuri.Core.DatabaseModel.Category> ExpectedCategories;

        [ClassInitialize]
        static public void SetUpCountries(Microsoft.VisualStudio.TestTools.UnitTesting.TestContext tc)
        {
            Connect(tc);
            ExpectedCategories = new List<DatabaseModel.Category>();

            ExpectedCategories.Add(new DatabaseModel.Category { Name = "American" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Bangladeshi" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Brazilian food" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Burgers" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Caribbean" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Chicken" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Chinese" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Drinks" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "English" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "French" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Indian" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Iranian" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Italian" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Japanese" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Kebabs" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Korean" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Lebanese" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Malaysian" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Mexican" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Middle Eastern" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Nepalese" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Pakistani" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Peri Peri" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Persian" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Pizza" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Russian" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Spanish" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Sushi" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Thai" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Turkish" });


            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Martian" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Scottish" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "American" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Armenian" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Belgian" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Zambian" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Somalian" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Icelandic" });
            ExpectedCategories.Add(new DatabaseModel.Category { Name = "Rwandan" });

        }

        [TestMethod]
        public void Examine()
        {
            foreach (DatabaseModel.Category Category in ExpectedCategories)
            {
                var qry = from b in db.Categories
                          where b.Name == Category.Name
                          select b;

                try
                {
                    Assert.IsNotNull(qry.FirstOrDefault());
                }
                catch (AssertFailedException)
                {
                    db.AddToCategories(Category);
                    db.SaveChanges();
                }




            }
        }
    }
}

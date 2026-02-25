using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace epicuri.Core.SelfRepair
{
    [TestClass]
    public class AdjustmentTypes : SRSUtils
    {
        protected static List<epicuri.Core.DatabaseModel.AdjustmentType> types;

        [ClassInitialize]
        static public void SetUpCountries(Microsoft.VisualStudio.TestTools.UnitTesting.TestContext tc)
        {
            Connect(tc);
            types = new List<DatabaseModel.AdjustmentType>();

            types.Add(new DatabaseModel.AdjustmentType { Name = "Cash", Type=0 });
            types.Add(new DatabaseModel.AdjustmentType { Name = "VISA", Type=0 });
            types.Add(new DatabaseModel.AdjustmentType { Name = "Mastercard", Type=0 });
            types.Add(new DatabaseModel.AdjustmentType { Name = "Managers Discount", Type=1 });
            types.Add(new DatabaseModel.AdjustmentType { Name = "Kitchen Error",Type=1 });
           

        }

        [TestMethod]
        public void Examine()
        {
            foreach (DatabaseModel.AdjustmentType type in types)
            {
                var qry = from b in db.AdjustmentTypes
                          where b.Name == type.Name
                          select b;

                try
                {
                    Assert.IsNotNull(qry.FirstOrDefault());
                }
                catch (AssertFailedException)
                {
                    db.AddToAdjustmentTypes(type);
                    db.SaveChanges();
                }




            }
        }
    }
}

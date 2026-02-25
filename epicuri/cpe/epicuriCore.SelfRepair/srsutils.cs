using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestTools.UnitTesting;
namespace epicuri.Core.SelfRepair
{
    public class SRSUtils
    {
        protected static epicuri.Core.DatabaseModel.epicuriContainer db;


        [ClassInitialize]
        public static void Connect(Microsoft.VisualStudio.TestTools.UnitTesting.TestContext tc)
        {
            db = new epicuri.Core.DatabaseModel.epicuriContainer();

        }

    }
}

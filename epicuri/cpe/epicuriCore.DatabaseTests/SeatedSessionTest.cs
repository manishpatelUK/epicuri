using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for SeatedSessionTest and is intended
    ///to contain all SeatedSessionTest Unit Tests
    ///</summary>
    [TestClass()]
    public class SeatedSessionTest
    {


        private TestContext testContextInstance;

        /// <summary>
        ///Gets or sets the test context which provides
        ///information about and functionality for the current test run.
        ///</summary>
        public TestContext TestContext
        {
            get
            {
                return testContextInstance;
            }
            set
            {
                testContextInstance = value;
            }
        }

        #region Additional test attributes
        // 
        //You can use the following additional attributes as you write your tests:
        //
        //Use ClassInitialize to run code before running the first test in the class
        //[ClassInitialize()]
        //public static void MyClassInitialize(TestContext testContext)
        //{
        //}
        //
        //Use ClassCleanup to run code after all tests in a class have run
        //[ClassCleanup()]
        //public static void MyClassCleanup()
        //{
        //}
        //
        //Use TestInitialize to run code before running each test
        //[TestInitialize()]
        //public void MyTestInitialize()
        //{
        //}
        //
        //Use TestCleanup to run code after each test has run
        //[TestCleanup()]
        //public void MyTestCleanup()
        //{
        //}
        //
        #endregion


        /// <summary>
        ///A test for SeatedSession Constructor
        ///</summary>
        [TestMethod()]
        public void SeatedSessionConstructorTest()
        {
            SeatedSession target = new SeatedSession();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for CreateSeatedSession
        ///</summary>
        [TestMethod()]
        public void CreateSeatedSessionTest()
        {
            int id = 0; // TODO: Initialize to an appropriate value
            int restaurantId = 0; // TODO: Initialize to an appropriate value
            DateTime startTime = new DateTime(); // TODO: Initialize to an appropriate value
            DateTime closedTime = new DateTime(); // TODO: Initialize to an appropriate value
            SeatedSession expected = null; // TODO: Initialize to an appropriate value
            SeatedSession actual;
            actual = SeatedSession.CreateSeatedSession(id, restaurantId, startTime,true,0,"");
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Tables
        ///</summary>
        [TestMethod()]
        public void TablesTest()
        {
            SeatedSession target = new SeatedSession(); // TODO: Initialize to an appropriate value
            EntityCollection<Table> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<Table> actual;
            target.Tables = expected;
            actual = target.Tables;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
    }
}

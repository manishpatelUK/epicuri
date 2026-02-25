using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for DinerTest and is intended
    ///to contain all DinerTest Unit Tests
    ///</summary>
    [TestClass()]
    public class DinerTest
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
        ///A test for Diner Constructor
        ///</summary>
        [TestMethod()]
        public void DinerConstructorTest()
        {
            Diner target = new Diner();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for CreateDiner
        ///</summary>
        [TestMethod()]
        public void CreateDinerTest()
        {
            int id = 0; // TODO: Initialize to an appropriate value
            int sessionId = 0; // TODO: Initialize to an appropriate value
            int customerDinerBindingId = 0; // TODO: Initialize to an appropriate value
            Diner expected = null; // TODO: Initialize to an appropriate value
            Diner actual;
            actual = Diner.CreateDiner(id);
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }


        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            Diner target = new Diner(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Orders
        ///</summary>
        [TestMethod()]
        public void OrdersTest()
        {
            Diner target = new Diner(); // TODO: Initialize to an appropriate value
            EntityCollection<Order> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<Order> actual;
            target.Orders = expected;
            actual = target.Orders;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

       
    }
}

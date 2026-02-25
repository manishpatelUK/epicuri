using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for RatingTest and is intended
    ///to contain all RatingTest Unit Tests
    ///</summary>
    [TestClass()]
    public class RatingTest
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
        ///A test for Rating Constructor
        ///</summary>
        [TestMethod()]
        public void RatingConstructorTest()
        {
            Rating target = new Rating();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for CreateRating
        ///</summary>
        [TestMethod()]
        public void CreateRatingTest()
        {
            int id = 0; // TODO: Initialize to an appropriate value
            int customerId = 0; // TODO: Initialize to an appropriate value
            int menuItemId = 0; // TODO: Initialize to an appropriate value
            Rating expected = null; // TODO: Initialize to an appropriate value
            Rating actual;
            actual = Rating.CreateRating(id, customerId, menuItemId);
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Customer
        ///</summary>
        [TestMethod()]
        public void CustomerTest()
        {
            Rating target = new Rating(); // TODO: Initialize to an appropriate value
            Customer expected = null; // TODO: Initialize to an appropriate value
            Customer actual;
            target.Customer = expected;
            actual = target.Customer;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for CustomerId
        ///</summary>
        [TestMethod()]
        public void CustomerIdTest()
        {
            Rating target = new Rating(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.CustomerId = expected;
            actual = target.CustomerId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for CustomerReference
        ///</summary>
        [TestMethod()]
        public void CustomerReferenceTest()
        {
            Rating target = new Rating(); // TODO: Initialize to an appropriate value
            EntityReference<Customer> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<Customer> actual;
            target.CustomerReference = expected;
            actual = target.CustomerReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            Rating target = new Rating(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuItem
        ///</summary>
        [TestMethod()]
        public void MenuItemTest()
        {
            Rating target = new Rating(); // TODO: Initialize to an appropriate value
            MenuItem expected = null; // TODO: Initialize to an appropriate value
            MenuItem actual;
            target.MenuItem = expected;
            actual = target.MenuItem;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuItemId
        ///</summary>
        [TestMethod()]
        public void MenuItemIdTest()
        {
            Rating target = new Rating(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.MenuItemId = expected;
            actual = target.MenuItemId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuItemReference
        ///</summary>
        [TestMethod()]
        public void MenuItemReferenceTest()
        {
            Rating target = new Rating(); // TODO: Initialize to an appropriate value
            EntityReference<MenuItem> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<MenuItem> actual;
            target.MenuItemReference = expected;
            actual = target.MenuItemReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
    }
}

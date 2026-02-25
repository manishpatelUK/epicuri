using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for RestaurantSettingsTest and is intended
    ///to contain all RestaurantSettingsTest Unit Tests
    ///</summary>
    [TestClass()]
    public class RestaurantSettingsTest
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
        ///A test for RestaurantSettings Constructor
        ///</summary>
        [TestMethod()]
        public void RestaurantSettingsConstructorTest()
        {
            RestaurantSettings target = new RestaurantSettings();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for CreateRestaurantSettings
        ///</summary>
        [TestMethod()]
        public void CreateRestaurantSettingsTest()
        {
            short maximumCapacity = 0; // TODO: Initialize to an appropriate value
            double freeDeliveryRadius = 0F; // TODO: Initialize to an appropriate value
            double maxDeliveryRadius = 0F; // TODO: Initialize to an appropriate value
            double freeDeliveryMinPurchase = 0F; // TODO: Initialize to an appropriate value
            double paidDeliveryMinPurchase = 0F; // TODO: Initialize to an appropriate value
            RestaurantSettings expected = null; // TODO: Initialize to an appropriate value
            RestaurantSettings actual;
            actual = RestaurantSettings.CreateRestaurantSettings(maximumCapacity, freeDeliveryRadius, maxDeliveryRadius, freeDeliveryMinPurchase, paidDeliveryMinPurchase);
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for FreeDeliveryMinPurchase
        ///</summary>
        [TestMethod()]
        public void FreeDeliveryMinPurchaseTest()
        {
            RestaurantSettings target = new RestaurantSettings(); // TODO: Initialize to an appropriate value
            double expected = 0F; // TODO: Initialize to an appropriate value
            double actual;
            target.FreeDeliveryMinPurchase = expected;
            actual = target.FreeDeliveryMinPurchase;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for FreeDeliveryRadius
        ///</summary>
        [TestMethod()]
        public void FreeDeliveryRadiusTest()
        {
            RestaurantSettings target = new RestaurantSettings(); // TODO: Initialize to an appropriate value
            double expected = 0F; // TODO: Initialize to an appropriate value
            double actual;
            target.FreeDeliveryRadius = expected;
            actual = target.FreeDeliveryRadius;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MaxDeliveryRadius
        ///</summary>
        [TestMethod()]
        public void MaxDeliveryRadiusTest()
        {
            RestaurantSettings target = new RestaurantSettings(); // TODO: Initialize to an appropriate value
            double expected = 0F; // TODO: Initialize to an appropriate value
            double actual;
            target.MaxDeliveryRadius = expected;
            actual = target.MaxDeliveryRadius;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MaximumCapacity
        ///</summary>
        [TestMethod()]
        public void MaximumCapacityTest()
        {
            RestaurantSettings target = new RestaurantSettings(); // TODO: Initialize to an appropriate value
            short expected = 0; // TODO: Initialize to an appropriate value
            short actual;
            target.MaximumCapacity = expected;
            actual = target.MaximumCapacity;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for PaidDeliveryMinPurchase
        ///</summary>
        [TestMethod()]
        public void PaidDeliveryMinPurchaseTest()
        {
            RestaurantSettings target = new RestaurantSettings(); // TODO: Initialize to an appropriate value
            double expected = 0F; // TODO: Initialize to an appropriate value
            double actual;
            target.PaidDeliveryMinPurchase = expected;
            actual = target.PaidDeliveryMinPurchase;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
    }
}

using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for ResourceTest and is intended
    ///to contain all ResourceTest Unit Tests
    ///</summary>
    [TestClass()]
    public class ResourceTest
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
        ///A test for Resource Constructor
        ///</summary>
        [TestMethod()]
        public void ResourceConstructorTest()
        {
            Resource target = new Resource();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for CreateResource
        ///</summary>
        [TestMethod()]
        public void CreateResourceTest()
        {
            int id = 0; // TODO: Initialize to an appropriate value
            int restaurantId = 0; // TODO: Initialize to an appropriate value
            string cDNUrl = string.Empty; // TODO: Initialize to an appropriate value
            Resource expected = null; // TODO: Initialize to an appropriate value
            Resource actual;
            actual = Resource.CreateResource(id, restaurantId, cDNUrl);
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for CDNUrl
        ///</summary>
        [TestMethod()]
        public void CDNUrlTest()
        {
            Resource target = new Resource(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.CDNUrl = expected;
            actual = target.CDNUrl;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            Resource target = new Resource(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Restaurant
        ///</summary>
        [TestMethod()]
        public void RestaurantTest()
        {
            Resource target = new Resource(); // TODO: Initialize to an appropriate value
            Restaurant expected = null; // TODO: Initialize to an appropriate value
            Restaurant actual;
            target.Restaurant = expected;
            actual = target.Restaurant;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for RestaurantId
        ///</summary>
        [TestMethod()]
        public void RestaurantIdTest()
        {
            Resource target = new Resource(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.RestaurantId = expected;
            actual = target.RestaurantId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for RestaurantReference
        ///</summary>
        [TestMethod()]
        public void RestaurantReferenceTest()
        {
            Resource target = new Resource(); // TODO: Initialize to an appropriate value
            EntityReference<Restaurant> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<Restaurant> actual;
            target.RestaurantReference = expected;
            actual = target.RestaurantReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
    }
}

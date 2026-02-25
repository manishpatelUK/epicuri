using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for FloorTest and is intended
    ///to contain all FloorTest Unit Tests
    ///</summary>
    [TestClass()]
    public class FloorTest
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
        ///A test for Floor Constructor
        ///</summary>
        [TestMethod()]
        public void FloorConstructorTest()
        {
            Floor target = new Floor();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for CreateFloor
        ///</summary>
        [TestMethod()]
        public void CreateFloorTest()
        {
            int id = 0; // TODO: Initialize to an appropriate value
            int restaurantId = 0; // TODO: Initialize to an appropriate value
            string name = string.Empty; // TODO: Initialize to an appropriate value
            short capacity = 0; // TODO: Initialize to an appropriate value
            int resourceId = 0; // TODO: Initialize to an appropriate value
            Floor expected = null; // TODO: Initialize to an appropriate value
            Floor actual;
            actual = Floor.CreateFloor(id, restaurantId, name, capacity, resourceId);
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for ActiveLayout
        ///</summary>
        [TestMethod()]
        public void ActiveLayoutTest()
        {
            Floor target = new Floor(); // TODO: Initialize to an appropriate value
            Layout expected = null; // TODO: Initialize to an appropriate value
            Layout actual;
            target.ActiveLayout = expected;
            actual = target.ActiveLayout;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for ActiveLayoutReference
        ///</summary>
        [TestMethod()]
        public void ActiveLayoutReferenceTest()
        {
            Floor target = new Floor(); // TODO: Initialize to an appropriate value
            EntityReference<Layout> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<Layout> actual;
            target.ActiveLayoutReference = expected;
            actual = target.ActiveLayoutReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Capacity
        ///</summary>
        [TestMethod()]
        public void CapacityTest()
        {
            Floor target = new Floor(); // TODO: Initialize to an appropriate value
            short expected = 0; // TODO: Initialize to an appropriate value
            short actual;
            target.Capacity = expected;
            actual = target.Capacity;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            Floor target = new Floor(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Layouts
        ///</summary>
        [TestMethod()]
        public void LayoutsTest()
        {
            Floor target = new Floor(); // TODO: Initialize to an appropriate value
            EntityCollection<Layout> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<Layout> actual;
            target.Layouts = expected;
            actual = target.Layouts;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Name
        ///</summary>
        [TestMethod()]
        public void NameTest()
        {
            Floor target = new Floor(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.Name = expected;
            actual = target.Name;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Resource
        ///</summary>
        [TestMethod()]
        public void ResourceTest()
        {
            Floor target = new Floor(); // TODO: Initialize to an appropriate value
            Resource expected = null; // TODO: Initialize to an appropriate value
            Resource actual;
            target.Resource = expected;
            actual = target.Resource;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for ResourceId
        ///</summary>
        [TestMethod()]
        public void ResourceIdTest()
        {
            Floor target = new Floor(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.ResourceId = expected;
            actual = target.ResourceId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for ResourceReference
        ///</summary>
        [TestMethod()]
        public void ResourceReferenceTest()
        {
            Floor target = new Floor(); // TODO: Initialize to an appropriate value
            EntityReference<Resource> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<Resource> actual;
            target.ResourceReference = expected;
            actual = target.ResourceReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Restaurant
        ///</summary>
        [TestMethod()]
        public void RestaurantTest()
        {
            Floor target = new Floor(); // TODO: Initialize to an appropriate value
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
            Floor target = new Floor(); // TODO: Initialize to an appropriate value
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
            Floor target = new Floor(); // TODO: Initialize to an appropriate value
            EntityReference<Restaurant> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<Restaurant> actual;
            target.RestaurantReference = expected;
            actual = target.RestaurantReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Tables
        ///</summary>
        [TestMethod()]
        public void TablesTest()
        {
            Floor target = new Floor(); // TODO: Initialize to an appropriate value
            EntityCollection<Table> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<Table> actual;
            target.Tables = expected;
            actual = target.Tables;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
    }
}

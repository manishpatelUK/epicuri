using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for LayoutTest and is intended
    ///to contain all LayoutTest Unit Tests
    ///</summary>
    [TestClass()]
    public class LayoutTest
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
        ///A test for Layout Constructor
        ///</summary>
        [TestMethod()]
        public void LayoutConstructorTest()
        {
            Layout target = new Layout();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /*
        /// <summary>
        ///A test for CreateLayout
        ///</summary>
        [TestMethod()]
        public void CreateLayoutTest()
        {
            int id = 0; // TODO: Initialize to an appropriate value
            int floorId = 0; // TODO: Initialize to an appropriate value
            Layout expected = null; // TODO: Initialize to an appropriate value
            Layout actual;
            actual = Layout.CreateLayout(id, floorId);
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
         * */

        /// <summary>
        ///A test for Floor
        ///</summary>
        [TestMethod()]
        public void FloorTest()
        {
            Layout target = new Layout(); // TODO: Initialize to an appropriate value
            Floor expected = null; // TODO: Initialize to an appropriate value
            Floor actual;
            target.Floor = expected;
            actual = target.Floor;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for FloorId
        ///</summary>
        [TestMethod()]
        public void FloorIdTest()
        {
            Layout target = new Layout(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.FloorId = expected;
            actual = target.FloorId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for FloorReference
        ///</summary>
        [TestMethod()]
        public void FloorReferenceTest()
        {
            Layout target = new Layout(); // TODO: Initialize to an appropriate value
            EntityReference<Floor> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<Floor> actual;
            target.FloorReference = expected;
            actual = target.FloorReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            Layout target = new Layout(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

    }
}

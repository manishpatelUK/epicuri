using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for TakeAwaySessionTest and is intended
    ///to contain all TakeAwaySessionTest Unit Tests
    ///</summary>
    [TestClass()]
    public class TakeAwaySessionTest
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
        ///A test for TakeAwaySession Constructor
        ///</summary>
        [TestMethod()]
        public void TakeAwaySessionConstructorTest()
        {
            TakeAwaySession target = new TakeAwaySession();
            Assert.IsNotNull(target);
        }

        /// <summary>
        ///A test for Delivery
        ///</summary>
        [TestMethod()]
        public void DeliveryTest()
        {
            TakeAwaySession target = new TakeAwaySession(); 
            bool expected = false; 
            bool actual;
            target.Delivery = expected;
            actual = target.Delivery;
            Assert.AreEqual(expected, actual);

        }



        /// <summary>
        ///A test for ExpectedTime
        ///</summary>
        [TestMethod()]
        public void ExpectedTimeTest()
        {
            TakeAwaySession target = new TakeAwaySession(); 
            DateTime expected = new DateTime(); 
            DateTime actual;
            target.ExpectedTime = expected;
            actual = target.ExpectedTime;
            Assert.AreEqual(expected, actual);
            
        }

        /// <summary>
        ///A test for TakeAwayTypeId
        ///</summary>
        [TestMethod()]
        public void TakeAwayTypeIdTest()
        {
            TakeAwaySession target = new TakeAwaySession(); 
            int expected = 0;
            int actual;
            target.TakeAwayTypeId = expected;
            actual = target.TakeAwayTypeId;
            Assert.AreEqual(expected, actual);

        }
    }
}

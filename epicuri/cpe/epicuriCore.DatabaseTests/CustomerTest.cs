using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for CustomerTest and is intended
    ///to contain all CustomerTest Unit Tests
    ///</summary>
    [TestClass()]
    public class CustomerTest
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
        ///A test for Customer Constructor
        ///</summary>
        [TestMethod()]
        public void CustomerConstructorTest()
        {
            Customer target = new Customer();
            Assert.IsNotNull(target);

            
        }

        /// <summary>
        ///A test for BlackMarks
        ///</summary>
        [TestMethod()]
        public void BlackMarksTest()
        {
            Customer target = new Customer();
            EntityCollection<BlackMark> expected = new EntityCollection<BlackMark>();
            expected.Add(new BlackMark());
            EntityCollection<BlackMark> actual;
            target.BlackMarks = expected;
            actual = target.BlackMarks;
            Assert.AreEqual(expected, actual);
            
        }

        
        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            Customer target = new Customer(); 
            int expected = 0; 
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            
        }

        /// <summary>
        ///A test for Name
        ///</summary>
        [TestMethod()]
        public void NameTest()
        {
            Customer target = new Customer();
            Name expected = new Name();
            Name actual;
            target.Name = expected;
            actual = target.Name;
            Assert.AreEqual(expected, actual);
            
        }

        /// <summary>
        ///A test for Ratings
        ///</summary>
        [TestMethod()]
        public void RatingsTest()
        {
            Customer target = new Customer(); // TODO: Initialize to an appropriate value
            EntityCollection<Rating> expected = new EntityCollection<Rating>();
            expected.Add(new Rating());
            EntityCollection<Rating> actual;
            target.Ratings = expected;
            actual = target.Ratings;
            Assert.AreEqual(expected, actual);
            
        }
    }
}

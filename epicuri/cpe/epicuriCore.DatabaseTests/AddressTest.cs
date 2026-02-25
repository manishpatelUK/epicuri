using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for AddressTest and is intended
    ///to contain all AddressTest Unit Tests
    ///</summary>
    [TestClass()]
    public class AddressTest
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
        ///A test for Address Constructor
        ///</summary>
        [TestMethod()]
        public void AddressConstructorTest()
        {
            Address target = new Address();
            Assert.IsNotNull(target);
        }

        /// <summary>
        ///A test for CreateAddress
        ///</summary>
        [TestMethod()]
        public void CreateAddressTest()
        {
            Address expected = new Address { City = "city", PostCode = "so50 1aa", Street = "123 street"}; 
            Address actual;
            actual = Address.CreateAddress("123 street", "city", "so50 1aa");

            Assert.AreEqual(expected.Town, actual.Town);
            Assert.AreEqual(expected.City, actual.City);
            Assert.AreEqual(expected.Street, actual.Street);
            Assert.AreEqual(expected.PostCode, actual.PostCode);           
        }

        /// <summary>
        ///A test for City
        ///</summary>
        [TestMethod()]
        public void CityTest()
        {
            Address target = new Address{ City="test", Town="test", Street="test", PostCode="test"}; 
            string expected = "test";
            string actual;
            target.City = expected;
            actual = target.City;
            Assert.AreEqual(expected, actual);
        }

        /// <summary>
        ///A test for PostCode
        ///</summary>
        [TestMethod()]
        public void PostCodeTest()
        {
            Address target = new Address { City = "test", Town = "test", Street = "test", PostCode = "test" };
            string expected = "test";
            string actual;
            target.PostCode = expected;
            actual = target.PostCode;
            Assert.AreEqual(expected, actual);
        }

        /// <summary>
        ///A test for Street
        ///</summary>
        [TestMethod()]
        public void StreetTest()
        {
            Address target = new Address { City = "test", Town = "test", Street = "test", PostCode = "test" };
            string expected = "test";
            string actual;
            target.Street = expected;
            actual = target.Street;
            Assert.AreEqual(expected, actual);
        }

        /// <summary>
        ///A test for Town
        ///</summary>
        [TestMethod()]
        public void TownTest()
        {
            Address target = new Address { City = "test", Town = "test", Street = "test", PostCode = "test" };
            string expected = "test";
            string actual;
            target.Town = expected;
            actual = target.Town;
            Assert.AreEqual(expected, actual);
        }
    }
}

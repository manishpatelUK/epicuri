using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for TaxTypeTest and is intended
    ///to contain all TaxTypeTest Unit Tests
    ///</summary>
    [TestClass()]
    public class TaxTypeTest
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
        ///A test for TaxType Constructor
        ///</summary>
        [TestMethod()]
        public void TaxTypeConstructorTest()
        {
            TaxType target = new TaxType();
            Assert.IsNotNull(target);
        }

        /// <summary>
        ///A test for CreateTaxType
        ///</summary>
        [TestMethod()]
        public void CreateTaxTypeTest()
        {
            int id = 0;
            string name = "VAT";
            double rate = 1.0F; 
            int countryId = 0;
            TaxType expected = new TaxType { Id = id, Name = name, CountryId = countryId , Rate = rate}; 
            TaxType actual;
            actual = TaxType.CreateTaxType(id, name, rate, countryId);
            Assert.AreEqual(expected.Id, actual.Id);
            Assert.AreEqual(expected.Country, actual.Country);
            Assert.AreEqual(expected.Name, actual.Name);
            Assert.AreEqual(expected.Rate, actual.Rate);
            
        }

        /// <summary>
        ///A test for Country
        ///</summary>
        [TestMethod()]
        public void CountryTest()
        {
            TaxType target = new TaxType();
            Country expected = new Country();
            Country actual;
            target.Country = expected;
            actual = target.Country;
            Assert.AreEqual(expected, actual);
            
        }

        /// <summary>
        ///A test for CountryId
        ///</summary>
        [TestMethod()]
        public void CountryIdTest()
        {
            TaxType target = new TaxType(); 
            int expected = 100; 
            int actual;
            target.CountryId = expected;
            actual = target.CountryId;
            Assert.AreEqual(expected, actual);
            
        }

      

        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            TaxType target = new TaxType(); 
            int expected = 10; 
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
            TaxType target = new TaxType();
            string expected = "VAT";
            string actual;
            target.Name = expected;
            actual = target.Name;
            Assert.AreEqual(expected, actual);
            
        }

        /// <summary>
        ///A test for Rate
        ///</summary>
        [TestMethod()]
        public void RateTest()
        {
            TaxType target = new TaxType(); 
            double expected = 20.0F; 
            double actual;
            target.Rate = expected;
            actual = target.Rate;
            Assert.AreEqual(expected, actual);
            
        }

    }
}

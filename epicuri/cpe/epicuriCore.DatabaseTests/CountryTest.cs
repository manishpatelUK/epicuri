using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;
using System.Linq;
using System.Data;
using System.Collections.Generic;
namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for CountryTest and is intended
    ///to contain all CountryTest Unit Tests
    ///</summary>
    [TestClass()]
    public class CountryTest
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
        ///A test for Country Constructor
        ///</summary>
        [TestMethod()]
        public void CountryConstructorTest()
        {
            Country target = new Country();
            Assert.IsNotNull(target);
        }

        /// <summary>
        ///A test for CreateCountry
        ///</summary>
        [TestMethod()]
        public void CreateCountryTest()
        {
            int id = 0; 
            string name = "Test";
            string acronym = "TE";
            Country expected = new Country { Name = name, Acronym = acronym, Id = id }; 
            Country actual;
            actual = Country.CreateCountry(id, name, acronym);
            Assert.AreEqual(expected.Id, actual.Id);
            Assert.AreEqual(expected.Name, actual.Name);
            Assert.AreEqual(expected.Acronym, actual.Acronym);

            
        }

        /// <summary>
        ///A test for Acronym
        ///</summary>
        [TestMethod()]
        public void AcronymTest()
        {
            Country target = new Country();
            string expected = "TE";
            string actual;
            target.Acronym = expected;
            actual = target.Acronym;
            Assert.AreEqual(expected, actual);
            
        }

        /// <summary>
        ///A test for DefaultCurrency
        ///</summary>
        [TestMethod()]
        public void DefaultCurrencyTest()
        {
            Country target = new Country();
            Currency expected = new Currency { CurrencyName = "Dollar", CurrencySymbol = "$", Id =999 };
            Currency actual;
            target.DefaultCurrency = expected;
            actual = target.DefaultCurrency;
            Assert.AreEqual(expected, actual);
            
        }


        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            Country target = new Country(); 
            int expected = 100; 
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
            Country target = new Country(); 
            string expected = "Hello";
            string actual;
            target.Name = expected;
            actual = target.Name;
            Assert.AreEqual(expected, actual);

        }

    
    }
}

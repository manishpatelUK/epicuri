using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for CurrencyTest and is intended
    ///to contain all CurrencyTest Unit Tests
    ///</summary>
    [TestClass()]
    public class CurrencyTest
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
        ///A test for Currency Constructor
        ///</summary>
        [TestMethod()]
        public void CurrencyConstructorTest()
        {
            Currency target = new Currency();
            Assert.IsNotNull(target);
        }

        /// <summary>
        ///A test for CreateCurrency
        ///</summary>
        [TestMethod()]
        public void CreateCurrencyTest()
        {
            int id = 0;
            string currencyName = "Dollar";
            string currencySymbol = "$";
            Currency expected = new Currency {Id=0, CurrencyName = "Dollar", CurrencySymbol = "$" };
            Currency actual;
            actual = Currency.CreateCurrency(id, currencyName, currencySymbol);

            Assert.AreEqual(expected.Id, actual.Id);
            Assert.AreEqual(expected.CurrencySymbol, actual.CurrencySymbol);
            Assert.AreEqual(expected.CurrencyName, actual.CurrencyName);

            
        }

        /// <summary>
        ///A test for CurrencyName
        ///</summary>
        [TestMethod()]
        public void CurrencyNameTest()
        {
            Currency target = new Currency(); 
            string expected = "Name";
            string actual;
            target.CurrencyName = expected;
            actual = target.CurrencyName;
            Assert.AreEqual(expected, actual);
            
        }

        /// <summary>
        ///A test for CurrencySymbol
        ///</summary>
        [TestMethod()]
        public void CurrencySymbolTest()
        {
            Currency target = new Currency();
            string expected = "$";
            string actual;
            target.CurrencySymbol = expected;
            actual = target.CurrencySymbol;
            Assert.AreEqual(expected, actual);
        }

        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            Currency target = new Currency();
            int expected = 1000; 
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            
        }
    }
}

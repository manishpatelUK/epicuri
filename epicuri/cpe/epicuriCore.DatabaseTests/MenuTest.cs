using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for MenuTest and is intended
    ///to contain all MenuTest Unit Tests
    ///</summary>
    [TestClass()]
    public class MenuTest
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
        ///A test for Menu Constructor
        ///</summary>
        [TestMethod()]
        public void MenuConstructorTest()
        {
            Menu target = new Menu();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for CreateMenu
        ///</summary>
        [TestMethod()]
        public void CreateMenuTest()
        {
            /*
            int id = 0; // TODO: Initialize to an appropriate value
            string menuName = string.Empty; // TODO: Initialize to an appropriate value
            Menu expected = null; // TODO: Initialize to an appropriate value
            Menu actual;
            actual = Menu.CreateMenu(id, menuName);
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
             */
        }

        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            Menu target = new Menu(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuCategories
        ///</summary>
        [TestMethod()]
        public void MenuCategoriesTest()
        {
            Menu target = new Menu(); // TODO: Initialize to an appropriate value
            EntityCollection<MenuCategory> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<MenuCategory> actual;
            target.MenuCategories = expected;
            actual = target.MenuCategories;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuName
        ///</summary>
        [TestMethod()]
        public void MenuNameTest()
        {
            Menu target = new Menu(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.MenuName = expected;
            actual = target.MenuName;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

    }
}

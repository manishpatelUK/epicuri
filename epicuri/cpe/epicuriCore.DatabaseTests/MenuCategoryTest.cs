using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for MenuCategoryTest and is intended
    ///to contain all MenuCategoryTest Unit Tests
    ///</summary>
    [TestClass()]
    public class MenuCategoryTest
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
        ///A test for MenuCategory Constructor
        ///</summary>
        [TestMethod()]
        public void MenuCategoryConstructorTest()
        {
            MenuCategory target = new MenuCategory();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for CreateMenuCategory
        ///</summary>
        [TestMethod()]
        public void CreateMenuCategoryTest()
        {
            int id = 0; // TODO: Initialize to an appropriate value
            int menuId = 0; // TODO: Initialize to an appropriate value
            string categoryName = string.Empty; // TODO: Initialize to an appropriate value
            MenuCategory expected = null; // TODO: Initialize to an appropriate value
            MenuCategory actual;
            actual = MenuCategory.CreateMenuCategory(id, menuId, categoryName);
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for CategoryName
        ///</summary>
        [TestMethod()]
        public void CategoryNameTest()
        {
            MenuCategory target = new MenuCategory(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.CategoryName = expected;
            actual = target.CategoryName;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            MenuCategory target = new MenuCategory(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Menu
        ///</summary>
        [TestMethod()]
        public void MenuTest()
        {
            MenuCategory target = new MenuCategory(); // TODO: Initialize to an appropriate value
            Menu expected = null; // TODO: Initialize to an appropriate value
            Menu actual;
            target.Menu = expected;
            actual = target.Menu;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuGroups
        ///</summary>
        [TestMethod()]
        public void MenuGroupsTest()
        {
            MenuCategory target = new MenuCategory(); // TODO: Initialize to an appropriate value
            EntityCollection<MenuGroup> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<MenuGroup> actual;
            target.MenuGroups = expected;
            actual = target.MenuGroups;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuId
        ///</summary>
        [TestMethod()]
        public void MenuIdTest()
        {
            MenuCategory target = new MenuCategory(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.MenuId = expected;
            actual = target.MenuId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuReference
        ///</summary>
        [TestMethod()]
        public void MenuReferenceTest()
        {
            MenuCategory target = new MenuCategory(); // TODO: Initialize to an appropriate value
            EntityReference<Menu> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<Menu> actual;
            target.MenuReference = expected;
            actual = target.MenuReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
    }
}

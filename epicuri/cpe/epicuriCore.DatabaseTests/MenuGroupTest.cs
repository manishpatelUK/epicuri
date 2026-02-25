using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for MenuGroupTest and is intended
    ///to contain all MenuGroupTest Unit Tests
    ///</summary>
    [TestClass()]
    public class MenuGroupTest
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
        ///A test for MenuGroup Constructor
        ///</summary>
        [TestMethod()]
        public void MenuGroupConstructorTest()
        {
            MenuGroup target = new MenuGroup();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for CreateMenuGroup
        ///</summary>
        [TestMethod()]
        public void CreateMenuGroupTest()
        {
            int id = 0; // TODO: Initialize to an appropriate value
            int menuCategoryId = 0; // TODO: Initialize to an appropriate value
            string groupName = string.Empty; // TODO: Initialize to an appropriate value
            MenuGroup expected = null; // TODO: Initialize to an appropriate value
            MenuGroup actual;
            actual = MenuGroup.CreateMenuGroup(id, menuCategoryId, groupName);
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for GroupName
        ///</summary>
        [TestMethod()]
        public void GroupNameTest()
        {
            MenuGroup target = new MenuGroup(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.GroupName = expected;
            actual = target.GroupName;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            MenuGroup target = new MenuGroup(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuCategory
        ///</summary>
        [TestMethod()]
        public void MenuCategoryTest()
        {
            MenuGroup target = new MenuGroup(); // TODO: Initialize to an appropriate value
            MenuCategory expected = null; // TODO: Initialize to an appropriate value
            MenuCategory actual;
            target.MenuCategory = expected;
            actual = target.MenuCategory;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuCategoryId
        ///</summary>
        [TestMethod()]
        public void MenuCategoryIdTest()
        {
            MenuGroup target = new MenuGroup(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.MenuCategoryId = expected;
            actual = target.MenuCategoryId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuCategoryReference
        ///</summary>
        [TestMethod()]
        public void MenuCategoryReferenceTest()
        {
            MenuGroup target = new MenuGroup(); // TODO: Initialize to an appropriate value
            EntityReference<MenuCategory> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<MenuCategory> actual;
            target.MenuCategoryReference = expected;
            actual = target.MenuCategoryReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuItems
        ///</summary>
        [TestMethod()]
        public void MenuItemsTest()
        {
            MenuGroup target = new MenuGroup(); // TODO: Initialize to an appropriate value
            EntityCollection<MenuItem> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<MenuItem> actual;
            target.MenuItems = expected;
            actual = target.MenuItems;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
    }
}

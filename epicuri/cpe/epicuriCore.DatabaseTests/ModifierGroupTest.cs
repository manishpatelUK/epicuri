using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for ModifierGroupTest and is intended
    ///to contain all ModifierGroupTest Unit Tests
    ///</summary>
    [TestClass()]
    public class ModifierGroupTest
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
        ///A test for ModifierGroup Constructor
        ///</summary>
        [TestMethod()]
        public void ModifierGroupConstructorTest()
        {
            ModifierGroup target = new ModifierGroup();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /*
         * 
        /// <summary>
        ///A test for CreateModifierGroup
        ///</summary>
        [TestMethod()]
        public void CreateModifierGroupTest()
        {
            int id = 0; // TODO: Initialize to an appropriate value
            int menuItemId = 0; // TODO: Initialize to an appropriate value
            string groupName = string.Empty; // TODO: Initialize to an appropriate value
            ModifierGroup expected = null; // TODO: Initialize to an appropriate value
            ModifierGroup actual;
            actual = ModifierGroup.CreateModifierGroup(id, menuItemId, groupName);
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
         * */

        /// <summary>
        ///A test for GroupName
        ///</summary>
        [TestMethod()]
        public void GroupNameTest()
        {
            ModifierGroup target = new ModifierGroup(); // TODO: Initialize to an appropriate value
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
            ModifierGroup target = new ModifierGroup(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /*
        /// <summary>
        ///A test for MenuItem
        ///</summary>
        [TestMethod()]
        public void MenuItemTest()
        {
            ModifierGroup target = new ModifierGroup(); // TODO: Initialize to an appropriate value
            MenuItem expected = null; // TODO: Initialize to an appropriate value
            MenuItem actual;
            target.MenuItem = expected;
            actual = target.MenuItem;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
         * */

        /*
        /// <summary>
        ///A test for MenuItemId
        ///</summary>
        [TestMethod()]
        public void MenuItemIdTest()
        {
            ModifierGroup target = new ModifierGroup(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.MenuItemId = expected;
            actual = target.MenuItemId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }*/
        /*
        /// <summary>
        ///A test for MenuItemReference
        ///</summary>
        [TestMethod()]

        public void MenuItemReferenceTest()
        {
            ModifierGroup target = new ModifierGroup(); // TODO: Initialize to an appropriate value
            EntityReference<MenuItem> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<MenuItem> actual;
            target.MenuItemReference = expected;
            actual = target.MenuItemReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Modifiers
        ///</summary>
        [TestMethod()]
        public void ModifiersTest()
        {
            ModifierGroup target = new ModifierGroup(); // TODO: Initialize to an appropriate value
            EntityCollection<Modifier> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<Modifier> actual;
            target.Modifiers = expected;
            actual = target.Modifiers;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
         * */
    }
}

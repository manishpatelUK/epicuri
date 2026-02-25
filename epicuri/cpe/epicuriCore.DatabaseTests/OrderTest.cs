using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for OrderTest and is intended
    ///to contain all OrderTest Unit Tests
    ///</summary>
    [TestClass()]
    public class OrderTest
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
        ///A test for Order Constructor
        ///</summary>
        [TestMethod()]
        public void OrderConstructorTest()
        {
            Order target = new Order();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for CreateOrder
        ///</summary>
        [TestMethod()]
        public void CreateOrderTest()
        {
            int id = 0; // TODO: Initialize to an appropriate value
            int sessionId = 0; // TODO: Initialize to an appropriate value
            int menuItemId = 0; // TODO: Initialize to an appropriate value
            int dinerId = 0; // TODO: Initialize to an appropriate value
            string note = string.Empty; // TODO: Initialize to an appropriate value
            Order expected = null; // TODO: Initialize to an appropriate value
            Order actual;
            actual = Order.CreateOrder(id, menuItemId, dinerId, note, sessionId,1,0);
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Diner
        ///</summary>
        [TestMethod()]
        public void DinerTest()
        {
            Order target = new Order(); // TODO: Initialize to an appropriate value
            Diner expected = null; // TODO: Initialize to an appropriate value
            Diner actual;
            target.Diner = expected;
            actual = target.Diner;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for DinerId
        ///</summary>
        [TestMethod()]
        public void DinerIdTest()
        {
            Order target = new Order(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.DinerId = expected;
            actual = target.DinerId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for DinerReference
        ///</summary>
        [TestMethod()]
        public void DinerReferenceTest()
        {
            Order target = new Order(); // TODO: Initialize to an appropriate value
            EntityReference<Diner> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<Diner> actual;
            target.DinerReference = expected;
            actual = target.DinerReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            Order target = new Order(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuItem
        ///</summary>
        [TestMethod()]
        public void MenuItemTest()
        {
            Order target = new Order(); // TODO: Initialize to an appropriate value
            MenuItem expected = null; // TODO: Initialize to an appropriate value
            MenuItem actual;
            target.MenuItem = expected;
            actual = target.MenuItem;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuItemId
        ///</summary>
        [TestMethod()]
        public void MenuItemIdTest()
        {
            Order target = new Order(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.MenuItemId = expected;
            actual = target.MenuItemId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuItemReference
        ///</summary>
        [TestMethod()]
        public void MenuItemReferenceTest()
        {
            Order target = new Order(); // TODO: Initialize to an appropriate value
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
            Order target = new Order(); // TODO: Initialize to an appropriate value
            EntityCollection<Modifier> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<Modifier> actual;
            target.Modifiers = expected;
            actual = target.Modifiers;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Note
        ///</summary>
        [TestMethod()]
        public void NoteTest()
        {
            Order target = new Order(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.Note = expected;
            actual = target.Note;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for SessionId
        ///</summary>
        [TestMethod()]
        public void SessionIdTest()
        {
            Order target = new Order(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.SessionId = expected;
            actual = target.SessionId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
    }
}

using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for MenuItemTest and is intended
    ///to contain all MenuItemTest Unit Tests
    ///</summary>
    [TestClass()]
    public class MenuItemTest
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
        ///A test for MenuItem Constructor
        ///</summary>
        [TestMethod()]
        public void MenuItemConstructorTest()
        {
            MenuItem target = new MenuItem();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for CreateMenuItem
        ///</summary>
        [TestMethod()]
        public void CreateMenuItemTest()
        {
            int id = 0; // TODO: Initialize to an appropriate value
            int menuGroupId = 0; // TODO: Initialize to an appropriate value
            int taxTypeId = 0; // TODO: Initialize to an appropriate value
            int restaurantId = 0; // TODO: Initialize to an appropriate value
            string name = string.Empty; // TODO: Initialize to an appropriate value
            Double price = new Double(); // TODO: Initialize to an appropriate value
            string description = string.Empty; // TODO: Initialize to an appropriate value
            string imageURL = string.Empty; // TODO: Initialize to an appropriate value
            MenuItem expected = null; // TODO: Initialize to an appropriate value
            MenuItem actual;
            actual = MenuItem.CreateMenuItem(id, menuGroupId, taxTypeId, restaurantId, name, price, description, imageURL);
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Description
        ///</summary>
        [TestMethod()]
        public void DescriptionTest()
        {
            MenuItem target = new MenuItem(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.Description = expected;
            actual = target.Description;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            MenuItem target = new MenuItem(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for ImageURL
        ///</summary>
        [TestMethod()]
        public void ImageURLTest()
        {
            MenuItem target = new MenuItem(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.ImageURL = expected;
            actual = target.ImageURL;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuGroupId
        ///</summary>
        [TestMethod()]
        public void MenuGroupIdTest()
        {
            MenuItem target = new MenuItem(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.MenuGroupId = expected;
            actual = target.MenuGroupId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuGroups
        ///</summary>
        [TestMethod()]
        public void MenuGroupsTest()
        {
            MenuItem target = new MenuItem(); // TODO: Initialize to an appropriate value
            EntityCollection<MenuGroup> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<MenuGroup> actual;
            target.MenuGroups = expected;
            actual = target.MenuGroups;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuTags
        ///</summary>
        [TestMethod()]
        public void MenuTagsTest()
        {
            MenuItem target = new MenuItem(); // TODO: Initialize to an appropriate value
            EntityCollection<MenuTag> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<MenuTag> actual;
            target.MenuTags = expected;
            actual = target.MenuTags;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for ModifierGroups
        ///</summary>
        [TestMethod()]
        public void ModifierGroupsTest()
        {
            MenuItem target = new MenuItem(); // TODO: Initialize to an appropriate value
            EntityCollection<ModifierGroup> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<ModifierGroup> actual;
            target.ModifierGroups = expected;
            actual = target.ModifierGroups;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Name
        ///</summary>
        [TestMethod()]
        public void NameTest()
        {
            MenuItem target = new MenuItem(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.Name = expected;
            actual = target.Name;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Price
        ///</summary>
        [TestMethod()]
        public void PriceTest()
        {
            MenuItem target = new MenuItem(); // TODO: Initialize to an appropriate value
            Double expected = new Double(); // TODO: Initialize to an appropriate value
            Double actual;
            target.Price = expected;
            actual = target.Price;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
        /*
        /// <summary>
        ///A test for Ratings
        ///</summary>
        [TestMethod()]
        
        public void RatingsTest()
        {
            MenuItem target = new MenuItem(); // TODO: Initialize to an appropriate value
            EntityCollection<Rating> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<Rating> actual;
            target.Ratings = expected;
            actual = target.Ratings;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
     
        /// <summary>
        ///A test for Restaurant
        ///</summary>
        [TestMethod()]
        public void RestaurantTest()
        {
            MenuItem target = new MenuItem(); // TODO: Initialize to an appropriate value
            Restaurant expected = null; // TODO: Initialize to an appropriate value
            Restaurant actual;
            target.Restaurant = expected;
            actual = target.Restaurant;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
           
        /// <summary>
        ///A test for RestaurantId
        ///</summary>
        [TestMethod()]
        public void RestaurantIdTest()
        {
            MenuItem target = new MenuItem(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.RestaurantId = expected;
            actual = target.RestaurantId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for RestaurantReference
        ///</summary>
        [TestMethod()]
        public void RestaurantReferenceTest()
        {
            MenuItem target = new MenuItem(); // TODO: Initialize to an appropriate value
            EntityReference<Restaurant> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<Restaurant> actual;
            target.RestaurantReference = expected;
            actual = target.RestaurantReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
         * */

        /// <summary>
        ///A test for TaxTypeId
        ///</summary>
        [TestMethod()]
        public void TaxTypeIdTest()
        {
            MenuItem target = new MenuItem(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.TaxTypeId = expected;
            actual = target.TaxTypeId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
    }
}

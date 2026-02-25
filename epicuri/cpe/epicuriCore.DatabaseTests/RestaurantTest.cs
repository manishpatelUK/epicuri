using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for RestaurantTest and is intended
    ///to contain all RestaurantTest Unit Tests
    ///</summary>
    [TestClass()]
    public class RestaurantTest
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
        ///A test for Restaurant Constructor
        ///</summary>
        [TestMethod()]
        public void RestaurantConstructorTest()
        {
            Restaurant target = new Restaurant();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for CreateRestaurant
        ///</summary>
        [TestMethod()]
        public void CreateRestaurantTest()
        {
            int id = 0; // TODO: Initialize to an appropriate value
            string name = string.Empty; // TODO: Initialize to an appropriate value
            Address address = null; // TODO: Initialize to an appropriate value
            int categoryId = 0; // TODO: Initialize to an appropriate value
            string description = string.Empty; // TODO: Initialize to an appropriate value
            int currencyId = 0; // TODO: Initialize to an appropriate value
            RestaurantSettings settings = null; // TODO: Initialize to an appropriate value
            int countryId = 0; // TODO: Initialize to an appropriate value
            Restaurant expected = null; // TODO: Initialize to an appropriate value
            Restaurant actual;
            actual = Restaurant.CreateRestaurant(id, name, address, categoryId, description, currencyId, settings, countryId,null);
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Address
        ///</summary>
        [TestMethod()]
        public void AddressTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            Address expected = null; // TODO: Initialize to an appropriate value
            Address actual;
            target.Address = expected;
            actual = target.Address;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Category
        ///</summary>
        [TestMethod()]
        public void CategoryTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            Category expected = null; // TODO: Initialize to an appropriate value
            Category actual;
            target.Category = expected;
            actual = target.Category;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for CategoryId
        ///</summary>
        [TestMethod()]
        public void CategoryIdTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.CategoryId = expected;
            actual = target.CategoryId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for CategoryReference
        ///</summary>
        [TestMethod()]
        public void CategoryReferenceTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            EntityReference<Category> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<Category> actual;
            target.CategoryReference = expected;
            actual = target.CategoryReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Country
        ///</summary>
        [TestMethod()]
        public void CountryTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            Country expected = null; // TODO: Initialize to an appropriate value
            Country actual;
            target.Country = expected;
            actual = target.Country;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for CountryId
        ///</summary>
        [TestMethod()]
        public void CountryIdTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.CountryId = expected;
            actual = target.CountryId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for CountryReference
        ///</summary>
        [TestMethod()]
        public void CountryReferenceTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            EntityReference<Country> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<Country> actual;
            target.CountryReference = expected;
            actual = target.CountryReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Currency
        ///</summary>
        [TestMethod()]
        public void CurrencyTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            Currency expected = null; // TODO: Initialize to an appropriate value
            Currency actual;
            target.Currency = expected;
            actual = target.Currency;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for CurrencyId
        ///</summary>
        [TestMethod()]
        public void CurrencyIdTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.CurrencyId = expected;
            actual = target.CurrencyId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for CurrencyReference
        ///</summary>
        [TestMethod()]
        public void CurrencyReferenceTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            EntityReference<Currency> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<Currency> actual;
            target.CurrencyReference = expected;
            actual = target.CurrencyReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Description
        ///</summary>
        [TestMethod()]
        public void DescriptionTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.Description = expected;
            actual = target.Description;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for DeviceRegistrationTokens
        ///</summary>
        [TestMethod()]
        public void DeviceRegistrationTokensTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            EntityCollection<DeviceRegistrationToken> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<DeviceRegistrationToken> actual;
            target.DeviceRegistrationTokens = expected;
            actual = target.DeviceRegistrationTokens;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Devices
        ///</summary>
        [TestMethod()]
        public void DevicesTest()
        {
            /*
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            EntityCollection<Device> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<Device> actual;
            target.Devices = expected;
            actual = target.Devices;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
             */
        }

        /// <summary>
        ///A test for Floors
        ///</summary>
        [TestMethod()]
        public void FloorsTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            EntityCollection<Floor> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<Floor> actual;
            target.Floors = expected;
            actual = target.Floors;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for HeadOffice
        ///</summary>
        [TestMethod()]
        public void HeadOfficeTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            Restaurant expected = null; // TODO: Initialize to an appropriate value
            Restaurant actual;
            target.HeadOffice = expected;
            actual = target.HeadOffice;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for HeadOfficeReference
        ///</summary>
        [TestMethod()]
        public void HeadOfficeReferenceTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            EntityReference<Restaurant> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<Restaurant> actual;
            target.HeadOfficeReference = expected;
            actual = target.HeadOfficeReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuItems
        ///</summary>
        [TestMethod()]
        public void MenuItemsTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            EntityCollection<MenuItem> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<MenuItem> actual;
            target.MenuItems = expected;
            actual = target.MenuItems;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuTags
        ///</summary>
        [TestMethod()]
        public void MenuTagsTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            EntityCollection<MenuTag> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<MenuTag> actual;
            target.MenuTags = expected;
            actual = target.MenuTags;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Name
        ///</summary>
        [TestMethod()]
        public void NameTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.Name = expected;
            actual = target.Name;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for PhoneNumber
        ///</summary>
        [TestMethod()]
        public void PhoneNumberTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.PhoneNumber = expected;
            actual = target.PhoneNumber;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for PhoneNumber2
        ///</summary>
        [TestMethod()]
        public void PhoneNumber2Test()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.PhoneNumber2 = expected;
            actual = target.PhoneNumber2;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for PublicEmailAddress
        ///</summary>
        [TestMethod()]
        public void PublicEmailAddressTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.PublicEmailAddress = expected;
            actual = target.PublicEmailAddress;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Resources
        ///</summary>
        [TestMethod()]
        public void ResourcesTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            EntityCollection<Resource> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<Resource> actual;
            target.Resources = expected;
            actual = target.Resources;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for RestaurantId
        ///</summary>
        [TestMethod()]
        public void RestaurantIdTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            Nullable<int> expected = new Nullable<int>(); // TODO: Initialize to an appropriate value
            Nullable<int> actual;
            target.RestaurantId = expected;
            actual = target.RestaurantId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Restaurants
        ///</summary>
        [TestMethod()]
        public void RestaurantsTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            EntityCollection<Restaurant> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<Restaurant> actual;
            target.Restaurants = expected;
            actual = target.Restaurants;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Services
        ///</summary>
        [TestMethod()]
        public void ServicesTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            EntityCollection<Service> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<Service> actual;
            target.Services = expected;
            actual = target.Services;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Sessions
        ///</summary>
        [TestMethod()]
        public void SessionsTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            EntityCollection<Session> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<Session> actual;
            target.Sessions = expected;
            actual = target.Sessions;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Settings
        ///</summary>
        [TestMethod()]
        public void SettingsTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            RestaurantSettings expected = null; // TODO: Initialize to an appropriate value
            RestaurantSettings actual;
            target.Settings = expected;
            actual = target.Settings;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Staffs
        ///</summary>
        [TestMethod()]
        public void StaffsTest()
        {
            Restaurant target = new Restaurant(); // TODO: Initialize to an appropriate value
            EntityCollection<Staff> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<Staff> actual;
            target.Staffs = expected;
            actual = target.Staffs;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }


    }
}

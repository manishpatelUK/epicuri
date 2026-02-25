using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects;
using System.Data.EntityClient;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for epicuriContainerTest and is intended
    ///to contain all epicuriContainerTest Unit Tests
    ///</summary>
    [TestClass()]
    public class epicuriContainerTest
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
        ///A test for TaxTypes
        ///</summary>
        [TestMethod()]
        public void TaxTypesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<TaxType> actual;
            actual = target.TaxTypes;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Tables
        ///</summary>
        [TestMethod()]
        public void TablesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Table> actual;
            actual = target.Tables;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Staffs
        ///</summary>
        [TestMethod()]
        public void StaffsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Staff> actual;
            actual = target.Staffs;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Sessions
        ///</summary>
        [TestMethod()]
        public void SessionsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Session> actual;
            actual = target.Sessions;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Services
        ///</summary>
        [TestMethod()]
        public void ServicesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Service> actual;
            actual = target.Services;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }



        /// <summary>
        ///A test for Roles
        ///</summary>
        [TestMethod()]
        public void RolesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Role> actual;
            actual = target.Roles;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Restaurants
        ///</summary>
        [TestMethod()]
        public void RestaurantsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Restaurant> actual;
            actual = target.Restaurants;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Resources
        ///</summary>
        [TestMethod()]
        public void ResourcesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Resource> actual;
            actual = target.Resources;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Ratings
        ///</summary>
        [TestMethod()]
        public void RatingsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Rating> actual;
            actual = target.Ratings;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Permissions
        ///</summary>
        [TestMethod()]
        public void PermissionsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Permission> actual;
            actual = target.Permissions;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Parties
        ///</summary>
        [TestMethod()]
        public void PartiesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Party> actual;
            actual = target.Parties;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Orders
        ///</summary>
        [TestMethod()]
        public void OrdersTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Order> actual;
            actual = target.Orders;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Modifiers
        ///</summary>
        [TestMethod()]
        public void ModifiersTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Modifier> actual;
            actual = target.Modifiers;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for ModifierGroups
        ///</summary>
        [TestMethod()]
        public void ModifierGroupsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<ModifierGroup> actual;
            actual = target.ModifierGroups;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Menus
        ///</summary>
        [TestMethod()]
        public void MenusTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Menu> actual;
            actual = target.Menus;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuTags
        ///</summary>
        [TestMethod()]
        public void MenuTagsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<MenuTag> actual;
            actual = target.MenuTags;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuItems
        ///</summary>
        [TestMethod()]
        public void MenuItemsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<MenuItem> actual;
            actual = target.MenuItems;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuGroups
        ///</summary>
        [TestMethod()]
        public void MenuGroupsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<MenuGroup> actual;
            actual = target.MenuGroups;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for MenuCategories
        ///</summary>
        [TestMethod()]
        public void MenuCategoriesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<MenuCategory> actual;
            actual = target.MenuCategories;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Layouts
        ///</summary>
        [TestMethod()]
        public void LayoutsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Layout> actual;
            actual = target.Layouts;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Floors
        ///</summary>
        [TestMethod()]
        public void FloorsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Floor> actual;
            actual = target.Floors;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Diners
        ///</summary>
        [TestMethod()]
        public void DinersTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Diner> actual;
            actual = target.Diners;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Devices
        ///</summary>
        [TestMethod()]
        public void DevicesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<DatabaseModel.Device> actual;
            actual = target.Devices;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for DeviceRegistrationTokens
        ///</summary>
        [TestMethod()]
        public void DeviceRegistrationTokensTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<DeviceRegistrationToken> actual;
            actual = target.DeviceRegistrationTokens;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Customers
        ///</summary>
        [TestMethod()]
        public void CustomersTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Customer> actual;
            actual = target.Customers;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        
        /// <summary>
        ///A test for Currencies
        ///</summary>
        [TestMethod()]
        public void CurrenciesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Currency> actual;
            actual = target.Currencies;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Countries
        ///</summary>
        [TestMethod()]
        public void CountriesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Country> actual;
            actual = target.Countries;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Categories
        ///</summary>
        [TestMethod()]
        public void CategoriesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<Category> actual;
            actual = target.Categories;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for BlackMarks
        ///</summary>
        [TestMethod()]
        public void BlackMarksTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ObjectSet<BlackMark> actual;
            actual = target.BlackMarks;
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for AddToTaxTypes
        ///</summary>
        [TestMethod()]
        public void AddToTaxTypesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            TaxType taxType = null; // TODO: Initialize to an appropriate value
            target.AddToTaxTypes(taxType);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToTables
        ///</summary>
        [TestMethod()]
        public void AddToTablesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Table table = null; // TODO: Initialize to an appropriate value
            target.AddToTables(table);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToStaffs
        ///</summary>
        [TestMethod()]
        public void AddToStaffsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Staff staff = null; // TODO: Initialize to an appropriate value
            target.AddToStaffs(staff);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToSessions
        ///</summary>
        [TestMethod()]
        public void AddToSessionsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Session session = null; // TODO: Initialize to an appropriate value
            target.AddToSessions(session);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToServices
        ///</summary>
        [TestMethod()]
        public void AddToServicesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Service service = null; // TODO: Initialize to an appropriate value
            target.AddToServices(service);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }


        /// <summary>
        ///A test for AddToRoles
        ///</summary>
        [TestMethod()]
        public void AddToRolesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Role role = null; // TODO: Initialize to an appropriate value
            target.AddToRoles(role);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToRestaurants
        ///</summary>
        [TestMethod()]
        public void AddToRestaurantsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Restaurant restaurant = null; // TODO: Initialize to an appropriate value
            target.AddToRestaurants(restaurant);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToResources
        ///</summary>
        [TestMethod()]
        public void AddToResourcesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Resource resource = null; // TODO: Initialize to an appropriate value
            target.AddToResources(resource);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToRatings
        ///</summary>
        [TestMethod()]
        public void AddToRatingsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Rating rating = null; // TODO: Initialize to an appropriate value
            target.AddToRatings(rating);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToPermissions
        ///</summary>
        [TestMethod()]
        public void AddToPermissionsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Permission permission = null; // TODO: Initialize to an appropriate value
            target.AddToPermissions(permission);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToParties
        ///</summary>
        [TestMethod()]
        public void AddToPartiesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Party party = null; // TODO: Initialize to an appropriate value
            target.AddToParties(party);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToOrders
        ///</summary>
        [TestMethod()]
        public void AddToOrdersTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Order order = null; // TODO: Initialize to an appropriate value
            target.AddToOrders(order);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToModifiers
        ///</summary>
        [TestMethod()]
        public void AddToModifiersTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Modifier modifier = null; // TODO: Initialize to an appropriate value
            target.AddToModifiers(modifier);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToModifierGroups
        ///</summary>
        [TestMethod()]
        public void AddToModifierGroupsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            ModifierGroup modifierGroup = null; // TODO: Initialize to an appropriate value
            target.AddToModifierGroups(modifierGroup);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToMenus
        ///</summary>
        [TestMethod()]
        public void AddToMenusTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Menu menu = null; // TODO: Initialize to an appropriate value
            target.AddToMenus(menu);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToMenuTags
        ///</summary>
        [TestMethod()]
        public void AddToMenuTagsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            MenuTag menuTag = null; // TODO: Initialize to an appropriate value
            target.AddToMenuTags(menuTag);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToMenuItems
        ///</summary>
        [TestMethod()]
        public void AddToMenuItemsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            MenuItem menuItem = null; // TODO: Initialize to an appropriate value
            target.AddToMenuItems(menuItem);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToMenuGroups
        ///</summary>
        [TestMethod()]
        public void AddToMenuGroupsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            MenuGroup menuGroup = null; // TODO: Initialize to an appropriate value
            target.AddToMenuGroups(menuGroup);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToMenuCategories
        ///</summary>
        [TestMethod()]
        public void AddToMenuCategoriesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            MenuCategory menuCategory = null; // TODO: Initialize to an appropriate value
            target.AddToMenuCategories(menuCategory);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToLayouts
        ///</summary>
        [TestMethod()]
        public void AddToLayoutsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Layout layout = null; // TODO: Initialize to an appropriate value
            target.AddToLayouts(layout);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToFloors
        ///</summary>
        [TestMethod()]
        public void AddToFloorsTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Floor floor = null; // TODO: Initialize to an appropriate value
            target.AddToFloors(floor);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToDiners
        ///</summary>
        [TestMethod()]
        public void AddToDinersTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Diner diner = null; // TODO: Initialize to an appropriate value
            target.AddToDiners(diner);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToDevices
        ///</summary>
        [TestMethod()]
        public void AddToDevicesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            DatabaseModel.Device device = new DatabaseModel.Device(); 
            target.AddToDevices(device);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToDeviceRegistrationTokens
        ///</summary>
        [TestMethod()]
        public void AddToDeviceRegistrationTokensTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            DeviceRegistrationToken deviceRegistrationToken = null; // TODO: Initialize to an appropriate value
            target.AddToDeviceRegistrationTokens(deviceRegistrationToken);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToCustomers
        ///</summary>
        [TestMethod()]
        public void AddToCustomersTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Customer customer = null; // TODO: Initialize to an appropriate value
            target.AddToCustomers(customer);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        

        /// <summary>
        ///A test for AddToCurrencies
        ///</summary>
        [TestMethod()]
        public void AddToCurrenciesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Currency currency = null; // TODO: Initialize to an appropriate value
            target.AddToCurrencies(currency);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToCountries
        ///</summary>
        [TestMethod()]
        public void AddToCountriesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Country country = null; // TODO: Initialize to an appropriate value
            target.AddToCountries(country);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToCategories
        ///</summary>
        [TestMethod()]
        public void AddToCategoriesTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            Category category = null; // TODO: Initialize to an appropriate value
            target.AddToCategories(category);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for AddToBlackMarks
        ///</summary>
        [TestMethod()]
        public void AddToBlackMarksTest()
        {
            epicuriContainer target = new epicuriContainer(); // TODO: Initialize to an appropriate value
            BlackMark blackMark = null; // TODO: Initialize to an appropriate value
            target.AddToBlackMarks(blackMark);
            Assert.Inconclusive("A method that does not return a value cannot be verified.");
        }

        /// <summary>
        ///A test for epicuriContainer Constructor
        ///</summary>
        [TestMethod()]
        public void epicuriContainerConstructorTest()
        {
            epicuriContainer target = new epicuriContainer();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for epicuriContainer Constructor
        ///</summary>
        [TestMethod()]
        public void epicuriContainerConstructorTest1()
        {
            string connectionString = string.Empty; // TODO: Initialize to an appropriate value
            epicuriContainer target = new epicuriContainer(connectionString);
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for epicuriContainer Constructor
        ///</summary>
        [TestMethod()]
        public void epicuriContainerConstructorTest2()
        {
            EntityConnection connection = null; // TODO: Initialize to an appropriate value
            epicuriContainer target = new epicuriContainer(connection);
            Assert.Inconclusive("TODO: Implement code to verify target");
        }
    }
}

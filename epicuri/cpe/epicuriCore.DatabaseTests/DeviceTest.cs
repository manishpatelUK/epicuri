using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for DeviceTest and is intended
    ///to contain all DeviceTest Unit Tests
    ///</summary>
    [TestClass()]
    public class DeviceTest
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
        ///A test for RestaurantReference
        ///</summary>
        [TestMethod()]
        public void RestaurantReferenceTest()
        {
            DatabaseModel.Device target = new DatabaseModel.Device(); 
            EntityReference<Restaurant> expected = new EntityReference<Restaurant>();
            EntityReference<Restaurant> actual;
            target.RestaurantReference = expected;
            actual = target.RestaurantReference;
            Assert.AreEqual(expected, actual);
            
        }

        /// <summary>
        ///A test for RestaurantId
        ///</summary>
        [TestMethod()]
        public void RestaurantIdTest()
        {
            DatabaseModel.Device target = new DatabaseModel.Device(); 
            int expected = 0; 
            int actual;
            target.RestaurantId = expected;
            actual = target.RestaurantId;
            Assert.AreEqual(expected, actual);
            
        }

        /// <summary>
        ///A test for Restaurant
        ///</summary>
        [TestMethod()]
        public void RestaurantTest()
        {
            DatabaseModel.Device target = new DatabaseModel.Device();
            Restaurant expected = new Restaurant() ; 
            Restaurant actual;
            target.Restaurant = expected;
            actual = target.Restaurant;
            Assert.AreEqual(expected, actual);
            
        }

        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            DatabaseModel.Device target = new DatabaseModel.Device(); 
            int expected = 0; 
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            
        }

        /// <summary>
        ///A test for Hash
        ///</summary>
        [TestMethod()]
        public void HashTest()
        {
            DatabaseModel.Device target = new DatabaseModel.Device(); 
            string expected = "hash";
            string actual;
            target.Hash = expected;
            actual = target.Hash;
            Assert.AreEqual(expected, actual);

        }

        /// <summary>
        ///A test for DeviceId
        ///</summary>
        [TestMethod()]
        public void DeviceIdTest()
        {
            DatabaseModel.Device target = new DatabaseModel.Device();
            DatabaseModel.Device device = new DatabaseModel.Device();
            string expected = "123-123";
            string actual;
            target.DeviceId = expected;
            actual = target.DeviceId;
            Assert.AreEqual(expected, actual);
            
        }

        /// <summary>
        ///A test for CreateDevice
        ///</summary>
        [TestMethod()]
        public void CreateDeviceTest()
        {
            int id = 0; 
            int restaurantId = 0; 
            string deviceId = "123-fga-123";
            string hash = "hash";
            string note = "";
            DatabaseModel.Device expected = new DatabaseModel.Device();
            DatabaseModel.Device actual;
            expected.Id = id;
            expected.RestaurantId = restaurantId;
            expected.DeviceId = deviceId;
            expected.Hash = hash;
            
            actual = DatabaseModel.Device.CreateDevice(id, restaurantId, deviceId, hash, note);
            Assert.AreEqual(expected.Id, actual.Id);
            Assert.AreEqual(expected.RestaurantId, actual.RestaurantId);
            Assert.AreEqual(expected.Hash, actual.Hash);
            Assert.AreEqual(expected.DeviceId, actual.DeviceId);

            
        }

        /// <summary>
        ///A test for Device Constructor
        ///</summary>
        [TestMethod()]
        public void DeviceConstructorTest()
        {
            Device target = new Device();
            Assert.IsNotNull(target);
        }
    }
}

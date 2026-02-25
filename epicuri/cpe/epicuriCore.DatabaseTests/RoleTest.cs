using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for RoleTest and is intended
    ///to contain all RoleTest Unit Tests
    ///</summary>
    [TestClass()]
    public class RoleTest
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
        ///A test for Role Constructor
        ///</summary>
        [TestMethod()]
        public void RoleConstructorTest()
        {
            Role target = new Role();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for CreateRole
        ///</summary>
        [TestMethod()]
        public void CreateRoleTest()
        {
            int id = 0; // TODO: Initialize to an appropriate value
            int staffId = 0; // TODO: Initialize to an appropriate value
            string name = string.Empty; // TODO: Initialize to an appropriate value
            Role expected = null; // TODO: Initialize to an appropriate value
            Role actual;
            actual = Role.CreateRole(id, staffId, name);
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            Role target = new Role(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Name
        ///</summary>
        [TestMethod()]
        public void NameTest()
        {
            Role target = new Role(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.Name = expected;
            actual = target.Name;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Permissions
        ///</summary>
        [TestMethod()]
        public void PermissionsTest()
        {
            Role target = new Role(); // TODO: Initialize to an appropriate value
            EntityCollection<Permission> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<Permission> actual;
            target.Permissions = expected;
            actual = target.Permissions;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for StaffId
        ///</summary>
        [TestMethod()]
        public void StaffIdTest()
        {
            Role target = new Role(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.StaffId = expected;
            actual = target.StaffId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Staffs
        ///</summary>
        [TestMethod()]
        public void StaffsTest()
        {
            Role target = new Role(); // TODO: Initialize to an appropriate value
            EntityCollection<Staff> expected = null; // TODO: Initialize to an appropriate value
            EntityCollection<Staff> actual;
            target.Staffs = expected;
            actual = target.Staffs;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
    }
}

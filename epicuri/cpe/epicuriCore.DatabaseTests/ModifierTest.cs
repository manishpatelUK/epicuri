using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Data.Objects.DataClasses;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for ModifierTest and is intended
    ///to contain all ModifierTest Unit Tests
    ///</summary>
    [TestClass()]
    public class ModifierTest
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
        ///A test for Modifier Constructor
        ///</summary>
        [TestMethod()]
        public void ModifierConstructorTest()
        {
            Modifier target = new Modifier();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for CreateModifier
        ///</summary>
        [TestMethod()]
        public void CreateModifierTest()
        {
            int id = 0; // TODO: Initialize to an appropriate value
            int modifierGroupId = 0; // TODO: Initialize to an appropriate value
            string modifierValue = string.Empty; // TODO: Initialize to an appropriate value
            double cost = 0F; // TODO: Initialize to an appropriate value
            Modifier expected = null; // TODO: Initialize to an appropriate value
            Modifier actual;
            actual = Modifier.CreateModifier(id, modifierGroupId, modifierValue, cost);
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Cost
        ///</summary>
        [TestMethod()]
        public void CostTest()
        {
            Modifier target = new Modifier(); // TODO: Initialize to an appropriate value
            double expected = 0F; // TODO: Initialize to an appropriate value
            double actual;
            target.Cost = expected;
            actual = target.Cost;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Id
        ///</summary>
        [TestMethod()]
        public void IdTest()
        {
            Modifier target = new Modifier(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.Id = expected;
            actual = target.Id;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for ModifierGroup
        ///</summary>
        [TestMethod()]
        public void ModifierGroupTest()
        {
            Modifier target = new Modifier(); // TODO: Initialize to an appropriate value
            ModifierGroup expected = null; // TODO: Initialize to an appropriate value
            ModifierGroup actual;
            target.ModifierGroup = expected;
            actual = target.ModifierGroup;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for ModifierGroupId
        ///</summary>
        [TestMethod()]
        public void ModifierGroupIdTest()
        {
            Modifier target = new Modifier(); // TODO: Initialize to an appropriate value
            int expected = 0; // TODO: Initialize to an appropriate value
            int actual;
            target.ModifierGroupId = expected;
            actual = target.ModifierGroupId;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for ModifierGroupReference
        ///</summary>
        [TestMethod()]
        public void ModifierGroupReferenceTest()
        {
            Modifier target = new Modifier(); // TODO: Initialize to an appropriate value
            EntityReference<ModifierGroup> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<ModifierGroup> actual;
            target.ModifierGroupReference = expected;
            actual = target.ModifierGroupReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for ModifierValue
        ///</summary>
        [TestMethod()]
        public void ModifierValueTest()
        {
            Modifier target = new Modifier(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.ModifierValue = expected;
            actual = target.ModifierValue;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        
        /// <summary>
        ///A test for TaxType
        ///</summary>
        [TestMethod()]
        public void TaxTypeTest()
        {
            Modifier target = new Modifier(); // TODO: Initialize to an appropriate value
            TaxType expected = null; // TODO: Initialize to an appropriate value
            TaxType actual;
            target.TaxType = expected;
            actual = target.TaxType;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for TaxTypeReference
        ///</summary>
        [TestMethod()]
        public void TaxTypeReferenceTest()
        {
            Modifier target = new Modifier(); // TODO: Initialize to an appropriate value
            EntityReference<TaxType> expected = null; // TODO: Initialize to an appropriate value
            EntityReference<TaxType> actual;
            target.TaxTypeReference = expected;
            actual = target.TaxTypeReference;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
    }
}

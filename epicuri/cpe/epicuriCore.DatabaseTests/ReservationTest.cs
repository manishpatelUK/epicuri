using epicuri.Core.DatabaseModel;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;

namespace epicuri.Core.DatabaseTests
{
    
    
    /// <summary>
    ///This is a test class for ReservationTest and is intended
    ///to contain all ReservationTest Unit Tests
    ///</summary>
    [TestClass()]
    public class ReservationTest
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
        ///A test for Reservation Constructor
        ///</summary>
        [TestMethod()]
        public void ReservationConstructorTest()
        {
            Reservation target = new Reservation();
            Assert.Inconclusive("TODO: Implement code to verify target");
        }

        /// <summary>
        ///A test for CreateReservation
        ///</summary>
        [TestMethod()]
        public void CreateReservationTest()
        {
            int id = 0; // TODO: Initialize to an appropriate value
            int restaurantId = 0; // TODO: Initialize to an appropriate value
            short NumberOfPeople = 0; // TODO: Initialize to an appropriate value
            string name = string.Empty; // TODO: Initialize to an appropriate value
            DateTime createdTime = new DateTime(); // TODO: Initialize to an appropriate value
            DateTime reservationTime = new DateTime(); // TODO: Initialize to an appropriate value
            string notes = string.Empty; // TODO: Initialize to an appropriate value
            Reservation expected = null; // TODO: Initialize to an appropriate value
            Reservation actual;
            actual = Reservation.CreateReservation(id, restaurantId, NumberOfPeople, name, createdTime, reservationTime, notes,"");
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for Notes
        ///</summary>
        [TestMethod()]
        public void NotesTest()
        {
            Reservation target = new Reservation(); // TODO: Initialize to an appropriate value
            string expected = string.Empty; // TODO: Initialize to an appropriate value
            string actual;
            target.Notes = expected;
            actual = target.Notes;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }

        /// <summary>
        ///A test for ReservationTime
        ///</summary>
        [TestMethod()]
        public void ReservationTimeTest()
        {
            Reservation target = new Reservation(); // TODO: Initialize to an appropriate value
            DateTime expected = new DateTime(); // TODO: Initialize to an appropriate value
            DateTime actual;
            target.ReservationTime = expected;
            actual = target.ReservationTime;
            Assert.AreEqual(expected, actual);
            Assert.Inconclusive("Verify the correctness of this test method.");
        }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework;

namespace epicuri.CPE.NUnit.Model
{
    [TestFixture]
    public class AdjustmentTest
    {
        [SetUp]
        public void SetUp() 
        {
            
            
        }
        [TestCase]
        public void TestAdjustmentConstructsWithNullStaff()
        {
            Core.DatabaseModel.Adjustment dbadj = new Core.DatabaseModel.Adjustment
            {
                Staff = null,
                Created= DateTime.MinValue,
                Value = 1,
                Session = new Core.DatabaseModel.Session
                {
                    Id = 1
                },
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Id=1,
                    SupportsChange=0,
                    Name="",
                    Type=1,
                },
            };
            var adj = new Models.Adjustment(dbadj);

            Assert.IsTrue(true);
        }

        [TestCase]
        public void TestAdjustmentConstructsWithMissingFirstNameAndLastName()
        {
            Core.DatabaseModel.Adjustment dbadj = new Core.DatabaseModel.MewsAdjustment
            {
                Staff = null,
                Created = DateTime.MinValue,
                Value = 1,
                Session = new Core.DatabaseModel.Session
                {
                    Id = 1
                },
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Id = 1,
                    SupportsChange = 0,
                    Name = "",
                    Type = 1,
                    
                },
                LastName="",
                FirstName="",
                ChargeId="",
                
            };
            var adj = new Models.Adjustment(dbadj);

            Assert.IsTrue(true);

        }

        [Test]
        public void TestAdjustmentFirstNameWithMissingLastName()
        {
            Core.DatabaseModel.Adjustment dbadj = new Core.DatabaseModel.MewsAdjustment
            {
                Staff = null,
                Created = DateTime.MinValue,
                Value = 1,
                Session = new Core.DatabaseModel.Session
                {
                    Id = 1
                },
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Id = 1,
                    SupportsChange = 0,
                    Name = "",
                    Type = 1,

                },
                LastName = "",
                FirstName = "first",
                ChargeId = "",

            };
            var adj = new Models.Adjustment(dbadj);

            Assert.AreEqual("first",adj.Name);
        }

        [Test]
        public void TestAdjustmentLastNameWithMissingFirstName()
        {
            Core.DatabaseModel.Adjustment dbadj = new Core.DatabaseModel.MewsAdjustment
            {
                Staff = null,
                Created = DateTime.MinValue,
                Value = 1,
                Session = new Core.DatabaseModel.Session
                {
                    Id = 1
                },
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Id = 1,
                    SupportsChange = 0,
                    Name = "",
                    Type = 1,

                },
                LastName = "last",
                FirstName = "",
                ChargeId = "",

            };
            var adj = new Models.Adjustment(dbadj);

            Assert.AreEqual("last", adj.Name);
        }


        [Test]
        public void TestAdjustmentFullName()
        {
            Core.DatabaseModel.Adjustment dbadj = new Core.DatabaseModel.MewsAdjustment
            {
                Staff = null,
                Created = DateTime.MinValue,
                Value = 1,
                Session = new Core.DatabaseModel.Session
                {
                    Id = 1
                },
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Id = 1,
                    SupportsChange = 0,
                    Name = "",
                    Type = 1,

                },
                LastName = "last",
                FirstName = "first",
                ChargeId = "",

            };
            var adj = new Models.Adjustment(dbadj);

            Assert.AreEqual("first last", adj.Name);
        }

        [Test]
        public void TestAdjustmentRoomNo()
        {
            Core.DatabaseModel.Adjustment dbadj = new Core.DatabaseModel.MewsAdjustment
            {
                Staff = null,
                Created = DateTime.MinValue,
                Value = 1,
                Session = new Core.DatabaseModel.Session
                {
                    Id = 1
                },
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Id = 1,
                    SupportsChange = 0,
                    Name = "",
                    Type = 1,

                },
                LastName = "",
                FirstName = "",
                ChargeId = "",
                RoomNo = "1",

            };
            var adj = new Models.Adjustment(dbadj);

            Assert.AreEqual("1", adj.RoomNo);
        }


        [Test]
        public void TestAdjustmentRoomNoMissing()
        {
            Core.DatabaseModel.Adjustment dbadj = new Core.DatabaseModel.MewsAdjustment
            {
                Staff = null,
                Created = DateTime.MinValue,
                Value = 1,
                Session = new Core.DatabaseModel.Session
                {
                    Id = 1
                },
                AdjustmentType = new Core.DatabaseModel.AdjustmentType
                {
                    Id = 1,
                    SupportsChange = 0,
                    Name = "",
                    Type = 1,

                },
                LastName = "",
                FirstName = "",
                ChargeId = "",
                RoomNo = "",

            };
            var adj = new Models.Adjustment(dbadj);

            Assert.AreEqual("", adj.RoomNo);
        }
    }
}

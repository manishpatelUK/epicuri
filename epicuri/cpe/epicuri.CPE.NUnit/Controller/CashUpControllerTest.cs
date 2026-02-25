using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework;
using epicuri.Core.DatabaseModel;
using epicuri.CPE.Controllers;
using System.Data.Objects.DataClasses;
namespace epicuri.CPE.NUnit.Controller
{
    [TestFixture]
    public class CashUpControllerTest
    {
        [TestCase]
        public void TestSeatedSessionInWindow()
        {
            CashUpDay cud = new CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MinValue.AddHours(2)
            };

            Core.DatabaseModel.SeatedSession ss = new Core.DatabaseModel.SeatedSession
            {
                StartTime = DateTime.MinValue.AddHours(1)
            };


            EntityCollection<Session> ec = new EntityCollection<Session>();
            ec.Add(ss);
            IQueryable<Session> qry = CashUpController.GetSessionQuery(ec,cud);

            Assert.AreEqual(ss, qry.First());
            
        }


        [TestCase]
        public void TestSeatedSessionBeforeWindow()
        {
            CashUpDay cud = new CashUpDay
            {
                StartTime = DateTime.MinValue.AddHours(1),
                EndTime = DateTime.MinValue.AddHours(2)
            };

            Core.DatabaseModel.SeatedSession ss = new Core.DatabaseModel.SeatedSession
            {
                StartTime = DateTime.MinValue
            };


            EntityCollection<Session> ec = new EntityCollection<Session>();
            ec.Add(ss);
            var qry = CashUpController.GetSessionQuery(ec, cud);

            Assert.IsFalse(qry.Any());
        }

        [TestCase]
        public void TestSeatedSessionAfterWindow()
        {
            CashUpDay cud = new CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MinValue.AddHours(1)
            };

            Core.DatabaseModel.SeatedSession ss = new Core.DatabaseModel.SeatedSession
            {
                StartTime = DateTime.MinValue.AddHours(2)
            };


            EntityCollection<Session> ec = new EntityCollection<Session>();
            ec.Add(ss);
            var qry = CashUpController.GetSessionQuery(ec, cud);

            Assert.IsFalse(qry.Any());
        }

        [TestCase]
        public void TestTakeawaySessionInWindow()
        {
            CashUpDay cud = new CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MinValue.AddHours(2)
            };

            Core.DatabaseModel.TakeAwaySession ss = new Core.DatabaseModel.TakeAwaySession
            {
                ClosedTime = DateTime.MinValue.AddHours(1),
                Accepted = true,
                Rejected = false
            };


            EntityCollection<Session> ec = new EntityCollection<Session>();
            ec.Add(ss);
            var qry = CashUpController.GetSessionQuery(ec, cud);

            Assert.AreEqual(ss,qry.First());
        }

        [TestCase]
        public void TestTakeawaySessionAfterWindow()
        {
            CashUpDay cud = new CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MinValue.AddHours(1)
            };

            Core.DatabaseModel.TakeAwaySession ss = new Core.DatabaseModel.TakeAwaySession
            {
                ClosedTime = DateTime.MinValue.AddHours(2),
                Accepted = true,
                Rejected = false
            };


            EntityCollection<Session> ec = new EntityCollection<Session>();
            ec.Add(ss);
            var qry = CashUpController.GetSessionQuery(ec, cud);

            Assert.IsFalse(qry.Any());
        }
        [TestCase]
        public void TestTakeawaySessionBeforeWindow()
        {
            CashUpDay cud = new CashUpDay
            {
                StartTime = DateTime.MinValue.AddHours(1),
                EndTime = DateTime.MinValue.AddHours(2)
            };

            Core.DatabaseModel.TakeAwaySession ss = new Core.DatabaseModel.TakeAwaySession
            {
                ClosedTime = DateTime.MinValue.AddHours(0),
                Accepted = true,
                Rejected = false
            };


            EntityCollection<Session> ec = new EntityCollection<Session>();
            ec.Add(ss);
            var qry = CashUpController.GetSessionQuery(ec, cud);

            Assert.IsFalse(qry.Any());
        }

        [TestCase]
        public void TestSeatedSessionWithCashUpDay()
        {
            CashUpDay cud = new CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MinValue.AddHours(2)
            };

            Core.DatabaseModel.SeatedSession ss = new Core.DatabaseModel.SeatedSession
            {
                StartTime = DateTime.MinValue.AddHours(1),
                CashUpDay = cud
            };


            EntityCollection<Session> ec = new EntityCollection<Session>();
            ec.Add(ss);
            var qry = CashUpController.GetSessionQuery(ec, cud);

            Assert.IsFalse(qry.Any());
        }

        [TestCase]
        public void TestTakeawaySessionWithCashUpDay()
        {
            CashUpDay cud = new CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MinValue.AddHours(2)
            };

            Core.DatabaseModel.TakeAwaySession ss = new Core.DatabaseModel.TakeAwaySession
            {
                ClosedTime = DateTime.MinValue.AddHours(1),
                Accepted = true,
                Rejected = false,
                CashUpDay = cud
            };


            EntityCollection<Session> ec = new EntityCollection<Session>();
            ec.Add(ss);
            var qry = CashUpController.GetSessionQuery(ec, cud);

            Assert.IsFalse(qry.Any());
        }

        [TestCase]
        public void TestTakeawaySessionNotAccepted()
        {
            CashUpDay cud = new CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MinValue.AddHours(2)
            };

            Core.DatabaseModel.TakeAwaySession ss = new Core.DatabaseModel.TakeAwaySession
            {
                ClosedTime = DateTime.MinValue.AddHours(1),
                Accepted = false,
                Rejected = false,
            };


            EntityCollection<Session> ec = new EntityCollection<Session>();
            ec.Add(ss);
            var qry = CashUpController.GetSessionQuery(ec, cud);

            Assert.IsFalse(qry.Any());
        }

        [TestCase]
        public void TestTakeawaySessionRejected()
        {
            CashUpDay cud = new CashUpDay
            {
                StartTime = DateTime.MinValue,
                EndTime = DateTime.MinValue.AddHours(2)
            };

            Core.DatabaseModel.TakeAwaySession ss = new Core.DatabaseModel.TakeAwaySession
            {
                ClosedTime = DateTime.MinValue.AddHours(1),
                Accepted = true,
                Rejected = true,
                CashUpDay = cud
            };


            EntityCollection<Session> ec = new EntityCollection<Session>();
            ec.Add(ss);
            var qry = CashUpController.GetSessionQuery(ec, cud);

            Assert.IsFalse(qry.Any());
        }

    }
}

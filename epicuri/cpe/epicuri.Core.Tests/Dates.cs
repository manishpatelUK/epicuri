using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Data.Linq;
using System.Data.Entity;


namespace epicuri.Core.Tests
{
    [TestClass]
    public class DateConstraintTests
    {
        [TestMethod]
        public void TestValidAbsoluteDate()
        {
            epicuri.Core.DatabaseModel.Restaurant r = new DatabaseModel.Restaurant { };
            var dc = new DatabaseModel.AbsoluteDateConstraint
            {
                Date = DateTime.UtcNow,
                StartTime = new TimeSpan(0,0,0),
                EndTime = new TimeSpan(23,59,0),
            };
            r.DateConstraints.Add(dc);

            TimeZoneInfo timezone = r.GetTimeZoneInfo();

            Assert.IsTrue(dc.Matches(timezone));
            
        }

        [TestMethod]
        public void TestInValidAbsoluteDateByTime()
        {
            epicuri.Core.DatabaseModel.Restaurant r = new DatabaseModel.Restaurant { };
            var dc = new DatabaseModel.AbsoluteDateConstraint
            {
                Date = DateTime.UtcNow,
                StartTime = DateTime.UtcNow.AddMinutes(5).TimeOfDay,
                EndTime = DateTime.UtcNow.AddMinutes(10).TimeOfDay,
            };
            r.DateConstraints.Add(dc);

            TimeZoneInfo timezone = r.GetTimeZoneInfo();

            Assert.IsFalse(dc.Matches(timezone));

        }

        [TestMethod]
        public void TestInValidAbsoluteDateByDay()
        {
            epicuri.Core.DatabaseModel.Restaurant r = new DatabaseModel.Restaurant { };
            var dc = new DatabaseModel.AbsoluteDateConstraint
            {
                Date = DateTime.UtcNow.AddDays(1),
                StartTime = new TimeSpan(0, 0, 0),
                EndTime = new TimeSpan(23, 59, 0),
            };
            r.DateConstraints.Add(dc);

            TimeZoneInfo timezone = r.GetTimeZoneInfo();

            Assert.IsFalse(dc.Matches(timezone));

        }

        [TestMethod]
        public void TestInValidAbsoluteDateByMonth()
        {
            epicuri.Core.DatabaseModel.Restaurant r = new DatabaseModel.Restaurant { };
            var dc = new DatabaseModel.AbsoluteDateConstraint
            {
                Date = DateTime.UtcNow.AddMonths(1),
                StartTime = new TimeSpan(0, 0, 0),
                EndTime = new TimeSpan(23, 59, 0),
            };
            r.DateConstraints.Add(dc);

            TimeZoneInfo timezone = r.GetTimeZoneInfo();

            Assert.IsFalse(dc.Matches(timezone));

        }

        [TestMethod]
        public void TestInValidAbsoluteDateByYear()
        {
            epicuri.Core.DatabaseModel.Restaurant r = new DatabaseModel.Restaurant { };
            var dc = new DatabaseModel.AbsoluteDateConstraint
            {
                Date = DateTime.UtcNow.AddYears(1),
                StartTime = new TimeSpan(0, 0, 0),
                EndTime = new TimeSpan(23, 59, 0),
            };
            r.DateConstraints.Add(dc);

            TimeZoneInfo timezone = r.GetTimeZoneInfo();

            Assert.IsFalse(dc.Matches(timezone));

        }



        [TestMethod]
        public void TestValidDayOfWeek()
        {
            epicuri.Core.DatabaseModel.Restaurant r = new DatabaseModel.Restaurant { };
            var dc = new DatabaseModel.DayOfWeekConstraint
            {
                DayOfWeek = Convert.ToInt16(DateTime.UtcNow.DayOfWeek),
                StartTime = new TimeSpan(0, 0, 0),
                EndTime = new TimeSpan(23, 59, 0),
            };
            r.DateConstraints.Add(dc);

            TimeZoneInfo timezone = r.GetTimeZoneInfo();

            Assert.IsTrue(dc.Matches(timezone));

        }


        [TestMethod]
        public void TestInValidDayOfWeek()
        {
            epicuri.Core.DatabaseModel.Restaurant r = new DatabaseModel.Restaurant { };
            var dc = new DatabaseModel.DayOfWeekConstraint
            {
                DayOfWeek = Convert.ToInt16(DateTime.UtcNow.AddDays(1).DayOfWeek),
                StartTime = new TimeSpan(0, 0, 0),
                EndTime = new TimeSpan(23, 59, 0),
            };
            r.DateConstraints.Add(dc);

            TimeZoneInfo timezone = r.GetTimeZoneInfo();

            Assert.IsFalse(dc.Matches(timezone));

        }

        [TestMethod]
        public void TestValidDayOfYear()
        {
            epicuri.Core.DatabaseModel.Restaurant r = new DatabaseModel.Restaurant { };
            var dc = new DatabaseModel.DayOfYearConstraint
            {
                Date = DateTime.UtcNow,
                StartTime = new TimeSpan(0, 0, 0),
                EndTime = new TimeSpan(23, 59, 0),
            };
            r.DateConstraints.Add(dc);

            TimeZoneInfo timezone = r.GetTimeZoneInfo();

            Assert.IsTrue(dc.Matches(timezone));

        }


        [TestMethod]
        public void TestInValidDayOfYearByDay()
        {
            epicuri.Core.DatabaseModel.Restaurant r = new DatabaseModel.Restaurant { };
            var dc = new DatabaseModel.DayOfYearConstraint
            {
                Date = DateTime.UtcNow.AddDays(1),
                StartTime = new TimeSpan(0, 0, 0),
                EndTime = new TimeSpan(23, 59, 0),
            };
            r.DateConstraints.Add(dc);

            TimeZoneInfo timezone = r.GetTimeZoneInfo();

            Assert.IsFalse(dc.Matches(timezone));

        }

        [TestMethod]
        public void TestInValidDayOfYearByMonth()
        {
            epicuri.Core.DatabaseModel.Restaurant r = new DatabaseModel.Restaurant { };
            var dc = new DatabaseModel.DayOfYearConstraint
            {
                Date = DateTime.UtcNow.AddMonths(1),
                StartTime = new TimeSpan(0, 0, 0),
                EndTime = new TimeSpan(23, 59, 0),
            };
            r.DateConstraints.Add(dc);

            TimeZoneInfo timezone = r.GetTimeZoneInfo();

            Assert.IsFalse(dc.Matches(timezone));

        }
    }
}

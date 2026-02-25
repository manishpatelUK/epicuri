using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework;
using NodaTime;

namespace epicuri.CPE.NUnit.Model
{
    [TestFixture]
    public class TimeZoneTest
    {

        [TestCase]
        public void TestSantiagoDaylightSavings()
        {
            var tz = DateTimeZoneProviders.Tzdb["America/Santiago"];

            DateTime dt = new DateTime(2015, 03, 27, 10, 0, 0);

            ZonedDateTime utc = new LocalDateTime(dt.Year, dt.Month, dt.Day, dt.Hour, dt.Minute, dt.Second).InUtc();
            ZonedDateTime before = utc.ToInstant().InZone(tz);
            Assert.AreEqual(new DateTime(2015, 03, 27, 07, 0, 0), new DateTime(before.Year,before.Month,before.Day,before.Hour,before.Minute,before.Second));
        }


        [TestCase]
        public void TestLondonBeforeDaylightSavings()
        {
            var tz = DateTimeZoneProviders.Tzdb["Europe/London"];

            DateTime dt = new DateTime(2015, 03, 27, 10, 0, 0);

            ZonedDateTime utc = new LocalDateTime(dt.Year, dt.Month, dt.Day, dt.Hour, dt.Minute, dt.Second).InUtc();
            ZonedDateTime before = utc.ToInstant().InZone(tz);
            Assert.AreEqual(new DateTime(2015, 03, 27, 10, 0, 0), new DateTime(before.Year, before.Month, before.Day, before.Hour, before.Minute, before.Second));
        }

        [TestCase]
        public void TestLondonAfterDaylightSavings()
        {
            var tz = DateTimeZoneProviders.Tzdb["Europe/London"];

            DateTime dt = new DateTime(2015, 03, 29, 10, 0, 0);

            ZonedDateTime utc = new LocalDateTime(dt.Year, dt.Month, dt.Day, dt.Hour, dt.Minute, dt.Second).InUtc();
            ZonedDateTime before = utc.ToInstant().InZone(tz);
            Assert.AreEqual(new DateTime(2015, 03, 29, 11, 0, 0), new DateTime(before.Year, before.Month, before.Day, before.Hour, before.Minute, before.Second));
        }
    }
}

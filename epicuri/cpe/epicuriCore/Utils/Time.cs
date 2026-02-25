using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace epicuri.Core.Utils
{
    public static class Time
    {
        public static double DateTimeToUnixTimestamp(DateTime dateTime)
        {
            
            return Math.Floor((dateTime - new DateTime(1970, 1, 1 ).ToUniversalTime()).TotalSeconds);
        }

        public static DateTime UnixTimeStampToDateTime(double unixTimeStamp)
        {
            
            System.DateTime dtDateTime = new DateTime(1970, 1, 1, 0, 0, 0, 0);
            dtDateTime = dtDateTime.AddSeconds(unixTimeStamp);
            return dtDateTime;

        }

        public static string IanaToWindows(string ianaZoneId)
        {
            var utcZones = new[] { "Etc/UTC", "Etc/UCT" };
            if (utcZones.Contains(ianaZoneId, StringComparer.OrdinalIgnoreCase))
                return "UTC";

            var tzdbSource = NodaTime.TimeZones.TzdbDateTimeZoneSource.Default;

            // resolve any link, since the CLDR doesn't necessarily use canonical IDs
            var links = tzdbSource.CanonicalIdMap
              .Where(x => x.Value.Equals(ianaZoneId, StringComparison.OrdinalIgnoreCase))
              .Select(x => x.Key);

            var mappings = tzdbSource.WindowsMapping.MapZones;
            var item = mappings.FirstOrDefault(x => x.TzdbIds.Any(links.Contains));
            if (item == null) return null;
            return item.WindowsId;
        }

        // This will return the "primary" IANA zone that matches the given windows zone.
        // If the primary zone is a link, it then resolves it to the canonical ID.
        public static string WindowsToIana(string windowsZoneId)
        {
            if (windowsZoneId.Equals("UTC", StringComparison.OrdinalIgnoreCase))
                return "Etc/UTC";

            var tzdbSource = NodaTime.TimeZones.TzdbDateTimeZoneSource.Default;
            var tzi = TimeZoneInfo.FindSystemTimeZoneById(windowsZoneId);
            var tzid = tzdbSource.MapTimeZoneId(tzi);
            return tzdbSource.CanonicalIdMap[tzid];
        }
    }
}

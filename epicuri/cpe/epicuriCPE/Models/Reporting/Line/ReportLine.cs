using epicuri.Core.DatabaseModel;
using NodaTime;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Web;

namespace epicuri.CPE.Models.Reporting.Line
{
    public class ReportLine<T>
    {
        public string ToString(Restaurant rest)
        {
            List<String> row = new List<string>();
            foreach (var p in typeof(T).GetProperties())
            {
                var val = p.GetValue(this, null);

                if (val == null)
                {
                    row.Add("");
                }
                else
                {

                    row.Add(GetValue(rest, val));
                }

            }

            return row.Aggregate((a, b) => a + "," + b);
           
        }

        private String GetValue(Restaurant rest, object p)
        {
            if (p.GetType() == typeof(DateTime))
            {
                return formatDate(rest.IANATimezone, (DateTime)p);
            }
            else if (p.GetType() == typeof(String))
            {
                return csv(p.ToString());
            }
            else if (p.GetType() == typeof(decimal))
            {
                return ((decimal)p).ToString("#.##");
            }

            return p.ToString();
        }


        public static String csv(string instr)
        {
            if (String.IsNullOrWhiteSpace(instr))
            {
                return "";
            }
            return string.Format("\"{0}\"", instr.Replace("\"","\"\""));
        }

        

        public static String formatDate(String timezone, DateTime dt)
        {
            var tz = DateTimeZoneProviders.Tzdb[timezone];

            ZonedDateTime utc = new LocalDateTime(dt.Year, dt.Month, dt.Day, dt.Hour, dt.Minute, dt.Second).InUtc();
            ZonedDateTime newTime = utc.ToInstant().InZone(tz);
            TimeSpan ts = tz.GetUtcOffset(newTime.ToInstant()).ToTimeSpan();
            DateTimeOffset ofs = new DateTimeOffset(newTime.Year, newTime.Month, newTime.Day, newTime.Hour, newTime.Minute, newTime.Second,ts);
            return ofs.ToString("yyyy'-'MM'-'dd' 'HH':'mm':'ssK",null);
            //return date.ToString("yyyy-MM-dd HH':'mm':'ss'+0000'");
        }
    }
}
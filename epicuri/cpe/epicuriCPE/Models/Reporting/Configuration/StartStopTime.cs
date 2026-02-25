using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models.Reporting.Configuration
{
    public class StartStopTime : ConfigurationOption
    {
        public string View
        {
            get { return "StartStopTime"; }
        }

        public void Process(CsvReport report, System.Collections.Specialized.NameValueCollection values)
        {
            if (values.AllKeys.Contains("starttime"))
            {
                try
                {
                    report.SetStartTime(DateTime.Parse(values["starttime"]));
                }
                catch
                {
                    report.SetStartTime(DateTime.MinValue);
                }
            }
            else
            {
                report.SetStartTime(DateTime.MinValue);
            }


            if (values.AllKeys.Contains("endtime"))
            {
                try
                {
                    var end = DateTime.Parse(values["endtime"]);
                    if (end.Hour == 0 && end.Minute == 0)
                    {
                        end = end.AddHours(23);
                        end = end.AddMinutes(59);
                        end = end.AddSeconds(59);
                    }
                    report.SetEndTime(end);
                }
                catch
                {
                    report.SetEndTime(DateTime.MaxValue);
                }
            }
            else
            {
                report.SetEndTime(DateTime.MaxValue);
            }
        }
    }
}
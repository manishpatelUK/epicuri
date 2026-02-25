using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models.Reporting.Configuration
{
    public class SortByOption : ConfigurationOption
    {
        public string View
        {
            get { return "SortBy"; }
        }

        public void Process(CsvReport report, System.Collections.Specialized.NameValueCollection values)
        {
            if (report.FieldNames().Contains(values["sortby"]))
            {
                report.Sort(values["sortby"],values.AllKeys.Contains("desc"));
            }
        }
    }
}
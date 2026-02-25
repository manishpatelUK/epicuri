using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models.Reporting.Configuration
{
    public class IncludeHeaderOption : ConfigurationOption
    {

        public string View
        {
            get { return "IncludeHeader"; }
        }


        public void Process(CsvReport report, System.Collections.Specialized.NameValueCollection values)
        {
            if (values.AllKeys.Contains("includeheader"))
            {
                report.SetHeader(report.FieldNames().Aggregate((a,b)=>a+","+b));
            }
        }
    }
}
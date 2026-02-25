using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models.Reporting.Configuration
{
    public class LiabilityQuestionOption : ConfigurationOption
    {
        public string View
        {
            get { return "LiabilityQuestion"; }
        }

        public void Process(CsvReport report, System.Collections.Specialized.NameValueCollection values)
        {
            if (!values.AllKeys.Contains("liabilityquestion"))
            {
                throw new InvalidOperationException("Liability question must be checked");
            }
        }
    }
}
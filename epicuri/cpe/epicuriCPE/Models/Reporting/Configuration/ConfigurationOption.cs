using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Linq;
using System.Text;

namespace epicuri.CPE.Models.Reporting.Configuration
{
    public interface ConfigurationOption
    {
        String View { get; }
        void Process(CsvReport report, NameValueCollection values);
    }
}

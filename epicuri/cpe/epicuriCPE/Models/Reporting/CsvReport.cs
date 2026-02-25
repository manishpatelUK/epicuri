using epicuri.CPE.Models.Reporting.Configuration;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace epicuri.CPE.Models.Reporting
{
    public interface CsvReport
    {
        String Name { get; }
        String Url { get; }
        String ReportBody(DateTime start, DateTime end);

        IEnumerable<ConfigurationOption> ConfigurationOptions();
        String[] SortBy { get; }

        void Configure(System.Collections.Specialized.NameValueCollection nameValueCollection);
        IEnumerable<String> FieldNames();
        void SetHeader(String header);

        void Sort(String field, Boolean descending);

        void SetEndTime(DateTime dateTime);

        void SetStartTime(DateTime dateTime);

        String DisplayName { get; }
        String Desc { get; }
    }
}

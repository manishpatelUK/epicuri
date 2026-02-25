using epicuri.CPE.Models.Reporting.Configuration;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Web;

namespace epicuri.CPE.Models.Reporting
{
    public abstract class Report<T> : CsvReport
    {
        protected Core.DatabaseModel.Restaurant restaurant;
        public Report(Core.DatabaseModel.Restaurant restaurant)
        {
            this.restaurant = restaurant;
        }

        public abstract String Name {get;}
        public abstract String DisplayName { get; }
        public abstract String Desc { get; }
        public abstract String[] SortBy {get;}


        public virtual IEnumerable<Line.ReportLine<T>> GetReport(DateTime? startTime, DateTime? stopTime)
        {
            if (startTime == null)
            {
                startTime = DateTime.MinValue;
            }

            if (stopTime == null)
            {
                stopTime = DateTime.MaxValue;
            }

            return ReportLines(startTime.Value, stopTime.Value);
        }


        protected abstract IEnumerable<Line.ReportLine<T>> ReportLines(DateTime startTime, DateTime stopTime);


        public string Url
        {
            get { throw new NotImplementedException(); }
        }
        private bool desc;
        private string sortfield;

        public string ReportBody(DateTime start, DateTime end)
        {
            
            StringBuilder sb = new StringBuilder();

            if (!String.IsNullOrWhiteSpace(header))
            {
                sb.AppendLine(header);
            }

            var report = ReportLines(this.start, this.end);
            if (!String.IsNullOrWhiteSpace(sortfield))
            {
                System.Reflection.PropertyInfo prop = typeof(T).GetProperty(this.sortfield);
                if (desc)
                {
                    report = report.OrderByDescending(a => prop.GetValue(a, null));
                }
                else
                {
                    report = report.OrderBy(a => prop.GetValue(a, null));
                }
            }
            if (report.Any())
            {
                foreach (var item in report)
                {
                    sb.AppendLine(item.ToString(restaurant));
                }
            }
            return sb.ToString();
        }


        public virtual IEnumerable<Configuration.ConfigurationOption> ConfigurationOptions()
        {
            var options = new List<Configuration.ConfigurationOption>();
            options.Add(new IncludeHeaderOption());
            options.Add(new SortByOption());
            options.Add(new StartStopTime());
            return options;
        }


        public void Configure(System.Collections.Specialized.NameValueCollection nameValueCollection)
        {
            foreach (var option in ConfigurationOptions())
            {
                option.Process(this,nameValueCollection);
            }
        }
        string header = "";
        public void SetHeader(string header)
        {
            this.header = header;
        }

        public IEnumerable<String> FieldNames()
        {
            return typeof(T).GetProperties().Select(p => p.Name);
        }


        public void Sort(string field, bool descending)
        {
            this.sortfield = field;
            this.desc = descending;
        }

        private DateTime start = DateTime.MinValue;
        private DateTime end = DateTime.MaxValue;

        public void SetEndTime(DateTime dateTime)
        {
            end = dateTime;
        }

        public void SetStartTime(DateTime dateTime)
        {
            start = dateTime;
        }
    }
}
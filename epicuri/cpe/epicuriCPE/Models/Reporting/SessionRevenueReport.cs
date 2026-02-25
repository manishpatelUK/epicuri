using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using epicuri.CPE.Models.Reporting.Line;

namespace epicuri.CPE.Models.Reporting
{
    public class SessionRevenueReport : Report<SessionRevenueLine>
    {
        public SessionRevenueReport(Core.DatabaseModel.Restaurant restaurant)
            : base(restaurant)
        {

        }
        public override string Name
        {
            get { return "Table Takeaway Revenue"; }
        }

        public override string DisplayName
        {
            get { return "Table & Takeaway Revenue"; }
        }

        public override string Desc
        {
            get { return "The details and totals listed by each table and every takeaway sold. Use this report to see how your tables and takeaways contribute to revenue across time."; }
        }
        public override string[] SortBy
        {
            get { throw new NotImplementedException(); }
        }

        protected override IEnumerable<Line.ReportLine<SessionRevenueLine>> ReportLines(DateTime startTime, DateTime stopTime)
        {

            var nclines = restaurant.Sessions.Where(s=>!s.RemoveFromReports && s.StartTime>=startTime&&s.StartTime<=stopTime).Select(s => new SessionRevenueLine(Session.Factory(s)));

            var report = new List<SessionRevenueLine>();
            foreach (var ncline in nclines)
            {
                var item = ncline;
                item.CurrencyCode = restaurant.ISOCurrency;
                report.Add(item);
            }
            return report;
        }
    }
}
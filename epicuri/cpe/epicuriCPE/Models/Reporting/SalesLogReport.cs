using epicuri.CPE.Models.Reporting.Line;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models.Reporting
{
    public class SalesLogReport : Report<SalesLogLine>
    {
        public SalesLogReport(Core.DatabaseModel.Restaurant restaurant)
            : base(restaurant)
        {

        }

        public override string Name
        {
            get { return "Menu Item Sales Details"; }
        }

        public override string DisplayName
        {
            get { return "Menu Item Sales (Details)"; }
        }

        public override string Desc
        {
            get { return "Each individual sale from the Front-of-House. Details of the individual items sold, how they were sold, which items were voided and why."; }
        }
        public override string[] SortBy
        {
            get { throw new NotImplementedException(); }
        }

        protected override IEnumerable<Line.ReportLine<SalesLogLine>> ReportLines(DateTime startTime, DateTime stopTime)
        {
            var nclines = restaurant.Sessions.Where(s=>!s.RemoveFromReports).SelectMany(s => s.Orders).Where(o=>!o.RemoveFromReports && o.OrderTime>=startTime && o.OrderTime<=stopTime).Select(o => new SalesLogLine(new Session(o.Session),new Order(o)));
            var report = new List<SalesLogLine>();
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
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using epicuri.CPE.Models.Reporting.Line;

namespace epicuri.CPE.Models.Reporting
{
    public class PaymentDetailsReport : Report<PaymentDetailsLine>
    {
        public PaymentDetailsReport(Core.DatabaseModel.Restaurant rest)
            : base(rest)
        {

        }

        public override String Name
        {
            get
            {
                return "Payment Details Report";
            }
        }

        public override string DisplayName
        {
            get { return "Payment Details"; }
        }

        public override string Desc
        {
            get { return "Details of how payments have been made to your restaurant (cash, visa, amex etc) including any adjustments made to the overall bill. Use this report to understand how revenue is flowing into your books."; }
        }

        public override string[] SortBy
        {
            get { throw new NotImplementedException(); }
        }

        protected override IEnumerable<Line.ReportLine<PaymentDetailsLine>> ReportLines(DateTime startTime, DateTime stopTime)
        {
            var nclines = restaurant.Sessions.Where(s=>s.StartTime>=startTime && s.StartTime<=stopTime && !s.RemoveFromReports).SelectMany(s => new Session(s).RealAdjustments).Select(a => new PaymentDetailsLine(a)).Union(restaurant.Sessions.Where(s=>s.StartTime>=startTime && s.StartTime<=stopTime).SelectMany(s=>new Session(s).RealPayments).Select(b=>new PaymentDetailsLine(b)));

            var report = new List<PaymentDetailsLine>();
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
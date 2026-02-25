using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using epicuri.CPE.Models.Reporting.Line;
namespace epicuri.CPE.Models.Reporting
{
    public class CustomerReport : Report<CustomerLine>
    {
        public CustomerReport(Core.DatabaseModel.Restaurant restaurant)
            : base(restaurant)
        {

        }

        public override string Name
        {
            get { return "Customer Details"; }
        }
        public override string DisplayName
        {
            get { return "Customer Details"; }
        }
     

        public override string Desc
        {
            get { return "Details of all the customers at the restaurant where contact details have been captured. Who are your high value and loyal customers? Use this reports to engage them. Encourage the use of the free Epicuri guest app to capture more of this priceless information!"; }
        }
        public override string[] SortBy
        {
            get { throw new NotImplementedException(); }
        }

        public override IEnumerable<Configuration.ConfigurationOption> ConfigurationOptions()
        {
            var options = (List<Configuration.ConfigurationOption>) base.ConfigurationOptions();
            options.Add(new Configuration.LiabilityQuestionOption());
            return options;
        }

        protected override IEnumerable<Line.ReportLine<CustomerLine>> ReportLines(DateTime startTime, DateTime stopTime)
        {
            var customerParties = restaurant.Parties.Where(p => p.Session != null && p.LeadCustomer != null && p.Session.StartTime >= startTime && p.Session.StartTime <= stopTime && !p.Session.RemoveFromReports).Select(p => p.LeadCustomer);
            var customerSession = restaurant.Sessions.OfType<Core.DatabaseModel.SeatedSession>().Where(s=>s.StartTime>=startTime && s.StartTime<=stopTime && !s.RemoveFromReports).SelectMany(s=>s.Diners).Where(d => d.Customer != null && d.SeatedSessionId != null).Select(d => d.Customer);
            var customerTakeaway = restaurant.Sessions.OfType<Core.DatabaseModel.TakeAwaySession>().Where(t => t.PrimaryCustomer != null && t.StartTime >= startTime && t.StartTime <= stopTime && !t.RemoveFromReports).Select(t => t.PrimaryCustomer);

            var customers  = customerParties.Union(customerSession.Union(customerTakeaway)).Distinct();

            var lines = customers.Select(c=>
                new CustomerLine(
                    c,
                    startTime,
                    stopTime,
                    restaurant.Sessions.OfType<Core.DatabaseModel.TakeAwaySession>().Where(t =>  !t.RemoveFromReports && t.PrimaryCustomer != null && t.PrimaryCustomer.Id == c.Id).AsEnumerable(),
                    restaurant.Parties.Where(p=>p.Session!=null && !p.Session.RemoveFromReports && p.LeadCustomer!=null && p.LeadCustomer.Id==c.Id).Union(
                    restaurant.Parties.Where(p => p.Session != null && !p.Session.RemoveFromReports && p.LeadCustomer != null && p.LeadCustomer.Id != c.Id && p.Session.Diners.Any() && p.Session.Diners.Where(d => d.Customer != null).Any(d => d.Customer.Id == c.Id))
                    ).AsEnumerable()));

            var nclines = restaurant.Sessions.OfType<Core.DatabaseModel.TakeAwaySession>().Where(t => t.PrimaryCustomer == null && t.StartTime >= startTime && t.StartTime <= stopTime).GroupBy(t => t.Telephone.Replace(" ","")).Select(t=>new CustomerLine(t));
            
            
            var all =  lines.Union(nclines);

            var report = new List<CustomerLine>();
            foreach (var ncline in all)
            {
                var item = ncline;
                item.CurrencyCode = restaurant.ISOCurrency;
                report.Add(item);
            }
            return report;
        }


    }
}
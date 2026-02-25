using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models.Reporting.Line
{
    public class CustomerLine : ReportLine<CustomerLine>
    {
     
        public String Name { get; set; }
        public String Email { get; set; }
        public String Telephone { get; set; }
        public DateTime FirstInteraction { get; set; }
        public DateTime LastInteraction { get; set; }
        public int SeatedInteractions { get; set; }
        public int TakeawayInteractions { get; set; }
     
        public decimal IndividualValue { get; set; }
        public decimal TotalGroupValue { get; set; }
        public decimal ActualValue { get; set; }
        public String CurrencyCode { get; set; }

        public CustomerLine(Core.DatabaseModel.Customer customer, DateTime start, DateTime end, IEnumerable<Core.DatabaseModel.TakeAwaySession>takeaways, IEnumerable<Core.DatabaseModel.Party> parties)
        {
            Name = customer.Name.Firstname + " " + customer.Name.Surname;
            Email = customer.Email;
            Telephone = customer.PhoneNumber;
            FirstInteraction = parties.Where(p => p.Session != null && !p.Session.RemoveFromReports && p.Session.StartTime >= start && p.Session.StartTime <= end).Select(p => p.Session.StartTime).Union(takeaways.Where(t=>t.StartTime>=start && t.StartTime<=end).Select(t=>t.StartTime)).Min(d=>d);
            LastInteraction = parties.Where(p => p.Session != null && !p.Session.RemoveFromReports && p.Session.StartTime >= start && p.Session.StartTime <= end).Select(p => p.Session.StartTime).Union(takeaways.Where(t => t.StartTime >= start && t.StartTime <= end).Select(t => t.StartTime)).Max(d => d);
            SeatedInteractions = parties.Count(p => p.Session != null && !p.Session.RemoveFromReports && p.Session.StartTime >= start && p.Session.StartTime <= end);
            TakeawayInteractions = takeaways.Count(t => !t.RemoveFromReports&&t.StartTime >= start && t.StartTime <= end);

            IndividualValue = takeaways.Where(t=> t.StartTime >= start && t.StartTime <= end).SelectMany(t=>t.Orders).Sum(o=>o.CalculatedPrice())+parties.Where(p => p.LeadCustomer != customer && p.Session != null).SelectMany(p => p.Session.Diners.Where(d => d.Customer == customer)).Sum(s => s.Orders.Sum(o => o.CalculatedPrice()));
            TotalGroupValue = parties.Where(p => p.LeadCustomer == customer && p.Session != null && p.Session.StartTime >= start && p.Session.StartTime <= end).Sum(p => p.Session.Orders.Sum(o => o.CalculatedPrice()));

            ActualValue = IndividualValue + parties.Where(p => p.LeadCustomer == customer && !p.Session.RemoveFromReports && p.Session != null && p.Session.StartTime >= start && p.Session.StartTime <= end).Sum(p => p.Session.Orders.Sum(o => o.CalculatedPrice()) / Math.Max(1, p.Session.Diners.Count(d => !d.IsTable)));
            
        }

        public CustomerLine(IGrouping<string, Core.DatabaseModel.TakeAwaySession> t)
        {
            Name = t.First().Name;
            Email = "";
            Telephone = t.First().Telephone;
            FirstInteraction = t.Min(s => s.StartTime);
            LastInteraction = t.Max(s => s.StartTime);
            SeatedInteractions = 0;
            TakeawayInteractions = t.Count();
            IndividualValue = t.Sum(s => s.Orders.Sum(o => o.CalculatedPrice()));
            ActualValue = IndividualValue;
            TotalGroupValue = 0;

        }
    }
}
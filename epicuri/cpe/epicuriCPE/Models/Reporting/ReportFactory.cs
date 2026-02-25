using epicuri.CPE.Models.Reporting;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace epicuri.CPE.Model.Reporting
{
    public class ReportFactory
    {

        private Core.DatabaseModel.Restaurant rest;
        private List<CsvReport> reportTypes;
        public ReportFactory(Core.DatabaseModel.Restaurant rest)
        {

            this.rest = rest;

            reportTypes = new List<CsvReport>();
            reportTypes.Add(new CustomerReport(rest));
            reportTypes.Add(new MenuItemReport(rest));
            reportTypes.Add(new PaymentDetailsReport(rest));
            reportTypes.Add(new SalesLogReport(rest));
            reportTypes.Add(new SessionRevenueReport(rest));

        }


     
        public IEnumerable<CsvReport> ReportTypes()
        {
            return reportTypes;
        }

        public CsvReport GetReport(String type)
        {
            return reportTypes.First(rt => rt.Name.Replace(" ","").Equals(type));
        }

        public IEnumerable<String> ReportTypesShort()
        {
            return reportTypes.Select(rt => rt.Name.Replace(" ",""));
        }
    }
}

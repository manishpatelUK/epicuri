using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models.Reporting.Line
{
    public class MenuItemLine : ReportLine<MenuItemLine>
    {
        public int MenuItemId { get; set; }
        public String MenuItemName { get; set; }
        public decimal Price { get; set; }
        public DateTime? LastSold { get; set; }
        public int SalesQuantity { get; set; }
        public decimal SalesValue { get; set; }
        public decimal AverageSalesPrice { get; set; }
        public String CurrencyCode { get; set; }
        public String ItemType { get; set; }
        public double TaxPercentage { get; set; }
        public String TaxName { get; set; }
    }
}
using epicuri.CPE.Models.Reporting.Line;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models.Reporting
{
    public class MenuItemReport : Report<MenuItemLine>
    {
        public MenuItemReport(Core.DatabaseModel.Restaurant restaurant)
            : base(restaurant)
        {

        }

        public override string Name
        {
            get { return "Menu Item Sales Aggregated"; }
        }

        public override string DisplayName
        {
            get { return "Menu Item Sales (Aggregated)"; }
        }

        public override string Desc
        {
            get { return "Aggregated view of menu items sold. How many time each item was sold, when it was last sold, at what average price. Use this report to ​engineer your menu, identify your star items and revitalise the others. (The values here do not include adjustments made to the overall bill!)"; }
        }
        public override string[] SortBy
        {
            get { throw new NotImplementedException(); }
        }

        protected override IEnumerable<Line.ReportLine<MenuItemLine>> ReportLines(DateTime startTime, DateTime stopTime)
        {
            var seslines= restaurant.Sessions.Where(s=>!s.RemoveFromReports).SelectMany(s => s.Orders).Where(o =>!o.RemoveFromReports&& o.OrderTime >= startTime && o.OrderTime <= stopTime).GroupBy(o=>o.MenuItem.Id).Select(o => new MenuItemLine
            {
                MenuItemId = o.Key,
                MenuItemName = o.First().MenuItem.Name,
                LastSold = o.Max(p=>p.OrderTime),
                SalesQuantity = o.Count(),
                Price = (decimal)o.First().MenuItem.Price,
                SalesValue = o.Sum(p=>p.CalculatedPrice()),
                AverageSalesPrice = o.Average(p=>p.CalculatedPrice()),
                CurrencyCode = restaurant.ISOCurrency,
                TaxPercentage = o.First().MenuItem.TaxType.Rate,
                TaxName = o.First().MenuItem.TaxType.Name,
                ItemType = ((Core.DatabaseModel.Enums.MenuItemType)o.First().MenuItem.MenuItemTypeId).ToString()

            });
            


            var nonseslines = restaurant.MenuItems.Where(m=>!seslines.Any(sl=>sl.MenuItemId == m.Id)).Select(m=>new MenuItemLine {
                MenuItemId= m.Id,
                MenuItemName = m.Name,
                LastSold=null,
                SalesQuantity=0,
                Price=(decimal)m.Price,
                SalesValue=0,
                AverageSalesPrice=0,
                CurrencyCode= restaurant.ISOCurrency,
                ItemType = ((Core.DatabaseModel.Enums.MenuItemType)m.MenuItemTypeId).ToString(),
                TaxPercentage = m.TaxType.Rate,
                TaxName = m.TaxType.Name
            });


            var nclines = seslines.Union(nonseslines);
            var report = new List<MenuItemLine>();
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
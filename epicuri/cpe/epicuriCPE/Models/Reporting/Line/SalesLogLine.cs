using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models.Reporting.Line
{
    public class SalesLogLine : ReportLine<SalesLogLine>
    {
        public DateTime Date { get; set; }
        public String SessionType { get; set; }
        
        public int StaffId { get; set; }
        public String StaffName { get; set; }
        public int MenuItemId { get; set; }
        public String MenuItemName { get; set; }
        public decimal SalesPrice { get; set; }
        public String CurrencyCode { get; set; }
        public String VoidReason { get; set; }
        public int SessionId { get; set; }
        public String ItemType { get; set; }
        public double TaxPercentage { get; set; }
        public String TaxName { get; set; }
        public SalesLogLine(Session sess, Order order)
        {
  
            Date = order.OrderTime;
            SessionType = sess.SessionType;
            SessionId = sess.Id;
            MenuItemId = order.MenuItem.Id;
            MenuItemName = order.MenuItem.Name;
            SalesPrice = order.OrderValue();
            VoidReason = order.DiscountReason ?? "";
            TaxPercentage = order.VatRate; 
            TaxName = order.TaxName;
            ItemType = order.MenuItem.TypeName;
            if (order.Staff == null) { StaffId = 0; StaffName = "Self Service"; }
            else
            {
                StaffId = order.Staff.Id;
                StaffName = order.Staff.Name.Replace(',',' ');
            }
        }
    }
}
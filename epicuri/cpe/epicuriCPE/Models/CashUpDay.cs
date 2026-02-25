using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Web;
using Newtonsoft.Json;

namespace epicuri.CPE.Models
{

    public class CashUpDay
    {

        public int Id;

        public double? StartTime;

        [Required]
        public double EndTime;

        public bool WrapUp;

        public Dictionary<string, Decimal> Report;
        public Dictionary<string, Decimal> PaymentReport;
        public Dictionary<string, Decimal> AdjustmentReport;
        public Dictionary<string, Decimal> ItemAdjustmentReport;
        public Dictionary<string, Decimal> ItemAdjustmentLossReport;

        public CashUpDay()
        {
        }

        public CashUpDay(epicuri.Core.DatabaseModel.CashUpDay cashUpDay)
        {
            this.Id = cashUpDay.Id;
            this.StartTime =  epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(cashUpDay.StartTime);
            this.EndTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(cashUpDay.EndTime);
            this.Report = JsonConvert.DeserializeObject<Dictionary<string, Decimal>>(cashUpDay.Report);
            this.WrapUp = cashUpDay.WrapUp;

            




            if (cashUpDay.PaymentReport != null)
            {
                this.PaymentReport = JsonConvert.DeserializeObject<Dictionary<string, Decimal>>(cashUpDay.PaymentReport);
            }

            if (cashUpDay.AdjustmentReport != null)
            {
                this.AdjustmentReport = JsonConvert.DeserializeObject<Dictionary<string, Decimal>>(cashUpDay.AdjustmentReport);
            }

            if (cashUpDay.ItemAdjustmentReport != null)
            {
                this.ItemAdjustmentReport = JsonConvert.DeserializeObject<Dictionary<string, Decimal>>(cashUpDay.ItemAdjustmentReport);
            }

            if (cashUpDay.ItemAdjustmentLossReport != null)
            {
                this.ItemAdjustmentLossReport = JsonConvert.DeserializeObject<Dictionary<string, Decimal>>(cashUpDay.ItemAdjustmentLossReport);
            }
            
            
        }

        public static Dictionary<string, Decimal> CreateCashUpReport(epicuri.Core.DatabaseModel.CashUpDay cashUpDay, IQueryable<Core.DatabaseModel.Session> sessionQuery, epicuri.Core.DatabaseModel.epicuriContainer context)
        {
            Dictionary<string, Decimal> report = new Dictionary<string, Decimal>();

            var successfulSessionQuery = sessionQuery.Where(s => s.ClosedTime != null && s.Paid == true)
                    .OfType<epicuri.Core.DatabaseModel.TakeAwaySession>()
                        .Where(s => s.Rejected == false && s.Deleted == false && s.Accepted == true)
                        .Cast<epicuri.Core.DatabaseModel.Session>()
                    .Union(
                        sessionQuery.Where(s => s.ClosedTime != null && s.Paid == true).
                        OfType<epicuri.Core.DatabaseModel.SeatedSession>()).Select(s=>s);
            var voidSessionQuery = sessionQuery.Where(s => s.ClosedTime.HasValue && s.Paid == false);

            report.Add("SeatedSessionsCount", successfulSessionQuery.OfType<epicuri.Core.DatabaseModel.SeatedSession>().Count());
            report.Add("SeatedSessionsValue", (decimal)successfulSessionQuery.OfType<epicuri.Core.DatabaseModel.SeatedSession>().Sum(s => new Session(s).TotalBeforeAdjustments));

            report.Add("TakeawaySessionsCount", successfulSessionQuery.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>().Count(s => s.Rejected == false && s.Deleted == false && s.Accepted == true));
            report.Add("TakeawaySessionsValue", (decimal)successfulSessionQuery.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>().Where(s => s.Rejected == false && s.Deleted == false && s.Accepted == true).Sum(s => new Session(s).TotalBeforeAdjustments));
            report.Add("CoversCount", successfulSessionQuery.OfType<epicuri.Core.DatabaseModel.SeatedSession>().Sum(s => s.Diners.Count(d => d.IsTable == false)));
            
            report.Add("VoidCount", voidSessionQuery.Count());
            report.Add("VoidValue", (decimal) voidSessionQuery.Sum(s=>new Session(s).TotalBeforeAdjustments));

            report.Add("VoidSeatedSessionValue", (decimal)voidSessionQuery.OfType<epicuri.Core.DatabaseModel.SeatedSession>().Sum(s => new Session(s).TotalBeforeAdjustments));
            report.Add("VoidSeatedSessionCount", voidSessionQuery.OfType<epicuri.Core.DatabaseModel.SeatedSession>().Count());

            report.Add("VoidTakeawaySessionValue", (decimal)voidSessionQuery.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>().Sum(s => new Session(s).TotalBeforeAdjustments));
            report.Add("VoidTakeawaySessionCount", voidSessionQuery.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>().Count());


            var succesfulSessions = successfulSessionQuery.Select(s => Models.Session.Factory(s));


            report.Add("FoodValue", successfulSessionQuery.Sum(s => s.Orders.Where(o => o.MenuItem.MenuItemTypeId == (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Food).Sum(o => o.CalculatedPrice())));
            report.Add("FoodVAT", successfulSessionQuery.Sum(s => s.Orders.Where(o => o.MenuItem.MenuItemTypeId == (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Food).Sum(o => o.CalculatedVAT())));
            report.Add("FoodCount", successfulSessionQuery.Sum(s => s.Orders.Count(o => o.MenuItem.MenuItemTypeId == (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Food)));

            report.Add("DrinkValue", successfulSessionQuery.Sum(s => s.Orders.Where(o => o.MenuItem.MenuItemTypeId == (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Drink).Sum(o => o.CalculatedPrice())));
            report.Add("DrinkVAT", successfulSessionQuery.Sum(s => s.Orders.Where(o => o.MenuItem.MenuItemTypeId == (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Drink).Sum(o => o.CalculatedVAT())));
            report.Add("DrinkCount", successfulSessionQuery.Sum(s => s.Orders.Count(o => o.MenuItem.MenuItemTypeId == (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Drink)));

            report.Add("OtherValue", successfulSessionQuery.Sum(s => s.Orders.Where(o => o.MenuItem.MenuItemTypeId == (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Other).Sum(o => o.CalculatedPrice())));
            report.Add("OtherVAT", successfulSessionQuery.Sum(s => s.Orders.Where(o => o.MenuItem.MenuItemTypeId == (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Other).Sum(o => o.CalculatedVAT())));
            report.Add("OtherCount", successfulSessionQuery.Sum(s => s.Orders.Count(o => o.MenuItem.MenuItemTypeId == (int)epicuri.Core.DatabaseModel.Enums.MenuItemType.Other)));


            decimal grossValue = succesfulSessions.Sum(s => s.Total-s.Tips);
            decimal overpayments = succesfulSessions.Sum(s => s.OverPayments);
            decimal tips = succesfulSessions.Sum(s => (decimal)s.Tips);

            decimal vatValue = 0;
            foreach (Session sesh in succesfulSessions)
            {
                
                if (sesh.Orders.Count() != 0)
                {
                    vatValue += sesh.Orders.Sum(o => (decimal)o.VATValueAfterAdjustment(sesh));
                }

                if (sesh.GetType() == typeof(TakeawaySession))
                {
                    vatValue += ((TakeawaySession)sesh).GetDeliveryVAT(sesh);
                }
            }

            report.Add("OverPayments", (decimal)overpayments+tips);
            
            report.Add("Guests",successfulSessionQuery.OfType<epicuri.Core.DatabaseModel.SeatedSession>().Sum(s=>s.Diners.Count(c=>c.IsTable==false)));

            report.Add("GrossValue", (decimal)grossValue);
            report.Add("VATValue", Decimal.Parse(vatValue.ToString("0.00")));
            report.Add("NetValue", Decimal.Parse(( grossValue - vatValue).ToString("0.00")));
            report.Add("TotalDelivery", successfulSessionQuery.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>().Select(s=>new Models.TakeawaySession(s)).Sum(s=>s.DeliveryCost ?? 0));
            report.Add("TotalSales", report["SeatedSessionsValue"] + report["TakeawaySessionsValue"] + report["TotalDelivery"]);
            report.Add("Payments", CreatePaymentReport(successfulSessionQuery).Sum(a=>a.Value));
            report.Add("TotalAdjustments", CreateAdjustmentReport(successfulSessionQuery).Sum(a => a.Value));
            return report;
        }

        public static Dictionary<String, Decimal> CreatePaymentReport(IQueryable<epicuri.Core.DatabaseModel.Session> successfulSessionsIn)
        {
            var successfulSessions = successfulSessionsIn.OfType<Core.DatabaseModel.SeatedSession>().Select(s => new SeatedSession(s)).Cast<Session>().Union(successfulSessionsIn.OfType<Core.DatabaseModel.TakeAwaySession>().Select(s => new TakeawaySession(s)).Cast<Session>());

            Dictionary<String, Decimal> payments = new Dictionary<string, Decimal>();
            foreach (KeyValuePair<String, Decimal> payment in
                successfulSessions.SelectMany(s => s.RealPayments)
                    .GroupBy(mod => mod.Name).Select(grp => new KeyValuePair<String, Decimal>(grp.Key, grp.Sum(item => item.AbsAdjustment))))
            {
                if (payment.Key.Equals("Cash"))
                {
                    payments.Add(payment.Key, payment.Value - successfulSessions.Sum(s => s.Change));
                } else {
                    payments.Add(payment.Key, payment.Value);
                }
            }

            return payments;
        }

        public static Dictionary<String,Decimal> CreateAdjustmentReport(IQueryable<epicuri.Core.DatabaseModel.Session> successfulSessions)
        {
            Dictionary<String, Decimal> payments = new Dictionary<string, Decimal>();

            foreach (KeyValuePair<String, Decimal> payment in 
                successfulSessions.SelectMany(s => new Session(s).RealAdjustments)
                    .GroupBy(mod => mod.Name).Select(grp => new KeyValuePair<String, Decimal>(grp.Key, grp.Sum(item => item.AbsAdjustment)))) 
            {
                payments.Add(payment.Key,payment.Value);
            }

            return payments;
        }



        public static Dictionary<String, Decimal> CreateItemAdjustmentReport(IQueryable<epicuri.Core.DatabaseModel.Session> successfulSessions)
        {
            Dictionary<String, Decimal> adjustments = new Dictionary<string, Decimal>();

            foreach (KeyValuePair<String, Decimal> adjustment in successfulSessions.Where(s => s.ClosedTime != null && s.Paid == true).SelectMany(s => s.Orders).Where(o => o.AdjustmentType != null).GroupBy(a => a.AdjustmentType).Select(a => new KeyValuePair<String, Decimal>(a.Key.Name, (decimal)successfulSessions.SelectMany(s => s.Orders).Where(o => o.AdjustmentType == a.Key && o.PriceOverride != null).Sum(o => o.PriceOverride.Value))))
            {
                adjustments.Add(adjustment.Key, adjustment.Value);
            }

            return adjustments;
        }


        public static Dictionary<String, Decimal> CreateItemAdjustmentLossReport(IQueryable<epicuri.Core.DatabaseModel.Session> successfulSessions)
        {
            Dictionary<String, Decimal> adjustments = new Dictionary<string, Decimal>();

            foreach (KeyValuePair<String, Decimal> adjustment in successfulSessions.Where(s => s.ClosedTime != null && s.Paid == true).SelectMany(s => s.Orders).Where(o => o.AdjustmentType != null).GroupBy(a => a.AdjustmentType).Select(a => new KeyValuePair<String, Decimal>(a.Key.Name, (decimal)(successfulSessions.SelectMany(s => s.Orders).Where(o => o.AdjustmentType == a.Key && o.PriceOverride != null).Sum(o => o.MenuItem.Price + o.Modifiers.Sum(m => m.Cost)) - successfulSessions.SelectMany(s => s.Orders).Where(o => o.AdjustmentType == a.Key && o.PriceOverride != null).Sum(o => o.PriceOverride.Value)))))
            {
                adjustments.Add(adjustment.Key, adjustment.Value);
            }

            return adjustments;
        }
      
    }


    
}
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace epicuri.UpgradeTool
{
    class Program
    {
        static void Main(string[] args)
        {
            upgradeCashups();
            Console.Read();
        }

        public static void upgradeCashups()
        {
            epicuri.Core.DatabaseModel.epicuriContainer db = new Core.DatabaseModel.epicuriContainer();
            
            IList<epicuri.Core.DatabaseModel.CashUpDay> cashups =  (from c in db.CashUpDay select c).ToList();
            foreach (Core.DatabaseModel.CashUpDay c in cashups)
            {
                var cashUpDay = db.CashUpDay.Single(cu => cu.Id == c.Id);
                Console.WriteLine("Upgrade Cashup " + cashUpDay.Id);
                if (cashUpDay.PaymentReport == null || cashUpDay.AdjustmentReport == null || cashUpDay.ItemAdjustmentReport == null || cashUpDay.ItemAdjustmentLossReport == null)
                {
                    var successfulSessionQuery = cashUpDay.Sessions.AsQueryable().Where(s => s.ClosedTime != null && s.Paid == true)
                        .OfType<epicuri.Core.DatabaseModel.TakeAwaySession>()
                            .Where(s => s.Rejected == false && s.Deleted == false && s.Accepted == true)
                            .Cast<epicuri.Core.DatabaseModel.Session>()
                        .Union(
                            cashUpDay.Sessions.AsQueryable().Where(s => s.ClosedTime != null && s.Paid == true).
                            OfType<epicuri.Core.DatabaseModel.SeatedSession>()).Select(s => s);


                    cashUpDay.PaymentReport = JsonConvert.SerializeObject(epicuri.CPE.Models.CashUpDay.CreatePaymentReport(successfulSessionQuery));

                    cashUpDay.AdjustmentReport = JsonConvert.SerializeObject(epicuri.CPE.Models.CashUpDay.CreateAdjustmentReport(successfulSessionQuery));
                    cashUpDay.ItemAdjustmentReport = JsonConvert.SerializeObject(epicuri.CPE.Models.CashUpDay.CreateItemAdjustmentReport(successfulSessionQuery));
                    cashUpDay.ItemAdjustmentLossReport = JsonConvert.SerializeObject(epicuri.CPE.Models.CashUpDay.CreateItemAdjustmentLossReport(successfulSessionQuery));
                       
                }
                db.SaveChanges();
            }
            Console.WriteLine("Finished upgrading");
            
        }
    }
}

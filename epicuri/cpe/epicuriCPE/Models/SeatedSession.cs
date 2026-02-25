using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using epicuri.Core;

namespace epicuri.CPE.Models
{
    public class SeatedSession : Session
    {

        public IEnumerable<Diner> Diners;
        public IEnumerable<Table> Tables;
        public String ChairData;
        public int Delay;
        public bool IsAdHoc;
        [NonSerialized]
        public decimal SuggestedTip;

        public SeatedSession() { SessionType = Models.SessionType.Seated.ToString(); }

        /*
         * Create a new seatedsessoin from a db entity
         */
        public SeatedSession(Core.DatabaseModel.SeatedSession sess) :base(sess)
        {
            IsAdHoc = sess.IsAdHoc;
            RequestedBill = sess.RequestedBill;
            SessionType = Models.SessionType.Seated.ToString();
            Id = sess.Id;
            NumberOfDiners = sess.Diners.Count(diner=>diner.IsTable==false);
            ChairData = sess.ChairData;
            Orders = sess.Orders.Where(o=>!o.RemoveFromReports).Select(o => new Models.Order(o));
            // Deprecated with Adjustment CR 12/9/14 A.M
            /*
            Payments =  from pay in sess.Payments
                        select new Models.Payment
                        {
                            Id = pay.Id,
                            Amount =pay.Amount,
                            SessionId = sess.Id
                        };
            */
            Diners =    from diner in sess.Diners
                        select new Models.Diner(diner,sess);

            StartTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(sess.StartTime);
            Tables = from tab in sess.Tables
                     select new Models.Table(tab);
            Delay = sess.Delay;
            if (sess.Party != null)
            {
                PartyName = sess.Party.Name;
            }
            else
            {
                PartyName = "";
            }

            if (sess.Service != null)
            {

                ServiceName = sess.Service.ServiceName;
                //EP-124
                MenuId = sess.Service.MenuId;
            }
            else
            {
                ServiceName = "";
                MenuId = 0;
            }
            
            if (Tables.Count() != 0)
            {
                RecurringScheduleItems = from recurringScheduleItem in sess.Service.RecurringScheduleItems
                                         select new Models.RecurringScheduleItem(recurringScheduleItem, sess);
                ScheduleItems = from scheduleItem in sess.Service.ScheduleItems
                                select new Models.ScheduleItem(scheduleItem, sess);
                AdhocNotifications = from adhoc in sess.AdhocNotifications
                                     select new Models.AdhocNotification(adhoc);
            }

            ServiceId = sess.ServiceId;

            if (sess.Service != null)
            {
                ServiceName = sess.Service.ServiceName;
            }
            else
            {
                ServiceName = "";
            }

            SuggestedTip = NumberOfDiners >= Settings.Setting<int>(sess.RestaurantId, "CoversBeforeAutoTip") ?
                    Settings.Setting<decimal>(sess.RestaurantId, "DefaultTipPercentage") : 0;


            if (!sess.TipTotal.HasValue)
            {
                TipTotal = SuggestedTip;
            }
            else
            {
                TipTotal = (decimal)sess.TipTotal.Value;
            }

            Paid = sess.Paid;
            CalculateVals(sess, 0);
        }
    }
}
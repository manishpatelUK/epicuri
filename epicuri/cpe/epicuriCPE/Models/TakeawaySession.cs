using epicuri.Core;
using System;
using System.Collections.Generic;
using System.Data.Objects.DataClasses;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class TakeawaySession : Session
    {
        public Models.Diner Diner;
        public epicuri.Core.DatabaseModel.Address DeliveryAddress;
        public double ExpectedTime;
        public String Message;
        public String Telephone;
        public bool Accepted;
        public int TakeawayMenuId;
        public String Name;
        public bool Rejected;
        public String RejectionNotice;
        public bool Deleted;
        public decimal? DeliveryCost;
        private Core.DatabaseModel.Restaurant restaurant;

        
        public TakeawaySession(Core.DatabaseModel.TakeAwaySession sess)
        {
            RealAdjustments = new List<SessionValueModification>();
            RealPayments = new List<SessionValueModification>();
            DeliveryAddress = sess.DeliveryAddress;
            
            restaurant = sess.Restaurant;
            Orders = from order in sess.Orders where !order.RemoveFromReports select new Models.Order(order);
        
            if (sess.Delivery)
                DeliveryCost = deliveryCost();
            else
                DeliveryCost = 0;

            CalculateVals(sess,DeliveryCost!=null ? DeliveryCost.Value : 0);

            SessionType = sess.Delivery ? Models.SessionType.Delivery.ToString() : Models.SessionType.Collection.ToString();
            Rejected = sess.Rejected;
            Void = sess.RemoveFromReports;
            if (Void)
            {
                var lastvr = sess.VoidReasons.LastOrDefault();
                VoidReason = lastvr != null && lastvr != null ? new VoidReasonPayload(lastvr) : null;
            }

            if (sess.Restaurant == null)
            {
                ServiceId = 0;
                ServiceName = "";
                MenuId = 0;
                TakeawayMenuId = 0;
            }
            else
            {
                ServiceId = sess.Restaurant.Services.FirstOrDefault(t => t.IsTakeaway) == null ? 0 : sess.Restaurant.Services.FirstOrDefault(t => t.IsTakeaway).Id;
                ServiceName = sess.Restaurant.Services.FirstOrDefault(t => t.IsTakeaway) == null ? "" : sess.Restaurant.Services.FirstOrDefault(t => t.IsTakeaway).ServiceName;
                //EP-124
                MenuId = sess.Restaurant.Services.FirstOrDefault(t => t.IsTakeaway) == null ? 0 : sess.Restaurant.Services.FirstOrDefault(t => t.IsTakeaway).MenuId;
                if (sess.Restaurant.TakeawayMenu != null)
                {
                    TakeawayMenuId = sess.Restaurant.TakeawayMenu.Id;
                }
            }

           
            Id = sess.Id;
            
            StartTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(sess.StartTime);
            
            ExpectedTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(sess.ExpectedTime);                   
            StartTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(sess.StartTime);
            DeliveryAddress = sess.DeliveryAddress;
            ExpectedTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(sess.ExpectedTime);
            Telephone = sess.Telephone;
            Message = sess.Message;
            Accepted = sess.Accepted;
            Rejected = sess.Rejected;
            RejectionNotice = sess.RejectionNotice;
            
            // Deprecated with Adjustment CR 12/9/14 A.M
            /*
            Payments = from pay in sess.Payments
                        select new Models.Payment
                        {
                            Id = pay.Id,
                            Amount = pay.Amount,
                            SessionId = sess.Id
                        };
            */
            if (sess.Diner != null)
            {
                Diner = new Models.Diner(sess.Diner, sess);
            }

            if (sess.AdhocNotifications != null) 
            { 
                AdhocNotifications = from adhoc in sess.AdhocNotifications
                                    select new Models.AdhocNotification(adhoc);
            }

            Name = sess.Name;
            Paid = sess.Paid;
            RequestedBill = sess.RequestedBill;
            Deleted = sess.Deleted;

            ClosedTime = sess.ClosedTime.HasValue ? epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(sess.ClosedTime.Value) : 0;

        }

        private decimal? deliveryCost()
        {
            var rest = this.restaurant;
            /*
             * If the restaurant doesn't have lat/long, we cant accept orders
             */
            if (rest == null)
            {
                return null;
            }
            if (rest.Position == null)
            {
                return null;
            }
            if (rest.Position.Longitude == 0)
            {
                return 999;
            }

            Tuple<double, double> pos = new Tuple<double,double>(0, 0);
            pos = Core.Utils.GeoCoding.LatLongFromPostCode(this.DeliveryAddress.PostCode);
            if (pos.Item1 == 0 && pos.Item2 == 0)
            {
                // Rubbish postcode so return -1
                return null;
            }
            
            double deliveryDistance = Core.Utils.GeoCoding.CalcDistance(pos.Item1, pos.Item2, rest.Position.Latitude, rest.Position.Longitude);
            bool freeDelivery = deliveryDistance <= Settings.Setting<double>(rest.Id, "FreeDeliveryRadius");

            if (freeDelivery)
                return 0;

            return  (decimal)Core.Settings.Setting<double>(rest.Id, "DeliverySurcharge");
        }

        public decimal GetDeliveryVAT(Session Session)
        {
            if (this.DeliveryCost > 0)
            {
                decimal vatr = 0;
                foreach (var order in this.Orders)
                {
                    vatr += order.OrderValueAfterAdjustment(Session) / ((decimal)(this.Total) - this.DeliveryCost ?? 0) * ((decimal)order.VatRate);
                }

                return Decimal.Round(((this.DeliveryCost ?? 0)-((this.DeliveryCost ?? 0) * 100 / (100 + vatr))),2);
            }
            return 0;
        }
    }
}
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
//using System.ComponentModel.DataAnnotations.Schema;
//using System.Data.Objects;
using epicuri.Core.DatabaseModel;
using epicuri.Core;

namespace epicuri.CPE.Models
{
    public class Session
    {
        public int Id;
        public string SessionType;
        public double StartTime;
        public double ClosedTime;
        public string ServiceName;
        public int MenuId;
        public string PartyName;
        public bool Void;
        public IEnumerable<ScheduleItem> ScheduleItems;
        public IEnumerable<RecurringScheduleItem> RecurringScheduleItems;
        public IEnumerable<AdhocNotification> AdhocNotifications;
        public IEnumerable<Order> Orders;
        public IEnumerable<Adjustment> Adjustments;
        public IEnumerable<SessionValueModification> RealAdjustments;
        public IEnumerable<SessionValueModification> RealPayments;
        public VoidReasonPayload VoidReason;
        public decimal OverPayments
        {
            get
            {
                if (RemainingTotal >= 0)
                {
                    return 0;
                }

                return Math.Min(RealPayments.Sum(p => p.AbsAdjustment) - RealPayments.OfType<SessionValueModificationWithChange>().Sum(p => p.AbsAdjustment), -(RemainingTotal) -Change);
            }
        }
        public decimal Change
        {
            get
            {
                if (RemainingTotal >= 0)
                {
                    return 0;
                }

               

                return Math.Min(RealPayments.OfType<SessionValueModificationWithChange>().Sum(p => p.AbsAdjustment), -RemainingTotal);
            }
        }
        public int NumberOfDiners;
        public decimal SubTotal;
        public decimal PaymentTotal;
        public decimal DiscountTotal;
        public decimal RemainingTotal;
        public decimal Tips = 0;
        public decimal TipTotal;
        public decimal Total;
        public decimal VATTotal = 0;
        public int ServiceId;
        public int InstantiatedFromId;
        public bool Paid;
        public bool RequestedBill;
        [NonSerialized]
        public decimal TotalBeforeAdjustments;
        [NonSerialized]
        public decimal VATTotalBeforeAdjustments;
        public decimal CashGiven { get { return RealPayments.OfType<SessionValueModificationWithChange>().Sum(p => p.AbsAdjustment); } }

        public Session() { }
        public Session(Core.DatabaseModel.Session sess)
        {
            SessionType = sess.GetType() == typeof(Core.DatabaseModel.SeatedSession) ? "Seated" : "Takeaway";
            RealAdjustments = new List<SessionValueModification>();
            RealPayments = new List<SessionValueModification>();
            Id = sess.Id;
            CalculateVals(sess, 0);
            StartTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(sess.StartTime);
            if (sess.ClosedTime.HasValue)
            {
                ClosedTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(sess.ClosedTime.Value);
            }
            Void = sess.RemoveFromReports;
            if (Void)
            {
                var lastvr = sess.VoidReasons.LastOrDefault();
                VoidReason = lastvr != null && lastvr != null ? new VoidReasonPayload(lastvr) : null;
            }

        }

        public void CalculateVals(Core.DatabaseModel.Session sess,decimal deliverycost)
        {
            RealAdjustments = new List<SessionValueModification>();
            RealPayments = new List<SessionValueModification>();

            decimal runningTotal = 0;
            decimal runningVAT = 0;
            decimal subTotal = 0;
            RemainingTotal = 0;

            Void = sess.RemoveFromReports;

            if (Void)
            {
                var lastvr = sess.VoidReasons.LastOrDefault();
                VoidReason = lastvr!=null && lastvr != null ? new VoidReasonPayload(lastvr) : null;
            }
            
            


            var orders = from order in sess.Orders where !order.RemoveFromReports select order;
            var ordersarray = orders.ToArray();
            DiscountTotal = 0;
            foreach (var order in ordersarray)
            {
                runningTotal += order.CalculatedPrice();
                runningVAT += order.CalculatedVAT();
            }

            TotalBeforeAdjustments = runningTotal;
            VATTotalBeforeAdjustments = runningVAT;

            // runningTotal is the total minus adjustments and payments - i.e. the amount the customer is yet to pay
            // subtotal will be the order total minus the adjustments for display on the bill


            // Get the adjustments
            Adjustments = sess.Adjustments.Where(aj => aj.Deleted == null).OrderBy(aj => aj.Created).ToList().Select(aj => new Models.Adjustment(aj));


            subTotal = runningTotal;

            foreach (Adjustment adjustment in Adjustments)
            {
                switch ((Enums.NumericalTypeType)adjustment.NumericalTypeId)
                {
                    case Enums.NumericalTypeType.Price:



                        switch (adjustment.Type.Type)
                        {
                            case Enums.AdjustmentTypeType.Discount:
                                decimal adj = adjustment.Value;
                                if (adjustment.Value > runningTotal)
                                {
                                    adj = runningTotal;
                                }
                                DiscountTotal -= adj;
                                runningTotal -= adj;
                                ((List<SessionValueModification>)RealAdjustments).Add(new SessionValueModification
                                {
                                    Name = adjustment.Type.Name,
                                    AbsAdjustment = Decimal.Parse(Math.Round(adj, 2).ToString()),
                                    Staff = adjustment.Staff,
                                    SessionId = this.Id,
                                    SessionType = this.SessionType,
                                    AdjustmentType = "Adjustment",
                                    Date = adjustment.Date
                                });
                                break;
                            default:
                                break;
                        }

                        break;
                    case Enums.NumericalTypeType.Percentage:

                        var thisDiscount = runningTotal * (adjustment.Value / 100);

                        if (runningTotal <= 0) { thisDiscount = 0; }
                        else if (runningTotal < thisDiscount)
                        {
                            thisDiscount = runningTotal;    //This case is if and only if the percentage is greater than 100%
                        }

                        
                        switch (adjustment.Type.Type)
                        {
                            case Enums.AdjustmentTypeType.Discount:
                                runningTotal -= thisDiscount;
                                DiscountTotal -= thisDiscount;
                                ((List<SessionValueModification>)RealAdjustments).Add(new SessionValueModification
                                {
                                    Name = adjustment.Type.Name,
                                    AbsAdjustment = Decimal.Parse(Math.Round(thisDiscount, 2).ToString()),
                                    Staff = adjustment.Staff,
                                    SessionId = this.Id,
                                    SessionType = this.SessionType,
                                    AdjustmentType = "Adjustment",
                                    Date = adjustment.Date
                                });
                                break;
                            default:
                                break;
                        }

                        break;
                    default:
                        break;
                }
            }

            if (deliverycost > 0)
            {
                runningTotal += deliverycost;
            }


            




            foreach (Adjustment adjustment in Adjustments)
            {
                switch ((Enums.NumericalTypeType)adjustment.NumericalTypeId)
                {
                    case Enums.NumericalTypeType.Price:
                        switch (adjustment.Type.Type)
                        {
                            case Enums.AdjustmentTypeType.Payment:
                                runningTotal -= adjustment.Value;
                                PaymentTotal -= adjustment.Value;

                                if (adjustment.Type.ChangeGiven)
                                {
                                    ((List<SessionValueModification>)RealPayments).Add(new SessionValueModificationWithChange
                                    {
                                        Name = adjustment.Type.Name,
                                        AbsAdjustment = Decimal.Parse(Math.Round(adjustment.Value, 2).ToString()),
                                        Staff = adjustment.Staff,
                                        SessionId = this.Id,
                                        SessionType = this.SessionType,
                                        AdjustmentType = "Payment",
                                        Date = adjustment.Date
                                    });
                                }
                                else
                                {
                                    ((List<SessionValueModification>)RealPayments).Add(new SessionValueModification
                                    {
                                        Name = adjustment.Type.Name,
                                        AbsAdjustment = Decimal.Parse(Math.Round(adjustment.Value, 2).ToString()),
                                        Staff = adjustment.Staff,
                                        SessionId = this.Id,
                                        SessionType = this.SessionType,
                                        AdjustmentType = "Payment",
                                        Date = adjustment.Date,
                                        
                                        Mews = adjustment.Mews ? new MewsAdjustment{FirstName = adjustment.FirstName,
                                            LastName=adjustment.LastName,
                                            RoomNumber= adjustment.RoomNo,
                                            ChargeId = adjustment.ChargeId}: null

                                    });
                                }

                                break;
                           
                            default:
                                break;
                        }

                        break;
                    case Enums.NumericalTypeType.Percentage:

                        var thisDiscount = runningTotal * (adjustment.Value / 100);

                        if (runningTotal <= 0) { thisDiscount = 0; }
                        else if (runningTotal < thisDiscount)
                        {
                            thisDiscount = runningTotal;    //This case is if and only if the percentage is greater than 100%
                        }

                        switch (adjustment.Type.Type)
                        {
                            case Enums.AdjustmentTypeType.Payment:

                                runningTotal -= thisDiscount;

                                PaymentTotal -= thisDiscount;
                                ((List<SessionValueModification>)RealPayments).Add(new SessionValueModification
                                {
                                    Name = adjustment.Type.Name,
                                    AbsAdjustment = Decimal.Parse(Math.Round(thisDiscount, 2).ToString()),
                                    Staff = adjustment.Staff,
                                    SessionId = this.Id,
                                    SessionType = this.SessionType,
                                    AdjustmentType = "Payment",
                                    Date = adjustment.Date
                                });
                                break;
                            default:
                                break;
                        }

                        break;
                    default:
                        break;
                }
            }


            // Total for Session
            SubTotal = subTotal;
            VATTotal = Math.Round(runningVAT, 2);
            RemainingTotal = runningTotal;

            

            if ((subTotal + DiscountTotal) <= 0)
            {
                Total = 0;
                VATTotal = 0;
            }
            else
            {   
                Total = subTotal + DiscountTotal + deliverycost;
                VATTotal = subTotal==0 ? 0 : Math.Round((Total / subTotal) * VATTotal, 2);

                if (TipTotal > 0)
                {
                    Tips = Decimal.Round(Total * (TipTotal / 100), 2);   
                }

                RemainingTotal += Tips;
                Total += Tips;

                RemainingTotal = Decimal.Round(RemainingTotal, 2);
                Total = Decimal.Round(Total, 2);   
            }


            

            
        }

        internal static Session Factory(Core.DatabaseModel.Session s)
        {
            if (s.GetType() == typeof(Core.DatabaseModel.SeatedSession))
            {
                return new Models.SeatedSession((Core.DatabaseModel.SeatedSession)s);
            } else if (s.GetType()==typeof(Core.DatabaseModel.TakeAwaySession))
            {
                return new Models.TakeawaySession((Core.DatabaseModel.TakeAwaySession)s);
            }
            else { return new Models.Session(s); }
        }
    }

    public enum SessionType
    {
        Seated, Collection, Delivery
    }
}
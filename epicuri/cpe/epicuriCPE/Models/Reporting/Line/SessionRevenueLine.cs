using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models.Reporting.Line
{
    public class SessionRevenueLine : ReportLine<SessionRevenueLine>
    {
        public DateTime StartTime { get; set; }
        public DateTime? ClosedTime { get; set; }
        public int SessionId { get; set; }
        public String SessionType { get; set; }
        public decimal SubTotal { get; set; }
        public decimal Tips { get; set; }
        public decimal Adjustments { get; set; }
        public decimal Total { get; set; }
        
        public decimal Payments { get; set; }
        public decimal Change { get; set; }
        public decimal OverPayment { get; set; }
        public String CurrencyCode { get; set; }
        
        public SessionRevenueLine(Session session)
        {
            StartTime = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(session.StartTime);
            ClosedTime = session.ClosedTime ==0?new DateTime?(): epicuri.Core.Utils.Time.UnixTimeStampToDateTime(session.ClosedTime) ;
            SessionId = session.Id;
            SessionType = session.SessionType;
            SubTotal = (decimal)session.SubTotal;
            Total = (decimal)session.Total;
            Payments = session.RealPayments.Sum(r => r.AbsAdjustment);
            Tips = session.Tips;
            Adjustments = session.RealAdjustments.Sum(r => r.AbsAdjustment);
            Change = session.Change;
            OverPayment = session.OverPayments;

        }
    }
}
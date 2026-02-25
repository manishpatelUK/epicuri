using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models.Reporting.Line
{
    public class PaymentDetailsLine : ReportLine<PaymentDetailsLine>
    {
        public DateTime Date { get; set; }
        public int SessionId { get; set; }
        public String SessionType { get; set; }
        public int StaffId { get; set; }
        public String StaffName { get; set; }
        public String AdjustmentType { get; set; }
        public String Method { get; set; }
        public Decimal Value {get;set;}
        public String CurrencyCode { get; set; }
        public String MEWSFirstName { get; set; }
        public String MEWSLastName { get; set; }
        public String MEWSRoomNumber { get; set; }
        public String MEWSChargeId { get; set; }
        public PaymentDetailsLine(SessionValueModification svm)
        {
            Date = svm.Date;
            SessionId = svm.SessionId;
            if (svm.Staff != null)
            {
                StaffId = svm.Staff.Id;
                StaffName = svm.Staff.Name;
            }
            else
            {
                StaffId= 0;
                StaffName = "Not Set";
            }
            AdjustmentType = svm.AdjustmentType;
            Method = svm.Name;
            if (svm.AdjustmentType.Equals("Payment",StringComparison.OrdinalIgnoreCase))
            {
                Value = svm.AbsAdjustment;
            }
            else
            {
                Value = -svm.AbsAdjustment;
            }
            
            SessionType = svm.SessionType;

            if (svm.Mews != null) { 
                MEWSChargeId = svm.Mews.ChargeId;
                MEWSFirstName = svm.Mews.FirstName;
                MEWSLastName = svm.Mews.LastName;
                MEWSRoomNumber = svm.Mews.RoomNumber;
            }
            
        }
        
    }
}
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.API.Models
{
    public class Session
    {
        public int Id;
        public int? SelfServiceMenuId;
        public Restaurant Restaurant;
        public Models.TakeAwayOrder TakeawayOrder;
        public double Time;
        public int? MenuId;
        public IEnumerable<CPE.Models.Diner> Diners;
        public IEnumerable<CPE.Models.Course> Courses;
        public IEnumerable<int> Tables;
        public string ClosedMessage;
        public string SocialMessage;
        public bool Delivery;
        public bool Deleted;
        public decimal Tips;
        public decimal TipTotal;
        public DateTime _Time
        {
            set
            {
                Time = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(value);
            }
        }

        public DateTime? _CTime
        {
            set
            {
                if (value.HasValue)
                {
                    ClosedTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(value.Value);
                }
            }
        }
        public double ClosedTime;

        public double ExpectedTime;
        internal DateTime _ExTime
        {
            set
            {
                ExpectedTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(value);
               
            }
            get
            {
                return epicuri.Core.Utils.Time.UnixTimeStampToDateTime(ExpectedTime);
            }
        }

        public IEnumerable<Order> Orders;
        public decimal Total;
        public decimal PriceOffset;
        public decimal SuggestedTip;
        public bool RequestedBill;
        public bool Accepted;
        public bool Rejected;
        public string RejectionNotice;
        public string Notes;
        public decimal SubTotal;
        public decimal Discounts;
    }
}
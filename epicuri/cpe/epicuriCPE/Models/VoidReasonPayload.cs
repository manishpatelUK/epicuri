using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class VoidReasonPayload
    {

        public VoidReasonPayload(Core.DatabaseModel.VoidReason voidReason)
        {
            Reason = voidReason.Reason;
            VoidTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(voidReason.VoidTime);
            Staff = new Staff(voidReason.Staff);
        }

        public VoidReasonPayload()
        {
        }


        public String Reason { get; set; }

        public double VoidTime { get; set; }

        public Staff Staff { get; set; }
    }
}
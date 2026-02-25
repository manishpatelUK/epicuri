using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace epicuri.Core.DatabaseModel
{
    public partial class Restaurant
    {
        public TimeZoneInfo GetTimeZoneInfo()
        {
            return TimeZoneInfo.FindSystemTimeZoneById(Core.Utils.Time.IanaToWindows(this.IANATimezone));
            //return TimeZoneInfo.FindSystemTimeZoneById(TimeZoneNames.TimeZoneNames.GetNamesForTimeZone(this.IANATimezone, "en-US").Generic);
        }
    }
}

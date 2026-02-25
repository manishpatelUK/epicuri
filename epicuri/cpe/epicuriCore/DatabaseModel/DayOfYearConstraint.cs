using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace epicuri.Core.DatabaseModel
{
    public partial class DayOfYearConstraint : DateConstraint
    {
        public override bool Matches(TimeZoneInfo timezone, DateTime resDateTime)
        {
            return base.Matches(timezone) && Date.Day == DateTime.UtcNow.Day && Date.Month == DateTime.UtcNow.Month;
        }

        public String DateString
        {
            get
            {
                return this.Date.ToString("MM-dd");
            }
        }
    }
}

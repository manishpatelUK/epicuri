using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class AllEvents
    {
        public IEnumerable<ScheduleItem> ScheduleItems;
        public IEnumerable<RecurringScheduleItem> RecurringScheduleItems;
    }
}
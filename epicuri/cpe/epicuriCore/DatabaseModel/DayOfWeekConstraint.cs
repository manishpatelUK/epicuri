using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace epicuri.Core.DatabaseModel
{
    public partial class DayOfWeekConstraint : DateConstraint
    {

        public String Day
        {
            get
            {
                switch (DayOfWeek)
                {
                    case 0:
                        return "Monday";
                    case 1:
                        return "Tuesday";
                    case 2:
                        return "Wednesday";
                    case 3:
                        return "Thursday";
                    case 4:
                        return "Friday";
                    case 5:
                        return "Saturday";
                    case 6:
                        return "Sunday";

                }
                return "";
            }
        }

        public virtual TimeSpan LocalTime
        {
            get
            {
                epicuriContainer db = new epicuriContainer();
                Restaurant restaurant = db.Restaurants.FirstOrDefault(r => r.Id == this.RestaurantId);

                DateTime blackoutDateTime = Convert.ToDateTime(this.StartTime.ToString());
                TimeZoneInfo timezone = restaurant.GetTimeZoneInfo();
                DateTimeOffset localTime = new DateTimeOffset(blackoutDateTime, timezone.GetUtcOffset(blackoutDateTime));
                if ((this.StartTime + localTime.Offset).Days == 1)
                    return new TimeSpan(0, 0, 0, 0);
                return this.StartTime + localTime.Offset;
            }
        }

        public virtual TimeSpan LocalTimeEndTime
        {
            get
            {
                epicuriContainer db = new epicuriContainer();
                Restaurant restaurant = db.Restaurants.FirstOrDefault(r => r.Id == this.RestaurantId);

                DateTime blackoutDateTime = Convert.ToDateTime(this.EndTime.ToString());
                TimeZoneInfo timezone = restaurant.GetTimeZoneInfo();
                DateTimeOffset localTime = new DateTimeOffset(blackoutDateTime, timezone.GetUtcOffset(blackoutDateTime));
                if ((this.EndTime + localTime.Offset).Days == 1)
                    return new TimeSpan(0, 0, 0, 0);
                return this.EndTime + localTime.Offset;
            }
        }

    }

    
}

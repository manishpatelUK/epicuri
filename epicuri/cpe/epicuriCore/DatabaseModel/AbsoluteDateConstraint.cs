using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace epicuri.Core.DatabaseModel
{
    public partial class AbsoluteDateConstraint : DateConstraint
    {
        public Restaurant Restaurant { 
            get
            {
                var db = new epicuriContainer();
                var restaurant = db.Restaurants.FirstOrDefault(r => r.Id == this.RestaurantId);

                return restaurant;
            }
        }

        public override bool Matches(TimeZoneInfo timezone, DateTime resDateTime)
        {
            // --- Blackout split over two days so check both days ---
            if (base.EndTime < base.StartTime)
            {
                TimeSpan endDayTime = new TimeSpan(24, 0, 0);

                if ((resDateTime.TimeOfDay.Ticks >= StartTime.Ticks) && (resDateTime.TimeOfDay.Ticks <= endDayTime.Ticks))
                {
                    bool dateMatch = (Date.Day == resDateTime.Day && Date.Month == resDateTime.Month && Date.Year == resDateTime.Year);

                    if (!dateMatch)
                    {
                        //Dates don't match so return false
                        return false;
                    }
                    return true;
                }

                TimeSpan startDayTime = new TimeSpan(0, 0, 0);

                if ((resDateTime.TimeOfDay.Ticks >= startDayTime.Ticks) && (resDateTime.TimeOfDay.Ticks <= EndTime.Ticks))
                {
                    Date = Date.AddDays(1);

                    bool dateMatch = (Date.Day == resDateTime.Day && Date.Month == resDateTime.Month && Date.Year == resDateTime.Year);

                    if (!dateMatch)
                    {
                        //Dates don't match so return false
                        return false;
                    }
                    return true;
                }

                return false;
            }
            else
            {
                bool baseMatch = base.Matches(timezone, resDateTime);
                if (!baseMatch)
                {
                    //If the times don't fall within the time constraint - return false (so book)
                    return false;
                }

                bool dateMatch = (Date.Day == resDateTime.Day && Date.Day == resDateTime.Day && Date.Month == resDateTime.Month && Date.Year == resDateTime.Year);

                if (!dateMatch)
                {
                    //Dates don't match so return false
                    return false;
                }
            }
     
            // If the dates and the times match - it's blackout so don't book
            return true;
         
            //return base.Matches(resDateTime) && Date.Day == resDateTime.Day && Date.Month == resDateTime.Month && Date.Year == resDateTime.Year;
        }

        public String DateString
        {
            get
            {
                return this.Date.ToShortDateString();
            }
        }

        public virtual TimeSpan LocalTime
        {
            get
            {
                DateTime blackoutDateTime =  this.Date + this.StartTime;
                TimeZoneInfo timezone = this.Restaurant.GetTimeZoneInfo();
                DateTimeOffset offsetTime = new DateTimeOffset(blackoutDateTime, timezone.GetUtcOffset(blackoutDateTime));
                if ((this.StartTime + offsetTime.Offset).Days == 1)
                    return new TimeSpan(0, 0, this.StartTime.Minutes, 0);
                return this.StartTime + offsetTime.Offset;
            }
        }
    }
}

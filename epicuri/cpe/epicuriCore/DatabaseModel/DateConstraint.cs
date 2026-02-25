using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace epicuri.Core.DatabaseModel
{
    public partial  class DateConstraint
    {
        public virtual bool Matches(TimeZoneInfo timezone)
        {
            return (DateTime.UtcNow.TimeOfDay.Ticks >= StartTime.Ticks) && (DateTime.UtcNow.TimeOfDay.Ticks <= EndTime.Ticks);
        }

        public virtual bool Matches(TimeZoneInfo timezone, DateTime resDateTime)
        {

            if (this.GetType() == typeof(DayOfWeekConstraint))
            {
                DateTime requestedTimeForRestaurant = TimeZoneInfo.ConvertTimeFromUtc(resDateTime, timezone);
                DayOfWeekConstraint dayOfWeekConstraint = (DayOfWeekConstraint)this;

                DayOfWeek theDay;
                switch (dayOfWeekConstraint.DayOfWeek)
                {
                    case 0: theDay = DayOfWeek.Monday; break;
                    case 1: theDay = DayOfWeek.Tuesday; break;
                    case 2: theDay = DayOfWeek.Wednesday; break;
                    case 3: theDay = DayOfWeek.Thursday; break;
                    case 4: theDay = DayOfWeek.Friday; break;
                    case 5: theDay = DayOfWeek.Saturday; break;
                    case 6: theDay = DayOfWeek.Sunday; break;
                    default: throw new Exception("Unrecognsied date");
                }

                if (requestedTimeForRestaurant.DayOfWeek == theDay)
                {
                    DateTime dowConstraintStartTime = new DateTime(requestedTimeForRestaurant.Year, requestedTimeForRestaurant.Month, requestedTimeForRestaurant.Day, this.StartTime.Hours, this.StartTime.Minutes, this.StartTime.Seconds);
                    DateTime dowConstraintEndTime = dowConstraintStartTime.AddHours(dayOfWeekConstraint.BlackoutHours).AddMinutes(dayOfWeekConstraint.BlackoutMinutes);
                    if (requestedTimeForRestaurant.Ticks >= dowConstraintStartTime.Ticks && requestedTimeForRestaurant.Ticks < dowConstraintEndTime.Ticks)
                    {
                        return true;
                    }
                }
                else if (requestedTimeForRestaurant.AddDays(-1).DayOfWeek == theDay)
                {
                    DateTime dayBefore = requestedTimeForRestaurant.AddDays(-1);
                    DateTime dowConstraintStartTime = new DateTime(dayBefore.Year, dayBefore.Month, dayBefore.Day, this.StartTime.Hours, this.StartTime.Minutes, this.StartTime.Seconds);
                    DateTime dowConstraintEndTime = dowConstraintStartTime.AddHours(dayOfWeekConstraint.BlackoutHours).AddMinutes(dayOfWeekConstraint.BlackoutMinutes);
                    if (requestedTimeForRestaurant.Ticks >= dowConstraintStartTime.Ticks && requestedTimeForRestaurant.Ticks < dowConstraintEndTime.Ticks)
                    {
                        return true;
                    }
                }
                return false;

            }
            else
            {
                return (resDateTime.TimeOfDay.Ticks >= StartTime.Ticks) && (resDateTime.TimeOfDay.Ticks <= EndTime.Ticks);
            }
        }

    }
}

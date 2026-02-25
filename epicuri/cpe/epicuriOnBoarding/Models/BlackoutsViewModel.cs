using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuriOnBoarding.Models
{
    public class BlackoutsViewModel
    {
        List<epicuri.Core.DatabaseModel.DateConstraint> Constraints;
        public int RestaurantId;
        public BlackoutsViewModel(int id, List<epicuri.Core.DatabaseModel.DateConstraint> constraints)
        {
            RestaurantId = id;
            Constraints = constraints;
        }

        public List<epicuri.Core.DatabaseModel.AbsoluteDateConstraint> AbsoluteTakeaway
        {
            get
            {
                return Constraints.OfType<epicuri.Core.DatabaseModel.AbsoluteDateConstraint>().Where(d => d.TargetSession).ToList();
            }
        }

        public List<epicuri.Core.DatabaseModel.AbsoluteDateConstraint> AbsoluteReservation
        {
            get
            {
                return Constraints.OfType<epicuri.Core.DatabaseModel.AbsoluteDateConstraint>().Where(d => !d.TargetSession).ToList();
            }
        }

        public List<epicuri.Core.DatabaseModel.DayOfWeekConstraint> DayOfWeekTakeaway
        {
            get
            {
                return Constraints.OfType<epicuri.Core.DatabaseModel.DayOfWeekConstraint>().Where(d => d.TargetSession).ToList();
            }
        }

        public List<epicuri.Core.DatabaseModel.DayOfWeekConstraint> DayOfWeekReservation
        {
            get
            {
                return Constraints.OfType<epicuri.Core.DatabaseModel.DayOfWeekConstraint>().Where(d => !d.TargetSession).ToList();
            }
        }

        public List<epicuri.Core.DatabaseModel.DayOfYearConstraint> DayOfYearTakeaway
        {
            get
            {
                return Constraints.OfType<epicuri.Core.DatabaseModel.DayOfYearConstraint>().Where(d => d.TargetSession).ToList();
            }
        }

        public List<epicuri.Core.DatabaseModel.DayOfYearConstraint> DayOfYearReservation
        {
            get
            {
                return Constraints.OfType<epicuri.Core.DatabaseModel.DayOfYearConstraint>().Where(d => !d.TargetSession).ToList();
            }
        }

    }
}
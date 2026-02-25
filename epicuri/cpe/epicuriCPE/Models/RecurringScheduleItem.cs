using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class RecurringScheduleItem 
    {
        public int Id;
        public int ServiceId;
        public int InitialDelay;
        public int Period;
        public IEnumerable<Notification> Notifications;

        public RecurringScheduleItem() { }

        public RecurringScheduleItem(Core.DatabaseModel.RecurringScheduleItem recurringScheduleItem)
        {
            Id = recurringScheduleItem.Id;
            ServiceId = recurringScheduleItem.ServiceId;
            InitialDelay = recurringScheduleItem.InitialDelay;
            Notifications = from notify in recurringScheduleItem.Notifications
                            select new Models.Notification(notify);
            Period = recurringScheduleItem.Period;
        }

        public RecurringScheduleItem(Core.DatabaseModel.RecurringScheduleItem recurringScheduleItem, Core.DatabaseModel.SeatedSession seatedSession)
        {
            Id = recurringScheduleItem.Id;
            ServiceId = recurringScheduleItem.ServiceId;
            InitialDelay = recurringScheduleItem.InitialDelay;
            Notifications = from notify in recurringScheduleItem.Notifications
                            select new Models.Notification(notify,seatedSession);
            
            Period = recurringScheduleItem.Period;
        }

        public Core.DatabaseModel.RecurringScheduleItem ToRecurringScheduleItem()
        {
            return new Core.DatabaseModel.RecurringScheduleItem
            {
                ServiceId = ServiceId,
                InitialDelay = Convert.ToInt16(InitialDelay),
                Period = Convert.ToInt16(Period),
                Comment = ""
                
            };
        }
    }
}
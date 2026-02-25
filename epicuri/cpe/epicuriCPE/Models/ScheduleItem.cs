using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class ScheduleItem
    {
        public int Delay;
        public int Order;
        public IEnumerable<Notification> Notifications;
        public int ServiceId;
        public int Id;

        public ScheduleItem() { }

        public ScheduleItem(Core.DatabaseModel.ScheduleItem scheduleItem, Core.DatabaseModel.SeatedSession sess)
        {
            Id = scheduleItem.Id;
            ServiceId = scheduleItem.ServiceId;
            Delay = scheduleItem.Delay;
            //EP-342
            //Order = scheduleItem.Order;
            Notifications = from notify in scheduleItem.Notifications
                            select new Models.Notification(notify, sess);
        }

        public ScheduleItem(Core.DatabaseModel.ScheduleItem scheduleItem)
        {
            Id = scheduleItem.Id;
            ServiceId = scheduleItem.ServiceId;
            Delay = scheduleItem.Delay;
            //EP-342
            //Order = scheduleItem.Order;
            Notifications = from notify in scheduleItem.Notifications
                            select new Models.Notification(notify);
        }


        public Core.DatabaseModel.ScheduleItem ToScheduleItem()
        {
            return new Core.DatabaseModel.ScheduleItem
            {
                ServiceId = this.ServiceId,
                Delay = this.Delay,
                //EP-342
                //Order = Convert.ToInt16(this.Order),
                Comment = ""
            };
        }

    }
}
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class Notification
    {
        public int Id;
        public string Text;
        public string Target;

        public IEnumerable<Acknowledgement> Acknowledgements;
        

        public Notification(Core.DatabaseModel.Notification notify, Core.DatabaseModel.Session sess)
        {
            Id = notify.Id;
            Target = notify.Target;
            Text = notify.Text;
            Acknowledgements = from ack in sess.NotificationAcks
                               where ack.NotificationId == notify.Id
                               select new Models.Acknowledgement(ack);
        }

        public Notification(Core.DatabaseModel.Notification notify)
        {
            Id = notify.Id;
            Target = notify.Target;
            Text = notify.Text;
        }

        public Notification()
        {
        }

        public Core.DatabaseModel.Notification ToNotification()
        {
            return new Core.DatabaseModel.Notification
            {
                Text = this.Text,
                Target = this.Target
            };
        }
    }
}
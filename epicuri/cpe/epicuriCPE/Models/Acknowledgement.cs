using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    public class Acknowledgement
    {
        public int Id;
        public double Time;


        public Acknowledgement(Core.DatabaseModel.NotificationAck ack)
        {
            Id = ack.Id;
            Time = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(ack.Time);

        }

        public Acknowledgement(Core.DatabaseModel.AdhocNotificationAck ack)
        {
            Id = ack.Id;
            Time = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(ack.Time);
        }

        [Required]
        public int Notification { get; set; }
    }
}
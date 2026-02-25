using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class AdhocNotification
    {
        public int Id;
        public IEnumerable<Acknowledgement> Acknowledgements;
        public string Text;
        public string Target;
        public Double Created;


        public AdhocNotification(Core.DatabaseModel.AdhocNotification adhoc)
        {
            Id = adhoc.Id;
            Text = adhoc.Text;
            Target = adhoc.Target;
            Created = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(adhoc.Created);
            Acknowledgements =  from ack in adhoc.AdhocNotificationAcks
                                select new Models.Acknowledgement(ack);
        }
    }
}
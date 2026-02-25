using epicuri.Core.DatabaseModel;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class Batch
    {
        public int Id;
        public string Identifier;
        public double Time;
        public List<string> Tables;
        //public IEnumerable<IEnumerable<Order>> Orders;
        public IEnumerable<Order> Orders;
        public bool Modify;
        public int PrinterId;

        public string BatchType;
        public double DueDate;
        public string ServerName;
        public bool IsSelfService;
        public int Covers;

        public string OrderName;
        public string Notes;

        public double SpoolTime;

        public string Type;

        public Batch(Core.DatabaseModel.Batch batch)
        {
            Id = batch.Id;
            PrinterId = batch.PrinterId;
            Modify = batch.Modify;
            Identifier = batch.Ident;
            Time = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(batch.OrderTime);

            if (batch.SpoolTime != null)
            {
                DateTime spoolTime = batch.SpoolTime.Value;
                SpoolTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(spoolTime);
            }

            Tables = new List<string>();
            if (batch.Orders.FirstOrDefault() != null)
            {
                if (batch.Orders.First().Session.GetType() == typeof(Core.DatabaseModel.SeatedSession))
                {
                    var seatedSess = (Core.DatabaseModel.SeatedSession)batch.Orders.First().Session;

                    if (seatedSess.Tables.Count != 0)
                    {
                        foreach (var table in seatedSess.Tables)
                        {
                            Tables.Add(table.Name);
                        }
                    }
                }
            }

            //Orders = (from order in batch.Orders
            //          select new Models.Order(order)).GroupBy(o => o.Course.Id);
            Orders = from o in batch.Orders
                     select new Models.Order(o);

        }
    }
}
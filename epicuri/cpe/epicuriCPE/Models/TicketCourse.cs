using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
//using System.ComponentModel.DataAnnotations.Schema;
//using System.Data.Objects;
using epicuri.Core.DatabaseModel;
using epicuri.Core;

namespace epicuri.CPE.Models
{
    public class TicketCourse : DataModel
    {


        public String Name { get; private set; }
        public IEnumerable<Models.AggregateOrder> Orders { get; private set; }
        public Boolean AllComplete { get; private set; }

        public TicketCourse(String course, IEnumerable<Models.Order> orders)
        {
            Init();

            Name = course;


            Orders = orders.OrderBy(o=>o.Completed).GroupBy(o => o.MenuItem.Id).Select(o => new AggregateOrder(o.First().MenuItem,o.ToList()));

            AllComplete = orders.All(o => o.Completed > 0);

        
        }

        public TicketCourse(String course, IEnumerable<Models.Order> orders, bool p_2) : this("",orders)
        {

        }
    }

    
}
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace epicuri.CPE.Models
{
    public class OrderConfiguration
    {
        public int Count { get; private set; }
        public String Note { get; private set; }
        public List<String> Options {get; private set;}
        public List<int> OrderIds { get; private set; }

        public Boolean IsComplete { get; private set; }
        public OrderConfiguration(int order, int count, String note, Boolean isComplete, List<String> options)
        {
            OrderIds = new List<int>();
            this.Note = note;
            this.Count = count;
            this.Options = options;
            this.OrderIds.Add(order);
            this.IsComplete = isComplete;
        }



        public void IncrementCount()
        {
            this.Count++;
        }

        public void AddOrder(int order)
        {
            this.OrderIds.Add(order);
        }
    }
}

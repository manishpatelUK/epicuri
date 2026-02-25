using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class AggregateOrder
    {

        public MenuItem MenuItem { get; private set; }

        public List<OrderConfiguration> Configurations { get; private set; }
        public List<OrderConfiguration> CompleteConfigurations { get; private set; }
        private Dictionary<List<int>, OrderConfiguration> Modifiers;
        private Dictionary<List<int>, OrderConfiguration> CompleteModifiers;
        public AggregateOrder(MenuItem item, IEnumerable<Models.Order> order)
        {
            this.MenuItem = item;
            int regularCount = 0;
            int completeCount = 0;
            OrderConfiguration regularConfig = null;
            OrderConfiguration completeConfig = null;

            Configurations = new List<OrderConfiguration>();
            CompleteConfigurations = new List<OrderConfiguration>();

            Modifiers = new Dictionary<List<int>,OrderConfiguration>();
            CompleteModifiers = new Dictionary<List<int>, OrderConfiguration>();

            foreach (Order o in order)
            {
                Boolean isComplete=o.Completed != 0;
                if (o.Modifiers.Count() > 0 || !String.IsNullOrWhiteSpace(o.Note))
                {
                    if (!String.IsNullOrWhiteSpace(o.Note))
                    {
                        if (isComplete)
                        {
                            CompleteConfigurations.Add(new OrderConfiguration(o.Id, 1, o.Note, isComplete, o.Modifiers.Select(m => m.ModifierValue).ToList()));
                        }
                        else
                        {
                            Configurations.Add(new OrderConfiguration(o.Id, 1, o.Note, isComplete, o.Modifiers.Select(m => m.ModifierValue).ToList()));
                        }
                    }
                    else
                    {
                        List<int> modifiers = o.Modifiers.Select(m=>m.Id).OrderBy(a=>a).ToList();
                        KeyValuePair<List<int>, OrderConfiguration> cachedConfig = new KeyValuePair<List<int>, OrderConfiguration>();
                        
                        if(isComplete) 
                        {
                            
                            cachedConfig = this.CompleteModifiers.FirstOrDefault(r => Enumerable.SequenceEqual(r.Key, modifiers));
                        } 
                        else 
                        {
                            
                            cachedConfig = this.Modifiers.FirstOrDefault(r => Enumerable.SequenceEqual(r.Key, modifiers));
                        }
                        if(cachedConfig.Value!=null) 
                        {
                            if (isComplete)
                            {
                                CompleteModifiers[cachedConfig.Key].IncrementCount();
                                CompleteModifiers[cachedConfig.Key].AddOrder(o.Id);
                            }
                            else
                            {
                                Modifiers[cachedConfig.Key].IncrementCount();
                                Modifiers[cachedConfig.Key].AddOrder(o.Id);
                            }
                        } 
                        else 
                        {
                            if (isComplete)
                            {
                                CompleteModifiers.Add(modifiers, new OrderConfiguration(o.Id, 1, o.Note, isComplete, o.Modifiers.Select(m => m.ModifierValue).ToList()));
                            }
                            else
                            {
                                Modifiers.Add(modifiers, new OrderConfiguration(o.Id, 1, o.Note, isComplete, o.Modifiers.Select(m => m.ModifierValue).ToList()));
                            }
                        }
                        
                    }
                }
                else 
                {
                    if (isComplete)
                    {
                        if (completeConfig == null)
                        {
                            completeConfig = new OrderConfiguration(o.Id, 1, "", isComplete, new List<String>());
                        }
                        else
                        {
                            completeConfig.IncrementCount();
                            completeConfig.AddOrder(o.Id);
                        }
                        completeCount++;
                    }
                    else
                    {
                        if (regularConfig == null)
                        {
                            regularConfig = new OrderConfiguration(o.Id, 1, "", isComplete, new List<String>());
                        }
                        else
                        {
                            regularConfig.IncrementCount();
                            regularConfig.AddOrder(o.Id);
                        }

                        regularCount++;
                    }
                  
                }
            }

           

            if (regularCount > 0)
            {
                Configurations.Add(regularConfig);
            }

            foreach (var m in Modifiers)
            {
                Configurations.Add(m.Value);
            }

            if (completeCount > 0)
            {
                CompleteConfigurations.Add(completeConfig);
            }

            foreach (var m in CompleteModifiers)
            {
                CompleteConfigurations.Add(m.Value);
            }
        }
    }
}
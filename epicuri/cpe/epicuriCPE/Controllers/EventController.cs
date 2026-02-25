using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using System.Net.Http;
using System.Net;

namespace epicuri.CPE.Controllers
{
    public class EventController : Models.EpicuriApiController
    {
        public HttpResponseMessage GetEvents()
        {
            try
            {
                Authenticate();
            }
            catch (Exception e)
            {
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, e);
                response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                return response;
            }


            
            List<Models.Event> events = new List<Models.Event>();

            IEnumerable<Models.Session> sessions = from sess in this.Restaurant.Sessions.OfType<epicuri.Core.DatabaseModel.SeatedSession>()
                                                   where sess.ClosedTime == null && sess.Tables.Count() != 0
                                                   select new Models.SeatedSession(sess);

            var takeaways = from sess in this.Restaurant.Sessions.OfType<epicuri.Core.DatabaseModel.TakeAwaySession>()
                            where sess.ClosedTime == null
                            select new Models.TakeawaySession(sess);

            List<Models.Session> outp = new List<Models.Session>();
            foreach (Models.Session s in sessions)
            {
               
                foreach(Models.ScheduleItem i in s.ScheduleItems.OrderBy(t=>t.Order))
                {
                    
                    foreach (Models.Notification n in i.Notifications)
                    {
                        if (n.Acknowledgements.ToList().Count == 0)
                        {
                            events.Add(new Models.Event
                                {
                                    Id = n.Id,
                                    Due = s.StartTime + i.Delay + ((CPE.Models.SeatedSession)s).Delay,
                                    Delay = ((CPE.Models.SeatedSession)s).Delay,
                                    Text = n.Text,
                                    Target = n.Target,
                                    Session = s.Id,
                                    Type = "Notification"
                                });
                           
                        }
                        
                    }

                   
                    
                }

                foreach (Models.RecurringScheduleItem r in s.RecurringScheduleItems)
                {
                    


                    foreach (Models.Notification n in r.Notifications)
                    {
                        //double basetime = s.StartTime + r.InitialDelay + ((CPE.Models.SeatedSession)s).Delay;
                        
                        double basetime = 0;

                        foreach (Models.Acknowledgement a in n.Acknowledgements)
                        {
                            if (a.Time + r.Period > basetime)
                            {
                                basetime = a.Time + r.Period;

                            }

                        }
                        if(basetime == 0){
                            basetime = s.StartTime + r.InitialDelay;
                        }

                        //if (basetime < epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(DateTime.UtcNow))
                        //{
                            events.Add(new Models.Event
                            {
                                Due = basetime,
                                Id = n.Id,
                                Target = n.Target,
                                Delay = ((CPE.Models.SeatedSession)s).Delay,
                                Text = n.Text,
                                Session = s.Id,
                                Type = "Recurring"
                            });
                        //}
                    }
                }

                foreach (Models.AdhocNotification ad in s.AdhocNotifications)
                {
                    if (ad.Acknowledgements.ToList().Count == 0)
                    {
                        events.Add(new Models.Event
                        {
                            Due = ad.Created,
                            Id = ad.Id,
                            Target = ad.Target,
                            Text = ad.Text,
                            Delay = ((CPE.Models.SeatedSession)s).Delay,
                            Session = s.Id,
                            Type = "Adhoc"
                        });
                    }
                }
            }


            foreach (Models.TakeawaySession ts in takeaways)
            {
                foreach (Models.AdhocNotification ad in ts.AdhocNotifications)
                {
                    if (ad.Acknowledgements.ToList().Count == 0)
                    {
                        events.Add(new Models.Event
                        {
                            Due = ad.Created,
                            Id = ts.Id,
                            Session = ts.Id,
                            Delay = 0,
                            Target = ts.AdhocNotifications.First().Target,
                            Text = ts.AdhocNotifications.First().Text,
                            Type = "Adhoc"
                        });
                    }
                }
            }

            
            
            return Request.CreateResponse(HttpStatusCode.OK, 
                events.Where(e=>
                   e.Due < Core.Utils.Time.DateTimeToUnixTimestamp(DateTime.UtcNow.AddSeconds(Core.Settings.Setting<int>(Restaurant.Id, "ActionTimeWindow")))
                || e.Due > Core.Utils.Time.DateTimeToUnixTimestamp(DateTime.UtcNow)).OrderBy(e=>e.Due)); 
          
        }


        
    }
}

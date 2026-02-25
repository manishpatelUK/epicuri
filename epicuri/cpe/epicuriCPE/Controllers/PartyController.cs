using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.Core.DatabaseModel;
using epicuri.Core;


namespace epicuri.CPE.Controllers
{
    public class PartyController : Models.EpicuriApiController
    {
        public IEnumerable<Models.Party> GetAll()
        {
            Authenticate();
            var Parties = from w in db.Parties
                          orderby w.CreatedTime ascending
                          where w.RestaurantId == this.Restaurant.Id
                          && !w.Deleted
                          && (w.Session == null ? true : !w.Session.IsAdHoc)
                          && (w.Session == null ? true : (w.Session.ClosedTime == null && w.Session.Tables.Count() == 0))
                          select w;
            List<Models.Party> output = new List<Models.Party>();
            
            foreach (Party p in Parties)
            {
                // Add in honoured reservations

                

                if (p.GetType() == typeof(epicuri.Core.DatabaseModel.WaitingList)) // AKA Walkin
                {
                    

                    if (p.Session != null || p.CreatedTime.AddMinutes(Settings.Setting<double>(this.Restaurant.Id, "WalkinExpirationTime")) > DateTime.UtcNow)
                    {
                        if (p.LeadCustomer != null)
                        {
                            CPE.Models.Customer leadCustomer = new CPE.Models.Customer(db.Customers.FirstOrDefault(lc => lc.Id == p.LeadCustomer.Id));
                            output.Add(new Models.WaitingParty { Created = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(p.CreatedTime), Id = p.Id, Name = p.Name, NumberOfPeople = p.NumberOfPeople, SessionId = p.Session == null ? 0 : p.Session.Id, LeadCustomer = leadCustomer });
                        }
                        else
                        {
                            output.Add(new Models.WaitingParty { Created = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(p.CreatedTime), Id = p.Id, Name = p.Name, NumberOfPeople = p.NumberOfPeople, SessionId = p.Session == null ? 0 : p.Session.Id });
                        }
                    }
                 }
                else if (p.GetType() == typeof(epicuri.Core.DatabaseModel.Reservation) && p.ArrivedTime.HasValue)
                {
                    Models.Reservation res = new Models.Reservation((Reservation)p);
                    if (!res.Rejected)
                    {

                        if (p.Session == null)
                        {
                            if (p.ArrivedTime.HasValue && ((DateTime)p.ArrivedTime).AddMinutes(Settings.Setting<double>(this.Restaurant.Id, "WalkinExpirationTime")) > DateTime.UtcNow)
                            {
                                output.Add(res);
                            }
                        }
                        else
                        {
                            output.Add(res);
                        }
                     
                    }
                }
            }

            return  output;

        }

        

    

    }

}
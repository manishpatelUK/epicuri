using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using epicuri.Core;
using epicuri.Core.DatabaseModel;

namespace epicuri.CPE.Controllers
{
    public class ArchiveController : Models.EpicuriApiController
    {
        public HttpResponseMessage Get()
        {
            try
            {
                Authenticate();
            }
            catch (Exception e)
            {
                return Request.CreateResponse(HttpStatusCode.Unauthorized, e);
            }

            return Request.CreateResponse(HttpStatusCode.OK, db.Archives.Select(a => new { Id = a.Id, Verb = a.Verb, Controller = a.Controller, Action = a.Action, DateTime = a.DateTime, IP = a.IP, 
                StaffId = a.StaffId, RestaurantId = a.RestaurantId, LocalIP = a.LocalIP, SSID = a.SSID, DeviceId = a.DeviceId}));
        }

    }
}

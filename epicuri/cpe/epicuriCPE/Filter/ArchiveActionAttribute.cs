using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Web;
using System.Web.Http;
using System.Web.Http.Filters;
using System.Web.Http.Controllers;
using System.Net.Http;
using System.Configuration;
using epicuri.Core.DatabaseModel;
using epicuri.CPE.Models;
using System.Diagnostics;
using System.Data.Entity.Validation;
using System.Data.Entity.Infrastructure;
using System.ComponentModel;

namespace epicuri.CPE.Filter
{
    public class ArchiveActionAttribute : ActionFilterAttribute
    {
        private epicuriContainer db = new epicuriContainer();

        public override void OnActionExecuting(HttpActionContext actionContext)
        {

            // Logs PUT, POST or DELETE http calls to an Archive Table

            string methodType = actionContext.Request.Method.Method;

            if (methodType.ToUpper().Equals("POST") || methodType.ToUpper().Equals("PUT") || methodType.ToUpper().Equals("DELETE"))
            {
                IDictionary<string, object> actConProp = actionContext.Request.Properties;
                string controller = actionContext.ActionDescriptor.ControllerDescriptor.ControllerName;
                string action = actionContext.ActionDescriptor.ActionName;
                string userIpAddress = ((HttpContextWrapper)actionContext.Request.Properties["MS_HttpContext"]).Request.UserHostName;

                epicuri.Core.DatabaseModel.Staff staffMember;
                epicuri.Core.DatabaseModel.Restaurant restaurant;
                Archive log;

                try
                {
                    string AuthKey = actionContext.Request.Headers.Authorization.Parameter;
                    int AuthId = int.Parse(AuthKey.Split('-')[0]);
                    AuthKey = AuthKey.Split('-')[1];
                    StaffAuthenticationKey k = db.StaffAuthenticationKeys.Where(key => key.Id == AuthId).FirstOrDefault();
                    staffMember = k.Staff;
                    restaurant = k.Restaurant;

                    log = new Archive()
                    {
                        Verb = methodType,
                        Controller = controller,
                        Action = action,
                        DateTime = DateTime.Now,
                        IP = userIpAddress,
                        StaffId = staffMember.Id,
                        RestaurantId = restaurant.Id

                    };
                }

                catch
                {
                    staffMember = null;
                    restaurant = null;

                    log = new Archive()
                    {
                        Verb = methodType,
                        Controller = controller,
                        Action = action,
                        DateTime = DateTime.Now,
                        IP = userIpAddress
                    };
                }

                // Get LocalIP, SSID and MAC Address

                string ip = null;
                string ssid = null;
                string mac = null;

                try
                {
                    ip = actionContext.Request.Headers.GetValues("X-Epicuri-IP").FirstOrDefault();
                    ssid = actionContext.Request.Headers.GetValues("X-Epicuri-SSID").FirstOrDefault();
                    mac = actionContext.Request.Headers.GetValues("X-Epicuri-MAC").FirstOrDefault();

                    log.LocalIP = ip;
                    log.SSID = ssid;
                    log.DeviceId = mac;
                }

                catch
                {
                    Console.WriteLine("Headers invalid for Archive MAC/IP/SSID."); 
                }

                // Get Web Post Content (e.g. JSON)

                var content = actionContext.Response;

                db.Archives.AddObject(log);
                db.SaveChanges();
            }

            base.OnActionExecuting(actionContext);

        }

    }
}

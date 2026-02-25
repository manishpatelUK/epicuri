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

namespace epicuri.CPE.Filter
{
    public class ResponseVersionAttribute : ActionFilterAttribute
    {
        private static string minimumCompatable = ConfigurationManager.AppSettings["supportedVersion"];

        public override void OnActionExecuting(System.Web.Http.Controllers.HttpActionContext actionContext)
        {
            if (actionContext.Request.Headers.Contains("X-Epicuri-API-Version"))
            {
                var version = actionContext.Request.Headers.GetValues("X-Epicuri-API-Version").First();

                var min = Int32.Parse(minimumCompatable);
                var req = Int32.Parse(version);

                if (req<min)
                {
                    actionContext.Response = actionContext.Request.CreateErrorResponse(System.Net.HttpStatusCode.NotAcceptable, new Exception("Minimum supported API version is " + minimumCompatable));
                    actionContext.Response.Headers.Add("X-Epicuri-API-Version", ResponseVersionAttribute.CurrentVersion);
                }
            } else {
                actionContext.Response = actionContext.Request.CreateErrorResponse(System.Net.HttpStatusCode.NotAcceptable,new Exception("Minimum supported API version is "+minimumCompatable));
                actionContext.Response.Headers.Add("X-Epicuri-API-Version", ResponseVersionAttribute.CurrentVersion);
            }
            base.OnActionExecuting(actionContext);
        }
        public override void OnActionExecuted(HttpActionExecutedContext filterContext)
        {
            if (filterContext.Response != null && filterContext.Response.Headers != null) { 
                filterContext.Response.Headers.Add("X-Epicuri-API-Version", ResponseVersionAttribute.CurrentVersion);
            }
            base.OnActionExecuted(filterContext);
        }


        public static string CurrentVersion = ConfigurationManager.AppSettings["CurrentVersion"];
            

    }
}
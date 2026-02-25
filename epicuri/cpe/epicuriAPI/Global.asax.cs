using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Http;
using System.Web.Mvc;
using System.Web.Routing;
using System.Web.Http.Validation.Providers;
using epicuri.CPE.Filter;

namespace epicuriAPI
{
    // Note: For instructions on enabling IIS6 or IIS7 classic mode, 
    // visit http://go.microsoft.com/?LinkId=9394801
    public class MvcApplication : System.Web.HttpApplication
    {
        protected void Application_Start()
        {
            AreaRegistration.RegisterAllAreas();

            epicuri.API.WebApiConfig.Register(GlobalConfiguration.Configuration);
            epicuri.API.FilterConfig.RegisterGlobalFilters(GlobalFilters.Filters);
            epicuri.API.RouteConfig.RegisterRoutes(RouteTable.Routes);

            GlobalConfiguration.Configuration.Formatters.XmlFormatter.SupportedMediaTypes.Clear();
            GlobalConfiguration.Configuration.Services.RemoveAll(
            typeof(System.Web.Http.Validation.ModelValidatorProvider),
            v => v is InvalidModelValidatorProvider);


            GlobalConfiguration.Configuration.Filters.Add(new ResponseVersionAttribute());
            //GlobalConfiguration.Configuration.Filters.Add(new DeviceMonitorAttribute());
            //GlobalConfiguration.Configuration.Filters.Add(new ArchiveActionAttribute());
        }
    }
}
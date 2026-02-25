using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Http;
using System.Web.Mvc;
using System.Web.Optimization;
using System.Web.Routing;
using System.Web.Http.Validation.Providers;
using Newtonsoft.Json;
using epicuri.CPE.Filter;

namespace epicuri.CPE
{
    // Note: For instructions on enabling IIS6 or IIS7 classic mode, 
    // visit http://go.microsoft.com/?LinkId=9394801

    public class WebApiApplication : System.Web.HttpApplication
    {
        protected void Application_Start()
        {
            AreaRegistration.RegisterAllAreas();
            
            WebApiConfig.Register(GlobalConfiguration.Configuration);
            FilterConfig.RegisterGlobalFilters(GlobalFilters.Filters);
            RouteConfig.RegisterRoutes(RouteTable.Routes);
            BundleConfig.RegisterBundles(BundleTable.Bundles);
           
            GlobalConfiguration.Configuration.Formatters.JsonFormatter.SerializerSettings.PreserveReferencesHandling = PreserveReferencesHandling.None;
            GlobalConfiguration.Configuration.Formatters.XmlFormatter.SupportedMediaTypes.Clear();
            GlobalConfiguration.Configuration.Services.RemoveAll(
            typeof(System.Web.Http.Validation.ModelValidatorProvider),
            v => v is InvalidModelValidatorProvider);

            GlobalConfiguration.Configuration.Filters.Add(new ResponseVersionAttribute());
            GlobalConfiguration.Configuration.Filters.Add(new DeviceMonitorAttribute());
            GlobalConfiguration.Configuration.Filters.Add(new ArchiveActionAttribute());
        }
    }
}
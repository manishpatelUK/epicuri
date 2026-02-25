using System;
using System.Collections.Generic;
using System.Linq;
using System.Web.Http;

namespace epicuri.API
{
    public static class WebApiConfig
    {
        public static void Register(HttpConfiguration config)
        {
            config.Routes.MapHttpRoute(
                name: "Login",
                routeTemplate: "api/Authentication/Login",
                defaults: new {controller="Authentication", action="Login"}
            );

            config.Routes.MapHttpRoute(
                name: "Register",
                routeTemplate: "api/Authentication/Register",
                defaults: new { controller = "Authentication", action = "Register" }
            );

            config.Routes.MapHttpRoute(
                name: "auth",
                routeTemplate: "api/Authentication/Authorize",
                defaults: new { controller = "Authentication", action = "Authorize" }
            );


            config.Routes.MapHttpRoute(
                name: "ns",
                routeTemplate: "api/Order/Takeaway/{id}",
                defaults: new { controller = "Order", action = "Takeaway", id=RouteParameter.Optional }



            );

            config.Routes.MapHttpRoute(
                name: "ts",
                routeTemplate: "api/Session",
                defaults: new { controller = "Session", action = "Sessions" }


            );

            config.Routes.MapHttpRoute(
                name: "sc",
                routeTemplate: "api/Session/ServiceRequest/{id}",
                defaults: new { controller = "Session", action = "ServiceRequest" }


            );

            config.Routes.MapHttpRoute(
                name: "bc",
                routeTemplate: "api/Session/BillRequest/{id}",
                defaults: new { controller = "Session", action = "BillRequest" }


            );

            config.Routes.MapHttpRoute(
                name: "so",
                routeTemplate: "api/Session/Order/{id}",
                defaults: new { controller = "Session", action = "Order" }


            );

            config.Routes.MapHttpRoute(
                name: "ot",
                routeTemplate: "api/Order/Takeaway/{id}",
                defaults: new { controller = "Order", action = "Takeaway" }


            );

            config.Routes.MapHttpRoute(
                name: "TakeawayCheck",
                routeTemplate: "api/Order/TakeawayCheck/{id}",
                defaults: new { controller = "Order", action = "TakeawayCheck" }


            );

            config.Routes.MapHttpRoute(
                name: "TakeawayMenu",
                routeTemplate: "api/TakeawayMenu/{id}",
                defaults: new { controller = "Menu", action = "TakeawayMenu" }
                );

            config.Routes.MapHttpRoute(
               name: "restaurantMenus",
               routeTemplate: "api/Menu/RestaurantMenus/{id}",
               defaults: new { controller = "Menu", action = "RestaurantMenus" }
           );

            config.Routes.MapHttpRoute(
                name: "User",
                routeTemplate: "api/User/User/{id}",
                defaults: new { controller = "epicuri.API.Controllers.User", action = "User" }
            );

            config.Routes.MapHttpRoute(
                name: "UserAuth",
                routeTemplate: "api/User/UserAuth/{id}",
                defaults: new { controller = "epicuri.API.Controllers.User", action = "UserAuth" }
            );

            config.Routes.MapHttpRoute(
                "DefaultApi",
                "api/{controller}/{id}",
                new { id = RouteParameter.Optional, controller = "Home" }
            ); 
        }
    }
}

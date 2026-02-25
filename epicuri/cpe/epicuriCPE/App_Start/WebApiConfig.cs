using System;
using System.Collections.Generic;
using System.Linq;
using System.Web.Http;
using Newtonsoft.Json;
using System.Web.Http.Tracing;

namespace epicuri.CPE
{
    public static class WebApiConfig
    {

        public static void Register(HttpConfiguration config)
        {

            SystemDiagnosticsTraceWriter traceWriter = config.EnableSystemDiagnosticsTracing();
            traceWriter.IsVerbose = false;
            traceWriter.MinimumLevel = TraceLevel.Debug;



            //config.Routes.MapHttpRoute(
            //    name: "Menus",
            //    routeTemplate: "api/Menu",
            //    defaults: new { controller = "Menu", action = "Menus" });

            //config.Routes.MapHttpRoute(
            //    name: "Menu",
            //    routeTemplate: "api/Menu/{id}",
            //    defaults: new { controller = "Menu", action = "Menu" });

            config.Routes.MapHttpRoute(
               name: "PostWaitingWithOrder",
               routeTemplate: "api/Waiting/PostWaitingWithOrder/",
               defaults: new { controller = "Waiting", action = "PostWaitingWithOrder" });

            config.Routes.MapHttpRoute(
               name: "PostWaitingWithoutOrder",
               routeTemplate: "api/Waiting/",
               defaults: new { controller = "Waiting", action = "PostWaitingWithoutOrder"});

            config.Routes.MapHttpRoute(
               name: "ConvertAdHocToTab",
               routeTemplate: "api/Session/ConvertAdHocToTab",
               defaults: new { controller = "Session", action = "ConvertAdHocToTab" });


            config.Routes.MapHttpRoute(
             name: "CashUp",
             routeTemplate: "api/CashUp/CheckStatus",
             defaults: new { controller = "CashUp", action = "IsOkToCashup" });

            config.Routes.MapHttpRoute(
                name: "CashUpSim",
                routeTemplate: "api/CashUp/Simulate",
                defaults: new { controller = "CashUp", action = "Simulate" });

            config.Routes.MapHttpRoute(
                name: "CashUpTime",
                routeTemplate: "api/CashUp/Time",
                defaults: new { controller = "CashUp", action = "Time" });

            config.Routes.MapHttpRoute(
                name: "CashUpSess",
                routeTemplate: "api/CashUp/Sessions",
                defaults: new { controller = "CashUp", action = "Sessions" });

            config.Routes.MapHttpRoute(
              name: "CashUpIdx",
              routeTemplate: "api/CashUp",
              defaults: new { controller = "CashUp", action = "Index" });

            config.Routes.MapHttpRoute(
                name: "CashUpView",
                routeTemplate: "api/CashUp/{id}",
                defaults: new { controller = "CashUp", action = "View", id = RouteParameter.Optional });

         


            config.Routes.MapHttpRoute(
                name: "AllMenus",
                routeTemplate: "api/Menu/All",
                defaults: new { controller = "Menu", action = "AllMenus" });
            config.Routes.MapHttpRoute(
                name: "FromParty",
                routeTemplate: "api/Session/FromParty/{id}",
                defaults: new { controller = "Session", action = "FromParty", id = RouteParameter.Optional });

            config.Routes.MapHttpRoute(
                name: "Takeaway",
                routeTemplate: "api/Session/Takeaway",
                defaults: new { controller = "Session", action = "Takeaway" });

            config.Routes.MapHttpRoute(
                name: "SessionOpen",
                routeTemplate: "api/Session/Open/{id}",
                defaults: new { controller = "Session", action = "Open" });

            config.Routes.MapHttpRoute(
                name: "SessionVoid",
                routeTemplate: "api/Session/Void/{id}",
                defaults: new { controller = "Session", action = "Void" });

            config.Routes.MapHttpRoute(
                name: "SessionUnvoid",
                routeTemplate: "api/Session/Unvoid/{id}",
                defaults: new { controller = "Session", action = "Unvoid" });

            config.Routes.MapHttpRoute(
                name: "Menus",
                routeTemplate: "api/Menu",
                defaults: new { controller = "Menu", action = "Menus" });

            config.Routes.MapHttpRoute(
                name: "Menu",
                routeTemplate: "api/Menu/{id}",
                defaults: new { controller = "Menu", action = "Menu" });


            config.Routes.MapHttpRoute(
               name: "ChangeTakeawayMenu",
               routeTemplate: "api/Menu/ChangeTakeawayMenu/{id}",
               defaults: new { controller = "Menu", action = "ChangeTakeawayMenu" });

            config.Routes.MapHttpRoute(
               name: "DisassociateCheckIn",
               routeTemplate: "api/Diner/DisassociateCheckIn/{id}",
               defaults: new { controller = "Diner", action = "DisassociateCheckIn" });

            config.Routes.MapHttpRoute(
               name: "DeleteDiner",
               routeTemplate: "api/Diner/DeleteDiner/{id}",
               defaults: new { controller = "Diner", action = "DeleteDiner" });

            config.Routes.MapHttpRoute(
               name: "Redirect",
               routeTemplate: "api/Printer/Redirect/{id}",
               defaults: new { controller = "Printer", action = "Redirect" });

            config.Routes.MapHttpRoute(
               name: "RedirectedPrinters",
               routeTemplate: "api/Printer/RedirectedPrinters",
               defaults: new { controller = "Printer", action = "RedirectedPrinters" });

            config.Routes.MapHttpRoute(
               name: "Printer",
               routeTemplate: "api/Printer",
               defaults: new { controller = "Printer", action = "Printer"});

            config.Routes.MapHttpRoute(
                name: "AcceptT",
                routeTemplate: "api/Session/Reject/{id}",
                defaults: new { controller = "Session", action = "Reject" });
            config.Routes.MapHttpRoute(
              name: "RejectT",
              routeTemplate: "api/Session/Accept/{id}",
              defaults: new { controller = "Session", action = "Accept" });


            config.Routes.MapHttpRoute(
              name: "Sesh",
              routeTemplate: "api/Session",
              defaults: new { controller = "Session", action = "All" });

            config.Routes.MapHttpRoute(
              name: "NotInCashup",
              routeTemplate: "api/Session/NotInCashup",
              defaults: new { controller = "Session", action = "NotInCashup" });

            config.Routes.MapHttpRoute(
              name: "SingleSession",
              routeTemplate: "api/Session/{id}",
              defaults: new { controller = "Session", action = "GetSingle" });

            

            config.Routes.MapHttpRoute(
              name: "RemoveSessionFromReports",
              routeTemplate: "api/Session/RemoveFromReports",
              defaults: new { controller = "Session", action = "RemoveFromReports" });

            // RESERVATIONS

                config.Routes.MapHttpRoute(
                    name: "ReservationCheck",
                    routeTemplate: "api/Reservation/ReservationCheck",
                    defaults: new { controller = "Reservation", action = "ReservationCheck" });

                config.Routes.MapHttpRoute(
                    name: "Index",
                    routeTemplate: "api/Reservation",
                    defaults: new { controller = "Reservation", action = "Index" });

                config.Routes.MapHttpRoute(
                    name: "ReservationSingle",
                    routeTemplate: "api/Reservation/{id}",
                    defaults: new { controller = "Reservation", action = "Single", id = RouteParameter.Optional });

                config.Routes.MapHttpRoute(
                    name: "AcceptR",
                    routeTemplate: "api/Reservation/Accept/{id}",
                    defaults: new { controller = "Reservation", action = "Accept" });

                config.Routes.MapHttpRoute(
                   name: "RejectR",
                   routeTemplate: "api/Reservation/Reject/{id}",
                   defaults: new { controller = "Reservation", action = "Reject" });

                config.Routes.MapHttpRoute(
                   name: "ArrivedR",
                   routeTemplate: "api/Reservation/Arrived/{id}",
                   defaults: new { controller = "Reservation", action = "Arrived" });

                

            


            config.Routes.MapHttpRoute(
               name: "Login1",
               routeTemplate: "api/Authentication/Login/{id}",
               defaults: new { controller = "Authentication", action = "Login", id = RouteParameter.Optional });

            config.Routes.MapHttpRoute(
                name: "DefaultApi",
                routeTemplate: "api/{controller}/{id}",
                defaults: new { id = RouteParameter.Optional }
            );

            config.Routes.MapHttpRoute(
               name: "OpenOrderClose",
               routeTemplate: "api/OpenOrder/Close",
               defaults: new { controller = "OpenOrder", action = "Close" });


            config.Routes.MapHttpRoute(
               name: "OpenOrderOpen",
               routeTemplate: "api/OpenOrder/Open",
               defaults: new { controller = "OpenOrder", action = "Open" });

            config.Routes.MapHttpRoute(
              name: "RemoveOrderFromReports",
              routeTemplate: "api/Order/RemoveFromReports",
              defaults: new { controller = "Order", action = "RemoveFromReports" });

            config.Routes.MapHttpRoute(
                name: "AddTables",
                routeTemplate: "api/Session/Tables/{id}",
                defaults: new { controller = "Session", action = "Tables", id = RouteParameter.Optional });

            config.Routes.MapHttpRoute(
                name: "AdChairs",
                routeTemplate: "api/Session/Chairs/{id}",
                defaults: new { controller = "Session", action = "Chairs", id = RouteParameter.Optional });
            config.Routes.MapHttpRoute(
                name: "Delay",
                routeTemplate: "api/Session/Delay/{id}",
                defaults: new { controller = "Session", action = "Delay", id = RouteParameter.Optional });
            config.Routes.MapHttpRoute(
                name: "PriceOffset",
                routeTemplate: "api/Session/PriceOffset/{id}",
                defaults: new { controller = "Session", action = "PriceOffset", id = RouteParameter.Optional });
            config.Routes.MapHttpRoute(
                name: "PercentageOffset",
                routeTemplate: "api/Session/PercentageOffset/{id}",
                defaults: new { controller = "Session", action = "PercentageOffset", id = RouteParameter.Optional });
            config.Routes.MapHttpRoute(
                name: "SetTip",
                routeTemplate: "api/Session/SetTip/{id}",
                defaults: new { controller = "Session", action = "SetTip", id = RouteParameter.Optional });
            config.Routes.MapHttpRoute(
                name: "RequestBill",
                routeTemplate: "api/Session/RequestBill/{id}",
                defaults: new { controller = "Session", action = "RequestBill", id = RouteParameter.Optional });
            config.Routes.MapHttpRoute(
                name: "UnrequestBill",
                routeTemplate: "api/Session/UnrequestBill/{id}",
                defaults: new { controller = "Session", action = "UnrequestBill", id = RouteParameter.Optional });
            config.Routes.MapHttpRoute(
                            name: "PayBill",
                            routeTemplate: "api/Session/PayBill/{id}",
                            defaults: new { controller = "Session", action = "PayBill", id = RouteParameter.Optional });

            config.Routes.MapHttpRoute(
                name: "Closed",
                routeTemplate: "api/Session/Close/{id}",
                defaults: new { controller = "Session", action = "Close", id = RouteParameter.Optional });

            config.Routes.MapHttpRoute(
                name: "AckNotification",
                routeTemplate: "api/Session/Acknowledge/{id}",
                defaults: new { controller = "Session", action = "PostAcknowledge", id = RouteParameter.Optional });

            config.Routes.MapHttpRoute(
                name: "AckNotificationAdhoc",
                routeTemplate: "api/Session/AcknowledgeAdhoc/{id}",
                defaults: new { controller = "Session", action = "PostAcknowledgeAdhoc", id = RouteParameter.Optional });

            config.Routes.MapHttpRoute(
                name: "RemoveOrderFromBill",
                routeTemplate: "api/Order/RemoveOrderFromBill/{id}",
                defaults: new { controller = "Order", action = "RemoveOrderFromBill", id = RouteParameter.Optional });

            config.Routes.MapHttpRoute(
                name: "RemoveAllOrdersFromSession",
                routeTemplate: "api/Order/RemoveAllOrdersFromSession/{id}",
                defaults: new { controller = "Order", action = "RemoveAllOrdersFromSession", id = RouteParameter.Optional });
  
            // Disable infinite loops during serialisation
            config.Formatters.JsonFormatter.SerializerSettings.ReferenceLoopHandling = Newtonsoft.Json.ReferenceLoopHandling.Ignore;

            
            
        }
    }
}

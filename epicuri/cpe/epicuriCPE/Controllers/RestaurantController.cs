using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Data.Entity.Infrastructure;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web;
using System.Web.Http;
using epicuri.Core.DatabaseModel;

namespace epicuri.CPE.Controllers
{
    public class RestaurantController : Models.EpicuriApiController
    {
        public HttpResponseMessage GetRestaurant()
        {
            try
            {
                Authenticate();
            }
            catch (RoleException e)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, e);
            }
            catch (Exception e)
            {
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, e);
                response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                return response;
            }

            var selectedRestaurant = new Models.InfoRestaurant(Restaurant);

            // Get the defaults for the restaurant
            Dictionary<String, object> ResDefaults = new Dictionary<string, object>();

            var localSettings = db.Settings.Where(r => r.RestaurantId == Restaurant.Id);
            foreach (var localSetting in localSettings)
            {
                ResDefaults.Add(localSetting.Key, localSetting.Value);
            }

            var globalSettings = db.DefaultSettings;

            foreach (var globalSetting in globalSettings)
            {
                if (!ResDefaults.ContainsKey(globalSetting.Key))
                    ResDefaults.Add(globalSetting.Key, globalSetting.Value);
            }

            selectedRestaurant.RestaurantDefaults = ResDefaults;

            selectedRestaurant.AdjustmentTypes = new List<Models.AdjustmentType>();

            foreach (Core.DatabaseModel.AdjustmentType adjType in db.AdjustmentTypes.Where(aj => aj.Deleted == null))
            {
                selectedRestaurant.AdjustmentTypes.Add(new Models.AdjustmentType(adjType));

            }

            return Request.CreateResponse(HttpStatusCode.OK, selectedRestaurant);
        }

      
    }
}
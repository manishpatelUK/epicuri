using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.CPE.Controllers
{
    public class SettingController : Models.EpicuriApiController
    {
        public HttpResponseMessage GetSettings()
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

            
            var globalSettings = db.DefaultSettings;
            var localSettings = db.Settings.Where(r => r.RestaurantId == Restaurant.Id);

            Dictionary<String, Models.Setting> settings = new Dictionary<string, Models.Setting>();
            foreach (var globalSetting in globalSettings)
            {
                settings[globalSetting.Key] = new Models.Setting
                {
                    Value = globalSetting.Value,
                    local = false,
                    Default = globalSetting.Value
                };
            }



            foreach (var localSetting in localSettings)
            {
                settings[localSetting.Key] = new Models.Setting
                {
                    Value = localSetting.Value,
                    local = true,
                    Default = globalSettings.Where(g=>g.Key== localSetting.Key).Single().Value
                };
            }



            return Request.CreateResponse(HttpStatusCode.OK, settings);
        }

    }
}

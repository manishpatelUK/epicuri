using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using epicuri.Core.DatabaseModel;

namespace epicuri.CPE.Controllers
{
    public class DeviceController : Models.EpicuriApiController
    {

        protected epicuriContainer db = new epicuriContainer();

        [HttpGet]
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

            return Request.CreateResponse(HttpStatusCode.OK, db.Devices.Select(device => new
            {
                Note = device.Note,
                Id = device.Id,
                DeviceId = device.DeviceId,
                RestaurantId = device.RestaurantId ?? default(int),
                OS = device.OS,
                LastKnownIP = device.LastKnownIP,
                SSID = device.SSID,
                LastLogTime = device.LastLogTime ?? default(DateTime),
                CurrentLanguageSetting = device.CurrentLanguageSetting,
                CurrentTimezoneSetting = device.CurrentTimezoneSetting,
                WaiterAppVersion = device.WaiterAppVersion,
                IsDeviceAutoUpdating = device.IsDeviceAutoUpdating ?? default(bool)
            }));
        }

        [HttpPost]
        public HttpResponseMessage UpdateDevice(Models.DevicePayload payloadDevice)
        {

            if (payloadDevice == null)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest);
            }

            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.NotAcceptable, new Exception("Model state is invalid " + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }

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

            // This method implements EP 55.2 and EP 55.3
            // https://docs.google.com/document/d/1g8uNUdDQKqZr_RCM6b1lrxvEji2nGrQnfG5iO5W7XOs

            String deviceId;
            try {
                deviceId = Request.Headers.GetValues("X-Epicuri-MAC").FirstOrDefault();
            }

            catch
            {
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.BadRequest, "No DeviceId specified in request headers.");
                return response;
            }

            Device device = GetStoredDeviceWithDeviceId(deviceId);

            if (device == null)
            {
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.BadRequest, "This device does not exist in storage");
                return response;
            }

            if (payloadDevice.Hash != null) device.Hash = payloadDevice.Hash;
            if (payloadDevice.Note != null) device.Note = payloadDevice.Note;
            if (payloadDevice.OS != null) device.OS = payloadDevice.OS;
            if (payloadDevice.WaiterAppVersion != null) device.WaiterAppVersion = payloadDevice.WaiterAppVersion;
            if (payloadDevice.LanguageSetting != null) device.CurrentLanguageSetting = payloadDevice.LanguageSetting;
            if (payloadDevice.TimezoneSetting != null) device.CurrentTimezoneSetting = payloadDevice.TimezoneSetting;
            if (payloadDevice.IsAutoUpdating != null) device.IsDeviceAutoUpdating = payloadDevice.IsAutoUpdating;
            if (payloadDevice.WaiterAppVersion != null) device.WaiterAppVersion = payloadDevice.WaiterAppVersion;
            if (payloadDevice.RestaurantId != null && payloadDevice.RestaurantId != 0) device.RestaurantId = payloadDevice.RestaurantId;

            device.LastLogTime = DateTime.Now;

            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.Accepted);
        }

        private Device GetStoredDeviceWithDeviceId(string deviceId)
        {
            Device device = db.Devices.FirstOrDefault(d => d.DeviceId == deviceId);
            return device;
        }
    }
}

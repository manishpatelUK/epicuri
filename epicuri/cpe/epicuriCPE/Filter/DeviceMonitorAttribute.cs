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
using System.Diagnostics;
using System.Data.Entity.Validation;
using System.Data.Entity.Infrastructure;
using System.ComponentModel;
using System.Runtime.Caching;
using System.Net;
using System.Web.Caching;

namespace epicuri.CPE.Filter
{
    public class DeviceMonitorAttribute : ActionFilterAttribute
    {
        private epicuriContainer db = new epicuriContainer();

        // Cache Handling
        private ObjectCache devicesCache { get { return MemoryCache.Default; } }

        private bool attemptedFromDatabase = false;

        public override void OnActionExecuting(HttpActionContext actionContext)
        {

            // This method implements EP 55.2 and EP 55.3
            // https://docs.google.com/document/d/1g8uNUdDQKqZr_RCM6b1lrxvEji2nGrQnfG5iO5W7XOs

            System.Net.Http.Headers.HttpRequestHeaders headers = actionContext.Request.Headers;

            string ip = null;
            //string gw = null;
            string mac = null;
            string ssid = null;

            try
            {
                ip = actionContext.Request.Headers.GetValues("X-Epicuri-IP").FirstOrDefault();
                ssid = actionContext.Request.Headers.GetValues("X-Epicuri-SSID").FirstOrDefault();
                //gw = actionContext.Request.Headers.GetValues("X-Epicuri-GW").FirstOrDefault();
                mac = actionContext.Request.Headers.GetValues("X-Epicuri-MAC").FirstOrDefault();
            }

            catch
            {
                Console.WriteLine("Headers invalid for Device Update.");
                return;
            }

            // MAC-"IP+SSID" pairing stored in dictionary cache

            Dictionary<string, string> deviceDict = devicesCache["DevicesDict"] as Dictionary<string, string>;
            string newIpAndSSIDString = ip + ssid;

            if ((deviceDict == null || deviceDict.Count == 0) && !attemptedFromDatabase)
            {
                // If no cached devices try getting from database once.
                attemptedFromDatabase = true;
                deviceDict = CreateCacheFromDatabase();
            }

            if (deviceDict != null && deviceDict.ContainsKey(mac))
            {
                // If the key is in the cached dictionary, its an already seen deviceId (mac), so get the ip/gw and update

                string storedIpAndSSIDStringForMac = deviceDict[mac];

                epicuri.Core.DatabaseModel.Device dbDevice = GetStoredDeviceWithDeviceId(mac);

                if (!storedIpAndSSIDStringForMac.Equals(newIpAndSSIDString) || dbDevice == null)
                {
                    // IP or SSID updated, so get Device from DB and update IP/SSID

                    if (dbDevice == null)
                    {
                        // This is the case when a device is deleted whilst the server is running.
                        // It still believes it to be in cache, however it does not exist on the database.

                        dbDevice = new Device();
                        dbDevice.DeviceId = mac;
                        db.Devices.AddObject(dbDevice);
                        db.SaveChanges();

                        // Check Cache count matches database count. If they are out of sync devices have been deleted, and the cache needs updating from db.
                        deviceDict = CreateCacheFromDatabase();
                    }
                    
                    dbDevice.LastKnownIP = ip;
                    dbDevice.SSID = ssid;
                    dbDevice.LastLogTime = DateTime.Now;
                    db.SaveChanges();

                    // Update Device Cache
                    deviceDict[mac] = newIpAndSSIDString;
                    UpdateDeviceCache(deviceDict);
                    
                }
            }
            else
            {
                // If the key isn't in the dictionary it has not yet been used. Store in db and update cache
                // Use blanks for info other than ip, mac and SSID

                Device newDbDevice = new Device()
                {
                    RestaurantId = null,
                    DeviceId = mac,
                    Hash = "",
                    Note = "",
                    OS = "",
                    LastKnownIP = ip,
                    SSID = ssid,
                    LastLogTime = DateTime.Now
                };

                db.Devices.AddObject(newDbDevice);
                db.SaveChanges();

                if (deviceDict == null)
                {
                    // No cache currently, create one
                    deviceDict = new Dictionary<string, string>();
                }

                deviceDict[mac] = newIpAndSSIDString;
                UpdateDeviceCache(deviceDict);
            }

        }

        private Dictionary<string, string> CreateCacheFromDatabase()
        {
            Device[] devices = GetStoredDevices();

            Dictionary<string, string>  deviceDict = new Dictionary<string, string>();

            foreach (Device dbDevice in devices)
            {
                deviceDict[dbDevice.DeviceId] = dbDevice.LastKnownIP + dbDevice.SSID;
            }

            UpdateDeviceCache(deviceDict);

            return deviceDict;
        }

        private Device GetStoredDeviceWithDeviceId(string deviceId)
        {
            Device device = db.Devices.FirstOrDefault(d => d.DeviceId == deviceId);
            return device;
        }

        private void UpdateDeviceCache(Dictionary<string, string> deviceDict)
        {
            CacheItemPolicy policy = new CacheItemPolicy();
            policy.AbsoluteExpiration = DateTime.Now + TimeSpan.FromMinutes(10);
            devicesCache.Add(new CacheItem("DevicesDict", deviceDict), policy);
        }

        private Device[] GetStoredDevices()
        {
            Device[] devices = db.Devices.ToArray();
            return devices;
        }
    }
}

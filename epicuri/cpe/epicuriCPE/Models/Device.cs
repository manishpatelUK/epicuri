using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class Device
    {
        public int Id;
        public String Name;
        public String DeviceId;
        public String OS;
        public int RestaurantId;
        public String LastKnownIP;
        public String SSID;
        public DateTime LastLogTime;
        public String CurrentLanguageSetting;
        public String CurrentTimezoneSetting;
        public String WaiterAppVersion;
        public bool IsDeviceAutoUpdating;

    }
}
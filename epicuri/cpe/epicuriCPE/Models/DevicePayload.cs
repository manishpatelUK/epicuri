using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    public class DevicePayload
    {
        //[Required]
        // MAC Address (e.g. 00:0a:95:9d:68:16)
        //public String DeviceId { get; set; }
        public String Hash { get; set; }
        public String Note { get; set; }
        public String OS { get; set; }
        public String WaiterAppVersion { get; set; }
        public String LanguageSetting {get; set;}
        public String TimezoneSetting { get; set; }
        public Boolean IsAutoUpdating { get; set; }
        public int RestaurantId { get; set; }
    }
}
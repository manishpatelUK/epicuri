using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuriOnBoarding.Models
{
    public class SettingsPayload
    {
        public Dictionary<string, Models.SimpleSetting> Settings { get; set; }
    }
}
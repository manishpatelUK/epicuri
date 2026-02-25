using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;

namespace epicuriOnBoarding.Models
{
    public class Setting
    {
        public String Value { get; set; }
        public bool local { set { Local = value; } }
        public bool Local;
        public String SettingDescription { set { _settingDescription = value; } }
        public String _settingDescription;
        public String Measure { set { _measure = value; } }
        public String _measure;
        public String Default { set { _default = value; } }
        private String _default;

        public string GetDefault()
        {
            return _default;
        }
        public string GetMeasure()
        {
            return _measure;
        }
        public string GetSettingDescription()
        {
            return _settingDescription;
        }
    }


    public class SimpleSetting
    {
        public String Value { get; set; }
        
    }
}

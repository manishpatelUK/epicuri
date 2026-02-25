using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{

        public class Setting
        {
            public String Value { get; set; }
            public bool local { set { Local = value; } }
            public bool Local;
            public String Default { set { _default = value; } }
            private String _default;
            public string GetDefault()
            {
                return _default;
            }
        }


        public class SimpleSetting
        {
            public String Value { get; set; }

        }

        public class SettingsViewModel
        {
            public Dictionary<string, Models.Setting> Settings;
        }
}
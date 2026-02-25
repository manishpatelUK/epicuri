using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace epicuri.Core
{
    
    public static class Settings
    {
        private static Dictionary<String,String> store = new Dictionary<String,String>();

        public static T Setting<T>(int Restaurant, string Key)
        {
            if (store.ContainsKey(Restaurant+"-"+Key))
            {
                string val = store[Restaurant + "-"+Key];
                return (T)Convert.ChangeType(val, typeof(T));
            }
            try
            {
                using (epicuri.Core.DatabaseModel.epicuriContainer db = new DatabaseModel.epicuriContainer())
                {

                    DatabaseModel.Setting setting = db.Settings.FirstOrDefault(s => s.RestaurantId == Restaurant && s.Key == Key);
                    string outVal;
                    if (setting == null)
                    {
                        DatabaseModel.DefaultSetting defaultSetting = db.DefaultSettings.FirstOrDefault(s => s.Key == Key);

                        if (defaultSetting == null)
                        {
                            throw new KeyNotFoundException("Setting not found");
                        }



                        outVal = defaultSetting.Value;
                    }
                    else
                    {
                        outVal = setting.Value;
                    }


                    return (T)Convert.ChangeType(outVal, typeof(T));

                }
            }
            catch (ArgumentException arex)
            {
                return (T)Convert.ChangeType(0, typeof(T));
            }


        }

        public static void DefineForSingleUse<T>(int Restaurant, string Key, string data) {
            if (store.ContainsKey(Restaurant + "-" + Key))
            {
                store[Restaurant + "-" + Key] = data;
            }
            else
            {
                store.Add(Restaurant + "-" + Key, data);
            }
        }
    }
}

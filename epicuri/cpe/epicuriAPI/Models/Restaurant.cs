using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.API.Models
{
    public class Restaurant
    {
        public int Id;
        public string Name;
        public string Email;
        public string PhoneNumber;

        public string Website;
        public DateTime? Deleted;
        public bool EnabledForDiner;

        public bool MewsIntegration;

        public string Description;

        public epicuri.Core.DatabaseModel.LatLongPair Position;
        public epicuri.Core.DatabaseModel.Address Address;
        public int TakeawayOffered;
        public int? TakeawayMenuId { get; set; }
        public int CategoryId;

        public int TakeawayMinimumTime { get { return epicuri.Core.Settings.Setting<int>(Id, "TakeawayMinimumTime"); } }

        public Dictionary<String, object> RestaurantDefaults;

        public string Currency;
        public string Timezone;
    }
}
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using epicuri.Core.DatabaseModel;

namespace epicuri.CPE.Models
{
    public class InfoRestaurant
    {
        protected epicuriContainer db = new epicuriContainer();

        public string Name { get; set; }
        public string Description { get; set; }
        public epicuri.Core.DatabaseModel.Address Address { get; set; }
        public int TakeawayMenuId;
        public string Telephone1 { get; set; }
        public string Telephone2 { get; set; }
        public string Email { get; set; }
        public string Website { get; set; }
        public string ReceiptFooter { get; set; }
        public string VATNumber { get; set; }
        public int TakeawayOffered { get; set; }
        public Dictionary<String, object> RestaurantDefaults;
        public string ReceiptImageURL { get; set; }
        public int? TakeawayPrinterId { get; set; }
        public int? BillingPrinterId { get; set; }
        //public IQueryable<AdjustmentType> AdjustmentTypes { get; set; }
        public List<AdjustmentType> AdjustmentTypes { get; set; }
        public bool MewsIntegration { get; set; }
        public string Currency { get; set; }
        public string Timezone { get; set; }
        public int ReceiptType { get; set; }
      

        public InfoRestaurant(Core.DatabaseModel.Restaurant r)
        {
            Name = r.Name;
            Description = r.Description;
            Address = r.Address;
            if (r.TakeawayMenu != null)
            {
                TakeawayMenuId = r.TakeawayMenu.Id;
            }
            Telephone1 = r.PhoneNumber;
            Telephone2 = r.PhoneNumber2;
            Email = r.PublicEmailAddress;
            Website = r.Website;
            ReceiptFooter = r.ReceiptFooter;
            VATNumber = r.VATNumber;
            TakeawayOffered = r.TakeawayOffered;
            ReceiptImageURL = (r.ReceiptResource != null ? r.ReceiptResource.CDNUrl: null);
            TakeawayPrinterId = r.TakeawayPrinterId;
            BillingPrinterId  = r.BillingPrinterId;
            MewsIntegration = r.MewsIntegration;
            Currency = r.ISOCurrency;
            Timezone = r.IANATimezone;
            ReceiptType = r.ReceiptType;
           
        }
        
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using Newtonsoft.Json;

namespace epicuri.CPE.Models
{
    [JsonObject(IsReference = false)]
    public class Customer : DataModel
    {
        public int Id;

        [JsonProperty(IsReference = false)]
        public epicuri.Core.DatabaseModel.Name Name;

        [JsonProperty(IsReference = false)]
        public epicuri.Core.DatabaseModel.NullableAddress Address;
        
        public String Email;

        public String PhoneNumber;
        public string FavouriteFood;
        public string FavouriteDrink;
        public string HatedFood;
        public bool IsBlackMarked;

        public IEnumerable<Diner> Interactions;

        public List<DietaryPreference> Allergies = new List<DietaryPreference>();
        public List<DietaryPreference> DietaryRequirements = new List<DietaryPreference>();
        public List<DietaryPreference> FoodPreferences = new List<DietaryPreference>();
        
        public Customer() { }
        public Customer(Core.DatabaseModel.Customer customer)
        {
            Init();
            this.Id = customer.Id;
            this.Name = customer.Name;
            this.Address = customer.Address;
            this.Email = customer.Email;
            this.PhoneNumber = customer.PhoneNumber;

            this.FavouriteFood = customer.FavouriteFood;
            this.FavouriteDrink = customer.FavouriteDrink;
            this.HatedFood = customer.HatedFood;

            if(GetEffectiveBlackMarkCount(customer) >= 3)
                this.IsBlackMarked = true;
            else
                this.IsBlackMarked = false;

            foreach (Core.DatabaseModel.Allergy allery in customer.Allergies)
            {
                this.Allergies.Add(new DietaryPreference(allery.Id, allery.Name));
            }

            foreach (Core.DatabaseModel.DietaryRequirement req in customer.DietaryRequirements)
            {
                this.DietaryRequirements.Add(new DietaryPreference(req.Id, req.Name));
            }

            foreach (Core.DatabaseModel.FoodPreference pref in customer.FoodPreferences)
            {
                this.FoodPreferences.Add(new DietaryPreference(pref.Id, pref.Name));
            }

            Allergies = Allergies.OrderBy(o => o.Name).ToList();
            //Interactions = customer.DinerInstances.Select(d=>new Diner(d));
        }

        public static int GetEffectiveBlackMarkCount(epicuri.Core.DatabaseModel.Customer c)
        {
            int retValue = c.BlackMarks.Count(bm => bm.Expires > DateTime.UtcNow);
            return retValue;

        }

        public static bool OKToOrder(epicuri.Core.DatabaseModel.Customer c)
        {
            int blackMarkCount;
            blackMarkCount = GetEffectiveBlackMarkCount(c);

            if (blackMarkCount < 3)
                return true;

            return false;
        }
        
    }
}
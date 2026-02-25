using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.API.Models
{
    public class Customer
    {
        public int Id;
        [Required]
        public epicuri.Core.DatabaseModel.Name Name { get; set; }
        public epicuri.Core.DatabaseModel.NullableAddress Address { get; set; }
        
        public String Email { get; set; }
        public Double? Birthday { get; set; }

        public string FavouriteFood;
        public string FavouriteDrink;
        public string HatedFood;
        

        public List<int> Allergies = new List<int>();
        public List<int> DietaryRequirements = new List<int>();
        public List<int> FoodPreferences = new List<int>();
 
        public String AuthKey;
        public String PhoneNumber { get; set; }
        public String FacebookId { get; set; }
        public String TwitterId { get; set; }

        private IEnumerable<NamedAuthToken> AuthTokens;


        public IEnumerable<NamedAuthToken> Auth
        {
            set
            {
                AuthTokens = value;
            }
        }
        public IEnumerable<NamedAuthToken> GetAuthTokens()
        {
            return AuthTokens;
        }

        private String _Password;


        public String Password
        {
            set
            {
                _Password = value;
            }
        }

        public String GetPassword(bool Update)
        {
            if (Update)
                return _Password;
            else
                return GetPassword();
        }

        public String GetPassword()
        {
            if (string.IsNullOrWhiteSpace(_Password))
            {
                return Support.String.RandomString(24);
            }
            else
            {
                return _Password;
            }
        }

        public Customer()
        {
        }

        public Customer(Core.DatabaseModel.Customer customer)
        {
            if (customer.AuthenticationKeys.Count() != 0)
                this.AuthKey = customer.AuthenticationKeys.OrderByDescending(ak => ak.Expires).First().Id + "-" + customer.AuthenticationKeys.OrderByDescending(ak => ak.Expires).First().Key;
            
            this.Id = customer.Id;
            this.Name = customer.Name;
            this.Address = customer.Address;
            this.Email = customer.Email;
            this.PhoneNumber = customer.PhoneNumber;

            this.FavouriteFood = customer.FavouriteFood;
            this.FavouriteDrink = customer.FavouriteDrink;
            this.HatedFood = customer.HatedFood;

            foreach (Core.DatabaseModel.Allergy allery in customer.Allergies)
            {
                this.Allergies.Add(allery.Id);
            }

            foreach (Core.DatabaseModel.DietaryRequirement req in customer.DietaryRequirements)
            {
                this.DietaryRequirements.Add(req.Id);
            }

            foreach (Core.DatabaseModel.FoodPreference pref in customer.FoodPreferences)
            {
                this.FoodPreferences.Add(pref.Id);
            }

        }

    }
}
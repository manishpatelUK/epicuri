using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.API.Controllers
{


    public class RestaurantController : Support.APIController
    {


        [HttpGet]
        public HttpResponseMessage GetRestaurants([FromUri] Models.PositionSquare pos = null, int Category = 0, string Name = null, int Id = 0, bool HasTakeaway = false)
        {
            if (HasTakeaway)
            {
                List<Models.Restaurant> resultList = new List<Models.Restaurant>();
 
                    resultList = (from restaurant in db.Restaurants
                                  where restaurant.TakeawayMenu != null && restaurant.Deleted == null && restaurant.Enabled == true
                                  select new Models.Restaurant
                                  {
                                      Id = restaurant.Id,
                                      Name = restaurant.Name,
                                      Address = restaurant.Address,
                                      Position = restaurant.Position,
                                      Description = restaurant.Description,
                                      Email = restaurant.PublicEmailAddress,
                                      PhoneNumber = restaurant.PhoneNumber,
                                      TakeawayOffered = restaurant.TakeawayOffered,
                                      TakeawayMenuId = restaurant.TakeawayMenu == null ? 0 : restaurant.TakeawayMenu.Id,
                                      CategoryId = restaurant.CategoryId,
                                      Website = restaurant.Website,
                                      Deleted = restaurant.Deleted,
                                      MewsIntegration = restaurant.MewsIntegration,
                                      Currency = restaurant.ISOCurrency,
                                      Timezone = restaurant.IANATimezone
                                  }).ToList();

                    //EP-288 HasTakeaway ignores name field
                    if (Name != null)
                    {
                        String[] nameCheck = Name.Trim().Split(' ');

                        List<Models.Restaurant> queryResults = new List<Models.Restaurant>();
                        foreach (String word in nameCheck)
                        {
                            queryResults.AddRange(resultList.Where(r => r.Name.ToLower().Contains(word.ToLower())));
                        }

                        resultList = queryResults;
                    }
                //if (result.Count() > 0)
                //{

                

                foreach (Models.Restaurant res in resultList)
                {
                    res.RestaurantDefaults = GetDefaults(res);
                }

                    return Request.CreateResponse(HttpStatusCode.OK, resultList);
                /*}
                //else
                //{
                    return Request.CreateResponse(HttpStatusCode.NotFound);
                }*/
            }


            if (Id != 0)
            {
                Models.Restaurant result = (from restaurant in db.Restaurants
                                            where restaurant.Id == Id && restaurant.Deleted == null && restaurant.Enabled == true
                                            select new Models.Restaurant
                                            {
                                                Id = restaurant.Id,
                                                Name = restaurant.Name,
                                                Address = restaurant.Address,
                                                Position = restaurant.Position,
                                                Description = restaurant.Description,
                                                Email = restaurant.PublicEmailAddress,
                                                PhoneNumber = restaurant.PhoneNumber,
                                                TakeawayOffered = restaurant.TakeawayOffered,
                                                TakeawayMenuId = restaurant.TakeawayMenu == null ? 0 : restaurant.TakeawayMenu.Id,
                                                CategoryId = restaurant.CategoryId,
                                                Website = restaurant.Website,
                                                Deleted = restaurant.Deleted,
                                                MewsIntegration = restaurant.MewsIntegration,
                                                Currency = restaurant.ISOCurrency,
                                                Timezone = restaurant.IANATimezone
                                            }).SingleOrDefault();

               result.RestaurantDefaults = GetDefaults(result);


                //if (result.Count() > 0)
                //{
                    return Request.CreateResponse(HttpStatusCode.OK, result);
                /*}
                else
                {
                    return Request.CreateResponse(HttpStatusCode.NotFound);
                }*/
            }

            if (pos.TopRightLongitude == 0 && pos.TopRightLatitude == 0 && pos.BottomLeftLongitude == 0 && pos.BottomLeftLatitude == 0)
            {
                pos = new Models.PositionSquare
                  {
                      TopRightLatitude = 999,
                      TopRightLongitude = 999,
                      BottomLeftLatitude = -999,
                      BottomLeftLongitude = -999
                  };

            }
            var restaurants = from restaurant in db.Restaurants
                              where restaurant.Deleted == null && (
                                (Id != 0 && restaurant.Id == Id) ||
                                (restaurant.Enabled &&
                                  restaurant.Position.Latitude != null &&
                                  restaurant.Position.Longitude != null &&
                                  restaurant.Position.Latitude >= pos.BottomLeftLatitude &&
                                  restaurant.Position.Longitude >= pos.BottomLeftLongitude &&
                                  restaurant.Position.Latitude <= pos.TopRightLatitude &&
                                  restaurant.Position.Longitude <= pos.TopRightLongitude))


                              select new Models.Restaurant
                                      {
                                          Id = restaurant.Id,
                                          Name = restaurant.Name,
                                          Address = restaurant.Address,
                                          Position = restaurant.Position,
                                          Description = restaurant.Description,
                                          Email = restaurant.PublicEmailAddress,
                                          PhoneNumber = restaurant.PhoneNumber,
                                          TakeawayOffered = restaurant.TakeawayOffered,
                                          TakeawayMenuId = restaurant.TakeawayMenu == null ? 0 : restaurant.TakeawayMenu.Id,
                                          CategoryId = restaurant.CategoryId,
                                          Website = restaurant.Website,
                                          Deleted = restaurant.Deleted,
                                          MewsIntegration = restaurant.MewsIntegration,
                                          Currency = restaurant.ISOCurrency,
                                          Timezone = restaurant.IANATimezone
                                      };

            if (Category != 0)
            {
                restaurants = restaurants.Where(r => r.CategoryId == Category);
            }

            if (string.IsNullOrWhiteSpace(Name))
            {
                List<Models.Restaurant> restaurantList = new List<Models.Restaurant>();

                foreach (Models.Restaurant res in restaurants)
                {
                    res.RestaurantDefaults = GetDefaults(res);
                    restaurantList.Add(res);
                }

                return Request.CreateResponse(HttpStatusCode.OK, restaurantList);
            }

            String[] nameWords = Name.Trim().Split(' ');

            List<Models.Restaurant> results = new List<Models.Restaurant>();
            foreach (String word in nameWords)
            {
                results.AddRange(restaurants.Where(r => r.Name.ToLower().Contains(word.ToLower())));
            }

            foreach (Models.Restaurant res in results)
            {
                res.RestaurantDefaults = GetDefaults(res);
            }

            return Request.CreateResponse(HttpStatusCode.OK, results.Distinct(new DistinctItemComparer()));

        }

        private Dictionary<String, object> GetDefaults(Models.Restaurant restaurant)
        {
            Dictionary<String, object> ResDefaults = new Dictionary<string, object>();

            var localSettings = db.Settings.Where(r => r.RestaurantId == restaurant.Id);
            foreach (var localSetting in localSettings)
            {
                ResDefaults.Add(localSetting.Key, localSetting.Value);
            }

            var globalSettings = db.DefaultSettings;

            foreach (var globalSetting in globalSettings)
            {
                if (!ResDefaults.ContainsKey(globalSetting.Key))
                    ResDefaults.Add(globalSetting.Key, globalSetting.Value);
            }

            return ResDefaults;
        }

    }

    class DistinctItemComparer : IEqualityComparer<Models.Restaurant>
    {

        public bool Equals(Models.Restaurant x, Models.Restaurant y)
        {
            return x.Id == y.Id;

        }

        public int GetHashCode(Models.Restaurant obj)
        {
            return obj.Id;
        }
    }
}

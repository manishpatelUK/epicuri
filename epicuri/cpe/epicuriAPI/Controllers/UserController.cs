using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using epicuri.Core.DatabaseModel;
using epicuri.API.Models;
using epicuri.Core.Social;
using System.Data.SqlClient;
using System.Data;

namespace epicuri.API.Controllers
{
    public class UserController : Support.APIController
    {
        public HttpResponseMessage GetFoodRequirements()
        {
            try
            {
                Authenticate();
            }
            catch (Exception e)
            {
                HttpResponseMessage responseMessage = Request.CreateResponse(HttpStatusCode.Unauthorized, e);
                responseMessage.Headers.Add("WWW-Authenticate", "Basic");
                return responseMessage;
            }

            Dictionary<string, List<KeyValuePair<int, string>>> response = new Dictionary<string, List<KeyValuePair<int, string>>>();

            response.Add("DietaryRequirements", new List<KeyValuePair<int, string>>());

            foreach (var dr in db.DietaryRequirements)
            {
                response["DietaryRequirements"].Add(new KeyValuePair<int, string>(dr.Id, dr.Name));
            }

            response.Add("FoodPreferences", new List<KeyValuePair<int, string>>());

            foreach (var fp in db.FoodPreferences)
            {
                response["FoodPreferences"].Add(new KeyValuePair<int, string>(fp.Id, fp.Name));
            }

            response.Add("Allergies", new List<KeyValuePair<int, string>>());

            foreach (var al in db.Allergies)
            {
                response["Allergies"].Add(new KeyValuePair<int, string>(al.Id, al.Name));
            }

            return Request.CreateResponse(HttpStatusCode.OK, response);
        }

        [ActionName("User")]
        public HttpResponseMessage PutUser(Models.Customer cust)
        {
            try
            {
                Authenticate();
            }
            catch (Exception e)
            {
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, e);
                response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                return response;
            }
            
            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state:" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }

            if (cust.Email != null)
            {

                if (customer.Email != cust.Email)
                {
                    if (db.Customers.FirstOrDefault(c => c.Email == cust.Email) != null)
                    {
                        // If registering
                        if (customer == null)
                        {
                            return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Email address has already been taken"));
                        }

                        else if (db.Customers.FirstOrDefault(c => c.Email == cust.Email) != null)
                        {
                            return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Email address has already been taken"));
                        }
                    }
                }
                customer.Email = cust.Email;
            }

            customer.Name = cust.Name;
            customer.PhoneNumber = cust.PhoneNumber;
            customer.Address = cust.Address;
            
            if (cust.Birthday.HasValue)
                customer.Birthday = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(cust.Birthday.Value);

            if (cust.FavouriteFood != null)
                customer.FavouriteFood = cust.FavouriteFood;
            if (cust.FavouriteDrink != null)
                customer.FavouriteDrink = cust.FavouriteDrink;
            if (cust.HatedFood != null)
                customer.HatedFood = cust.HatedFood;

            customer.Allergies.Clear();
            foreach (int allergyId in cust.Allergies) {
                customer.Allergies.Add(db.Allergies.FirstOrDefault(alg => alg.Id == allergyId));
            }

            customer.FoodPreferences.Clear();
            foreach (int prefId in cust.FoodPreferences)
            {
                if (prefId != 0)
                {
                    /*
                    var param1 = new SqlParameter("custId", SqlDbType.Int);
                    var param2 = new SqlParameter("foodPref", SqlDbType.Int);

                    param1.Value = customer.Id;
                    param2.Value = prefId;

                    db.ExecuteStoreCommand("INSERT INTO CustomerFoodPreference (Customers_Id, FoodPreferences_Id) VALUES (@custId, @foodPref)", param1, param2);
                    */

                    customer.FoodPreferences.Add(db.FoodPreferences.FirstOrDefault(pref => pref.Id == prefId));
                    
                }
            }

            customer.DietaryRequirements.Clear();
            foreach (int reqId in cust.DietaryRequirements)
            {
                customer.DietaryRequirements.Add(db.DietaryRequirements.FirstOrDefault(req => req.Id == reqId));
            }
            
            /*
             * Check if facebook or twitter
             */
            var t = cust.GetAuthTokens();
            if (t != null)
            {
                foreach (Models.NamedAuthToken token in t)
                {
                    /*
                     * Check if token is empty
                     */
                    if (String.IsNullOrWhiteSpace(token.Token))
                    {
                        return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Token cannot be empty"));
                    }


                    /*
                     * Get the user by the token
                     */
                    epicuri.Core.DatabaseModel.Customer getCustomer;
                    if (token.Provider == AuthProvider.Twitter)
                    {
                        

                        try
                        {
                            Twitter twUser = new Twitter(
                                System.Web.Configuration.WebConfigurationManager.AppSettings["TwitterAuth"],
                                System.Web.Configuration.WebConfigurationManager.AppSettings["TwitterSecret"],
                                token.Token,
                                token.Secret);
                            var uid = twUser.GetUserId();
                            getCustomer = db.Customers.FirstOrDefault(c => c.TwitterId == uid);
                            customer.TwitterId = twUser.GetUserId();
                        }
                        catch (Exception ex)
                        {
                            return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Invalid Twitter Token", ex));
                        }
                        /*
                         * Check that the customer is not null
                         */
                        if (getCustomer != null)
                        {
                            return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("User already associated"));
                        }
                        
                    }
                    else if (token.Provider == AuthProvider.Facebook)
                    {
                        string id = "";
                        try
                        {
                            Facebook fbUser = new Facebook(token.Token);
                            id = fbUser.Id();
                            /*
                             * Get the epicuri user from the facebook user id
                             */
                            getCustomer = db.Customers.FirstOrDefault(c => c.FacebookId == id);
                        }
                        catch
                        {
                            return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Invalid Facebook Token"));
                        }
                        if (getCustomer != null)
                        {
                            return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("This facebook account is already assigned to another user"));
                        }
                        else
                        {
                            customer.FacebookId = id;
                        }
                    }
                    else
                    {
                        return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Unknown token type"));
                    }
                }
            }

            /*
             * Check if password
             */

            var password = cust.GetPassword(true);

            if (password == null)
            {
                var customerRecord = db.Customers.FirstOrDefault(c => c.Id == customer.Id);
                customer.Auth = customerRecord.Auth;
                customer.Salt = customerRecord.Salt;
            }
            else
            {

                if (!string.IsNullOrWhiteSpace(cust.GetPassword()))
                {
                    if (cust.GetPassword().Length < 6)
                    {
                        return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Password too short"));
                    }

                    string Salt = Support.Salt.GetSalt();
                    string Auth = Salt + cust.GetPassword() + Salt;
                    Auth = epicuri.API.Support.String.SHA1(Auth);
                    customer.Auth = Auth;
                    customer.Salt = Salt;
                }
            }

            db.SaveChanges();

            AuthenticationKey k = new AuthenticationKey
            {
                Key = Support.String.RandomString(24),
                Expires = DateTime.UtcNow.AddDays(30)
            };
            customer.AuthenticationKeys.Add(k);
            db.SaveChanges();

            var result = new Models.Customer(customer);

            /*
            var result = new Models.Customer
            {
                Id = customer.Id,
                Name = customer.Name,
                Email = customer.Email,
                Address = customer.Address,
                AuthKey = k.Id + "-" + k.Key,
                PhoneNumber = customer.PhoneNumber,
            };

             */

            if (customer.Birthday.HasValue)
                result.Birthday = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(customer.Birthday.Value);

            
            result.FacebookId = customer.FacebookId;
            result.TwitterId = customer.TwitterId;

            return Request.CreateResponse(HttpStatusCode.OK, result);
        }

        [ActionName("UserAuth")]
        public HttpResponseMessage PostUserAuth(Models.NamedAuthToken authToken)
        {
            epicuri.Core.DatabaseModel.Customer checkCustomer;

            try
            {
                string AuthKey = "";
                int AuthId = 0;
                try
                {
                    AuthKey = Request.Headers.Authorization.Parameter;
                    AuthId = int.Parse(AuthKey.Split('-')[0]);
                    AuthKey = AuthKey.Split('-')[1];
                }
                catch
                {
                    HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, "No Devicekey Sent");
                    response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                    return response;
                    //throw new Exception("No Devicekey Sent");
                }
                /*
                 * If auth key is null then throw an auth exception
                 */
                if (string.IsNullOrWhiteSpace(AuthKey))
                {
                    HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, "Device Key is null");
                    response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                    return response;
                    //throw new Exception("Device Key is null");
                }

                /*
                 * Check for devices with this key
                 */
                epicuri.Core.DatabaseModel.AuthenticationKey key = db.AuthenticationKeys.Where(k => k.Id == AuthId && k.Expires > DateTime.UtcNow).FirstOrDefault();

                if (key == null)
                {
                    HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, "No Key Found");
                    response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                    return response;
                    //throw new Exception("No Key Found");
                }

                if (key.Key == AuthKey)
                {
                    customer = db.Customers.Single(c => c.Id == key.CustomerId);
                }

                if (customer == null)
                {
                    HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Forbidden, "Customer not found");
                    return response;
                    //throw new Exception("Customer not found");
                }
            }
            catch (Exception e)
            {
                HttpResponseMessage response = Request.CreateResponse(HttpStatusCode.Unauthorized, e);
                response.Headers.Add("WWW-Authenticate", string.Format("Basic realm=\"{0}\"", "Epicuri"));
                return response;
            }

            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state:" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }

            if (authToken != null)
            {
                /*
                 * Check if token is empty
                 */
                if (String.IsNullOrWhiteSpace(authToken.Token))
                {
                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Token cannot be empty"));
                }
                                
                if (authToken.Provider == AuthProvider.Twitter)
                {
                    try
                    {
                        Twitter twUser = new Twitter(
                            System.Web.Configuration.WebConfigurationManager.AppSettings["TwitterAuth"],
                            System.Web.Configuration.WebConfigurationManager.AppSettings["TwitterSecret"],
                            authToken.Token,
                            authToken.Secret);
                        var uid = twUser.GetUserId();
                        checkCustomer = db.Customers.FirstOrDefault(c => c.TwitterId == uid);
                        if (checkCustomer != null)
                        {
                            return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("This Twitter account is already assigned to another user"));
                        }
                        customer.TwitterId = twUser.GetUserId();
                    }
                    catch
                    {
                        return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Invalid Twitter Token"));
                    }
                }
                else if (authToken.Provider == AuthProvider.Facebook)
                {
                    string id = "";
                    try
                    {
                        Facebook fbUser = new Facebook(authToken.Token);
                        id = fbUser.Id();
                        /*
                         * Get the epicuri user from the facebook user id
                         */
                        checkCustomer = db.Customers.FirstOrDefault(c => c.FacebookId == id);
                    }
                    catch
                    {
                        return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Invalid Facebook Token"));
                    }
                    if (checkCustomer != null)
                    {
                        return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("This facebook account is already assigned to another user"));
                    }
                    else
                    {
                        customer.FacebookId = id;
                    }
                }
                else
                {
                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Unknown token type"));
                }
            }

            db.SaveChanges();

            AuthenticationKey aKey = new AuthenticationKey
            {
                Key = Support.String.RandomString(24),
                Expires = DateTime.UtcNow.AddDays(30)
            };
            customer.AuthenticationKeys.Add(aKey);
            db.SaveChanges();

            return Request.CreateResponse(HttpStatusCode.OK);
        }
    }
}

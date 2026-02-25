using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using epicuri.Core.DatabaseModel;
using System.ComponentModel.DataAnnotations;
using epicuri.API.Models;
using epicuri.Core.Social;
using Newtonsoft.Json.Linq;
namespace epicuri.API.Controllers
{
    


    public class AuthenticationController : Support.APIController
    {
        public int GetIndex()
        {
            Authenticate();
            return 0;
        }


        [HttpPost]
        [ActionName("Register")]
        public HttpResponseMessage PostRegister(Models.Customer newCustomer)
        {
            
            if (!ModelState.IsValid)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Invalid model state:" + ModelState.First(ms => ms.Value.Errors.Count != 0).Key));
            }

            string Salt, Auth;

            /*
             * Check email not already in use
             */
            var emailCheckCust = db.Customers.FirstOrDefault(c => c.Email == newCustomer.Email);

            if (emailCheckCust != null)
            {
                Salt = emailCheckCust.Salt;
                Auth = Salt + newCustomer.GetPassword() + Salt;
                Auth = epicuri.API.Support.String.SHA1(Auth);

                // Check if their is not currently a facebook account (sorry already associated)
                if (!string.IsNullOrEmpty(emailCheckCust.FacebookId))
                {
                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Facebook account already associated with this email address"));
                }
                else {
                    return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("This email address already registered. Please retry with a different email address or email passwords@epicuri.co.uk for password recovery"));
                }
            }

            /*
             * Check for any 3rd party auth tokens
             */

            string FacebookId = newCustomer.FacebookId;
            string TwitterId = newCustomer.TwitterId;

            var t = newCustomer.GetAuthTokens();
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
                            String id = twUser.GetUserId();
                            getCustomer = db.Customers.FirstOrDefault(c => c.TwitterId == id);
                            TwitterId = twUser.GetUserId();
                        }
                        catch(Exception e)
                        {
                            return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Invalid Twitter Token",e));
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
                        try
                        {
                            Facebook fbUser = new Facebook(token.Token);
                            string id = fbUser.Id();
                            /*
                             * Get the epicuri user from the facebook user id
                             */
                            getCustomer = db.Customers.FirstOrDefault(c => c.FacebookId == id);
                            if (getCustomer != null)
                            {
                                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("This facebook account is already assigned to another user"));
                            }
                            else
                            {

                                FacebookId = id;
                            }
                        } 
                        catch
                        {
                            return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Invalid Facebook Token"));
                        }
                    }
                    else
                    {
                        return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Unknown token type"));
                    }
                }
            }

            if (newCustomer.GetPassword().Length < 6)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Password too short"));
            }

            /*
             * Encrypt password
             */
            Salt = Support.Salt.GetSalt();
            Auth = Salt + newCustomer.GetPassword() + Salt;
            Auth = epicuri.API.Support.String.SHA1(Auth);

            /*
             * Create new customer object and add to db
             * 
             */
            if (emailCheckCust != null)
            {
                if (!string.IsNullOrEmpty(FacebookId))
                    emailCheckCust.FacebookId = FacebookId;

                if (!string.IsNullOrEmpty(TwitterId))
                    emailCheckCust.TwitterId = TwitterId;

                customer = emailCheckCust;
            }
            else
            {
                if (newCustomer.Address == null)
                {
                    newCustomer.Address = new NullableAddress();
                }

                epicuri.Core.DatabaseModel.Customer cust = new Core.DatabaseModel.Customer
                {
                    Name = newCustomer.Name,
                    PhoneNumber = newCustomer.PhoneNumber,
                    Address = newCustomer.Address,
                    FacebookId = FacebookId,
                    Auth = Auth,
                    Salt = Salt,
                    TwitterId = TwitterId,
                    Email = newCustomer.Email,
                };

                if (newCustomer.Birthday.HasValue)
                    cust.Birthday = Core.Utils.Time.UnixTimeStampToDateTime(newCustomer.Birthday.Value);

                db.AddToCustomers(cust);
                customer = cust;
            }

            db.SaveChanges();
            
            AuthenticationKey k = new AuthenticationKey
            {
                Key = Support.String.RandomString(24),
                Expires = DateTime.UtcNow.AddDays(30)
            };
            customer.AuthenticationKeys.Add(k);
            db.SaveChanges();

            var result = new Models.Customer
            {
                Id = customer.Id,
                Name = customer.Name,
                Email = customer.Email,
                Address = customer.Address,
                AuthKey = k.Id + "-" + k.Key,
                PhoneNumber = customer.PhoneNumber,
                TwitterId = customer.TwitterId,
                FacebookId = customer.FacebookId,
                FavouriteFood = customer.FavouriteFood,
                FavouriteDrink = customer.FavouriteDrink,
            };

            if (customer.Birthday.HasValue)
                result.Birthday = Core.Utils.Time.DateTimeToUnixTimestamp(customer.Birthday.Value);

            if (customer.FacebookId != null)
                result.FacebookId = customer.FacebookId;

            if (customer.TwitterId != null)
                result.TwitterId = customer.TwitterId;

            return Request.CreateResponse(HttpStatusCode.Created, result);
   
        }

        [HttpPost]
        [ActionName("Authorize")]
        public HttpResponseMessage PostAuthorize(Models.NamedAuthToken token)
        {
            /*
             * Check if token is empty
             */
            if (String.IsNullOrWhiteSpace(token.Token))
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, new Exception("Token cannot be empty"));
            }

            Dictionary<string, object> response = new Dictionary<string,object>();

            /*
             * Get the user by the token
             */
            epicuri.Core.DatabaseModel.Customer getCustomer;


            // -- TWITTER --
            if (token.Provider == AuthProvider.Twitter)
            {
                string twitName;
                string uid;

                try
                {
                    Twitter twUser = new Twitter(
                        System.Web.Configuration.WebConfigurationManager.AppSettings["TwitterAuth"],
                        System.Web.Configuration.WebConfigurationManager.AppSettings["TwitterSecret"],
                        token.Token, 
                        token.Secret);
                    uid = twUser.GetUserId();
                    twitName = twUser.GetUserName();
                    getCustomer = db.Customers.FirstOrDefault(c => c.TwitterId == uid);
                }
                catch(Exception e)
                {
                    return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Invalid Twitter Token",e));
                }
                /*
                 * Check that the customer is not null
                 */
                if (getCustomer == null)
                {
                    response.Add("Status", "emailRequired");

                    response.Add("Twitter", new { Uid = uid, Firstname = twitName });
                    return Request.CreateResponse(HttpStatusCode.OK, response);
                }

                
                AuthenticationKey k = new AuthenticationKey
                {
                    Key = Support.String.RandomString(24),
                    Expires = DateTime.UtcNow.AddDays(30)
                };
                getCustomer.AuthenticationKeys.Add(k);
                db.SaveChanges();

                var result = new Models.Customer
                {
                    Id = getCustomer.Id,
                    Name = getCustomer.Name,
                    Email = getCustomer.Email,
                    Address = getCustomer.Address,
                    AuthKey = k.Id + "-" + k.Key,
                    PhoneNumber = getCustomer.PhoneNumber,
                    FavouriteFood = getCustomer.FavouriteFood,
                    FavouriteDrink = getCustomer.FavouriteDrink,
                    HatedFood = getCustomer.HatedFood,
                    FacebookId = getCustomer.FacebookId,
                    TwitterId = getCustomer.TwitterId
                };

                if (getCustomer.Birthday.HasValue)
                    result.Birthday = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(getCustomer.Birthday.Value);

                if (getCustomer.FacebookId != null)
                    result.FacebookId = getCustomer.FacebookId;

                if (getCustomer.TwitterId != null)
                    result.TwitterId = getCustomer.TwitterId;

                foreach (var allery in getCustomer.Allergies)
                {
                    result.Allergies.Add(allery.Id);
                }

                foreach (var dr in getCustomer.DietaryRequirements)
                {
                    result.DietaryRequirements.Add(dr.Id);
                }

                foreach (var fp in getCustomer.FoodPreferences)
                {
                    result.FoodPreferences.Add(fp.Id);
                }

                response.Add("Status", "authorised");
                response.Add("Customer", result);

                return Request.CreateResponse(HttpStatusCode.OK, response);
            }

            // -- FACEBOOK -- 
            else if (token.Provider == AuthProvider.Facebook)
            {
                string fbFirst_name;
                string fbLast_name;
                string fbEmail;

                try
                {
                    Facebook fbUser = new Facebook(token.Token);
                    string id = fbUser.Id();
                    fbFirst_name = fbUser.First_name();
                    fbLast_name = fbUser.Last_name();
                    fbEmail = fbUser.Email();

                    /*
                     * Get the epicuri user from the facebook user id
                     */
                    getCustomer = db.Customers.FirstOrDefault(c => c.FacebookId == id);

                }
                catch
                {
                    return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Invalid Facebook Token"));
                }
                /*
                 * Check that the customer is not null
                 */
                if (getCustomer == null)
                {
                    //Check for the email to see if it's in the database already ? email already in use : create new user based on details

                    var emailCheckCust = db.Customers.FirstOrDefault(c => c.Email == fbEmail);
                    if (emailCheckCust != null)
                    {
                        return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Email already in use.  Please login using Email and Password"));
                    }
                    else
                    {
                        List<NamedAuthToken> AuthToken = new List<NamedAuthToken>();
                        AuthToken.Add(token);
                        IEnumerable<NamedAuthToken> IEAuthToken = AuthToken;

                        var newCustomer = new Models.Customer
                        {
                            Email = fbEmail,
                            Name = Name.CreateName(fbFirst_name, fbLast_name),
                            Auth = IEAuthToken
                        };
                        PostRegister(newCustomer);
                    }
                    
                    try
                    {
                    Facebook fbUser = new Facebook(token.Token);
                    string id = fbUser.Id();

                    /*
                     * Get the epicuri user from the facebook user id
                     */
                    getCustomer = db.Customers.FirstOrDefault(c => c.FacebookId == id);

                    }
                    catch
                    {
                        return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("Invalid Facebook Token"));
                    }
                }

                AuthenticationKey k = new AuthenticationKey
                {
                    Key = Support.String.RandomString(24),
                    Expires = DateTime.UtcNow.AddDays(30)
                };
                getCustomer.AuthenticationKeys.Add(k);
                db.SaveChanges();

                var result = new Models.Customer
                {
                    Id = getCustomer.Id,
                    Name = getCustomer.Name,
                    Email = getCustomer.Email,
                    Address = getCustomer.Address,
                    AuthKey = k.Id + "-" + k.Key,
                    PhoneNumber = getCustomer.PhoneNumber,
                    FavouriteFood = getCustomer.FavouriteFood,
                    FavouriteDrink = getCustomer.FavouriteDrink,
                    HatedFood = getCustomer.HatedFood,
                    FacebookId = getCustomer.FacebookId,
                    TwitterId = getCustomer.TwitterId
                };

                if (getCustomer.Birthday.HasValue)
                    result.Birthday = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(getCustomer.Birthday.Value);

                if (getCustomer.FacebookId != null)
                    result.FacebookId = getCustomer.FacebookId;

                if (getCustomer.TwitterId != null)
                    result.TwitterId = getCustomer.TwitterId;

                foreach (var allery in getCustomer.Allergies)
                {
                    result.Allergies.Add(allery.Id);
                }

                foreach (var dr in getCustomer.DietaryRequirements)
                {
                    result.DietaryRequirements.Add(dr.Id);
                }

                foreach (var fp in getCustomer.FoodPreferences)
                {
                    result.FoodPreferences.Add(fp.Id);
                }

                response.Add("Status", "authorised");
                response.Add("Customer", result);

                return Request.CreateResponse(HttpStatusCode.OK, response);
             
            }
            else
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden, new Exception("No such user"));
            }
     
        }



        [HttpPost]
        [ActionName("Login")]
        public HttpResponseMessage PostLogin(AuthPayload p) 
        {
    

            epicuri.Core.DatabaseModel.Customer customer = db.Customers.FirstOrDefault(c => c.Email == p.Email);
            
            
            if (customer == null)
            {
                return Request.CreateResponse(HttpStatusCode.Forbidden);
            }

            string Salt = customer.Salt;
            string Auth = Salt + p.Password + Salt;
            Auth = epicuri.API.Support.String.SHA1(Auth);


            if (customer.Auth == Auth)
            {
                /*
                 * Create an auth and link it to the custoemr
                 */
                AuthenticationKey k = new AuthenticationKey
                {
                    Key = Support.String.RandomString(24),
                    Expires = DateTime.UtcNow.AddDays(30)
                };
                customer.AuthenticationKeys.Add(k);
                db.SaveChanges();

                var authedCustomer = new Models.Customer
                    {
                        Id = customer.Id,
                        Name = customer.Name,
                        Email = customer.Email,
                        Address = customer.Address,
                        AuthKey = k.Id + "-" + k.Key,
                        PhoneNumber = customer.PhoneNumber,
                        FavouriteFood = customer.FavouriteFood,
                        FavouriteDrink = customer.FavouriteDrink,
                        HatedFood = customer.HatedFood,
                        FacebookId = customer.FacebookId,
                        TwitterId = customer.TwitterId
                    };

                if (customer.Birthday.HasValue)
                    authedCustomer.Birthday = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(customer.Birthday.Value);

                if (customer.FacebookId != null)
                    authedCustomer.FacebookId = customer.FacebookId;

                if (customer.TwitterId != null)
                    authedCustomer.TwitterId = customer.TwitterId;

                foreach (var allery in customer.Allergies)
                {
                    authedCustomer.Allergies.Add(allery.Id);
                }

                foreach (var dr in customer.DietaryRequirements)
                {
                    authedCustomer.DietaryRequirements.Add(dr.Id);
                }

                foreach (var fp in customer.FoodPreferences)
                {
                    authedCustomer.FoodPreferences.Add(fp.Id);
                }


                Dictionary<string, object> response = new Dictionary<string, object>();
                response.Add("Status", "authorised");
                response.Add("Customer", authedCustomer);

                return Request.CreateResponse(HttpStatusCode.OK, response);
            }


            return Request.CreateResponse(HttpStatusCode.Forbidden);
        }

        
    }
}

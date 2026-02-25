using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Web;

namespace epicuri.CPE.Mews
{
    public static class MewsAPI
    {
        /// <summary>
        /// Performs a customer search on the Mews API
        /// </summary>
        /// <param name="name"></param>
        /// <param name="roomNumber"></param>
        /// <returns></returns>
        public static MewsResponse SearchCustomer(string accessToken, string name = null, string roomNumber = null)
        {
            using (var client = new HttpClient())
            {
                client.BaseAddress = new Uri(System.Configuration.ConfigurationManager.AppSettings["MewsUrl"]);
                client.DefaultRequestHeaders.Accept.Clear();
                client.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));

                Dictionary<string, string> bodyDic = new Dictionary<string, string>();
                bodyDic.Add("AccessToken", accessToken);

                if (!string.IsNullOrEmpty(name))
                    bodyDic.Add("Name", name);

                if (!string.IsNullOrEmpty(roomNumber))
                    bodyDic.Add("RoomNumber", roomNumber);

                StringContent content = new StringContent(JsonConvert.SerializeObject(bodyDic, new KeyValuePairConverter()), System.Text.UTF8Encoding.UTF8, "application/json");

                HttpResponseMessage response = client.PostAsync("customers/search", content).Result;

                if (response.StatusCode == System.Net.HttpStatusCode.OK)
                {
                    var result = JsonConvert.DeserializeObject<Dictionary<string, object>>(response.Content.ReadAsStringAsync().Result)["Customers"];

                    return new MewsResponse()
                    {
                        Code = System.Net.HttpStatusCode.OK,
                        Result = ((Newtonsoft.Json.Linq.JArray)result).ToObject<List<MewsCustomer>>()
                    };

                }
                else
                {
                    return new MewsResponse()
                    {
                        Code = response.StatusCode,
                        Result = response.Content.ReadAsStringAsync().Result
                    };
                }

            }
        }

        /// <summary>
        /// Commit a charge to a customer
        /// </summary>
        /// <param name="charge"></param>
        /// <returns></returns>
        public static MewsResponse ChargeCustomer(string accessToken, Mews.MewsCharge charge)
        {
            using (var client = new HttpClient())
            {
                client.BaseAddress = new Uri(System.Configuration.ConfigurationManager.AppSettings["MewsUrl"]);
                client.DefaultRequestHeaders.Accept.Clear();
                client.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));

                Dictionary<string, object> bodyDic = new Dictionary<string, object>();
                bodyDic.Add("AccessToken", accessToken);

                bodyDic.Add("CustomerId", charge.Customer.Id);
                bodyDic.Add("Items", charge.Items);

                if (!string.IsNullOrEmpty(charge.Notes))
                    bodyDic.Add("Notes", charge.Notes);

                StringContent content = new StringContent(JsonConvert.SerializeObject(bodyDic, new KeyValuePairConverter()), System.Text.UTF8Encoding.UTF8, "application/json");

                HttpResponseMessage response = client.PostAsync("customers/charge", content).Result;

                if (response.StatusCode == System.Net.HttpStatusCode.OK)
                {
                    var result = JsonConvert.DeserializeObject<Dictionary<string, object>>(response.Content.ReadAsStringAsync().Result)["ChargeId"];

                    return new MewsResponse() {
                        Code = System.Net.HttpStatusCode.OK,
                        Result = (string)result
                    };
                    
                }
                else
                {
                    return new MewsResponse()
                    {
                        Code = response.StatusCode,
                        Result = response.Content.ReadAsStringAsync().Result
                    };
                }

            }
        }

    }
}
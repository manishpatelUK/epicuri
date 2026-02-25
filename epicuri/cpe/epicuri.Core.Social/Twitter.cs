using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DotNetOpenAuth.OAuth;
using System.Net;
using Newtonsoft.Json;
using System.Configuration;

namespace epicuri.Core.Social
{
    public class Twitter
    {
        TwitterUser User;

        public Twitter(string TwitterAuth, string TwitterSecret, string AccessToken, string AccessSecret)
        {
            var oauth = new epicuri.Core.Social.OAuth.Manager(TwitterAuth, TwitterSecret, AccessToken, AccessSecret);
            string header = oauth.GenerateAuthzHeader("https://api.twitter.com/1.1/account/verify_credentials.json", "GET");

            var request = (HttpWebRequest)WebRequest.Create("https://api.twitter.com/1.1/account/verify_credentials.json");
            
            request.Method = "GET";
            request.PreAuthenticate = true;
            request.AllowWriteStreamBuffering = true;
            request.Headers.Add("Authorization", header);

            using (var response = (HttpWebResponse)request.GetResponse())
            {
               
                var encoding = ASCIIEncoding.ASCII;
                using (var reader = new System.IO.StreamReader(response.GetResponseStream(), encoding))
                {
                    string responseText = reader.ReadToEnd();
                    User = JsonConvert.DeserializeObject<TwitterUser>(responseText);
                }

            }

        }
        public string GetUserId()
        {
            return User.id;
        }

        public string GetUserName()
        {
            return User.name;
        }
    }

    class TwitterUser
    {
        public String id;
        public String name;
    }
}

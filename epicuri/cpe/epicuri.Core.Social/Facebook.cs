using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Redslide.HttpLib;
using Newtonsoft.Json;
using System.Threading;

namespace epicuri.Core.Social
{
    public class Facebook
    {
        private const string BaseUrl = "https://graph.facebook.com/";
        private string Token;
        private FacebookUser User;
        public event EventHandler DataReceived;
        
        public Facebook(string Token)
        {
            var completion = new ManualResetEvent(false);
            
            this.Token = Token;
            Request.Get(BaseUrl + "/me", new
            {
                fields = "id, first_name, last_name, email",
                access_token = this.Token,
            },
            result =>
            {
                User = JsonConvert.DeserializeObject<FacebookUser>(result);
                completion.Set();
            },
            ex =>
            {
                
                completion.Set();
                
            });

            completion.WaitOne();
        }

        public String Id()
        {
            return User.id;
        }
        public String First_name()
        {
            return User.first_name;
        }
        public String Last_name()
        {
            return User.last_name;
        }
        public String Email()
        {
            return User.email;
        }


        class FacebookUser
        {
            public String id;
            public String first_name;
            public String last_name;
            public String email;
        }
    }
}

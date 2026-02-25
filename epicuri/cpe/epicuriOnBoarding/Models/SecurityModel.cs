using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuriOnBoarding.Models
{
    public class SecurityModel
    {

        public virtual string Username { get; set; }
        public virtual string Password { get; set; }
        public virtual string NewPassword { get; set; }
    
    }
}
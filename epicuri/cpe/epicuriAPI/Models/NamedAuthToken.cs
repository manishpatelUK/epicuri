using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace epicuri.API.Models
{
    public class NamedAuthToken
    {
        public AuthProvider Provider;
        public String Token;
        public String Secret;
    }
}

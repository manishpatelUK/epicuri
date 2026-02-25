using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Web;

namespace epicuri.CPE.Mews
{
    public class MewsResponse
    {
        public HttpStatusCode Code { get; set; }
        public object Result { get; set; }
    }
}
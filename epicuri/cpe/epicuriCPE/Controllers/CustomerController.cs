using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.CPE.Controllers
{
    public class CustomerController : Models.EpicuriApiController
    {
        public HttpResponseMessage GetCustomer(string phoneNumber = null, string email = null)
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

            var results = from cust in this.db.Customers
                           where cust.PhoneNumber.Replace(" ", "") == phoneNumber.Replace(" ", "") || 
                               cust.Email.ToLower() == email.ToLower()
                           select cust;

            List<Models.Customer> outp = new List<Models.Customer>();
            foreach (var c in results)
            {
                outp.Add(new Models.Customer(c));
            }

            return Request.CreateResponse(HttpStatusCode.OK, outp);
        }
    }
}

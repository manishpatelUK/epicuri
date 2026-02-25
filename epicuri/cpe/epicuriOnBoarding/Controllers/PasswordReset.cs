using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.Core.DatabaseModel;
using epicuriOnBoarding.Filters;
namespace epicuriOnBoarding.Controllers
{
    [Authorize]
    [InitializeSimpleMembership]
    public class PasswordResetController : Controller
    {
        private epicuriContainer db = new epicuriContainer();

        //
        // GET: /Tax/

        public ActionResult Index()
        {
            return View();
        }

        [HttpPost]
        public ActionResult Index(String emailAddress)
        {
            string returnMessage = "There was an error processing the request. Password has not been reset";

            if (ModelState.IsValid)
            {
                Customer customer = db.Customers.FirstOrDefault(c => c.Email == emailAddress);
                if (customer == null || emailAddress == "")
                {
                    returnMessage = "Invalid email address. Please try again.";
                }
                else
                {
                    // Change the password for the user
                    string resetPassword = GetRandomPasswordUsingGUID(8);
                    returnMessage = "Password reset successfully.  New Password is: " + resetPassword;

                    string Salt = epicuri.API.Support.Salt.GetSalt();
                    string Auth = Salt + resetPassword + Salt;
                    Auth = epicuri.API.Support.String.SHA1(Auth);
                    customer.Auth = Auth;
                    customer.Salt = Salt;

                    db.SaveChanges();
                }
            }
     
            ViewBag.StatusMessage = returnMessage;
            return View();
        }

        public string GetRandomPasswordUsingGUID(int length)
        {
            // Get the GUID
            string guidResult = System.Guid.NewGuid().ToString();

            // Remove the hyphens
            guidResult = guidResult.Replace("-", string.Empty);

            // Make sure length is valid
            if (length <= 0 || length > guidResult.Length)
                throw new ArgumentException("Length must be between 1 and " + guidResult.Length);

            // Return the first length bytes
            return guidResult.Substring(0, length);
        }
    
        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }
    }
}
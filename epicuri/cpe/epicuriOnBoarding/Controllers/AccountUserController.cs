using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuriOnBoarding.Models;
using System.Web.Security;
using WebMatrix.WebData;
namespace epicuriOnBoarding.Controllers
{
    [Authorize]
    public class AccountUserController : Controller
    {
        public ViewResult Index()
        {
            var memberList = Membership.GetAllUsers();
            var model = new List<AccountUser>();
            foreach (MembershipUser user in memberList)
            {
                model.Add(new AccountUser(user.ProviderUserKey.ToString()));
            }
            return View(model);
        }

        public ViewResult Details(Guid id)
        {
            AccountUser accountuser = new AccountUser(id);
            return View(accountuser);
        }

        public ActionResult Create()
        {
            return View();
        }

        [HttpPost]
        public ActionResult Create(AccountUser myUser)
        {
            if (ModelState.IsValid)
            {
                WebSecurity.c
                 Membership.CreateUser(myUser.UserName, myUser.Password, myUser.Email);

                return RedirectToAction("Index");
            }

            return View(myUser);
        }

        public ActionResult Edit(Guid id)
        {
            AccountUser accountuser = new AccountUser(id);
            return View(accountuser);
        }

        [HttpPost]
        public ActionResult Edit(AccountUser accountuser)
        {
            if (ModelState.IsValid)
            {
                return RedirectToAction("Index");
            }
            return View(accountuser);
        }

        public ActionResult Delete(Guid id)
        {
            AccountUser accountuser = new AccountUser(id);
            return View(accountuser);
        }

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(Guid id)
        {
            AccountUser accountuser = new AccountUser(id);
            Membership.DeleteUser(accountuser.User.UserName);

            return RedirectToAction("Index");
        }

        protected override void Dispose(bool disposing)
        {
            //db.Dispose();
            base.Dispose(disposing);
        }
    }
}

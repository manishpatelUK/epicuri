using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations.Schema;
using System.ComponentModel.DataAnnotations;
using System.Web.Security;

namespace epicuriOnBoarding.Models
{
    public class AccountUser
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.None)]
        public virtual Guid UserId { get; set; }

        [Display(Name = "User Name")]
        public virtual string UserName { get; set; }

        [Display(Name = "E-mail")]
        public virtual string Email { get; set; }

        public virtual string Password { get; set; }

        [Display(Name = "Approved")]
        public virtual bool IsApproved { get; set; }

        /* contructors based on string GUID or actual */
        public AccountUser() { }
        public AccountUser(string UID)
        {
            UserId = new Guid(UID);
            Initialize();
        }

        public AccountUser(Guid UID)
        {
            UserId = UID;
            Initialize();
        }

        /* loads Membership User into model to access other properties */
        public virtual MembershipUser User
        {
            get
            {
                // note that I don't have a test for null in here, 
                // but should in a real case.
                return Membership.GetUser(UserId);
            }
        }

        /* do this once when opening a user instead of every time you access one of these three *
         * as well as allow override when editing / creating                                    */
        private void Initialize()
        {
            UserName = User.UserName;
            Email = User.Email;
            IsApproved = User.IsApproved;
        }

    }
}
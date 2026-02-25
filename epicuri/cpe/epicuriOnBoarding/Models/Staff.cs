using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using System.ComponentModel.DataAnnotations;
using System.Text.RegularExpressions;

namespace epicuriOnBoarding.Models
{
    public class Staff
    {
        private epicuri.Core.DatabaseModel.Staff User;

        private bool passwordChanged = false;
        public bool PasswordChanged()
        {
            return passwordChanged;
        }
        public epicuri.Core.DatabaseModel.Staff GetUser() { 
            return User; }
        public void init()
        {
            User = new epicuri.Core.DatabaseModel.Staff();
            User.Deleted = false;
        }
        public Staff()
        {
            init();   
        }

        public Staff(epicuri.Core.DatabaseModel.Staff staff)
        {
            User = staff;
        }


        [HiddenInput(DisplayValue = false)]
        public int Id
        {
            get
            {
                return User.Id;
            }
            
        }


        [HiddenInput(DisplayValue = false)]
        public int RestaurantId
        {
            get
            {
                if (this.User == null)
                {
                    init();
                }
                return User.RestaurantId;
            }
            set
            {
                User.RestaurantId = value;
            }
        }

        [Required]
        public String Name
        {
            get
            {
                if (this.User == null)
                {
                    init();
                }
                return User.Name;
            }
            set
            {
                User.Name = value;
            }
        }
        
        [Required]
        public String Username
        {
            get
            {
                if (this.User == null)
                {
                    init();
                }
                return User.Username;
            }
            set
            {
                User.Username = value;

            }
        }

        [Required (ErrorMessage="Pin must be exactly 4 digits")]
        public string Pin
        {
            get
            {
                if (this.User == null)
                {
                    init();
                }
                return User.Pin;
            }
            set
            {
                string reg = @"^[0-9]{4}$";
                if (Regex.IsMatch(value, reg))
                {
                    User.Pin = value;
                } 
            }
        }

        public String Password
        {
            set
            {
                if (!String.IsNullOrWhiteSpace(value))
                {
                    passwordChanged = true;
                    string Salt = epicuri.API.Support.Salt.GetSalt();
                    string Auth = Salt + value + Salt;
                    User.Auth = epicuri.API.Support.String.SHA1(Auth);
                    User.Salt = Salt;
                }
                else
                {
                    
                }

            }
            get
            {
                return "";
            }
        }

        public Boolean Manager
        {
            get
            {
                if (User.Roles.Count(r => r.Name == "Manager") > 0)
                {
                    return true;
                }
                return false;
            }
            set
            {
                foreach (var role in User.Roles.ToList())
                {
                    User.Roles.Remove(role);
                }
                if (value)
                {
                    User.Roles.Add(new epicuri.Core.DatabaseModel.Role
                    {
                        Name = "Manager"
                    });                  
                }

            }
        }
    }
}
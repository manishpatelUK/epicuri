using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class User
    {
        public int Id;
        public string Name;
        public IEnumerable<Role> Roles;
        public string Email;
        public string Pin;
    }
}
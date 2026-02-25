using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class Staff
    {
        public int Id;
        public String Name;
        public String Username;
        public String AuthKey;
        public String Pin;
        public bool Manager;
        public String Password;
        
        public Staff() { }
        public Staff(Core.DatabaseModel.Staff staff)
        {
            if (staff == null) {
                Id = 0;
                Name = "";
                Username = "";
                Pin = "";
                Manager = false;
            }
            else
            {
                Id = staff.Id;
                Name = staff.Name;
                Pin = staff.Pin;
                Username = staff.Username;
                Manager = false;
                if (staff.Roles.Count(r => r.Name == "Manager") > 0)
                {
                    Manager = true;
                }
            }
        }


        public Staff(Core.DatabaseModel.Staff staff, string Key)
        {
            Id = staff.Id;
            Name = staff.Name;
            Pin = staff.Pin.ToString();
            Username = staff.Username;
            AuthKey = Key;

            if (staff.Roles.Count(r => r.Name == "Manager") > 0)
            {
                Manager = true;
            }
        }

    }
}
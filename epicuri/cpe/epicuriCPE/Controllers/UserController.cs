using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace epicuri.CPE.Controllers
{
    public class UserController : Models.EpicuriApiController
    {
        public IEnumerable<Models.User> GetUsers()
        {
            Authenticate();

            var users = from user in this.Restaurant.Staffs
                        select new Models.User
                        {
                            Name = user.Name,
                            Id = user.Id,
                            Email = user.Username,
                            Pin = user.Pin,
                            Roles = from role in user.Roles
                                    select new Models.Role
                                    {
                                        Id = role.Id,
                                        Name = role.Name
                                    }
                        };
            return users;
        }
    }
}

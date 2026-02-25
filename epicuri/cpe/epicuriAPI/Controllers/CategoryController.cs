using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using System.Net;
using System.Net.Http;

namespace epicuri.API.Controllers
{
    public class CategoryController : Support.APIController
    {
        [HttpGet]
        public HttpResponseMessage GetIndex()
        {
            return Request.CreateResponse(HttpStatusCode.OK, db.Categories.Select(o=>new {Id=o.Id, Name=o.Name}));
        }

    }
}

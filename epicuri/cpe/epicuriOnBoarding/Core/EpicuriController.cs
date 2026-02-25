using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;

namespace epicuriOnBoarding.Core
{
    public class EpicuriController : Controller
    {
        protected epicuri.Core.DatabaseModel.epicuriContainer db;
        public EpicuriController()
        {
            db = new epicuri.Core.DatabaseModel.epicuriContainer();
        }
    }
}
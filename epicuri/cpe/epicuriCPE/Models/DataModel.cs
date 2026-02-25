using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace epicuri.CPE.Models
{
    public class DataModel
    {
        protected epicuri.Core.DatabaseModel.epicuriContainer db;
        protected void Init()
        {
            db = new Core.DatabaseModel.epicuriContainer();
        }

        
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    public class SessionPayload
    {
        [Required]
        public int ServiceId;
  //      [Required]
    //    public int PartyId;
        public int[] Tables;
        public Boolean IsAdHoc;
    }
}
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    public class PartyUpdateAndSessionId
    {
        [Required]
        public WaitingParty PartyUpdate { get; set; }
        [Required]
        public int SessionId { get; set; }


    }
}
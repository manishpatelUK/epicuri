using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Models
{
    public class SessionValueModification
    {
        public String Name { get; set; }
        public decimal AbsAdjustment { get; set; }
        public DateTime Date { get; set; }
        public Staff Staff { get; set; }
        public String AdjustmentType { get; set; }
        public int SessionId { get; set; }
        public String SessionType { get; set; }
        public Models.MewsAdjustment Mews { get; set; }
    }
}
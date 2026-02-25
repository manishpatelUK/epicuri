using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.CPE.Mews
{
    public class MewsPayload
    {
        public int SessionId { get; set; }
        public MewsCustomer Customer { get; set; }
        public decimal PaymentAmount { get; set; }
    }


    public class MewsCustomer
    {
        public string Id { get; set; }
        public string FirstName { get; set; }
        public string LastName { get; set; }
        public string RoomNumber { get; set; }

    }

    public class MewsCharge
    {
        public MewsCustomer Customer { get; set; }

        public List<MewsChargeItem> Items { get; set; }

        public string Notes { get; set; }
    }

    public class MewsChargeItem
    {
        public string Name { get; set; }
        public int UnitCount { get; set; }
        public MewsChargeItemUnitCost UnitCost { get; set; }
        public MewsChargeItemCategory Category { get; set; }
    }

    public class MewsChargeItemUnitCost
    {
        public decimal Amount { get; set; }
        public string Currency { get; set; }
        public double Tax { get; set; }
    }

    public class MewsChargeItemCategory
    {
        public string Code { get; set; }
        public string Name { get; set; }     
    }
}
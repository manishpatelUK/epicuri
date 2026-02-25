using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace epicuri.Core.DatabaseModel
{
    public class Enums
    {
        public enum AdjustmentTypeType
        {
            Payment=0, Discount=1
        }

        public enum NumericalTypeType
        {
            Price=0, Percentage=1
        }

        public enum SupportsChangeType
        {
            NoChange = 0, Change = 1
        }


        public enum MenuItemType
        {
            Food = 0, Drink = 1, Other = 2
        }

        public enum ReceiptType
        {
            Standard = 0, Hotel = 1
        }
    }
}

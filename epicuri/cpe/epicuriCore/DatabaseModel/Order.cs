using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace epicuri.Core.DatabaseModel
{
    public partial class Order
    {
        public Decimal CalculatedPrice()
        {
            if (this.PriceOverride != null)
                return Decimal.Parse(this.PriceOverride.Value.ToString("0.00"));

            double price = this.MenuItem.Price;

            foreach (var modifier in this.Modifiers)
            {
                price += modifier.Cost;
            }

            var ps = price.ToString("0.00");
            return Decimal.Parse(ps);

        }

        public Decimal CalculatedVAT()
        {
            if (this.MenuItem.TaxType==null) {
                return 0m;
            }
            // this ignores the VAT rate of any modifiers
            if (this.PriceOverride != null)
                return Decimal.Parse(Math.Round((this.PriceOverride.Value * (this.MenuItem.TaxType.Rate / (100 + this.MenuItem.TaxType.Rate))),2).ToString("0.00"));

            double price = this.MenuItem.Price - (this.MenuItem.Price / ((100 + this.MenuItem.TaxType.Rate)/100));

            foreach (var modifier in this.Modifiers)
            {
                price += modifier.Cost - (modifier.Cost  / ((100 + modifier.TaxType.Rate)/100 ));
            }


            var p2 = Math.Round(price,2).ToString();
            
            return Decimal.Parse(p2);
        }
    }
}

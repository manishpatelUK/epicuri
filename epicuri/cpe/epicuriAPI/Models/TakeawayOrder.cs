using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using epicuri.Core;

namespace epicuri.API.Models
{
    public class TakeAwayOrder : CPE.Models.DataModel
    {
        public int Id { get; set; }
        public int RestaurantId { get; set; }
        public bool Delivery { get; set; }
        public double ExpectedTime { get; set; }
        public bool Accepted { get; set; }
        public decimal Total { get; set; }
        public epicuri.Core.DatabaseModel.Address Address { get; set; }
        public IEnumerable<Order> Items { get; set; }
        public double RequestedTime { get; set; }
        public int InstantiatedFromId { get; set; }
        public string Notes { get; set; }
        public bool Deleted { get; set; }
        public decimal? DeliveryCost { get; set; }
        public int ItemCount { get; set; }
        public String Telephone { get; set; }
        public decimal Discounts { get; set; }
        public decimal SubTotal { get; set; }
        public bool CheckMaxTakeawaysPerHour()
        {
            Init();

            // Use the RequestedTime not the current time
            DateTime minTime = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(this.RequestedTime).AddMinutes(-30);
            DateTime maxTime = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(this.RequestedTime).AddMinutes(30);
            //DateTime minTime = GetExpectedTime().AddMinutes(-30);
            //DateTime maxTime = GetExpectedTime().AddMinutes(30);


            var maxPerHour = Core.Settings.Setting<int>(RestaurantId, "MaxTakeawaysPerHour");
            
            var activeTakeaways = db
                                    .Sessions
                                    .OfType<Core.DatabaseModel.TakeAwaySession>()
                                    .Count(s =>
                                        s.RestaurantId == RestaurantId &&
                                        s.ExpectedTime >= minTime &&
                                        s.ExpectedTime <= maxTime &&
                                        s.Deleted == false &&
                                        s.Rejected == false &&
                                        s.Accepted == true
                                        );
            return activeTakeaways < maxPerHour;
            
        }

        internal DateTime GetExpectedTime()
        {
            return epicuri.Core.Utils.Time.UnixTimeStampToDateTime(
                Math.Max(
                epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(DateTime.UtcNow.AddMinutes(epicuri.Core.Settings.Setting<int>(RestaurantId,"TakeawayMinimumTime"))),
                RequestedTime));
        }
        
        internal bool CheckMinPrice()
        {
            return this.Total >= Settings.Setting<decimal>(RestaurantId, "MinTakeawayValue");
        }

        internal bool CheckMaxPrice()
        {
            return this.Total <= Settings.Setting<decimal>(RestaurantId, "MaxTakeawayValue");
        }


        double GetDeliveryDistance()
        {
            Init();
            var rest = db.Restaurants.Where(r=>r.Id == RestaurantId).FirstOrDefault();



            /*
             * If the restaurant doesn't have lat/long, we cant accept orders
             */
            if (rest.Position.Longitude == 0)
            {
                return 999;
            }

            Tuple<double, double> pos = new Tuple<double,double>(0, 0);

            try
            {
                pos = Core.Utils.GeoCoding.LatLongFromPostCode(Address.PostCode);
            }
            catch (Exception)
            {
                Console.WriteLine("Did find lat/long");
            }

            if (pos.Item1 == 0 && pos.Item2 == 0)
            {
                // Rubbish postcode so return -1
                return -1;
            }
            
            return Core.Utils.GeoCoding.CalcDistance(pos.Item1, pos.Item2, rest.Position.Latitude, rest.Position.Longitude);
        }

        internal bool CheckDeliveryRadius()
        {
            double dist = GetDeliveryDistance();

            if (dist == -1)
            {
                // Bad Postcode, continue with order, but flag up for waiter
                this.Accepted = false;
                return true;
            }
            else
            {
                this.Accepted = true;
            }

            return dist <= Settings.Setting<double>(RestaurantId, "MaxDeliveryRadius");
        }

        internal bool CheckDeliverySurcharge()
        {
            return GetDeliveryDistance() <= Settings.Setting<double>(RestaurantId, "FreeDeliveryRadius");
        }

        internal decimal? CheckDeliveryCost(int RestId)
        {
            Init();
            var rest = db.Restaurants.Where(r => r.Id == RestaurantId).FirstOrDefault();
            /*
             * If the restaurant doesn't have lat/long, we cant accept orders
             */
            if (rest.Position.Longitude == 0)
            {
                return 999;
            }

            Tuple<double, double> pos = new Tuple<double, double>(0, 0);

            if (this.Address == null)
            {
                return null;
            }

            pos = Core.Utils.GeoCoding.LatLongFromPostCode(this.Address.PostCode);

            if (pos.Item1 == 0 && pos.Item2 == 0)
            {
                // Rubbish postcode so return -1
                return null;
            }

            double deliveryDistance = Core.Utils.GeoCoding.CalcDistance(pos.Item1, pos.Item2, rest.Position.Latitude, rest.Position.Longitude);

            bool freeDelivery = deliveryDistance <= Settings.Setting<double>(RestaurantId, "FreeDeliveryRadius");

            if (freeDelivery)
                return 0;
            return Core.Settings.Setting<decimal>(RestaurantId, "DeliverySurcharge");
        }

    }


}
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;
using epicuri.Core;

namespace epicuri.API.Models
{
    public class Reservation : CPE.Models.DataModel
    {
        public int Id { get; set; }
        public string Notes { get; set; }
        [Required]
        public String Telephone { get; set; }
        [Required]
        public double ReservationTime { get; set; }
        public short NumberOfPeople { get; set; }
        [Required]
        public int RestaurantId { get; set; }
        public Restaurant Restaurant;
        public int SessionId { get; set; }
        public double? ArrivedTime { get; set; }

        public bool Accepted;
        public bool Rejected;
        public string RejectionNotice;
        public bool Deleted;

        public DateTime _Time
        {
            set
            {
                ReservationTime = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(value);
            }
        }
        public int InstantiatedFromId { get; set; }

        public bool CheckMaxActiveReservationsCovers(Reservation currentReservation)
        {
            Init();

            DateTime minTime = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(ReservationTime).AddMinutes(-Core.Settings.Setting<int>(RestaurantId, "ReservationTimeSlot"));
            DateTime maxTime = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(ReservationTime).AddMinutes(Core.Settings.Setting<int>(RestaurantId, "ReservationTimeSlot"));

            DateTime minWalkinTime = DateTime.UtcNow.AddMinutes(-Settings.Setting<double>(RestaurantId, "WalkinExpirationTime"));

            var activeCovers = db
                .Parties
                .OfType<Core.DatabaseModel.Reservation>()
                .Where(r =>
                    r.RestaurantId == RestaurantId &&
                    r.Session == null && 
                    r.Accepted == true &&
                    r.Rejected == false &&
                    r.Deleted == false &&
                    r.Id != currentReservation.Id && 
                    r.ReservationTime > minTime &&
                    r.ReservationTime < maxTime &&
                    r.CheckIn.FirstOrDefault(chk => chk.Time < minWalkinTime) == null);
            int totalCovers = 0;

            if (activeCovers.Count() > 0)
            {
                totalCovers = activeCovers.Sum(r => r.NumberOfPeople);
            }

            totalCovers += currentReservation.NumberOfPeople;

            int maxCovers = Settings.Setting<int>(RestaurantId, "MaxActiveReservationsCovers");
            return totalCovers < maxCovers;
        }

        public bool CheckMaxActiveReservations(int currentReservation)
        {
            Init();

            DateTime minTime = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(ReservationTime).AddMinutes(-Core.Settings.Setting<int>(RestaurantId, "ReservationTimeSlot"));
            DateTime maxTime = epicuri.Core.Utils.Time.UnixTimeStampToDateTime(ReservationTime).AddMinutes(Core.Settings.Setting<int>(RestaurantId, "ReservationTimeSlot"));

            DateTime minWalkinTime = DateTime.UtcNow.AddMinutes(-Settings.Setting<double>(RestaurantId, "WalkinExpirationTime"));

            int activeReservations = db
                .Parties
                .OfType<Core.DatabaseModel.Reservation>()
                .Count(r =>
                    r.RestaurantId == RestaurantId &&
                    r.Session == null && 
                    r.Accepted == true &&
                    r.Rejected == false &&
                    r.Deleted == false &&
                    r.Id != currentReservation && 
                    r.ReservationTime > minTime &&
                    r.ReservationTime < maxTime &&
                    r.CheckIn.FirstOrDefault(chk => chk.Time < minWalkinTime) == null);

            int maxActive =  Core.Settings.Setting<int>(RestaurantId, "MaxActiveReservations");
            return activeReservations < maxActive;
        }


        public bool CheckMaxCoversPerReservation()
        {
            Init();
            if (NumberOfPeople > Core.Settings.Setting<int>(RestaurantId, "MaxCoversPerReservation"))
            {
                return false;
            }
            return true;
        }
    }
}
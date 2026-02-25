using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

using System.ComponentModel.DataAnnotations;
using epicuri.Core;
namespace epicuri.CPE.Models
{
    public class Reservation : Party
    {
        
        public string Notes{get; set;}
        [Required]
        public String Telephone{get; set;}
        [Required]
        public double ReservationTime { get; set; }
        public Customer LeadCustomer { get; set; }
        public int LeadCustomerId { get; set; }
        public bool Accepted { get; set; }
        public CheckIn CustomerCheckIn;
        public double? ArrivedTime { get; set; }
        public int RestaurantId { get; set; }
        public bool Deleted { get; set; }
        public string RejectionNotice { get; set; }
        public bool Rejected { get; set; }
        public bool IsBirthday { get; set; }
        public bool TimedOut { get; set; }

        public Reservation()
        {
        }

        public Reservation(Core.DatabaseModel.Reservation res)
        {
            Id = res.Id;
            if (res.Session != null)
                SessionId = res.Session.Id;
            Accepted = res.Accepted;
            Notes = res.Notes;
            Name = res.Name;
            Telephone = res.Telephone;
            NumberOfPeople = res.NumberOfPeople;
            Deleted = res.Deleted;
            RejectionNotice = res.RejectionNotice;
            Rejected = res.Rejected;
            
            if (res.LeadCustomer != null)
            {
                LeadCustomer = new Customer(res.LeadCustomer);
            }
            ReservationTime = Core.Utils.Time.DateTimeToUnixTimestamp(res.ReservationTime);

            if (res.ArrivedTime.HasValue)
            {
                ArrivedTime = Core.Utils.Time.DateTimeToUnixTimestamp(res.ArrivedTime.Value);
            }

            if(res.LeadCustomer!=null)
            {
                Init();

                
                DateTime minWalkinTime = DateTime.UtcNow.AddMinutes(-Settings.Setting<double>(res.Restaurant.Id, "WalkinExpirationTime"));

                if (res.CheckIn.FirstOrDefault(chk => chk.Time < minWalkinTime) != null)              
                    TimedOut = true;

                // Check the birthday time setting
                DateTime? customerBirthday = res.LeadCustomer.Birthday;

                if (customerBirthday.HasValue)
                {
                    DateTime birthdayDefaultTop = res.ReservationTime.AddDays(Settings.Setting<int>(res.Restaurant.Id, "BirthdayTimespan"));
                    DateTime birthdayDefaultBottom = res.ReservationTime.AddDays(-Settings.Setting<int>(res.Restaurant.Id, "BirthdayTimespan"));

                    DateTime topDateRange = new DateTime(Convert.ToInt32(DateTime.UtcNow.Year), Convert.ToInt32(birthdayDefaultTop.Month), Convert.ToInt32(birthdayDefaultTop.Day));
                    DateTime bottomDateRange = new DateTime(Convert.ToInt32(DateTime.UtcNow.Year), Convert.ToInt32(birthdayDefaultBottom.Month), Convert.ToInt32(birthdayDefaultBottom.Day));
                    DateTime birthdayDate = new DateTime(Convert.ToInt32(DateTime.UtcNow.Year), Convert.ToInt32(customerBirthday.Value.Month), Convert.ToInt32(customerBirthday.Value.Day));

                    if (topDateRange > birthdayDate && birthdayDate > bottomDateRange)
                    {
                        IsBirthday = true;
                    }
                    else
                    {
                        IsBirthday = false;
                    }
                }
                else
                {
                    IsBirthday = false;
                }
            }
        }

        public bool CheckMaxActiveReservationsCovers(int RestaurantId, Reservation currentReservation)
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

        public bool CheckMaxActiveReservations(int RestaurantId, int currentReservation)
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

            int maxActive = Core.Settings.Setting<int>(RestaurantId, "MaxActiveReservations");
            return activeReservations < maxActive;
        }


        public bool CheckMaxCoversPerReservation(int RestaurantId)
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
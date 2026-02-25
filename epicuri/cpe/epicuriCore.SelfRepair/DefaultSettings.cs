using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace epicuri.Core.SelfRepair
{
    [TestClass]
    public class DefaultSettings : SRSUtils
    {
        protected static List<epicuri.Core.DatabaseModel.DefaultSetting> settings;

        [ClassInitialize]
        static public void SetUpCountries(Microsoft.VisualStudio.TestTools.UnitTesting.TestContext tc)
        {
            Connect(tc);
            settings = new List<DatabaseModel.DefaultSetting>();

            settings.Add(new DatabaseModel.DefaultSetting { Key = "CheckInExpirationTime", Value = "15", SettingDescription = "Minutes after checking in before a CheckIn expires", Measure = "Minutes",SortId=1 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "TakeawayMinimumTime", Value = "30", SettingDescription = "Minimum amount of time required before a takeaway can be booked", Measure = "Minutes", SortId = 3 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "ReservationLockWindow", Value = "120", SettingDescription = "The time before a reservation is due where changes are locked", Measure = "Minutes", SortId = 2 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "ReservationTimeSlot", Value = "120", SettingDescription = "Average session duration, used for capacity checking", Measure = "Minutes", SortId = 2 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "ActionTimeWindow", Value = "60", SettingDescription = "NOT CURRENTLY USED", Measure = "Seconds", SortId = 0 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "MaxCoversPerReservation", Value = "6", SettingDescription = "The maximum number of covers on a particular reservation", Measure = "Number", SortId = 2 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "MinTimeBetweenServiceRequests", Value = "5", SettingDescription = "The minimum amount of time between service calls", Measure = "Minutes", SortId = 0 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "MaxActiveReservations", Value = "5", SettingDescription = "The maximum number of active reservations within ReservationTimeslot", Measure = "Number", SortId = 0 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "SessionIdleTime", Value = "1800", SettingDescription = "NOT CURRENTLY USED", Measure = "-", SortId = 0 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "MaxActiveReservationsCovers", Value = "30", SettingDescription = "Maximum number of covers in reservations within ReservationTimeslot", Measure = "Number", SortId = 0 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "MaxTakeawaysPerHour", Value = "8", SettingDescription = "Maximum number of Takeaways in a 60 min slot", Measure = "Number", SortId = 3 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "MaxTakeawayValue", Value = "80", SettingDescription = "Maximum value of a Takeaway request", Measure = "Currency", SortId = 3 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "MinTakeawayValue", Value = "10", SettingDescription = "Minimum value of a Takeaway request", Measure = "Currency", SortId = 3 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "DeliverySurcharge", Value = "1.5", SettingDescription = "Surchage for a delivered Takeaway", Measure = "Currency", SortId = 0 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "MaxDeliveryRadius", Value = "4", SettingDescription = "Maximum Takeaway Delivery radius from restaurant", Measure = "Miles", SortId = 0 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "FreeDeliveryRadius", Value = "1.5", SettingDescription = "Radius for free Takeaway Delivery", Measure = "Miles", SortId = 0 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "DefaultTipPercentage", Value = "10", SettingDescription = "Default % value for automatic tips", Measure = "Integer Number", SortId = 0 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "CoversBeforeAutoTip", Value = "4", SettingDescription = "Number of diners at a table before a tip is automatically added", Measure = "Number", SortId = 0 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "BirthdayTimespan", Value = "10", SettingDescription = "Show a diners birthday indicator if it falls in +/- these number of days", Measure = "Days", SortId = 0 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "WalkinExpirationTime", Value = "90", SettingDescription = "Time before a Walkin expires", Measure = "Minutes", SortId = 0 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "ReservationFilterTime(hours)", Value = "12", SettingDescription = "Reservations to be displayed from now till X hours in the future", Measure = "Hours", SortId = 0 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "ReservationMinimumTime", Value = "120", SettingDescription = "Minimum amount of time required before a reservation can be booked", Measure = "Minutes", SortId = 0 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "TakeawayLockWindow", Value = "120", SettingDescription = "The time before a takeaway is due where changes are locked", Measure = "Minutes", SortId = 3 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "ClosedSessionMessage", Value = "", SettingDescription = "Message to be displayed upon closing a session", Measure = "-", SortId = 0 });
            settings.Add(new DatabaseModel.DefaultSetting { Key = "SocialMediaMessage", Value = "@Epicuri.app helped me fill my belly. Highly recommended! I am #friendswithepicuri", SettingDescription = "Message to be displayed for social media", Measure = "-", SortId = 0 });
              

        }

        [TestMethod]
        public void Examine()
        {
            foreach (DatabaseModel.DefaultSetting setting in settings)
            {
                var qry = from b in db.DefaultSettings
                          where b.Key == setting.Key
                          select b;

                try
                {
                    Assert.IsNotNull(qry.FirstOrDefault());
                }
                catch (AssertFailedException)
                {
                    db.AddToDefaultSettings(setting);
                    db.SaveChanges();
                }




            }
        }
    }
}

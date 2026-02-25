using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using epicuri.Core.SelfRepair;

namespace epicuri.Core.SelfRepair
{
    [TestClass]
    public class Settings : SRSUtils
    {
        protected static List<epicuri.Core.DatabaseModel.DefaultSetting> ExpectedSettings;

        [ClassInitialize]
        static public void SetUpCountries(Microsoft.VisualStudio.TestTools.UnitTesting.TestContext tc)
        {
            Connect(tc);
            ExpectedSettings = new List<DatabaseModel.DefaultSetting>();

            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "CheckinExpirationTime", Value = "3600" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "TakeawayMinimumTime", Value = "30" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "ReservationLockWindow", Value = "120" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "ReservationTimeSlot", Value = "120" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "ActionTimeWindow", Value = "60" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "MaxCoversPerReservation", Value = "6" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "MinTimeBetweenServiceRequests", Value = "5" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "MaxActiveReservations", Value = "5" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "SessionIdleTime", Value = "1800" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "MaxActiveReservationsCovers", Value = "30" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "MaxTakeawaysPerHour", Value = "8" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "MaxTakeawayValue", Value = "80.00" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "MinTakeawayValue", Value = "10.00" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "DeliverySurcharge", Value = "1.5" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "MaxDeliveryRadius", Value = "4" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "FreeDeliveryRadius", Value = "1.5" });

            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "DefaultTipPercentage", Value = "10" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "CoversBeforeAutoTip", Value = "3" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "BirthdayTimespan", Value = "10" });
            ExpectedSettings.Add(new DatabaseModel.DefaultSetting { Key = "WalkinExpirationTime", Value = "90" });
        }

        [TestMethod]
        public void Examine()
        {
            foreach (DatabaseModel.DefaultSetting DefaultSetting in ExpectedSettings)
            {
                var qry = from b in db.DefaultSettings
                          where b.Key == DefaultSetting.Key
                          select b;

                try
                {
                    Assert.IsNotNull(qry.FirstOrDefault());
                }
                catch (AssertFailedException)
                {
                    db.AddToDefaultSettings(DefaultSetting);
                    db.SaveChanges();
                }




            }
        }
       
    }
}

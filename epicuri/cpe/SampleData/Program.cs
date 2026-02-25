using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace SampleData
{
    class Program
    {
        static void Main(string[] args)
        {
            epicuri.Core.DatabaseModel.epicuriContainer db = new epicuri.Core.DatabaseModel.epicuriContainer();

            var customer = new epicuri.Core.DatabaseModel.Customer
            {
                Name = new epicuri.Core.DatabaseModel.Name
                {
                    Firstname = "John",
                    Surname = "Smith"
                },
                Auth = "password",
                Salt = "$$$",
                Address = new epicuri.Core.DatabaseModel.NullableAddress
                {
                    City= "York",
                    PostCode= "YO10 5GH",
                    Street = "Ron Cooke Hub",
                    Town = "University of York",
                    
                },
                Email = "test@thedistance.co.uk",
                Birthday = DateTime.UtcNow,
                PhoneNumber = "01904 320000",

            };
            db.AddToCustomers(customer);

            var auth = new epicuri.Core.DatabaseModel.AuthenticationKey
            {
                Expires = DateTime.UtcNow,
                Key = "tempkey",
                CustomerId = customer.Id,
            };
            db.AddToAuthenticationKeys(auth);
            db.SaveChanges();
            

            var reservation = new epicuri.Core.DatabaseModel.Reservation {
                ReservationTime = DateTime.UtcNow,
                Name = "John Smith",
                NumberOfPeople = 2,
                Notes = "Wedding Anniversairy",
                CreatedTime = DateTime.UtcNow,
                RestaurantId = db.Restaurants.First().Id,
                Telephone = "01794 000000"
            };

            db.AddToParties(reservation);


            var waiting = new epicuri.Core.DatabaseModel.WaitingList
            {
                Name = "Joe Bloggs",
                CreatedTime = DateTime.UtcNow,
                NumberOfPeople = 5,
                RestaurantId = db.Restaurants.First().Id
            };

            db.AddToParties(waiting);



            var reservation2  = new epicuri.Core.DatabaseModel.Reservation
            {
                ReservationTime = DateTime.UtcNow,
                Name = "Jim",
                NumberOfPeople = 3,
                Notes = "End of work party",
                CreatedTime = DateTime.UtcNow,
                RestaurantId = db.Restaurants.First().Id,
                Telephone = "01904 659659"
            };

            var session = new epicuri.Core.DatabaseModel.SeatedSession
            {
                Party = reservation2,
                StartTime= DateTime.UtcNow,
                RestaurantId = db.Restaurants.First().Id,
                Started = true,
                Service = db.Services.Where(s=>!s.IsTakeaway).First(),
                ChairData = ""
            };

    
            
            session.Tables.Add(db.Tables.First());

            epicuri.Core.DatabaseModel.Diner d0 = new epicuri.Core.DatabaseModel.Diner { IsTable = true };
            epicuri.Core.DatabaseModel.Diner d1 = new epicuri.Core.DatabaseModel.Diner();
            epicuri.Core.DatabaseModel.Diner d2 = new epicuri.Core.DatabaseModel.Diner();
            epicuri.Core.DatabaseModel.Diner d3 = new epicuri.Core.DatabaseModel.Diner();

            db.AddToDiners(d0);
            db.AddToDiners(d1);
            db.AddToDiners(d2);
            db.AddToDiners(d3);

            session.Diners.Add(d0);
            session.Diners.Add(d1);
            session.Diners.Add(d2);
            session.Diners.Add(d3);

            var ack1 = new epicuri.Core.DatabaseModel.NotificationAck
            {
                Time = DateTime.UtcNow,
                NotificationId = session.Service.ScheduleItems.First().Notifications.First().Id

            };

            var ack2 = new epicuri.Core.DatabaseModel.NotificationAck
            {
                Time = DateTime.UtcNow,
                NotificationId = session.Service.RecurringScheduleItems.First().Notifications.First().Id

            };

            var ack3 = new epicuri.Core.DatabaseModel.NotificationAck
            {
                Time = DateTime.UtcNow,
                NotificationId = session.Service.RecurringScheduleItems.First().Notifications.First().Id

            };

            db.AddToNotificationAcks(ack1);
            db.AddToNotificationAcks(ack2);
            db.AddToNotificationAcks(ack3);


            session.NotificationAcks.Add(ack1);
            session.NotificationAcks.Add(ack2);
            session.NotificationAcks.Add(ack3);



            db.AddToParties(reservation2);
            db.AddToSessions(session);

            var o0 = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = db.MenuItems.First(),
                Note = "No cheese at all",
                SessionId = session.Id,
                Course = db.Courses.First(c=>c.Ordering ==1),
                PriceOverride = null
            };

            o0.Modifiers.Add(db.MenuItems.Where(m=>m.ModifierGroups.Count>0).First().ModifierGroups.First().Modifiers.First());


            d0.Orders.Add(o0);


            var o3 = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = db.MenuItems.First(),
                Note = "No cheese at all",
                SessionId = session.Id,
                Course = db.Courses.First(c => c.Ordering == 2),
                PriceOverride = null
            };

            o3.Modifiers.Add(db.MenuItems.Where(m => m.ModifierGroups.Count > 0).First().ModifierGroups.First().Modifiers.First());


            d0.Orders.Add(o3);

            var b1 = new epicuri.Core.DatabaseModel.Batch
            {
                Ident = "Table 1",
                OrderTime = DateTime.UtcNow,
                RestaurantId = db.Restaurants.First().Id
            };
            b1.Orders.Add(o0);


            b1.Orders.Add(o3);
            db.AddToBatches(b1);

            db.SaveChanges();


            var takeaway = new epicuri.Core.DatabaseModel.TakeAwaySession
            {
                StartTime = DateTime.UtcNow,
                RestaurantId = db.Restaurants.First().Id,
                ExpectedTime = DateTime.UtcNow,
                DeliveryAddress = new epicuri.Core.DatabaseModel.Address { City = "York", PostCode = "YO10 5GE", Street = "The Distance, The Catalyst", Town = "University of York" },
                Delivery = true
            };

            db.AddToSessions(takeaway);

            var d4 = new epicuri.Core.DatabaseModel.Diner
            {
                IsTable = false,
                
            };

            var o1 = new epicuri.Core.DatabaseModel.Order
            {
                MenuItem = db.MenuItems.First(),
                Note = "No cheese at all",
                SessionId = takeaway.Id,
                PriceOverride = null,
                Course = db.Courses.First(c=>c.Ordering ==1)
            };

            o1.Modifiers.Add(db.MenuItems.Where(m => m.ModifierGroups.Count > 0).First().ModifierGroups.First().Modifiers.First());
            db.AddToOrders(o1);
            d4.Orders.Add(o1);
            db.AddToDiners(d4);

            takeaway.Diner = d4;


            var b2 = new epicuri.Core.DatabaseModel.Batch
            {
                Ident = "Takeaway",
                OrderTime = DateTime.UtcNow,
                
                RestaurantId = db.Restaurants.First().Id
            };
            b1.Orders.Add(o1);

            db.AddToBatches(b2);
         
            db.SaveChanges();

            var e1 = new epicuri.Core.DatabaseModel.AdhocNotification
            {
                SessionId = session.Id,
                Target = "waiter/action",
                Text = "We need more salt",
                Created = DateTime.UtcNow
            };

            db.AddToAdhocNotifications(e1);

            var e2 = new epicuri.Core.DatabaseModel.AdhocNotification
            {
                SessionId = takeaway.Id,
                Target = "waiter/action",
                Text = "We need more takeaway containers",
                Created = DateTime.UtcNow
            };

            db.AddToAdhocNotifications(e2);
            db.SaveChanges();
        }
    }
}

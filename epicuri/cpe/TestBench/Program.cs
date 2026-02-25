using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data.Objects.DataClasses;
namespace TestBench
{
    class Program
    {
        static void Main(string[] args)
        {
            epicuri.Core.DatabaseModel.epicuriContainer db = new epicuri.Core.DatabaseModel.epicuriContainer();

            var r = new epicuri.Core.DatabaseModel.Restaurant
            {
                Name = "Test Restaurant",
                Category = db.Categories.First(cat => cat.Name == "Mexican"),
                ISOCurrency = "GBP",
                //Country = db.Countries.First(country => country.Name == "United Kingdom"),
                IANATimezone = "Europe/London",
                Description = "My Mexican Restaurant",
                Address = new epicuri.Core.DatabaseModel.Address { City = "Southampton", PostCode = "SO16 1AA", Street = "High Street", Town = "Milbrook" },
                Position = new epicuri.Core.DatabaseModel.LatLongPair
                {
                    Latitude = 50.9339,
                    Longitude = 1.3961
                },
            };

            db.AddToRestaurants(r);
            db.SaveChanges();

            epicuri.Core.DatabaseModel.Printer p = new epicuri.Core.DatabaseModel.Printer
            {
                Name = "Kitchen",
                IP = "kitchen.printers.local"
            };
            r.Printers.Add(p);

            db.SaveChanges();

            epicuri.Core.DatabaseModel.Device d = new epicuri.Core.DatabaseModel.Device
            {
                RestaurantId = r.Id,
                Hash = "testDevice",
                DeviceId = "000",
                Note = "Test Device"
            };

            db.AddToDevices(d);
            db.SaveChanges();


            epicuri.Core.DatabaseModel.Resource res = new epicuri.Core.DatabaseModel.Resource
            {
                Restaurant = r,
                CDNUrl = "http://epicuri.thinktouchsee.com/static/a559f276-8832-494a-979b-8b8e7fd09231.jpg"
            };

            epicuri.Core.DatabaseModel.Floor floor1 = new epicuri.Core.DatabaseModel.Floor
            {
                Resource= res,
                Capacity=200,
                Name = "Upstairs",
                Restaurant = r
            };

            epicuri.Core.DatabaseModel.Floor floor2 = new epicuri.Core.DatabaseModel.Floor
            {
                Resource = res,
                Capacity = 200,
                Name = "Downstairs",
                Restaurant = r
                
            };

            epicuri.Core.DatabaseModel.Table table1 = new epicuri.Core.DatabaseModel.Table
            {
                DefaultCovers = 4,
                Shape = 0,
                Name = "Table 1",
                
                Restaurant = r
            };
            db.AddToTables(table1);

            epicuri.Core.DatabaseModel.Table table5 = new epicuri.Core.DatabaseModel.Table
            {
                DefaultCovers = 4,
                Shape = 0,
                Name = "Table 5",
                
                Restaurant = r
            };
            db.AddToTables(table5);

            epicuri.Core.DatabaseModel.Table table2 = new epicuri.Core.DatabaseModel.Table
            {
                DefaultCovers = 4,
                Shape = 0,
                Name = "Table 2",
                
                Restaurant = r
            };
            db.AddToTables(table2);

            epicuri.Core.DatabaseModel.Table table3 = new epicuri.Core.DatabaseModel.Table
            {
                DefaultCovers = 4,
                Shape = 0,
                Name = "Table 3",
                
                Restaurant = r
            };
            db.AddToTables(table3);

            epicuri.Core.DatabaseModel.Table table4 = new epicuri.Core.DatabaseModel.Table
            {
                DefaultCovers = 4,
                Shape = 0,
                Name = "Table 4 (upstairs)",
                
                Restaurant = r
            };
            db.AddToTables(table4);

            
            db.AddToResources(res);
            epicuri.Core.DatabaseModel.Layout layout = new epicuri.Core.DatabaseModel.Layout
            {
                Floor = floor1,
                Name = "Normal Service Downstairs",
                LastModified = DateTime.UtcNow
            };

            db.AddToLayouts(layout);
            layout.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table1,
                Position =  new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 1, Y = 1, ScaleX = 1, ScaleY = 1 }
            });

            layout.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table2,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 4, Y = 1, ScaleX = 1, ScaleY = 1 }
            });

            layout.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table3,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 3, Y = 3, ScaleX = 1, ScaleY = 1 }
            });

            layout.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table5,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 5, Y = 5, ScaleX = 1, ScaleY = 1 }
            });



            epicuri.Core.DatabaseModel.Layout layout1 = new epicuri.Core.DatabaseModel.Layout
            {
                
                Floor = floor2,
                Name = "Normal Service Upstairs",
                LastModified = DateTime.UtcNow
            };

            layout1.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table4,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 4, Y = 3, ScaleX = 1, ScaleY = 1 }
            });

            db.AddToLayouts(layout1);

            epicuri.Core.DatabaseModel.Layout layout2 = new epicuri.Core.DatabaseModel.Layout
            {
                Floor = floor1,
                Name = "Modified Service Downstairs",
                LastModified = DateTime.UtcNow
            };

            db.AddToLayouts(layout2);
            layout2.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table1,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 1, Y = 1, ScaleX = 1, ScaleY = 1 }
            });

            layout2.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table2,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 4, Y = 1, ScaleX = 1, ScaleY = 1 }
            });

            layout2.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table3,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 3, Y = 3, ScaleX = 1, ScaleY = 1 }
            });

            layout2.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table5,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 5, Y = 5, ScaleX = 1, ScaleY = 1 }
            });

            
          

           

           
            db.AddToFloors(floor1);
            db.AddToFloors(floor2);


            var menu = new epicuri.Core.DatabaseModel.Menu { RestaurantId = r.Id, MenuName = "Weekday Dinner Menu", LastUpdated = DateTime.UtcNow };
            db.AddToMenus(menu);

            var takmenu = new epicuri.Core.DatabaseModel.Menu { RestaurantId = r.Id, MenuName = "Weekday Takeaway Menu", LastUpdated = DateTime.UtcNow };
            db.AddToMenus(takmenu);
            r.TakeawayMenu = takmenu;
            db.SaveChanges();


            floor1.ActiveLayout = layout;
            floor2.ActiveLayout = layout1;

            db.SaveChanges();

            var menucat1 = new epicuri.Core.DatabaseModel.MenuCategory { MenuId = takmenu.Id, CategoryName = "Mains" };
            db.AddToMenuCategories(menucat1);

            var menucat2 = new epicuri.Core.DatabaseModel.MenuCategory { MenuId = takmenu.Id, CategoryName = "Deserts" };
            db.AddToMenuCategories(menucat2);

            takmenu.MenuCategories.Add(menucat2);
            db.SaveChanges();

            var menugrp1 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategoryId = menucat1.Id, GroupName = "Chef\'s specials" };
            var menugrp2 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategoryId = menucat1.Id, GroupName = "Pasta Dishes" };
            var menugrp3 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategoryId = menucat1.Id, GroupName = "Pizzas" };
            db.AddToMenuGroups(menugrp1);
            db.AddToMenuGroups(menugrp2);
            db.AddToMenuGroups(menugrp3);

            db.SaveChanges();

            var g1 = new EntityCollection<epicuri.Core.DatabaseModel.MenuGroup>();


            var menuitem1 = new epicuri.Core.DatabaseModel.MenuItem {Printer = p, Name = "Seafood Salad", MenuGroupId = 0, Price = 4.95, Description = "A Fishy Surprise", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            var menuitem2 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Hawaiian Pizza", MenuGroupId = 0, Price = 4.95, Description = "A great all round dish", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            var menuitem3 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Plain Pizza", MenuGroupId = 0, Price = 4.95, Description = "A great all round dish", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            var menuitem4 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Special Pizza", MenuGroupId = 0, Price = 4.95, Description = "A great all round dish", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            var menuitem5 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Spaghetti Surprise", MenuGroupId = 0, Price = 4.95, Description = "A great all round dish", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            var menuitem6 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Sirloin Steak", MenuGroupId = 0, Price = 4.95, Description = "A meaty treat", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };

            menuitem1.MenuGroups.Add(menugrp1);
            menuitem2.MenuGroups.Add(menugrp3);
            menuitem3.MenuGroups.Add(menugrp3);
            menuitem4.MenuGroups.Add(menugrp1);
            menuitem4.MenuGroups.Add(menugrp3);
            menuitem5.MenuGroups.Add(menugrp2);
            menuitem6.MenuGroups.Add(menugrp1);



            var modgrp1 = new epicuri.Core.DatabaseModel.ModifierGroup { GroupName = "Steak Doneness", RestaurantId = r.Id, UpperLimit = 1, LowerLimit = 1 };

            var mod1 = new epicuri.Core.DatabaseModel.Modifier { Cost = 0, ModifierValue = "Rare", TaxType = db.TaxTypes.First() };
            var mod2 = new epicuri.Core.DatabaseModel.Modifier { Cost = 0, ModifierValue = "Medium-Rare", TaxType= db.TaxTypes.First()};
            var mod3 = new epicuri.Core.DatabaseModel.Modifier { Cost = 0, ModifierValue = "Medium",TaxType= db.TaxTypes.First() };
            var mod4 = new epicuri.Core.DatabaseModel.Modifier { Cost = 0, ModifierValue = "Well Done",TaxType= db.TaxTypes.First() };


            db.AddToModifiers(mod1);
            db.AddToModifiers(mod2);
            db.AddToModifiers(mod3);
            db.AddToModifiers(mod4);
            
            modgrp1.Modifiers.Add(mod1);
            modgrp1.Modifiers.Add(mod2);
            modgrp1.Modifiers.Add(mod3);
            modgrp1.Modifiers.Add(mod4);


            var modgrp2 = new epicuri.Core.DatabaseModel.ModifierGroup { GroupName = "Sauce", RestaurantId = r.Id , UpperLimit = 2, LowerLimit=0};

            var mod5 = new epicuri.Core.DatabaseModel.Modifier { Cost = 1, ModifierValue = "Cheese Sauce", TaxType = db.TaxTypes.First() };
            var mod6 = new epicuri.Core.DatabaseModel.Modifier { Cost = 2, ModifierValue = "Mustard", TaxType = db.TaxTypes.First() };
            var mod7 = new epicuri.Core.DatabaseModel.Modifier { Cost = 0, ModifierValue = "Garlic Sauce", TaxType = db.TaxTypes.First() };

            modgrp2.Modifiers.Add(mod5);
            modgrp2.Modifiers.Add(mod6);
            modgrp2.Modifiers.Add(mod7);


            var modgrp3 = new epicuri.Core.DatabaseModel.ModifierGroup { GroupName = "Cheese", RestaurantId = r.Id, UpperLimit = 1, LowerLimit = 1 };
            var mod8 = new epicuri.Core.DatabaseModel.Modifier { Cost = 0, ModifierValue = "Low Fat", TaxType = db.TaxTypes.First() };
            var mod9 = new epicuri.Core.DatabaseModel.Modifier { Cost = 0, ModifierValue = "Regular", TaxType = db.TaxTypes.First() };
            var mod10 = new epicuri.Core.DatabaseModel.Modifier { Cost = 2, ModifierValue = "Double", TaxType = db.TaxTypes.First() };

            modgrp3.Modifiers.Add(mod8);
            modgrp3.Modifiers.Add(mod9);
            modgrp3.Modifiers.Add(mod10);

            var modgrp4 = new epicuri.Core.DatabaseModel.ModifierGroup { GroupName = "Free Toppings", RestaurantId = r.Id, UpperLimit = 2, LowerLimit=0 };
            var modgrp5 = new epicuri.Core.DatabaseModel.ModifierGroup { GroupName = "Extra Toppings", RestaurantId = r.Id, UpperLimit = 2, LowerLimit=0 };
            
        
            var mod11 = new epicuri.Core.DatabaseModel.Modifier { Cost = 0, ModifierValue = "Mushroom", TaxType = db.TaxTypes.First() };
            var mod12 = new epicuri.Core.DatabaseModel.Modifier { Cost = 0, ModifierValue = "Sweetcorn", TaxType = db.TaxTypes.First() };
            var mod13 = new epicuri.Core.DatabaseModel.Modifier { Cost = 0, ModifierValue = "Jalepeno", TaxType = db.TaxTypes.First() };
            var mod14 = new epicuri.Core.DatabaseModel.Modifier { Cost = 0, ModifierValue = "Onion", TaxType = db.TaxTypes.First() };
            var mod15 = new epicuri.Core.DatabaseModel.Modifier { Cost = 0, ModifierValue = "BBQ Sauce", TaxType = db.TaxTypes.First() };

            var mod16 = new epicuri.Core.DatabaseModel.Modifier { Cost = 2, ModifierValue = "Mushroom", TaxType = db.TaxTypes.First() };
            var mod17 = new epicuri.Core.DatabaseModel.Modifier { Cost = 2, ModifierValue = "Sweetcorn", TaxType = db.TaxTypes.First() };
            var mod18 = new epicuri.Core.DatabaseModel.Modifier { Cost = 2, ModifierValue = "Jalepeno", TaxType = db.TaxTypes.First() };
            var mod19 = new epicuri.Core.DatabaseModel.Modifier { Cost = 2, ModifierValue = "Onion", TaxType = db.TaxTypes.First() };
            var mod20 = new epicuri.Core.DatabaseModel.Modifier { Cost = 2, ModifierValue = "BBQ Sauce", TaxType = db.TaxTypes.First() };



            menuitem6.ModifierGroups.Add(modgrp1);
            menuitem6.ModifierGroups.Add(modgrp2);

            menuitem2.ModifierGroups.Add(modgrp3);
            menuitem2.ModifierGroups.Add(modgrp4);
            menuitem2.ModifierGroups.Add(modgrp5);



            menuitem3.ModifierGroups.Add(modgrp3);
            menuitem3.ModifierGroups.Add(modgrp4);
            menuitem3.ModifierGroups.Add(modgrp5);


            menuitem4.ModifierGroups.Add(modgrp3);
            menuitem4.ModifierGroups.Add(modgrp4);
            menuitem4.ModifierGroups.Add(modgrp5);




            modgrp4.Modifiers.Add(mod11);
            modgrp4.Modifiers.Add(mod12);
            modgrp4.Modifiers.Add(mod13);
            modgrp4.Modifiers.Add(mod14);
            modgrp4.Modifiers.Add(mod15);

            modgrp5.Modifiers.Add(mod16);
            modgrp5.Modifiers.Add(mod17);
            modgrp5.Modifiers.Add(mod18);
            modgrp5.Modifiers.Add(mod19);
            modgrp5.Modifiers.Add(mod20);



            db.AddToModifiers(mod1);
            db.AddToModifiers(mod2);
            db.AddToModifiers(mod3);
            db.AddToModifiers(mod4);
            db.AddToModifiers(mod5);
            db.AddToModifiers(mod6);
            db.AddToModifiers(mod7);
            db.AddToModifiers(mod8);
            db.AddToModifiers(mod9);
            db.AddToModifiers(mod10);
            db.AddToModifiers(mod11);
            db.AddToModifiers(mod12);
            db.AddToModifiers(mod13);
            db.AddToModifiers(mod14);
            db.AddToModifiers(mod15);

            db.AddToModifierGroups(modgrp1);
            db.AddToModifierGroups(modgrp2);
            db.AddToModifierGroups(modgrp3);
            db.AddToModifierGroups(modgrp4);
            db.AddToModifierGroups(modgrp5);


            db.AddToMenuItems(menuitem1);
            db.AddToMenuItems(menuitem2);
            db.AddToMenuItems(menuitem3);
            db.AddToMenuItems(menuitem4);
            db.AddToMenuItems(menuitem5);
            db.AddToMenuItems(menuitem6);


            db.SaveChanges();





            epicuri.Core.DatabaseModel.Service ser = new epicuri.Core.DatabaseModel.Service
            {
                DefaultMenu = menu,
                ServiceName = "Weekday service",
                Notes = "Weekday services between March and November",
                Restaurant = r,
                Active = true,
                Updated = DateTime.UtcNow,
                IsTakeaway = false,
                
            };
            db.AddToServices(ser);

            epicuri.Core.DatabaseModel.Service ser2 = new epicuri.Core.DatabaseModel.Service
            {
                DefaultMenu = menu,
                ServiceName = "Weekday service",
                Notes = "Weekday services between March and November",
                Restaurant = r,
                Active = true,
                Updated = DateTime.UtcNow,
                IsTakeaway = true
            };
            db.AddToServices(ser2);

            var tc1 = new epicuri.Core.DatabaseModel.Course
            {
                Name = "Takeaway",
                RestaurantId = r.Id,
                Ordering = 0,
            };
            ser2.Courses.Add(tc1);

            var c1 = new epicuri.Core.DatabaseModel.Course
            {
                Name = "Starters",
                RestaurantId = r.Id,
                Ordering = 0,
            };

            var c2 = new epicuri.Core.DatabaseModel.Course
            {
                Name = "Mains",
                RestaurantId = r.Id,
                Ordering = 1,
            };
            var c3 = new epicuri.Core.DatabaseModel.Course
            {
                Name = "Deserts",
                RestaurantId = r.Id,
                Ordering = 2,
            };
            var c4 = new epicuri.Core.DatabaseModel.Course
            {
                Name = "Drinks",
                RestaurantId = r.Id,
                Ordering = 3,
            };

            menucat1.Courses.Add(c2);
            menucat2.Courses.Add(c3);

            
       

            ser.Courses.Add(c1);
            ser.Courses.Add(c2);
            ser.Courses.Add(c3);
            ser.Courses.Add(c4);

            db.SaveChanges();

            epicuri.Core.DatabaseModel.Notification no1 = new epicuri.Core.DatabaseModel.Notification
            {
                Target = "waiter/action",
                Text = "Seat Party",
            };
            epicuri.Core.DatabaseModel.Notification no2 = new epicuri.Core.DatabaseModel.Notification
            {
                Target = "waiter/actions",
                Text = "Take Starters Order",
            };
            epicuri.Core.DatabaseModel.Notification no3 = new epicuri.Core.DatabaseModel.Notification
            {
                Target = "waiter/action",
                Text = "Take Mains Order",
            };
            epicuri.Core.DatabaseModel.Notification no4 = new epicuri.Core.DatabaseModel.Notification
            {
                Target = "waiter/action",
                Text = "Take Dessert Order",
            };
            epicuri.Core.DatabaseModel.Notification no5 = new epicuri.Core.DatabaseModel.Notification
            {
                Target = "waiter/action",
                Text = "Take Drinks Order",
            };

            r.Notifications.Add(no1);
            r.Notifications.Add(no2);
            r.Notifications.Add(no3);
            r.Notifications.Add(no4);
            r.Notifications.Add(no5);
            
            db.SaveChanges();
            epicuri.Core.DatabaseModel.ScheduleItem def1 = new epicuri.Core.DatabaseModel.ScheduleItem
            {
                Comment = "",
                Delay =0,
                //Order = 0,
                ServiceId = ser.Id

            };

            def1.Notifications.Add(no1);

            epicuri.Core.DatabaseModel.ScheduleItem def2 = new epicuri.Core.DatabaseModel.ScheduleItem
            {
                Comment = "",
                Delay = 10,
                //Order = 1,
                ServiceId = ser.Id

            };
            def2.Notifications.Add(no2);
            epicuri.Core.DatabaseModel.ScheduleItem def3 = new epicuri.Core.DatabaseModel.ScheduleItem
            {
                Comment = "",
                Delay = 0,
                //Order = 2,
                ServiceId = ser.Id

            };
            def3.Notifications.Add(no3);
            epicuri.Core.DatabaseModel.ScheduleItem def4 = new epicuri.Core.DatabaseModel.ScheduleItem
            {
                Comment = "delay to let the diners eat starter - mark as complete if no starters ordered",
                Delay = 30,
                ServiceId = ser.Id,
                //Order = 3,

            };

            epicuri.Core.DatabaseModel.ScheduleItem def5 = new epicuri.Core.DatabaseModel.ScheduleItem
            {
                Comment = "",
                Delay = 0,
               // Order = 4,
                ServiceId = ser.Id

            };
            def4.Notifications.Add(no4);


            epicuri.Core.DatabaseModel.ScheduleItem def0 = new epicuri.Core.DatabaseModel.ScheduleItem
            {
                Comment = "",
                Delay =0,
                //Order = 0,
                ServiceId = ser2.Id

            };

            def0.Notifications.Add(no1);

            epicuri.Core.DatabaseModel.ScheduleItem def6 = new epicuri.Core.DatabaseModel.ScheduleItem
            {
                Comment = "",
                Delay = 10,
                //Order = 1,
                ServiceId = ser2.Id

            };
            def6.Notifications.Add(no2);
            epicuri.Core.DatabaseModel.ScheduleItem def7 = new epicuri.Core.DatabaseModel.ScheduleItem
            {
                Comment = "",
                Delay = 0,
               //Order = 2,
                ServiceId = ser2.Id

            };
            def3.Notifications.Add(no3);
            
            epicuri.Core.DatabaseModel.ScheduleItem def8 = new epicuri.Core.DatabaseModel.ScheduleItem
            {
                Comment = "",
                Delay = 0,
                //Order = 4,
                ServiceId = ser2.Id

            };
            def8.Notifications.Add(no4);

            epicuri.Core.DatabaseModel.RecurringScheduleItem rec1 = new epicuri.Core.DatabaseModel.RecurringScheduleItem
            {
                InitialDelay = 300,
         
                Period = 1800,
                ServiceId = ser.Id,
                Comment =""
            };

            epicuri.Core.DatabaseModel.RecurringScheduleItem rec2 = new epicuri.Core.DatabaseModel.RecurringScheduleItem
            {
                InitialDelay = 300,

                Period = 1800,
                ServiceId = ser2.Id,
                Comment = ""
            };
            rec1.Notifications.Add(no5);

            db.AddToScheduleItems(def1);
            db.AddToScheduleItems(def2);
            db.AddToScheduleItems(def3);
            db.AddToScheduleItems(def4);
            db.AddToScheduleItems(def5);

            db.AddToScheduleItems(def6);
            db.AddToScheduleItems(def7);
            db.AddToScheduleItems(def8);
            db.AddToScheduleItems(def0);
            


            db.AddToRecurringScheduleItems(rec1);
            db.AddToRecurringScheduleItems(rec2);

            
            db.SaveChanges();

            var role1 = new epicuri.Core.DatabaseModel.Role
            {
                Name = "Waiter"
                
            };
            db.AddToRoles(role1);
            
            var user1 = new epicuri.Core.DatabaseModel.Staff
            {
                Username = "waiter",
                Pin = "1234",
                Phone = "000",
                Name = "waiter",
                Salt = "$$",
                Auth = "13cbc9ed5d3cc5fb0ab3b88f67be6a8c63fb5f30",
                
            };

            role1.Staffs.Add(user1);


            r.Staffs.Add(user1);
            db.SaveChanges();
            
            
            //db.AddToCategories(new epicuri.Core.DatabaseModel.Category { Name = "Mexican" });

            //db.AddToCurrencies(new epicuri.Core.DatabaseModel.Currency { RestaurantId = 0,
            //    CurrencyName=@"Pounds",CurrencySymbol=@"£"});

            
            /*
            epicuri.Core.DatabaseModel.Restaurant r = new epicuri.Core.DatabaseModel.Restaurant();
            r.Name = "test";
            r.Country = c;
            r.Address.Street = "";
            r.Address.Town = "";
            r.Address.PostCode = "";
            r.Address.City = "";
            r.Description = "";
            db.AddToRestaurants(r);
            db.SaveChanges();
   

            */

            epicuri.Core.DatabaseModel.DietaryRequirement dr = new epicuri.Core.DatabaseModel.DietaryRequirement();
            dr.Name = "Kosher";
            db.AddToDietaryRequirements(dr);
            dr = new epicuri.Core.DatabaseModel.DietaryRequirement();
            dr.Name = "Halal";
            db.AddToDietaryRequirements(dr);
            dr = new epicuri.Core.DatabaseModel.DietaryRequirement();
            dr.Name = "Vegan";
            db.AddToDietaryRequirements(dr);
            dr = new epicuri.Core.DatabaseModel.DietaryRequirement();
            dr.Name = "Vegetarian";
            db.AddToDietaryRequirements(dr);
            dr = new epicuri.Core.DatabaseModel.DietaryRequirement();
            db.SaveChanges();

            epicuri.Core.DatabaseModel.Allergy allergy = new epicuri.Core.DatabaseModel.Allergy();
            allergy.Name = "Additives";
            db.AddToAllergies(allergy);
            allergy = new epicuri.Core.DatabaseModel.Allergy();
            allergy.Name = "Dairy";
            db.AddToAllergies(allergy);
            allergy = new epicuri.Core.DatabaseModel.Allergy();
            allergy.Name = "Gluten";
            db.AddToAllergies(allergy);
            allergy = new epicuri.Core.DatabaseModel.Allergy();
            allergy.Name = "Nuts";
            db.AddToAllergies(allergy);
            allergy = new epicuri.Core.DatabaseModel.Allergy();
            allergy.Name = "Seafood";
            db.AddToAllergies(allergy);
            allergy = new epicuri.Core.DatabaseModel.Allergy();
            allergy.Name = "Soya";
            db.AddToAllergies(allergy);
            allergy = new epicuri.Core.DatabaseModel.Allergy();
            allergy.Name = "Wheat";
            db.AddToAllergies(allergy);
            allergy = new epicuri.Core.DatabaseModel.Allergy();
            db.SaveChanges();

            epicuri.Core.DatabaseModel.FoodPreference foodPref = new epicuri.Core.DatabaseModel.FoodPreference();
            foodPref.Name = "Bitter";
            db.AddToFoodPreferences(foodPref);
            foodPref = new epicuri.Core.DatabaseModel.FoodPreference();
            foodPref.Name = "Salty";
            db.AddToFoodPreferences(foodPref);
            foodPref = new epicuri.Core.DatabaseModel.FoodPreference();
            foodPref.Name = "Savoury";
            db.AddToFoodPreferences(foodPref);
            foodPref = new epicuri.Core.DatabaseModel.FoodPreference();
            foodPref.Name = "Sour";
            db.AddToFoodPreferences(foodPref);
            foodPref = new epicuri.Core.DatabaseModel.FoodPreference();
            foodPref.Name = "Spicy";
            db.AddToFoodPreferences(foodPref);
            foodPref = new epicuri.Core.DatabaseModel.FoodPreference();
            foodPref.Name = "Sweet";
            db.AddToFoodPreferences(foodPref);
            foodPref = new epicuri.Core.DatabaseModel.FoodPreference();
            db.SaveChanges();

        }

        

    }

    public static class Util
    {
        public static EntityCollection<T> ToEntityCollection<T>(this IEnumerable<T> source) where T : class, IEntityWithRelationships
        {

            EntityCollection<T> collection = new EntityCollection<T>();
            foreach (var item in source)
                collection.Add(item);
            return collection;

        }
    }
}

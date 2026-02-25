using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using epicuri.Core.DatabaseModel;
namespace Fortune_House
{
    class Program
    {
        static epicuri.Core.DatabaseModel.epicuriContainer db;
        static epicuri.Core.DatabaseModel.Resource res;
        static epicuri.Core.DatabaseModel.Resource res2;
        static epicuri.Core.DatabaseModel.Printer p;
        static void Main(string[] args)
        {

            Console.WriteLine("Epicuri Test Restaurant");
            Thread.Sleep(1000);
            Console.Write(".");
            Console.Clear();
            Connect();

            var r = CreateRestaurant();

            CreateResources(r);
            CreateFloorsAndTables(r);

            CreateMenus(r);
            CreateStaff(r);
            Console.WriteLine("Finished - wooohoo!");
            Console.Read();

        }

        private static void CreateStaff(Restaurant r)
        {
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



            var role2 = new epicuri.Core.DatabaseModel.Role
            {
                Name = "Manager"

            };
            db.AddToRoles(role2);

            var user2 = new epicuri.Core.DatabaseModel.Staff
            {
                Username = "manager",
                Pin = "4321",
                Phone = "000",
                Name = "manager",
                Salt = "$$",
                Auth = "13cbc9ed5d3cc5fb0ab3b88f67be6a8c63fb5f30",

            };

            role1.Staffs.Add(user2);


            r.Staffs.Add(user2);
            db.SaveChanges();
        }

        private static void CreateMenus(Restaurant r)
        {
            Console.WriteLine("Creating Menus");
            var menu = new epicuri.Core.DatabaseModel.Menu { RestaurantId = r.Id, MenuName = "A la carte", LastUpdated = DateTime.UtcNow };
            db.AddToMenus(menu);
            db.SaveChanges();
            Console.WriteLine(": Done 1");

            var takmenu = new epicuri.Core.DatabaseModel.Menu { RestaurantId = r.Id, MenuName = "Takeaway Menu", LastUpdated = DateTime.UtcNow };
            db.AddToMenus(takmenu);
            db.SaveChanges();
            Console.WriteLine(": Done 2");

            var ssmenu = new epicuri.Core.DatabaseModel.Menu { RestaurantId = r.Id, MenuName = "Self-Service Menu", LastUpdated = DateTime.UtcNow };
            db.AddToMenus(ssmenu);
            db.SaveChanges();
            Console.WriteLine(": Done 3");

            Console.WriteLine("Setting Takeaway Menu");
            r.TakeawayMenu = takmenu;
            db.SaveChanges();

            Console.WriteLine("Creating Menu Categories");
            var menucat1 = new epicuri.Core.DatabaseModel.MenuCategory { MenuId = menu.Id, CategoryName = "Starters" };
            db.AddToMenuCategories(menucat1);
            db.SaveChanges();
            Console.WriteLine(": Done 1");


            var menucat2 = new epicuri.Core.DatabaseModel.MenuCategory { MenuId = menu.Id, CategoryName = "Mains" };
            db.AddToMenuCategories(menucat2);
            db.SaveChanges();
            Console.WriteLine(": Done 2");


            var menucat3 = new epicuri.Core.DatabaseModel.MenuCategory { MenuId = menu.Id, CategoryName = "Deserts" };
            db.AddToMenuCategories(menucat3);
            db.SaveChanges();
            Console.WriteLine(": Done 3");


            var menucat4 = new epicuri.Core.DatabaseModel.MenuCategory { MenuId = menu.Id, CategoryName = "Drinks" };
            db.AddToMenuCategories(menucat4);
            db.SaveChanges();
            Console.WriteLine(": Done 4");


            var menucat5 = new epicuri.Core.DatabaseModel.MenuCategory { MenuId = ssmenu.Id, CategoryName = "Self-Service" };
            db.AddToMenuCategories(menucat5);
            db.SaveChanges();
            Console.WriteLine(": Done 5");


            var menucat6 = new epicuri.Core.DatabaseModel.MenuCategory { MenuId = takmenu.Id, CategoryName = "Take-Away" };
            db.AddToMenuCategories(menucat6);
            db.SaveChanges();
            Console.WriteLine(": Done 6");






            Console.WriteLine("Creating Menu Groups");
            var menugrp1 = new epicuri.Core.DatabaseModel.MenuGroup {MenuCategory = menucat1, GroupName = "Starters" };
            db.AddToMenuGroups(menugrp1);
            db.SaveChanges();
            Console.WriteLine(": Done 1");

            
            var menugrp2 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategory = menucat2, GroupName = "Szechuan Style" };
            db.AddToMenuGroups(menugrp2);
            db.SaveChanges();
            Console.WriteLine(": Done 2");

            
            var menugrp3 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategory = menucat2, GroupName = "Kung Poh Dishes" };
            db.AddToMenuGroups(menugrp3);
            db.SaveChanges();
            Console.WriteLine(": Done 3");

            /*
            var menugrp4 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategory = menucat2, GroupName = "Chop Suey" };
            db.AddToMenuGroups(menugrp4);
            db.SaveChanges();
            Console.WriteLine(": Done 4");
            */
            
            var menugrp5 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategory = menucat2, GroupName = "Roasted Duckling Dishes" };
            db.AddToMenuGroups(menugrp5);
            db.SaveChanges();
            Console.WriteLine(": Done 5");

            
            var menugrp6 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategory = menucat3, GroupName = "Hot deserts" };
            db.AddToMenuGroups(menugrp6);
            db.SaveChanges();
            Console.WriteLine(": Done 6");

            
            var menugrp7 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategory = menucat3, GroupName = "Cold deserts" };
            db.AddToMenuGroups(menugrp7);
            db.SaveChanges();
            Console.WriteLine(": Done 7");

            
            var menugrp8 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategory = menucat4, GroupName = "Wine" };
            db.AddToMenuGroups(menugrp8);
            db.SaveChanges();
            Console.WriteLine(": Done 8");

            var menugrp9 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategory = menucat4, GroupName = "Soft Drinks" };
            db.AddToMenuGroups(menugrp9);
            db.SaveChanges();
            Console.WriteLine(": Done 9");


            var menugrp10 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategory = menucat5, GroupName = "Extras" };
            db.AddToMenuGroups(menugrp10);
            db.SaveChanges();
            Console.WriteLine(": Done 10");

            var menugrp11 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategory = menucat5, GroupName = "Wine" };
            db.AddToMenuGroups(menugrp11);
            db.SaveChanges();
            Console.WriteLine(": Done 11");

            var menugrp12 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategory = menucat5, GroupName = "Soft Drinks" };
            db.AddToMenuGroups(menugrp12);
            db.SaveChanges();
            Console.WriteLine(": Done 12");


            var menugrp13 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategory = menucat6, GroupName = "Starters" };
            db.AddToMenuGroups(menugrp13);
            db.SaveChanges();
            Console.WriteLine(": Done 13");


            var menugrp14 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategory = menucat6, GroupName = "Szechuan Style" };
            db.AddToMenuGroups(menugrp14);
            db.SaveChanges();
            Console.WriteLine(": Done 14");


            var menugrp15 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategory = menucat6, GroupName = "Kung Poh Dishes" };
            db.AddToMenuGroups(menugrp15);
            db.SaveChanges();
            Console.WriteLine(": Done 15");





            var menugrp16 = new epicuri.Core.DatabaseModel.MenuGroup { MenuCategory = menucat6, GroupName = "Roasted Duckling Dishes" };
            db.AddToMenuGroups(menugrp16);
            db.SaveChanges();
            Console.WriteLine(": Done 16");

            Thread.Sleep(1000);
            Console.Clear();



            Console.WriteLine("Menu Items");

            var menuitem1 = new epicuri.Core.DatabaseModel.MenuItem {Printer=p, Name = "Mixed Starter", Price = 6.8, Description = "Spring Roll (2), Crispy Wanton (4), Spare Ribs in a Peking Sauce & Crispy Seaweed", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem1);
            db.SaveChanges();
            Console.WriteLine(": Done 1");

            var menuitem2 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Crispy Prawn Toast", Price = 3.2, Description = "6 Slices", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem2);
            db.SaveChanges();
            Console.WriteLine(": Done 2");

            var menuitem3 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Crispy Wanton", Price = 2.8, Description = "8 Wantons", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem3);
            db.SaveChanges();
            Console.WriteLine(": Done 3");

            var menuitem4 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Spare Ribs in Peking Sauce", Price = 4.5, Description = "Served with Peking Sauce", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem4);
            db.SaveChanges();
            Console.WriteLine(": Done 4");

            var menuitem5 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Barbecued Spare Ribs", Price = 4.5, Description = "House BBQ Recipe", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem5);
            db.SaveChanges();
            Console.WriteLine(": Done 5");

            var menuitem6 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Chicken & Sweetcorn Soup", Price = 2.2, Description = "With fresh sweetcorn", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem6);
            db.SaveChanges();
            Console.WriteLine(": Done 6");

















            var menuitem7 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Szechuan Special", Price = 6.5, Description = "Spicy hot flavor dishes. ", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem7);
            db.SaveChanges();
            Console.WriteLine(": Done 7");


            var menuitem8 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Szechuan Beef", Price = 6.5, Description = "Spicy hot flavor dishes. ", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem8);
            db.SaveChanges();
            Console.WriteLine(": Done 8");

            var menuitem9 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Szechuan King Prawn", Price = 6.3, Description = "Spicy hot flavor dishes. ", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem9);
            db.SaveChanges();
            Console.WriteLine(": Done 9");

            var menuitem10 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Szechuan Chicken", Price = 6, Description = "Spicy hot flavor dishes. ", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem10);
            db.SaveChanges();
            Console.WriteLine(": Done 10");

            var menuitem11 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Szechuan Vegetables", Price = 5.5, Description = "Spicy hot flavor dishes. ", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem11);
            db.SaveChanges();
            Console.WriteLine(": Done 11");








            var menuitem12 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Kung Poh Special", Price = 6.5, Description = "Spicy hot with Cashew Nuts. ", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem12);
            db.SaveChanges();
            Console.WriteLine(": Done 12");


            var menuitem13 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Kung Poh Beef", Price = 6.3, Description = "Spicy hot with Cashew Nuts. ", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem13);
            db.SaveChanges();
            Console.WriteLine(": Done 13");

            var menuitem14 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Kung Poh King Prawn", Price = 6.2, Description = "Spicy hot with Cashew Nuts. ", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem14);
            db.SaveChanges();
            Console.WriteLine(": Done 14");

            var menuitem15 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Kung Poh Chicken", Price = 6.10, Description = "Spicy hot with Cashew Nuts. ", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem15);
            db.SaveChanges();
            Console.WriteLine(": Done 15");

            var menuitem16 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Kung Poh Vegetables", Price = 5.8, Description = "Spicy hot with Cashew Nuts. ", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem16);
            db.SaveChanges();
            Console.WriteLine(": Done 16");





            var menuitem17 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Roast Duck with Plum Sauce", Price = 6.5, Description = "Roast Duck with Plum Sauce", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem17);
            db.SaveChanges();
            Console.WriteLine(": Done 17");

            var menuitem18 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Roast Duck with Ginger & Spring Onions", Price = 6.5, Description = "Roast Duck with Ginger & Spring Onions", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem18);
            db.SaveChanges();
            Console.WriteLine(": Done 18");

            var menuitem19 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Roast Duck with Orange Sauce", Price = 6.5, Description = "Roast Duck with Orange Sauce", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem19);
            db.SaveChanges();
            Console.WriteLine(": Done 19");

            var menuitem20 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Roast Duck with Black Bean Sauce", Price = 6.5, Description = "Roast Duck with Black Bean Sauce", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem20);
            db.SaveChanges();
            Console.WriteLine(": Done 20");

            var menuitem21 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Roast Duck with Mushrooms", Price = 6.5, Description = "Roast Duck with Mushrooms", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem21);
            db.SaveChanges();
            Console.WriteLine(": Done 21");

            var menuitem22 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Roast Duck with Pineapple", Price = 6.5, Description = "Roast Duck with Pineapple", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem22);
            db.SaveChanges();
            Console.WriteLine(": Done 22");


            var menuitem23 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Apple Pie", Price = 3.5, Description = "With fresh apples", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem23);
            db.SaveChanges();
            Console.WriteLine(": Done 23");

            var menuitem24 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Hotcakes", Price = 3.5, Description = "Made with chocolate", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem24);
            db.SaveChanges();
            Console.WriteLine(": Done 24");

            var menuitem25 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Jelly", Price = 3.5, Description = "Out of the packet", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem25);
            db.SaveChanges();
            Console.WriteLine(": Done 25");

            var menuitem26 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Icecream", Price = 3.5, Description = "Any flavor", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem26);
            db.SaveChanges();
            Console.WriteLine(": Done 26");






            var menuitem27 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Red Wine", Price = 9.5, Description = "Its a bottle of wine", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem27);
            db.SaveChanges();
            Console.WriteLine(": Done 27");

            var menuitem28 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "White Wine", Price = 9.5, Description = "Its a bottle of wine", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem28);
            db.SaveChanges();
            Console.WriteLine(": Done 28");

            var menuitem29 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Coca Cola", Price = 2.5, Description = "1 pint", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem29);
            db.SaveChanges();
            Console.WriteLine(": Done 29");

            var menuitem30 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Fanta", Price = 2.5, Description = "1 pint", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem30);
            db.SaveChanges();
            Console.WriteLine(": Done 30");








            var menuitem31 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Spring Rolls", Price = 3.5, Description = "1 pint", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem31);
            db.SaveChanges();
            Console.WriteLine(": Done 31");
            var menuitem32 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Vegi Spring Rolls", Price = 2.5, Description = "1 pint", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem32);
            db.SaveChanges();
            Console.WriteLine(": Done 32");
            var menuitem33 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Extra Rice", Price = 2.2, Description = "1 pint", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem33);
            db.SaveChanges();
            Console.WriteLine(": Done 33");
            var menuitem34 = new epicuri.Core.DatabaseModel.MenuItem { Printer = p, Name = "Duck Sauce", Price = 0.5, Description = "1 pint", TaxTypeId = db.TaxTypes.First().Id, ImageURL = "", RestaurantId = r.Id };
            db.AddToMenuItems(menuitem34);
            db.SaveChanges();
            Console.WriteLine(": Done 34");

            Thread.Sleep(500);
            Console.Clear();
            
            
            
            
            Console.WriteLine("MenuItems to MenuGroup");
            menuitem1.MenuGroups.Add(menugrp1);
            db.SaveChanges();
            Console.WriteLine(": Done 1");

            menuitem2.MenuGroups.Add(menugrp1);
            db.SaveChanges();
            Console.WriteLine(": Done 2");


            menuitem3.MenuGroups.Add(menugrp1);
            db.SaveChanges();
            Console.WriteLine(": Done 3");


            menuitem4.MenuGroups.Add(menugrp1);
            db.SaveChanges();
            Console.WriteLine(": Done 4");


            menuitem5.MenuGroups.Add(menugrp1);
            db.SaveChanges();
            Console.WriteLine(": Done 5");


            menuitem6.MenuGroups.Add(menugrp1);
            db.SaveChanges();
            Console.WriteLine(": Done 6");


            menuitem1.MenuGroups.Add(menugrp13);
            db.SaveChanges();
            Console.WriteLine(": Done 7");

            menuitem2.MenuGroups.Add(menugrp13);
            db.SaveChanges();
            Console.WriteLine(": Done 8");


            menuitem3.MenuGroups.Add(menugrp13);
            db.SaveChanges();
            Console.WriteLine(": Done 9");


            menuitem4.MenuGroups.Add(menugrp13);
            db.SaveChanges();
            Console.WriteLine(": Done 10");


            menuitem5.MenuGroups.Add(menugrp13);
            db.SaveChanges();
            Console.WriteLine(": Done 11");


            menuitem6.MenuGroups.Add(menugrp13);
            db.SaveChanges();
            Console.WriteLine(": Done 12");




            menuitem7.MenuGroups.Add(menugrp2);
            db.SaveChanges();
            Console.WriteLine(": Done 13");
            menuitem8.MenuGroups.Add(menugrp2);
            db.SaveChanges();
            Console.WriteLine(": Done 14");
            menuitem9.MenuGroups.Add(menugrp2);
            db.SaveChanges();
            Console.WriteLine(": Done 15");
            menuitem10.MenuGroups.Add(menugrp2);
            db.SaveChanges();
            Console.WriteLine(": Done 16");
            menuitem11.MenuGroups.Add(menugrp2);
            db.SaveChanges();
            Console.WriteLine(": Done 17");



            menuitem7.MenuGroups.Add(menugrp14);
            db.SaveChanges();
            Console.WriteLine(": Done 18");
            menuitem8.MenuGroups.Add(menugrp14);
            db.SaveChanges();
            Console.WriteLine(": Done 19");
            menuitem9.MenuGroups.Add(menugrp14);
            db.SaveChanges();
            Console.WriteLine(": Done 20");
            menuitem10.MenuGroups.Add(menugrp14);
            db.SaveChanges();
            Console.WriteLine(": Done 21");
            menuitem11.MenuGroups.Add(menugrp14);
            db.SaveChanges();
            Console.WriteLine(": Done 22");







            menuitem12.MenuGroups.Add(menugrp3);
            db.SaveChanges();
            Console.WriteLine(": Done 23");
            menuitem13.MenuGroups.Add(menugrp3);
            db.SaveChanges();
            Console.WriteLine(": Done 24");
            menuitem14.MenuGroups.Add(menugrp3);
            db.SaveChanges();
            Console.WriteLine(": Done 25");
            menuitem15.MenuGroups.Add(menugrp3);
            db.SaveChanges();
            Console.WriteLine(": Done 26");
            menuitem16.MenuGroups.Add(menugrp3);
            db.SaveChanges();
            Console.WriteLine(": Done 27");


            menuitem12.MenuGroups.Add(menugrp15);
            db.SaveChanges();
            Console.WriteLine(": Done 28");
            menuitem13.MenuGroups.Add(menugrp15);
            db.SaveChanges();
            Console.WriteLine(": Done 29");
            menuitem14.MenuGroups.Add(menugrp15);
            db.SaveChanges();
            Console.WriteLine(": Done 30");
            menuitem15.MenuGroups.Add(menugrp15);
            db.SaveChanges();
            Console.WriteLine(": Done 31");
            menuitem16.MenuGroups.Add(menugrp15);
            db.SaveChanges();
            Console.WriteLine(": Done 32");






            menuitem17.MenuGroups.Add(menugrp5);
            db.SaveChanges();
            Console.WriteLine(": Done 33");
            menuitem18.MenuGroups.Add(menugrp5);
            db.SaveChanges();
            Console.WriteLine(": Done 34");
            menuitem19.MenuGroups.Add(menugrp5);
            db.SaveChanges();
            Console.WriteLine(": Done 35");
            menuitem20.MenuGroups.Add(menugrp5);
            db.SaveChanges();
            Console.WriteLine(": Done 36");
            menuitem21.MenuGroups.Add(menugrp5);
            db.SaveChanges();
            Console.WriteLine(": Done 37");
            menuitem22.MenuGroups.Add(menugrp5);
            db.SaveChanges();
            Console.WriteLine(": Done 38");


            menuitem17.MenuGroups.Add(menugrp16);
            db.SaveChanges();
            Console.WriteLine(": Done 33");
            menuitem18.MenuGroups.Add(menugrp16);
            db.SaveChanges();
            Console.WriteLine(": Done 34");
            menuitem19.MenuGroups.Add(menugrp16);
            db.SaveChanges();
            Console.WriteLine(": Done 35");
            menuitem20.MenuGroups.Add(menugrp16);
            db.SaveChanges();
            Console.WriteLine(": Done 36");
            menuitem21.MenuGroups.Add(menugrp16);
            db.SaveChanges();
            Console.WriteLine(": Done 37");
            menuitem22.MenuGroups.Add(menugrp16);
            db.SaveChanges();
            Console.WriteLine(": Done 38");





            menuitem23.MenuGroups.Add(menugrp6);
            db.SaveChanges();
            Console.WriteLine(": Done 39");
            menuitem24.MenuGroups.Add(menugrp6);
            db.SaveChanges();
            Console.WriteLine(": Done 40");
            menuitem25.MenuGroups.Add(menugrp7);
            db.SaveChanges();
            Console.WriteLine(": Done 41");
            menuitem26.MenuGroups.Add(menugrp7);
            db.SaveChanges();
            Console.WriteLine(": Done 42");



            menuitem27.MenuGroups.Add(menugrp8);
            db.SaveChanges();
            Console.WriteLine(": Done 43");
            menuitem28.MenuGroups.Add(menugrp8);
            db.SaveChanges();
            Console.WriteLine(": Done 44");
            menuitem19.MenuGroups.Add(menugrp9);
            db.SaveChanges();
            Console.WriteLine(": Done 45");
            menuitem30.MenuGroups.Add(menugrp9);
            db.SaveChanges();
            Console.WriteLine(": Done 46");

            menuitem27.MenuGroups.Add(menugrp11);
            db.SaveChanges();
            Console.WriteLine(": Done 47");
            menuitem28.MenuGroups.Add(menugrp11);
            db.SaveChanges();
            Console.WriteLine(": Done 48");
            menuitem19.MenuGroups.Add(menugrp12);
            db.SaveChanges();
            Console.WriteLine(": Done 49");
            menuitem30.MenuGroups.Add(menugrp12);
            db.SaveChanges();
            Console.WriteLine(": Done 50");

            menuitem31.MenuGroups.Add(menugrp10);
            db.SaveChanges();
            menuitem32.MenuGroups.Add(menugrp10);
            db.SaveChanges();
            menuitem33.MenuGroups.Add(menugrp10);
            db.SaveChanges();
            menuitem34.MenuGroups.Add(menugrp10);
            db.SaveChanges();

            Thread.Sleep(500);
            Console.Clear();

            Console.WriteLine("Adding Menu Tags");
            MenuTag tag1 = new MenuTag { RestaurantId = r.Id, Tag = "Vegitarian" };
            db.AddToMenuTags(tag1);
            db.SaveChanges();
            Console.WriteLine(": Done 1");
            MenuTag tag2 = new MenuTag { RestaurantId = r.Id, Tag = "Spicy" };
            db.AddToMenuTags(tag2);
            db.SaveChanges();
            Console.WriteLine(": Done 2");
            MenuTag tag3 = new MenuTag { RestaurantId = r.Id, Tag = "Vegan" };
            db.AddToMenuTags(tag3);
            db.SaveChanges();
            Console.WriteLine(": Done 3");


            Thread.Sleep(500);

            Console.Clear();






            Console.WriteLine("Adding Menu Tags to Items");

            menuitem3.MenuTags.Add(tag1);
            db.SaveChanges();
            Console.Write(".");

          

            menuitem7.MenuTags.Add(tag2);
            db.SaveChanges();
            Console.Write(".");

            menuitem8.MenuTags.Add(tag2);
            db.SaveChanges();
            Console.Write(".");

            menuitem9.MenuTags.Add(tag2);
            db.SaveChanges();
            Console.Write(".");

            menuitem10.MenuTags.Add(tag2);
            db.SaveChanges();
            Console.Write(".");

            menuitem11.MenuTags.Add(tag1);
            db.SaveChanges();
            Console.Write(".");

            menuitem11.MenuTags.Add(tag2);
            db.SaveChanges();
            Console.Write(".");


            menuitem16.MenuTags.Add(tag1);
            db.SaveChanges();
            Console.Write(".");

            Thread.Sleep(500);
            Console.Clear();




            Console.WriteLine("Adding Modifier Groups");
            ModifierGroup g1 = new ModifierGroup { RestaurantId = r.Id, GroupName = "Side Dish", LowerLimit = 1, UpperLimit = 1 };
            db.SaveChanges();

            Modifier mod1 = new Modifier { ModifierGroup = g1, Cost = 0, TaxType = db.TaxTypes.First(), ModifierValue = "Boiled Rice", Deleted = false };
            db.SaveChanges();
            Console.WriteLine("Add Child 1");
            Modifier mod2 = new Modifier { ModifierGroup = g1, Cost = 0, TaxType = db.TaxTypes.First(), ModifierValue = "Chips", Deleted = false };
            db.SaveChanges();
            Console.WriteLine("Add Child 2");
            Modifier mod3 = new Modifier { ModifierGroup = g1, Cost = 0.6, TaxType = db.TaxTypes.First(), ModifierValue = "Fried Rice", Deleted = false };
            db.SaveChanges();
            Console.WriteLine("Add Child 3");

            ModifierGroup g2 = new ModifierGroup { RestaurantId = r.Id, GroupName = "Vegetables", LowerLimit = 0, UpperLimit = 3 };
            db.SaveChanges();

            Modifier mod4 = new Modifier { ModifierGroup = g2, Cost = 0, TaxType = db.TaxTypes.First(), ModifierValue = "Pepper", Deleted = false };
            db.SaveChanges();
            Console.WriteLine("Add Child 4");
            Modifier mod5 = new Modifier { ModifierGroup = g2, Cost = 0, TaxType = db.TaxTypes.First(), ModifierValue = "Sweetcorn", Deleted = false };
            db.SaveChanges();
            Console.WriteLine("Add Child 5");
            Modifier mod6 = new Modifier { ModifierGroup = g2, Cost = 0.6, TaxType = db.TaxTypes.First(), ModifierValue = "Pak Choi", Deleted = false };
            db.SaveChanges();
            Console.WriteLine("Add Child 6");


            Console.WriteLine("Linking Modifier Group To Menu Items");
 

            menuitem7.ModifierGroups.Add(g1);
            db.SaveChanges();
            Console.Write(".");

            menuitem7.ModifierGroups.Add(g2);
            db.SaveChanges();
            Console.Write(".");

            menuitem8.ModifierGroups.Add(g1);
            db.SaveChanges();
            Console.Write(".");

            menuitem9.ModifierGroups.Add(g1);
            db.SaveChanges();
            Console.Write(".");

            menuitem10.ModifierGroups.Add(g1);
            db.SaveChanges();
            Console.Write(".");

            menuitem11.ModifierGroups.Add(g1);
            db.SaveChanges();
            Console.Write(".");

            menuitem12.ModifierGroups.Add(g1);
            db.SaveChanges();
            Console.Write(".");

            menuitem13.ModifierGroups.Add(g1);
            db.SaveChanges();
            Console.Write(".");

            menuitem14.ModifierGroups.Add(g1);
            db.SaveChanges();
            Console.Write(".");
            menuitem15.ModifierGroups.Add(g1);
            db.SaveChanges();
            Console.Write(".");
            menuitem16.ModifierGroups.Add(g1);
            db.SaveChanges();
            Console.Write(".");
            menuitem17.ModifierGroups.Add(g1);
            db.SaveChanges();
            Console.Write(".");
            menuitem18.ModifierGroups.Add(g1);
            db.SaveChanges();
            Console.Write(".");
            menuitem19.ModifierGroups.Add(g1);
            db.SaveChanges();
            Console.Write(".");
            menuitem20.ModifierGroups.Add(g1);
            db.SaveChanges();
            Console.Write(".");
            menuitem21.ModifierGroups.Add(g1);
            db.SaveChanges();
            Console.Write(".");
            menuitem22.ModifierGroups.Add(g1);
            db.SaveChanges();
            Console.Write(".");


            Thread.Sleep(500);

            Console.Clear();


            Console.WriteLine("Adding Services");

            epicuri.Core.DatabaseModel.Service ser = new epicuri.Core.DatabaseModel.Service
            {
                DefaultMenu = menu,
                ServiceName = "Evening a la carte",
                Notes = "Service from 1700",
                Restaurant = r,
                Active = true,
                Updated = DateTime.UtcNow,
                IsTakeaway = false,
                SelfServiceMenu = ssmenu
            };
            db.AddToServices(ser);
            db.SaveChanges();
            Console.WriteLine(": Done 1");
            epicuri.Core.DatabaseModel.Service ser2 = new epicuri.Core.DatabaseModel.Service
            {
                DefaultMenu = menu,
                ServiceName = "Lunchtime a la cart",
                Notes = "From 1200 to 1500",
                Restaurant = r,
                Active = true,
                Updated = DateTime.UtcNow,
                IsTakeaway = false,
                SelfServiceMenu = ssmenu
            };
            db.AddToServices(ser2);
            db.SaveChanges();
            Console.WriteLine(": Done 2");

            epicuri.Core.DatabaseModel.Service ser3 = new epicuri.Core.DatabaseModel.Service
            {
                DefaultMenu = menu,
                ServiceName = "Takeaway Service",
                Notes = "",
                Restaurant = r,
                Active = true,
                Updated = DateTime.UtcNow,
                IsTakeaway = true
            };
            db.AddToServices(ser3);
            db.SaveChanges();
            Console.WriteLine(": Done 3");


            Thread.Sleep(500);
            Console.Clear();


            Console.WriteLine("Adding Courses");
            Course c1 = new Course { Name = "Takeaway", RestaurantId = r.Id, ServiceId = ser3.Id, Ordering = 0 };
            db.AddToCourses(c1);
            Console.WriteLine(": Done 1");

            Course c2 = new Course { Name = "Starters", RestaurantId = r.Id, ServiceId = ser.Id, Ordering = 0 };
            db.AddToCourses(c2);
            Console.WriteLine(": Done 2");

            Course c3 = new Course { Name = "Mains", RestaurantId = r.Id, ServiceId = ser.Id, Ordering = 1 };
            db.AddToCourses(c3);
            Console.WriteLine(": Done 3");

            Course c4 = new Course { Name = "Desert", RestaurantId = r.Id, ServiceId = ser.Id, Ordering = 2 };
            db.AddToCourses(c4);
            Console.WriteLine(": Done 4");

            Course c5 = new Course { Name = "Drinks", RestaurantId = r.Id, ServiceId = ser.Id, Ordering = 3 };
            db.AddToCourses(c5);
            Console.WriteLine(": Done 5");



            Course c6 = new Course { Name = "Starters", RestaurantId = r.Id, ServiceId = ser2.Id, Ordering = 0 };
            db.AddToCourses(c6);
            Console.WriteLine(": Done 6");

            Course c7 = new Course { Name = "Mains", RestaurantId = r.Id, ServiceId = ser2.Id, Ordering = 1 };
            db.AddToCourses(c7);
            Console.WriteLine(": Done 7");

            Course c8 = new Course { Name = "Drinks", RestaurantId = r.Id, ServiceId = ser2.Id, Ordering = 3 };
            db.AddToCourses(c8);
            Console.WriteLine(": Done 8");


            menucat1.Courses.Add(c2);
            menucat2.Courses.Add(c3);
            menucat3.Courses.Add(c4);
            menucat4.Courses.Add(c5);


            menucat1.Courses.Add(c6);
            menucat2.Courses.Add(c7);
            menucat3.Courses.Add(c8);

            Console.Clear();
            epicuri.Core.DatabaseModel.Notification no1 = new epicuri.Core.DatabaseModel.Notification
            {
                Target = "waiter/action",
                Text = "Seat Party",
            };
            epicuri.Core.DatabaseModel.Notification no2 = new epicuri.Core.DatabaseModel.Notification
            {
                Target = "waiter/action",
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
                Delay = 0,
                //Order = 0,
                ServiceId = ser.Id

            };

            def1.Notifications.Add(no1);

            epicuri.Core.DatabaseModel.ScheduleItem def2 = new epicuri.Core.DatabaseModel.ScheduleItem
            {
                Comment = "",
                Delay = 10*60,
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
                Delay = 30*60,
                ServiceId = ser.Id,
                //Order = 3,

            };

            epicuri.Core.DatabaseModel.ScheduleItem def5 = new epicuri.Core.DatabaseModel.ScheduleItem
            {
                Comment = "",
                Delay = 60*60,
                //Order = 4,
                ServiceId = ser.Id

            };
            def4.Notifications.Add(no4);

            epicuri.Core.DatabaseModel.ScheduleItem def0 = new epicuri.Core.DatabaseModel.ScheduleItem
            {
                Comment = "",
                Delay = 0,
                //Order = 0,
                ServiceId = ser2.Id

            };

            def0.Notifications.Add(no1);

            epicuri.Core.DatabaseModel.ScheduleItem def6 = new epicuri.Core.DatabaseModel.ScheduleItem
            {
                Comment = "",
                Delay = 10*60,
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
                Delay = 45*60,
                //Order = 4,
                ServiceId = ser2.Id

            };
            def8.Notifications.Add(no4);

            epicuri.Core.DatabaseModel.RecurringScheduleItem rec1 = new epicuri.Core.DatabaseModel.RecurringScheduleItem
            {
                InitialDelay = 300,

                Period = 30*60,
                ServiceId = ser.Id,
                Comment = ""
            };

            epicuri.Core.DatabaseModel.RecurringScheduleItem rec2 = new epicuri.Core.DatabaseModel.RecurringScheduleItem
            {
                InitialDelay = 5,

                Period = 30,
                ServiceId = ser2.Id,
                Comment = ""
            };
            rec1.Notifications.Add(no5);
            rec2.Notifications.Add(no5);

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




        }

        private static void CreateFloorsAndTables(Restaurant r)
        {
            Console.WriteLine("Creating Floors");
            epicuri.Core.DatabaseModel.Floor floor1 = new epicuri.Core.DatabaseModel.Floor
            {
                Resource = res,
                Capacity = 200,
                Name = "Upstairs",
                Restaurant = r
            };
            db.AddToFloors(floor1);
            db.SaveChanges();

            Console.WriteLine(": Done 1");


            epicuri.Core.DatabaseModel.Floor floor2 = new epicuri.Core.DatabaseModel.Floor
            {
                Resource = res2,
                Capacity = 200,
                Name = "Downstairs",
                Restaurant = r

            };
            db.AddToFloors(floor2);
            db.SaveChanges();
            Console.WriteLine(": Done 2");
            Thread.Sleep(500);



            Console.WriteLine("Creating Tables");
            epicuri.Core.DatabaseModel.Table table1 = new epicuri.Core.DatabaseModel.Table
            {
                DefaultCovers = 4,
                Shape = 0,
                Name = "Table 1",
                Restaurant = r

            };
            db.AddToTables(table1);
            db.SaveChanges();
            Console.WriteLine(": Done 1");



            

            epicuri.Core.DatabaseModel.Table table2 = new epicuri.Core.DatabaseModel.Table
            {
                DefaultCovers = 4,
                Shape = 0,
                Name = "Table 2",
                
                Restaurant = r
            };
            db.AddToTables(table2);
            db.SaveChanges();
            Console.WriteLine(": Done 2");


            epicuri.Core.DatabaseModel.Table table3 = new epicuri.Core.DatabaseModel.Table
            {
                DefaultCovers = 4,
                Shape = 0,
                Name = "Table 3",
                Restaurant = r

            };
            db.AddToTables(table3);
            db.SaveChanges();
            Console.WriteLine(": Done 3");


            epicuri.Core.DatabaseModel.Table table4 = new epicuri.Core.DatabaseModel.Table
            {
                DefaultCovers = 4,
                Shape = 0,
                Name = "Table 4 (upstairs)",
                Restaurant = r

            };
            db.AddToTables(table4);
            db.SaveChanges();
            Console.WriteLine(": Done 4");


            epicuri.Core.DatabaseModel.Table table5 = new epicuri.Core.DatabaseModel.Table
            {
                DefaultCovers = 4,
                Shape = 0,
                Name = "Table 5",
                Restaurant = r

            };
            db.AddToTables(table5);
            db.SaveChanges();
            Console.WriteLine(": Done 5");


            Thread.Sleep(500);


            Console.WriteLine("Creating Layouts");

            epicuri.Core.DatabaseModel.Layout layout = new epicuri.Core.DatabaseModel.Layout
            {
                Floor = floor1,
                Name = "Normal Service Downstairs",
                LastModified = DateTime.UtcNow
            };

            db.AddToLayouts(layout);
            Console.WriteLine(": Done 1");
            db.SaveChanges();

            
            epicuri.Core.DatabaseModel.Layout layout1 = new epicuri.Core.DatabaseModel.Layout
            {

                Floor = floor2,
                Name = "Normal Service Upstairs",
                LastModified = DateTime.UtcNow
            };
            db.AddToLayouts(layout1);
            db.SaveChanges();
            Console.WriteLine(": Done 2");
            

            epicuri.Core.DatabaseModel.Layout layout2 = new epicuri.Core.DatabaseModel.Layout
            {
                Floor = floor1,
                Name = "Modified Service Downstairs",
                LastModified = DateTime.UtcNow
            };
            db.AddToLayouts(layout2);
            db.SaveChanges();


            Console.WriteLine("Adding Tables To Layouts");

            layout.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table1,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 1, Y = 1, ScaleX = 1, ScaleY = 1 }
            });
            Console.WriteLine(": Done 1");
            db.SaveChanges();

            layout.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table2,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 4, Y = 1, ScaleX = 1, ScaleY = 1 }
            });
            Console.WriteLine(": Done 2");
            db.SaveChanges();


            layout.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table3,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 3, Y = 3, ScaleX = 1, ScaleY = 1 }
            });
            Console.WriteLine(": Done 3");
            db.SaveChanges();


            layout.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table5,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 5, Y = 5, ScaleX = 1, ScaleY = 1 }
            });
            Console.WriteLine(": Done 4");
            db.SaveChanges();


            layout1.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table4,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 4, Y = 3, ScaleX = 1, ScaleY = 1 }
            });
            Console.WriteLine(": Done 5");
            db.SaveChanges();

         
            layout2.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table1,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 1, Y = 1, ScaleX = 1, ScaleY = 1 }
            });
            Console.WriteLine(": Done 6");
            db.SaveChanges();

            layout2.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table2,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 4, Y = 1, ScaleX = 1, ScaleY = 1 }
            });
            Console.WriteLine(": Done 7");
            db.SaveChanges();

            layout2.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table3,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 3, Y = 3, ScaleX = 1, ScaleY = 1 }
            });
            Console.WriteLine(": Done 8");
            db.SaveChanges();


            layout2.Tables.Add(new epicuri.Core.DatabaseModel.TableLayout
            {
                Table = table5,
                Position = new epicuri.Core.DatabaseModel.Position { Rotation = 0, X = 5, Y = 5, ScaleX = 1, ScaleY = 1 }
            });
            Console.WriteLine(": Done 9");
            db.SaveChanges();

            Thread.Sleep(500);
            Console.Clear();
            Console.WriteLine("Active Floor Layouts");
            floor1.ActiveLayout = layout;
            floor2.ActiveLayout = layout1;
            db.SaveChanges();

            Thread.Sleep(500);
            Console.Clear();
            
        }
        private static void CreateResources(Restaurant r)
        {
            Console.WriteLine("Creating Resources");
            res = new epicuri.Core.DatabaseModel.Resource
            {
                Restaurant = r,
                CDNUrl = "http://epicuri.thinktouchsee.com/static/43ce9377-9f26-4710-a6a5-61191dee1099.jpg"
            };
            db.AddToResources(res);
            db.SaveChanges();

            Console.WriteLine(": Done 1");

            res2 = new epicuri.Core.DatabaseModel.Resource
            {
                Restaurant = r,
                CDNUrl = "http://epicuri.thinktouchsee.com/static/57cdfe44-6e88-4417-ac0b-90c96bd6458e.png"
            };
            db.AddToResources(res2);
            db.SaveChanges();

            Console.WriteLine(": Done 2");
            Thread.Sleep(500);
            Console.Clear();
        }


        static void Connect()
        {
            Console.WriteLine("Connect to DB");
            db = new epicuri.Core.DatabaseModel.epicuriContainer();
            Console.WriteLine(": Connected");
            Console.Clear();
        }
        static Restaurant CreateRestaurant()
        {
            Console.WriteLine("Create Restaurant");
            var r = new epicuri.Core.DatabaseModel.Restaurant
            {
                Name = "Fortune House",
                Category = db.Categories.First(cat => cat.Name == "Chinese"),
                ISOCurrency = "GBP",
                //Country = db.Countries.First(country => country.Name == "United Kingdom"),
                IANATimezone = "Europe/London",
                Description = "Selected chinese & english hot meals to take away",
                Address = new epicuri.Core.DatabaseModel.Address { City = "York", PostCode = "YO10 3JL", Street = "19 Hull Road", Town = "York" },
                PhoneNumber = "123",
                PhoneNumber2 = "456",
                Position = new epicuri.Core.DatabaseModel.LatLongPair
                {
                    Latitude = 53.954065,
                    Longitude = -1.059322
                },
            };

            p = new epicuri.Core.DatabaseModel.Printer
            {
                Name = "Kitchen",
                IP = "kitchen.printers.local"
            };
            r.Printers.Add(p);

            db.SaveChanges();
            Console.WriteLine(": Created");
            Thread.Sleep(500);
            return r;
        }

    }
}

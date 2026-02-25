
-- --------------------------------------------------
-- Entity Designer DDL Script for SQL Server 2005, 2008, 2012 and Azure
-- --------------------------------------------------
-- Date Created: 04/22/2015 09:49:48
-- Generated from EDMX file: C:\work\epicuri\cpe\epicuriCore\DatabaseModel\epicuri.edmx
-- --------------------------------------------------

SET QUOTED_IDENTIFIER OFF;
GO
USE [epicuri-CR11-CR7];
GO
IF SCHEMA_ID(N'dbo') IS NULL EXECUTE(N'CREATE SCHEMA [dbo]');
GO

-- --------------------------------------------------
-- Dropping existing FOREIGN KEY constraints
-- --------------------------------------------------

IF OBJECT_ID(N'[dbo].[FK_AbsoluteDateConstraint_inherits_DateConstraint]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[DateConstraints_AbsoluteDateConstraint] DROP CONSTRAINT [FK_AbsoluteDateConstraint_inherits_DateConstraint];
GO
IF OBJECT_ID(N'[dbo].[FK_AdhocNotificationAdhocNotificationAck]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[AdhocNotificationAcks] DROP CONSTRAINT [FK_AdhocNotificationAdhocNotificationAck];
GO
IF OBJECT_ID(N'[dbo].[FK_Adjustment_Staffs]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Adjustment] DROP CONSTRAINT [FK_Adjustment_Staffs];
GO
IF OBJECT_ID(N'[dbo].[FK_AdjustmentAdjustmentType]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Adjustment] DROP CONSTRAINT [FK_AdjustmentAdjustmentType];
GO
IF OBJECT_ID(N'[dbo].[FK_Batches_Printers]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Batches] DROP CONSTRAINT [FK_Batches_Printers];
GO
IF OBJECT_ID(N'[dbo].[FK_BatchOrder]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Orders] DROP CONSTRAINT [FK_BatchOrder];
GO
IF OBJECT_ID(N'[dbo].[FK_CashUpDayRestaurant]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[CashUpDay] DROP CONSTRAINT [FK_CashUpDayRestaurant];
GO
IF OBJECT_ID(N'[dbo].[FK_CashUpDayStaff]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[CashUpDay] DROP CONSTRAINT [FK_CashUpDayStaff];
GO
IF OBJECT_ID(N'[dbo].[FK_CategoryRestaurant]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Restaurants] DROP CONSTRAINT [FK_CategoryRestaurant];
GO
IF OBJECT_ID(N'[dbo].[FK_CheckInCustomer]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[CheckIns] DROP CONSTRAINT [FK_CheckInCustomer];
GO
IF OBJECT_ID(N'[dbo].[FK_CheckInDiner]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Diners] DROP CONSTRAINT [FK_CheckInDiner];
GO
IF OBJECT_ID(N'[dbo].[FK_CheckInParty]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[CheckIns] DROP CONSTRAINT [FK_CheckInParty];
GO
IF OBJECT_ID(N'[dbo].[FK_CheckInRestaurant]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[CheckIns] DROP CONSTRAINT [FK_CheckInRestaurant];
GO
IF OBJECT_ID(N'[dbo].[FK_CountryRestaurant]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Restaurants] DROP CONSTRAINT [FK_CountryRestaurant];
GO
IF OBJECT_ID(N'[dbo].[FK_CountryTaxType]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[TaxTypes] DROP CONSTRAINT [FK_CountryTaxType];
GO
IF OBJECT_ID(N'[dbo].[FK_CourseOrder]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Orders] DROP CONSTRAINT [FK_CourseOrder];
GO
IF OBJECT_ID(N'[dbo].[FK_CustomerAllergy_Allergy]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[CustomerAllergy] DROP CONSTRAINT [FK_CustomerAllergy_Allergy];
GO
IF OBJECT_ID(N'[dbo].[FK_CustomerAllergy_Customer]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[CustomerAllergy] DROP CONSTRAINT [FK_CustomerAllergy_Customer];
GO
IF OBJECT_ID(N'[dbo].[FK_CustomerAuthenticationKey]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[AuthenticationKeys] DROP CONSTRAINT [FK_CustomerAuthenticationKey];
GO
IF OBJECT_ID(N'[dbo].[FK_CustomerBlackMark]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[BlackMarks] DROP CONSTRAINT [FK_CustomerBlackMark];
GO
IF OBJECT_ID(N'[dbo].[FK_CustomerDietaryRequirement_Customer]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[CustomerDietaryRequirement] DROP CONSTRAINT [FK_CustomerDietaryRequirement_Customer];
GO
IF OBJECT_ID(N'[dbo].[FK_CustomerDietaryRequirement_DietaryRequirement]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[CustomerDietaryRequirement] DROP CONSTRAINT [FK_CustomerDietaryRequirement_DietaryRequirement];
GO
IF OBJECT_ID(N'[dbo].[FK_CustomerDiner]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Diners] DROP CONSTRAINT [FK_CustomerDiner];
GO
IF OBJECT_ID(N'[dbo].[FK_CustomerFoodPreference_Customer]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[CustomerFoodPreference] DROP CONSTRAINT [FK_CustomerFoodPreference_Customer];
GO
IF OBJECT_ID(N'[dbo].[FK_CustomerFoodPreference_FoodPreference]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[CustomerFoodPreference] DROP CONSTRAINT [FK_CustomerFoodPreference_FoodPreference];
GO
IF OBJECT_ID(N'[dbo].[FK_CustomerRating]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Ratings] DROP CONSTRAINT [FK_CustomerRating];
GO
IF OBJECT_ID(N'[dbo].[FK_DayOfWeekConstraint_inherits_DateConstraint]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[DateConstraints_DayOfWeekConstraint] DROP CONSTRAINT [FK_DayOfWeekConstraint_inherits_DateConstraint];
GO
IF OBJECT_ID(N'[dbo].[FK_DayOfYearConstraint_inherits_DateConstraint]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[DateConstraints_DayOfYearConstraint] DROP CONSTRAINT [FK_DayOfYearConstraint_inherits_DateConstraint];
GO
IF OBJECT_ID(N'[dbo].[FK_DinerOrder]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Orders] DROP CONSTRAINT [FK_DinerOrder];
GO
IF OBJECT_ID(N'[dbo].[FK_DinerTakeAwaySession]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Sessions_TakeAwaySession] DROP CONSTRAINT [FK_DinerTakeAwaySession];
GO
IF OBJECT_ID(N'[dbo].[FK_FloorLayout]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Layouts] DROP CONSTRAINT [FK_FloorLayout];
GO
IF OBJECT_ID(N'[dbo].[FK_HeadOffice]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Restaurants] DROP CONSTRAINT [FK_HeadOffice];
GO
IF OBJECT_ID(N'[dbo].[FK_LayoutFloor]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Floors] DROP CONSTRAINT [FK_LayoutFloor];
GO
IF OBJECT_ID(N'[dbo].[FK_LayoutTableLayout]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[TableLayouts] DROP CONSTRAINT [FK_LayoutTableLayout];
GO
IF OBJECT_ID(N'[dbo].[FK_MenuCategoryCourse_Course]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[MenuCategoryCourse] DROP CONSTRAINT [FK_MenuCategoryCourse_Course];
GO
IF OBJECT_ID(N'[dbo].[FK_MenuCategoryCourse_MenuCategory]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[MenuCategoryCourse] DROP CONSTRAINT [FK_MenuCategoryCourse_MenuCategory];
GO
IF OBJECT_ID(N'[dbo].[FK_MenuCategoryMenuGroup]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[MenuGroups] DROP CONSTRAINT [FK_MenuCategoryMenuGroup];
GO
IF OBJECT_ID(N'[dbo].[FK_MenuGroupMenuItem_MenuGroup]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[MenuGroupMenuItem] DROP CONSTRAINT [FK_MenuGroupMenuItem_MenuGroup];
GO
IF OBJECT_ID(N'[dbo].[FK_MenuGroupMenuItem_MenuItem]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[MenuGroupMenuItem] DROP CONSTRAINT [FK_MenuGroupMenuItem_MenuItem];
GO
IF OBJECT_ID(N'[dbo].[FK_MenuItemMenuTag_MenuItem]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[MenuItemMenuTag] DROP CONSTRAINT [FK_MenuItemMenuTag_MenuItem];
GO
IF OBJECT_ID(N'[dbo].[FK_MenuItemMenuTag_MenuTag]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[MenuItemMenuTag] DROP CONSTRAINT [FK_MenuItemMenuTag_MenuTag];
GO
IF OBJECT_ID(N'[dbo].[FK_MenuItemModifierGroup_MenuItem]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[MenuItemModifierGroup] DROP CONSTRAINT [FK_MenuItemModifierGroup_MenuItem];
GO
IF OBJECT_ID(N'[dbo].[FK_MenuItemModifierGroup_ModifierGroup]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[MenuItemModifierGroup] DROP CONSTRAINT [FK_MenuItemModifierGroup_ModifierGroup];
GO
IF OBJECT_ID(N'[dbo].[FK_MenuItemOrder]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Orders] DROP CONSTRAINT [FK_MenuItemOrder];
GO
IF OBJECT_ID(N'[dbo].[FK_MenuItemRating]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Ratings] DROP CONSTRAINT [FK_MenuItemRating];
GO
IF OBJECT_ID(N'[dbo].[FK_MenuMenuCategory]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[MenuCategories] DROP CONSTRAINT [FK_MenuMenuCategory];
GO
IF OBJECT_ID(N'[dbo].[FK_MenuRestaurant]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Restaurants] DROP CONSTRAINT [FK_MenuRestaurant];
GO
IF OBJECT_ID(N'[dbo].[FK_MenuService]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Services] DROP CONSTRAINT [FK_MenuService];
GO
IF OBJECT_ID(N'[dbo].[FK_MenuService1]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Services] DROP CONSTRAINT [FK_MenuService1];
GO
IF OBJECT_ID(N'[dbo].[FK_MewsAdjustment_inherits_Adjustment]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Adjustment_Mews] DROP CONSTRAINT [FK_MewsAdjustment_inherits_Adjustment];
GO
IF OBJECT_ID(N'[dbo].[FK_ModifierGroupModifier]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Modifiers] DROP CONSTRAINT [FK_ModifierGroupModifier];
GO
IF OBJECT_ID(N'[dbo].[FK_ModifierTaxType]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Modifiers] DROP CONSTRAINT [FK_ModifierTaxType];
GO
IF OBJECT_ID(N'[dbo].[FK_NotificationNotificationAck]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[NotificationAcks] DROP CONSTRAINT [FK_NotificationNotificationAck];
GO
IF OBJECT_ID(N'[dbo].[FK_NotificationRecurringScheduleItem_Notification]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[NotificationRecurringScheduleItem] DROP CONSTRAINT [FK_NotificationRecurringScheduleItem_Notification];
GO
IF OBJECT_ID(N'[dbo].[FK_NotificationRecurringScheduleItem_RecurringScheduleItem]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[NotificationRecurringScheduleItem] DROP CONSTRAINT [FK_NotificationRecurringScheduleItem_RecurringScheduleItem];
GO
IF OBJECT_ID(N'[dbo].[FK_OrderAdjustmentType]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Orders] DROP CONSTRAINT [FK_OrderAdjustmentType];
GO
IF OBJECT_ID(N'[dbo].[FK_OrderModifier_Modifier]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[OrderModifier] DROP CONSTRAINT [FK_OrderModifier_Modifier];
GO
IF OBJECT_ID(N'[dbo].[FK_OrderModifier_Order]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[OrderModifier] DROP CONSTRAINT [FK_OrderModifier_Order];
GO
IF OBJECT_ID(N'[dbo].[FK_Orders_Staffs]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Orders] DROP CONSTRAINT [FK_Orders_Staffs];
GO
IF OBJECT_ID(N'[dbo].[FK_PartyCustomer]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Parties] DROP CONSTRAINT [FK_PartyCustomer];
GO
IF OBJECT_ID(N'[dbo].[FK_PartySession]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Sessions] DROP CONSTRAINT [FK_PartySession];
GO
IF OBJECT_ID(N'[dbo].[FK_PrinterMenuItem]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[MenuItems] DROP CONSTRAINT [FK_PrinterMenuItem];
GO
IF OBJECT_ID(N'[dbo].[FK_Reservation_inherits_Party]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Parties_Reservation] DROP CONSTRAINT [FK_Reservation_inherits_Party];
GO
IF OBJECT_ID(N'[dbo].[FK_ResourceFloor]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Floors] DROP CONSTRAINT [FK_ResourceFloor];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantBatch]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Batches] DROP CONSTRAINT [FK_RestaurantBatch];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantCourse]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Courses] DROP CONSTRAINT [FK_RestaurantCourse];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantDateConstraint]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[DateConstraints] DROP CONSTRAINT [FK_RestaurantDateConstraint];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantDevice]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Devices] DROP CONSTRAINT [FK_RestaurantDevice];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantDeviceRegistrationToken]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[DeviceRegistrationTokens] DROP CONSTRAINT [FK_RestaurantDeviceRegistrationToken];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantEvent]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Events] DROP CONSTRAINT [FK_RestaurantEvent];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantFloor]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Floors] DROP CONSTRAINT [FK_RestaurantFloor];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantMenu]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Menus] DROP CONSTRAINT [FK_RestaurantMenu];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantMenuItem]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[MenuItems] DROP CONSTRAINT [FK_RestaurantMenuItem];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantMenuTag]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[MenuTags] DROP CONSTRAINT [FK_RestaurantMenuTag];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantModifierGroup]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[ModifierGroups] DROP CONSTRAINT [FK_RestaurantModifierGroup];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantNotification]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Notifications] DROP CONSTRAINT [FK_RestaurantNotification];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantParty]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Parties] DROP CONSTRAINT [FK_RestaurantParty];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantPrinter]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Printers] DROP CONSTRAINT [FK_RestaurantPrinter];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantResource]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Resources] DROP CONSTRAINT [FK_RestaurantResource];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantService]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Services] DROP CONSTRAINT [FK_RestaurantService];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantSession]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Sessions] DROP CONSTRAINT [FK_RestaurantSession];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantSetting]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Settings] DROP CONSTRAINT [FK_RestaurantSetting];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantsReceiptResource]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Restaurants] DROP CONSTRAINT [FK_RestaurantsReceiptResource];
GO
IF OBJECT_ID(N'[dbo].[FK_RestaurantStaff]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Staffs] DROP CONSTRAINT [FK_RestaurantStaff];
GO
IF OBJECT_ID(N'[dbo].[FK_RolePermission_Permission]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[RolePermission] DROP CONSTRAINT [FK_RolePermission_Permission];
GO
IF OBJECT_ID(N'[dbo].[FK_RolePermission_Role]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[RolePermission] DROP CONSTRAINT [FK_RolePermission_Role];
GO
IF OBJECT_ID(N'[dbo].[FK_ScheduleItemEvent]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Events] DROP CONSTRAINT [FK_ScheduleItemEvent];
GO
IF OBJECT_ID(N'[dbo].[FK_ScheduleItemNotification_Notification]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[ScheduleItemNotification] DROP CONSTRAINT [FK_ScheduleItemNotification_Notification];
GO
IF OBJECT_ID(N'[dbo].[FK_ScheduleItemNotification_ScheduleItem]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[ScheduleItemNotification] DROP CONSTRAINT [FK_ScheduleItemNotification_ScheduleItem];
GO
IF OBJECT_ID(N'[dbo].[FK_SeatedSession_inherits_Session]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Sessions_SeatedSession] DROP CONSTRAINT [FK_SeatedSession_inherits_Session];
GO
IF OBJECT_ID(N'[dbo].[FK_SeatedSessionDiner]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Diners] DROP CONSTRAINT [FK_SeatedSessionDiner];
GO
IF OBJECT_ID(N'[dbo].[FK_SeatedSessionParty]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Parties] DROP CONSTRAINT [FK_SeatedSessionParty];
GO
IF OBJECT_ID(N'[dbo].[FK_ServiceCourse]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Courses] DROP CONSTRAINT [FK_ServiceCourse];
GO
IF OBJECT_ID(N'[dbo].[FK_ServiceRecurringScheduleItem]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[RecurringScheduleItems] DROP CONSTRAINT [FK_ServiceRecurringScheduleItem];
GO
IF OBJECT_ID(N'[dbo].[FK_ServiceScheduleItem]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[ScheduleItems] DROP CONSTRAINT [FK_ServiceScheduleItem];
GO
IF OBJECT_ID(N'[dbo].[FK_ServiceSeatedSession]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Sessions_SeatedSession] DROP CONSTRAINT [FK_ServiceSeatedSession];
GO
IF OBJECT_ID(N'[dbo].[FK_SessionAdhocNotification]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[AdhocNotifications] DROP CONSTRAINT [FK_SessionAdhocNotification];
GO
IF OBJECT_ID(N'[dbo].[FK_SessionAdjustment]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Adjustment] DROP CONSTRAINT [FK_SessionAdjustment];
GO
IF OBJECT_ID(N'[dbo].[FK_SessionCashUpDay]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Sessions] DROP CONSTRAINT [FK_SessionCashUpDay];
GO
IF OBJECT_ID(N'[dbo].[FK_SessionCustomer]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Sessions] DROP CONSTRAINT [FK_SessionCustomer];
GO
IF OBJECT_ID(N'[dbo].[FK_SessionEvent]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Events] DROP CONSTRAINT [FK_SessionEvent];
GO
IF OBJECT_ID(N'[dbo].[FK_SessionNotificationAck]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[NotificationAcks] DROP CONSTRAINT [FK_SessionNotificationAck];
GO
IF OBJECT_ID(N'[dbo].[FK_SessionOrder]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Orders] DROP CONSTRAINT [FK_SessionOrder];
GO
IF OBJECT_ID(N'[dbo].[FK_SessionPayment]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Payments] DROP CONSTRAINT [FK_SessionPayment];
GO
IF OBJECT_ID(N'[dbo].[FK_StaffAuthenticationKeyRestaurant]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[StaffAuthenticationKeys] DROP CONSTRAINT [FK_StaffAuthenticationKeyRestaurant];
GO
IF OBJECT_ID(N'[dbo].[FK_StaffAuthenticationKeyStaff]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[StaffAuthenticationKeys] DROP CONSTRAINT [FK_StaffAuthenticationKeyStaff];
GO
IF OBJECT_ID(N'[dbo].[FK_StaffRole_Role]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[StaffRole] DROP CONSTRAINT [FK_StaffRole_Role];
GO
IF OBJECT_ID(N'[dbo].[FK_StaffRole_Staff]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[StaffRole] DROP CONSTRAINT [FK_StaffRole_Staff];
GO
IF OBJECT_ID(N'[dbo].[FK_TableRestaurant]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Tables] DROP CONSTRAINT [FK_TableRestaurant];
GO
IF OBJECT_ID(N'[dbo].[FK_TableSeatedSession_SeatedSession]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[TableSeatedSession] DROP CONSTRAINT [FK_TableSeatedSession_SeatedSession];
GO
IF OBJECT_ID(N'[dbo].[FK_TableSeatedSession_Table]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[TableSeatedSession] DROP CONSTRAINT [FK_TableSeatedSession_Table];
GO
IF OBJECT_ID(N'[dbo].[FK_TableTableLayout]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[TableLayouts] DROP CONSTRAINT [FK_TableTableLayout];
GO
IF OBJECT_ID(N'[dbo].[FK_TakeAwaySession_inherits_Session]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Sessions_TakeAwaySession] DROP CONSTRAINT [FK_TakeAwaySession_inherits_Session];
GO
IF OBJECT_ID(N'[dbo].[FK_TaxTypeMenuItem]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[MenuItems] DROP CONSTRAINT [FK_TaxTypeMenuItem];
GO
IF OBJECT_ID(N'[dbo].[FK_WaitingList_inherits_Party]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Parties_WaitingList] DROP CONSTRAINT [FK_WaitingList_inherits_Party];
GO

-- --------------------------------------------------
-- Dropping existing tables
-- --------------------------------------------------

IF OBJECT_ID(N'[dbo].[AdhocNotificationAcks]', 'U') IS NOT NULL
    DROP TABLE [dbo].[AdhocNotificationAcks];
GO
IF OBJECT_ID(N'[dbo].[AdhocNotifications]', 'U') IS NOT NULL
    DROP TABLE [dbo].[AdhocNotifications];
GO
IF OBJECT_ID(N'[dbo].[Adjustment]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Adjustment];
GO
IF OBJECT_ID(N'[dbo].[Adjustment_Mews]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Adjustment_Mews];
GO
IF OBJECT_ID(N'[dbo].[AdjustmentTypes]', 'U') IS NOT NULL
    DROP TABLE [dbo].[AdjustmentTypes];
GO
IF OBJECT_ID(N'[dbo].[Allergies]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Allergies];
GO
IF OBJECT_ID(N'[dbo].[AuthenticationKeys]', 'U') IS NOT NULL
    DROP TABLE [dbo].[AuthenticationKeys];
GO
IF OBJECT_ID(N'[dbo].[Batches]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Batches];
GO
IF OBJECT_ID(N'[dbo].[BlackMarks]', 'U') IS NOT NULL
    DROP TABLE [dbo].[BlackMarks];
GO
IF OBJECT_ID(N'[dbo].[CashUpDay]', 'U') IS NOT NULL
    DROP TABLE [dbo].[CashUpDay];
GO
IF OBJECT_ID(N'[dbo].[Categories]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Categories];
GO
IF OBJECT_ID(N'[dbo].[CheckIns]', 'U') IS NOT NULL
    DROP TABLE [dbo].[CheckIns];
GO
IF OBJECT_ID(N'[dbo].[Countries]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Countries];
GO
IF OBJECT_ID(N'[dbo].[Courses]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Courses];
GO
IF OBJECT_ID(N'[dbo].[CustomerAllergy]', 'U') IS NOT NULL
    DROP TABLE [dbo].[CustomerAllergy];
GO
IF OBJECT_ID(N'[dbo].[CustomerDietaryRequirement]', 'U') IS NOT NULL
    DROP TABLE [dbo].[CustomerDietaryRequirement];
GO
IF OBJECT_ID(N'[dbo].[CustomerFoodPreference]', 'U') IS NOT NULL
    DROP TABLE [dbo].[CustomerFoodPreference];
GO
IF OBJECT_ID(N'[dbo].[Customers]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Customers];
GO
IF OBJECT_ID(N'[dbo].[DateConstraints]', 'U') IS NOT NULL
    DROP TABLE [dbo].[DateConstraints];
GO
IF OBJECT_ID(N'[dbo].[DateConstraints_AbsoluteDateConstraint]', 'U') IS NOT NULL
    DROP TABLE [dbo].[DateConstraints_AbsoluteDateConstraint];
GO
IF OBJECT_ID(N'[dbo].[DateConstraints_DayOfWeekConstraint]', 'U') IS NOT NULL
    DROP TABLE [dbo].[DateConstraints_DayOfWeekConstraint];
GO
IF OBJECT_ID(N'[dbo].[DateConstraints_DayOfYearConstraint]', 'U') IS NOT NULL
    DROP TABLE [dbo].[DateConstraints_DayOfYearConstraint];
GO
IF OBJECT_ID(N'[dbo].[DefaultSettings]', 'U') IS NOT NULL
    DROP TABLE [dbo].[DefaultSettings];
GO
IF OBJECT_ID(N'[dbo].[DeviceRegistrationTokens]', 'U') IS NOT NULL
    DROP TABLE [dbo].[DeviceRegistrationTokens];
GO
IF OBJECT_ID(N'[dbo].[Devices]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Devices];
GO
IF OBJECT_ID(N'[dbo].[DietaryRequirements]', 'U') IS NOT NULL
    DROP TABLE [dbo].[DietaryRequirements];
GO
IF OBJECT_ID(N'[dbo].[Diners]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Diners];
GO
IF OBJECT_ID(N'[dbo].[Events]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Events];
GO
IF OBJECT_ID(N'[dbo].[Floors]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Floors];
GO
IF OBJECT_ID(N'[dbo].[FoodPreferences]', 'U') IS NOT NULL
    DROP TABLE [dbo].[FoodPreferences];
GO
IF OBJECT_ID(N'[dbo].[Layouts]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Layouts];
GO
IF OBJECT_ID(N'[dbo].[MenuCategories]', 'U') IS NOT NULL
    DROP TABLE [dbo].[MenuCategories];
GO
IF OBJECT_ID(N'[dbo].[MenuCategoryCourse]', 'U') IS NOT NULL
    DROP TABLE [dbo].[MenuCategoryCourse];
GO
IF OBJECT_ID(N'[dbo].[MenuGroupMenuItem]', 'U') IS NOT NULL
    DROP TABLE [dbo].[MenuGroupMenuItem];
GO
IF OBJECT_ID(N'[dbo].[MenuGroups]', 'U') IS NOT NULL
    DROP TABLE [dbo].[MenuGroups];
GO
IF OBJECT_ID(N'[dbo].[MenuItemMenuTag]', 'U') IS NOT NULL
    DROP TABLE [dbo].[MenuItemMenuTag];
GO
IF OBJECT_ID(N'[dbo].[MenuItemModifierGroup]', 'U') IS NOT NULL
    DROP TABLE [dbo].[MenuItemModifierGroup];
GO
IF OBJECT_ID(N'[dbo].[MenuItems]', 'U') IS NOT NULL
    DROP TABLE [dbo].[MenuItems];
GO
IF OBJECT_ID(N'[dbo].[Menus]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Menus];
GO
IF OBJECT_ID(N'[dbo].[MenuTags]', 'U') IS NOT NULL
    DROP TABLE [dbo].[MenuTags];
GO
IF OBJECT_ID(N'[dbo].[ModifierGroups]', 'U') IS NOT NULL
    DROP TABLE [dbo].[ModifierGroups];
GO
IF OBJECT_ID(N'[dbo].[Modifiers]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Modifiers];
GO
IF OBJECT_ID(N'[dbo].[NotificationAcks]', 'U') IS NOT NULL
    DROP TABLE [dbo].[NotificationAcks];
GO
IF OBJECT_ID(N'[dbo].[NotificationRecurringScheduleItem]', 'U') IS NOT NULL
    DROP TABLE [dbo].[NotificationRecurringScheduleItem];
GO
IF OBJECT_ID(N'[dbo].[Notifications]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Notifications];
GO
IF OBJECT_ID(N'[dbo].[OrderModifier]', 'U') IS NOT NULL
    DROP TABLE [dbo].[OrderModifier];
GO
IF OBJECT_ID(N'[dbo].[Orders]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Orders];
GO
IF OBJECT_ID(N'[dbo].[Parties]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Parties];
GO
IF OBJECT_ID(N'[dbo].[Parties_Reservation]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Parties_Reservation];
GO
IF OBJECT_ID(N'[dbo].[Parties_WaitingList]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Parties_WaitingList];
GO
IF OBJECT_ID(N'[dbo].[Payments]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Payments];
GO
IF OBJECT_ID(N'[dbo].[Permissions]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Permissions];
GO
IF OBJECT_ID(N'[dbo].[Printers]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Printers];
GO
IF OBJECT_ID(N'[dbo].[Ratings]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Ratings];
GO
IF OBJECT_ID(N'[dbo].[RecurringScheduleItems]', 'U') IS NOT NULL
    DROP TABLE [dbo].[RecurringScheduleItems];
GO
IF OBJECT_ID(N'[dbo].[Resources]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Resources];
GO
IF OBJECT_ID(N'[dbo].[Restaurants]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Restaurants];
GO
IF OBJECT_ID(N'[dbo].[RolePermission]', 'U') IS NOT NULL
    DROP TABLE [dbo].[RolePermission];
GO
IF OBJECT_ID(N'[dbo].[Roles]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Roles];
GO
IF OBJECT_ID(N'[dbo].[ScheduleItemNotification]', 'U') IS NOT NULL
    DROP TABLE [dbo].[ScheduleItemNotification];
GO
IF OBJECT_ID(N'[dbo].[ScheduleItems]', 'U') IS NOT NULL
    DROP TABLE [dbo].[ScheduleItems];
GO
IF OBJECT_ID(N'[dbo].[Services]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Services];
GO
IF OBJECT_ID(N'[dbo].[Sessions]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Sessions];
GO
IF OBJECT_ID(N'[dbo].[Sessions_SeatedSession]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Sessions_SeatedSession];
GO
IF OBJECT_ID(N'[dbo].[Sessions_TakeAwaySession]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Sessions_TakeAwaySession];
GO
IF OBJECT_ID(N'[dbo].[Settings]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Settings];
GO
IF OBJECT_ID(N'[dbo].[StaffAuthenticationKeys]', 'U') IS NOT NULL
    DROP TABLE [dbo].[StaffAuthenticationKeys];
GO
IF OBJECT_ID(N'[dbo].[StaffRole]', 'U') IS NOT NULL
    DROP TABLE [dbo].[StaffRole];
GO
IF OBJECT_ID(N'[dbo].[Staffs]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Staffs];
GO
IF OBJECT_ID(N'[dbo].[TableLayouts]', 'U') IS NOT NULL
    DROP TABLE [dbo].[TableLayouts];
GO
IF OBJECT_ID(N'[dbo].[Tables]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Tables];
GO
IF OBJECT_ID(N'[dbo].[TableSeatedSession]', 'U') IS NOT NULL
    DROP TABLE [dbo].[TableSeatedSession];
GO
IF OBJECT_ID(N'[dbo].[TaxTypes]', 'U') IS NOT NULL
    DROP TABLE [dbo].[TaxTypes];
GO

-- --------------------------------------------------
-- Creating all tables
-- --------------------------------------------------

-- Creating table 'Restaurants'
CREATE TABLE [dbo].[Restaurants] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Name] nvarchar(max)  NOT NULL,
    [Address_Street] nvarchar(max)  NOT NULL,
    [Address_Town] nvarchar(max)  NULL,
    [Address_City] nvarchar(max)  NOT NULL,
    [Address_PostCode] nvarchar(max)  NOT NULL,
    [CategoryId] int  NOT NULL,
    [RestaurantId] int  NULL,
    [Description] nvarchar(max)  NOT NULL,
    [Settings_MaximumCapacity] smallint  NOT NULL,
    [Settings_FreeDeliveryRadius] float  NOT NULL,
    [Settings_MaxDeliveryRadius] float  NOT NULL,
    [Settings_FreeDeliveryMinPurchase] float  NOT NULL,
    [Settings_PaidDeliveryMinPurchase] float  NOT NULL,
    [CountryId] int  NOT NULL,
    [PhoneNumber] nvarchar(max)  NULL,
    [PublicEmailAddress] nvarchar(max)  NULL,
    [PhoneNumber2] nvarchar(max)  NULL,
    [Position_Latitude] float  NOT NULL,
    [Position_Longitude] float  NOT NULL,
    [MenuId] int  NULL,
    [Enabled] bit  NOT NULL,
    [ReceiptFooter] nvarchar(max)  NULL,
    [Website] nvarchar(max)  NULL,
    [VATNumber] nvarchar(max)  NULL,
    [ReceiptResourceId] int  NULL,
    [Deleted] datetime  NULL,
    [EnabledForWaiter] bit  NOT NULL,
    [TakeawayPrinterId] int  NULL,
    [BillingPrinterId] int  NULL,
    [TakeawayOffered] int  NOT NULL,
    [MewsIntegration] bit  NOT NULL,
    [MewsAccessToken] varchar(100)  NULL,
    [ISOCurrency] varchar(4)  NOT NULL,
    [IANATimezone] varchar(max)  NOT NULL,
    [ReceiptType] int  NOT NULL
);
GO

-- Creating table 'Sessions'
CREATE TABLE [dbo].[Sessions] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [RestaurantId] int  NOT NULL,
    [StartTime] datetime  NOT NULL,
    [ClosedTime] datetime  NULL,
    [Started] bit  NOT NULL,
    [PriceOffset] float  NOT NULL,
    [InstantiatedFromId] int  NOT NULL,
    [Paid] bit  NOT NULL,
    [RequestedBill] bit  NOT NULL,
    [PercentageOffset] float  NOT NULL,
    [CashUpDayId] int  NULL,
    [PartySession_Session_Id] int  NULL,
    [PrimaryCustomer_Id] int  NULL
);
GO

-- Creating table 'Services'
CREATE TABLE [dbo].[Services] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [RestaurantId] int  NOT NULL,
    [ServiceName] nvarchar(max)  NOT NULL,
    [Notes] nvarchar(max)  NOT NULL,
    [MenuId] int  NOT NULL,
    [Updated] datetime  NOT NULL,
    [Active] bit  NOT NULL,
    [IsTakeaway] bit  NOT NULL,
    [MenuId1] int  NULL,
    [Deleted] datetime  NULL
);
GO

-- Creating table 'Menus'
CREATE TABLE [dbo].[Menus] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [MenuName] nvarchar(max)  NOT NULL,
    [RestaurantId] int  NOT NULL,
    [LastUpdated] datetime  NOT NULL,
    [Active] bit  NOT NULL,
    [Deleted] bit  NULL
);
GO

-- Creating table 'MenuCategories'
CREATE TABLE [dbo].[MenuCategories] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [MenuId] int  NOT NULL,
    [CategoryName] nvarchar(max)  NOT NULL,
    [Order] int  NOT NULL,
    [Deleted] bit  NOT NULL
);
GO

-- Creating table 'MenuItems'
CREATE TABLE [dbo].[MenuItems] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [MenuGroupId] int  NOT NULL,
    [TaxTypeId] int  NOT NULL,
    [RestaurantId] int  NOT NULL,
    [Name] nvarchar(max)  NOT NULL,
    [Price] float  NOT NULL,
    [Description] nvarchar(max)  NOT NULL,
    [ImageURL] nvarchar(max)  NOT NULL,
    [Deleted] bit  NOT NULL,
    [PrinterId] int  NOT NULL,
    [MenuItemTypeId] int  NOT NULL,
    [Unavailable] bit  NOT NULL
);
GO

-- Creating table 'MenuTags'
CREATE TABLE [dbo].[MenuTags] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [RestaurantId] int  NOT NULL,
    [Tag] nvarchar(max)  NOT NULL
);
GO

-- Creating table 'MenuGroups'
CREATE TABLE [dbo].[MenuGroups] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [MenuCategoryId] int  NOT NULL,
    [GroupName] nvarchar(max)  NOT NULL,
    [Order] int  NOT NULL
);
GO

-- Creating table 'Orders'
CREATE TABLE [dbo].[Orders] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [MenuItemId] int  NOT NULL,
    [DinerId] int  NOT NULL,
    [Note] nvarchar(max)  NOT NULL,
    [SessionId] int  NOT NULL,
    [Completed] datetime  NULL,
    [CourseId] int  NULL,
    [BatchId] int  NOT NULL,
    [PriceOverride] float  NULL,
    [Deleted] bit  NOT NULL,
    [Locked] bit  NOT NULL,
    [InstantiatedFromId] int  NOT NULL,
    [DiscountReason_Id] int  NULL,
    [OrderTime] datetime  NOT NULL,
    [StaffId] int  NULL
);
GO

-- Creating table 'Tables'
CREATE TABLE [dbo].[Tables] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Name] nvarchar(max)  NOT NULL,
    [Shape] smallint  NOT NULL,
    [DefaultCovers] smallint  NOT NULL,
    [RestaurantId] int  NOT NULL
);
GO

-- Creating table 'Diners'
CREATE TABLE [dbo].[Diners] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [SeatedSessionId] int  NULL,
    [IsTable] bit  NOT NULL,
    [CustomerId] int  NULL,
    [CheckInDiner_Diner_Id] int  NULL
);
GO

-- Creating table 'Customers'
CREATE TABLE [dbo].[Customers] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Name_Firstname] nvarchar(max)  NOT NULL,
    [Name_Surname] nvarchar(max)  NOT NULL,
    [Address_Street] nvarchar(max)  NULL,
    [Address_Town] nvarchar(max)  NULL,
    [Address_City] nvarchar(max)  NULL,
    [Address_PostCode] nvarchar(max)  NULL,
    [Birthday] datetime  NULL,
    [PhoneNumber] nvarchar(max)  NULL,
    [Auth] nvarchar(max)  NOT NULL,
    [Salt] nvarchar(max)  NOT NULL,
    [Email] nvarchar(max)  NULL,
    [FacebookId] nvarchar(max)  NULL,
    [TwitterId] nvarchar(max)  NULL,
    [FavouriteFood] nvarchar(max)  NULL,
    [FavouriteDrink] nvarchar(max)  NULL,
    [HatedFood] nvarchar(max)  NULL
);
GO

-- Creating table 'Ratings'
CREATE TABLE [dbo].[Ratings] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [CustomerId] int  NOT NULL,
    [MenuItemId] int  NOT NULL
);
GO

-- Creating table 'Floors'
CREATE TABLE [dbo].[Floors] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [RestaurantId] int  NOT NULL,
    [Name] nvarchar(max)  NOT NULL,
    [Capacity] smallint  NOT NULL,
    [ResourceId] int  NOT NULL,
    [LayoutId] int  NULL,
    [Scale] float  NOT NULL,
    [Deleted] bit  NOT NULL
);
GO

-- Creating table 'Layouts'
CREATE TABLE [dbo].[Layouts] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [FloorId] int  NOT NULL,
    [Name] nvarchar(max)  NOT NULL,
    [LastModified] datetime  NOT NULL,
    [Temporary] bit  NOT NULL
);
GO

-- Creating table 'Parties'
CREATE TABLE [dbo].[Parties] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [NumberOfPeople] smallint  NOT NULL,
    [Name] nvarchar(max)  NOT NULL,
    [CreatedTime] datetime  NOT NULL,
    [Deleted] bit  NOT NULL,
    [InstantiatedFromId] int  NOT NULL,
    [ArrivedTime] datetime  NULL,
    [RestaurantId] int  NOT NULL,
    [LeadCustomer_Id] int  NULL,
    [Session_Id] int  NULL
);
GO

-- Creating table 'Staffs'
CREATE TABLE [dbo].[Staffs] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Name] nvarchar(max)  NOT NULL,
    [Pin] nvarchar(max)  NOT NULL,
    [Username] nvarchar(max)  NOT NULL,
    [Phone] nvarchar(max)  NULL,
    [Auth] nvarchar(max)  NOT NULL,
    [Salt] nvarchar(max)  NOT NULL,
    [RestaurantId] int  NOT NULL,
    [Deleted] bit  NOT NULL
);
GO

-- Creating table 'Roles'
CREATE TABLE [dbo].[Roles] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [StaffId] int  NOT NULL,
    [Name] nvarchar(max)  NOT NULL
);
GO

-- Creating table 'Permissions'
CREATE TABLE [dbo].[Permissions] (
    [Id] uniqueidentifier  NOT NULL,
    [Description] nvarchar(max)  NOT NULL
);
GO

-- Creating table 'ModifierGroups'
CREATE TABLE [dbo].[ModifierGroups] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [GroupName] nvarchar(max)  NOT NULL,
    [RestaurantId] int  NOT NULL,
    [UpperLimit] smallint  NOT NULL,
    [Deleted] bit  NOT NULL,
    [LowerLimit] smallint  NOT NULL
);
GO

-- Creating table 'Categories'
CREATE TABLE [dbo].[Categories] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Name] nvarchar(max)  NOT NULL
);
GO

-- Creating table 'Countries'
CREATE TABLE [dbo].[Countries] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Name] nvarchar(max)  NOT NULL,
    [Acronym] nvarchar(max)  NOT NULL
);
GO

-- Creating table 'BlackMarks'
CREATE TABLE [dbo].[BlackMarks] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [CustomerId] int  NOT NULL,
    [Reason] nvarchar(max)  NOT NULL,
    [Added] datetime  NOT NULL,
    [Expires] datetime  NOT NULL
);
GO

-- Creating table 'Devices'
CREATE TABLE [dbo].[Devices] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [RestaurantId] int  NOT NULL,
    [DeviceId] nvarchar(max)  NOT NULL,
    [Hash] nvarchar(max)  NOT NULL,
    [Note] nvarchar(max)  NOT NULL
);
GO

-- Creating table 'DeviceRegistrationTokens'
CREATE TABLE [dbo].[DeviceRegistrationTokens] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Token] nvarchar(max)  NOT NULL,
    [Expires] datetime  NOT NULL,
    [RestaurantId] int  NOT NULL
);
GO

-- Creating table 'Resources'
CREATE TABLE [dbo].[Resources] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [RestaurantId] int  NOT NULL,
    [CDNUrl] nvarchar(max)  NOT NULL
);
GO

-- Creating table 'ScheduleItems'
CREATE TABLE [dbo].[ScheduleItems] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [ServiceId] int  NOT NULL,
    [Delay] int  NOT NULL,
    [Comment] nvarchar(max)  NOT NULL
);
GO

-- Creating table 'Notifications'
CREATE TABLE [dbo].[Notifications] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Text] nvarchar(max)  NOT NULL,
    [Target] nvarchar(max)  NOT NULL,
    [RestaurantId] int  NOT NULL
);
GO

-- Creating table 'Events'
CREATE TABLE [dbo].[Events] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [SessionId] int  NOT NULL,
    [Created] datetime  NOT NULL,
    [ScheduleItemId] int  NOT NULL,
    [RestaurantId] int  NOT NULL
);
GO

-- Creating table 'RecurringScheduleItems'
CREATE TABLE [dbo].[RecurringScheduleItems] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [InitialDelay] smallint  NOT NULL,
    [Period] smallint  NOT NULL,
    [Comment] nvarchar(max)  NOT NULL,
    [ServiceId] int  NOT NULL
);
GO

-- Creating table 'NotificationAcks'
CREATE TABLE [dbo].[NotificationAcks] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Time] datetime  NOT NULL,
    [NotificationId] int  NOT NULL,
    [SessionId] int  NOT NULL
);
GO

-- Creating table 'Courses'
CREATE TABLE [dbo].[Courses] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Name] nvarchar(max)  NOT NULL,
    [RestaurantId] int  NULL,
    [Ordering] smallint  NULL,
    [ServiceId] int  NULL
);
GO

-- Creating table 'AdhocNotifications'
CREATE TABLE [dbo].[AdhocNotifications] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [SessionId] int  NOT NULL,
    [Target] nvarchar(max)  NOT NULL,
    [Text] nvarchar(max)  NOT NULL,
    [Created] datetime  NOT NULL
);
GO

-- Creating table 'AdhocNotificationAcks'
CREATE TABLE [dbo].[AdhocNotificationAcks] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [AdhocNotificationId] int  NOT NULL,
    [Time] datetime  NOT NULL
);
GO

-- Creating table 'Batches'
CREATE TABLE [dbo].[Batches] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [OrderTime] datetime  NOT NULL,
    [RestaurantId] int  NOT NULL,
    [Ident] nvarchar(max)  NOT NULL,
    [Modify] bit  NOT NULL,
    [SpoolTime] datetime  NULL,
    [PrintedTime] datetime  NULL,
    [PrinterId] int  NOT NULL,
    [Printed] bit  NULL
);
GO

-- Creating table 'AuthenticationKeys'
CREATE TABLE [dbo].[AuthenticationKeys] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [CustomerId] int  NOT NULL,
    [Expires] datetime  NOT NULL,
    [Key] nvarchar(max)  NOT NULL
);
GO

-- Creating table 'TableLayouts'
CREATE TABLE [dbo].[TableLayouts] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [TableId] int  NOT NULL,
    [LayoutId] int  NOT NULL,
    [Position_X] float  NOT NULL,
    [Position_Y] float  NOT NULL,
    [Position_Rotation] float  NOT NULL,
    [Position_ScaleX] float  NOT NULL,
    [Position_ScaleY] float  NOT NULL
);
GO

-- Creating table 'CheckIns'
CREATE TABLE [dbo].[CheckIns] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Time] datetime  NOT NULL,
    [Restaurant_Id] int  NOT NULL,
    [Customer_Id] int  NOT NULL,
    [Party_Id] int  NULL
);
GO

-- Creating table 'Payments'
CREATE TABLE [dbo].[Payments] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Amount] float  NOT NULL,
    [SessionId] int  NOT NULL,
    [PaidTime] datetime  NOT NULL
);
GO

-- Creating table 'Settings'
CREATE TABLE [dbo].[Settings] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Key] nvarchar(max)  NOT NULL,
    [Value] nvarchar(max)  NOT NULL,
    [RestaurantId] int  NOT NULL
);
GO

-- Creating table 'DefaultSettings'
CREATE TABLE [dbo].[DefaultSettings] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Key] nvarchar(max)  NOT NULL,
    [Value] nvarchar(max)  NOT NULL,
    [Measure] nvarchar(max)  NULL,
    [SettingDescription] nvarchar(max)  NULL,
    [SortId] int  NULL
);
GO

-- Creating table 'DateConstraints'
CREATE TABLE [dbo].[DateConstraints] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [RestaurantId] int  NOT NULL,
    [TargetSession] bit  NOT NULL,
    [StartTime] time  NOT NULL,
    [EndTime] time  NOT NULL
);
GO

-- Creating table 'Printers'
CREATE TABLE [dbo].[Printers] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [RestaurantId] int  NOT NULL,
    [Name] nvarchar(max)  NOT NULL,
    [IP] nvarchar(max)  NULL,
    [RedirectPrinterId] int  NULL
);
GO

-- Creating table 'FoodPreferences'
CREATE TABLE [dbo].[FoodPreferences] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Name] nvarchar(max)  NOT NULL
);
GO

-- Creating table 'Allergies'
CREATE TABLE [dbo].[Allergies] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Name] nvarchar(max)  NOT NULL
);
GO

-- Creating table 'DietaryRequirements'
CREATE TABLE [dbo].[DietaryRequirements] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Name] nvarchar(max)  NOT NULL
);
GO

-- Creating table 'CashUpDay'
CREATE TABLE [dbo].[CashUpDay] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [StaffId] int  NOT NULL,
    [StartTime] datetime  NOT NULL,
    [EndTime] datetime  NOT NULL,
    [Report] nvarchar(max)  NOT NULL,
    [WrapUp] bit  NOT NULL,
    [RestaurantId] int  NOT NULL,
    [PaymentReport] nvarchar(max)  NULL,
    [AdjustmentReport] nvarchar(max)  NULL,
    [ItemAdjustmentReport] nvarchar(max)  NULL,
    [ItemAdjustmentLossReport] nvarchar(max)  NULL
);
GO

-- Creating table 'Adjustment'
CREATE TABLE [dbo].[Adjustment] (
    [SessionId] int  NOT NULL,
    [Id] int IDENTITY(1,1) NOT NULL,
    [AdjustmentTypeId] int  NOT NULL,
    [NumericalType] int  NOT NULL,
    [Value] float  NOT NULL,
    [Reference] nvarchar(max)  NULL,
    [Created] datetime  NOT NULL,
    [Deleted] datetime  NULL,
    [StaffId] int  NULL
);
GO

-- Creating table 'AdjustmentTypes'
CREATE TABLE [dbo].[AdjustmentTypes] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Name] nvarchar(max)  NOT NULL,
    [Type] int  NOT NULL,
    [Deleted] datetime  NULL,
    [SupportsChange] int  NOT NULL
);
GO

-- Creating table 'StaffAuthenticationKeys'
CREATE TABLE [dbo].[StaffAuthenticationKeys] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Key] nvarchar(max)  NOT NULL,
    [Expires] datetime  NOT NULL,
    [StaffAuthenticationKeyStaff_StaffAuthenticationKey_Id] int  NOT NULL,
    [Restaurant_Id] int  NOT NULL
);
GO

-- Creating table 'TaxTypes'
CREATE TABLE [dbo].[TaxTypes] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [Name] nvarchar(max)  NOT NULL,
    [Rate] float  NOT NULL,
    [CountryId] int  NOT NULL,
    [Deleted] datetime  NULL
);
GO

-- Creating table 'Modifiers'
CREATE TABLE [dbo].[Modifiers] (
    [Id] int IDENTITY(1,1) NOT NULL,
    [ModifierGroupId] int  NOT NULL,
    [ModifierValue] nvarchar(max)  NOT NULL,
    [Cost] float  NOT NULL,
    [Deleted] bit  NOT NULL,
    [TaxType_Id] int  NOT NULL
);
GO

-- Creating table 'Sessions_SeatedSession'
CREATE TABLE [dbo].[Sessions_SeatedSession] (
    [ServiceId] int  NOT NULL,
    [ChairData] nvarchar(max)  NOT NULL,
    [Delay] int  NOT NULL,
    [TipTotal] float  NULL,
    [Id] int  NOT NULL
);
GO

-- Creating table 'Sessions_TakeAwaySession'
CREATE TABLE [dbo].[Sessions_TakeAwaySession] (
    [TakeAwayTypeId] int  NOT NULL,
    [DeliveryAddress_Street] nvarchar(max)  NOT NULL,
    [DeliveryAddress_Town] nvarchar(max)  NULL,
    [DeliveryAddress_City] nvarchar(max)  NOT NULL,
    [DeliveryAddress_PostCode] nvarchar(max)  NOT NULL,
    [Delivery] bit  NOT NULL,
    [ExpectedTime] datetime  NOT NULL,
    [Message] nvarchar(max)  NULL,
    [Telephone] nvarchar(max)  NULL,
    [Name] nvarchar(max)  NULL,
    [RejectionNotice] nvarchar(max)  NULL,
    [Accepted] bit  NOT NULL,
    [Rejected] bit  NOT NULL,
    [Deleted] bit  NOT NULL,
    [Id] int  NOT NULL,
    [Diner_Id] int  NOT NULL
);
GO

-- Creating table 'Parties_WaitingList'
CREATE TABLE [dbo].[Parties_WaitingList] (
    [Id] int  NOT NULL
);
GO

-- Creating table 'Parties_Reservation'
CREATE TABLE [dbo].[Parties_Reservation] (
    [ReservationTime] datetime  NOT NULL,
    [Notes] nvarchar(max)  NOT NULL,
    [Telephone] nvarchar(max)  NOT NULL,
    [Accepted] bit  NOT NULL,
    [Rejected] bit  NOT NULL,
    [RejectionNotice] nvarchar(max)  NULL,
    [Id] int  NOT NULL
);
GO

-- Creating table 'DateConstraints_DayOfYearConstraint'
CREATE TABLE [dbo].[DateConstraints_DayOfYearConstraint] (
    [Date] datetime  NOT NULL,
    [Id] int  NOT NULL
);
GO

-- Creating table 'DateConstraints_DayOfWeekConstraint'
CREATE TABLE [dbo].[DateConstraints_DayOfWeekConstraint] (
    [DayOfWeek] smallint  NOT NULL,
    [BlackoutHours] int  NOT NULL,
    [BlackoutMinutes] int  NOT NULL,
    [Id] int  NOT NULL
);
GO

-- Creating table 'DateConstraints_AbsoluteDateConstraint'
CREATE TABLE [dbo].[DateConstraints_AbsoluteDateConstraint] (
    [Date] datetime  NOT NULL,
    [BlackoutHours] int  NOT NULL,
    [BlackoutMinutes] int  NOT NULL,
    [Id] int  NOT NULL
);
GO

-- Creating table 'Adjustment_MewsAdjustment'
CREATE TABLE [dbo].[Adjustment_MewsAdjustment] (
    [FirstName] varchar(max)  NULL,
    [LastName] varchar(max)  NULL,
    [RoomNo] varchar(max)  NULL,
    [ChargeId] varchar(max)  NOT NULL,
    [Id] int  NOT NULL
);
GO

-- Creating table 'MenuItemMenuTag'
CREATE TABLE [dbo].[MenuItemMenuTag] (
    [MenuItemMenuTag_MenuTag_Id] int  NOT NULL,
    [MenuTags_Id] int  NOT NULL
);
GO

-- Creating table 'MenuGroupMenuItem'
CREATE TABLE [dbo].[MenuGroupMenuItem] (
    [MenuGroups_Id] int  NOT NULL,
    [MenuItems_Id] int  NOT NULL
);
GO

-- Creating table 'TableSeatedSession'
CREATE TABLE [dbo].[TableSeatedSession] (
    [Tables_Id] int  NOT NULL,
    [SeatedSessions_Id] int  NOT NULL
);
GO

-- Creating table 'StaffRole'
CREATE TABLE [dbo].[StaffRole] (
    [Staffs_Id] int  NOT NULL,
    [Roles_Id] int  NOT NULL
);
GO

-- Creating table 'RolePermission'
CREATE TABLE [dbo].[RolePermission] (
    [Roles_Id] int  NOT NULL,
    [Permissions_Id] uniqueidentifier  NOT NULL
);
GO

-- Creating table 'MenuItemModifierGroup'
CREATE TABLE [dbo].[MenuItemModifierGroup] (
    [MenuItemModifierGroup_ModifierGroup_Id] int  NOT NULL,
    [ModifierGroups_Id] int  NOT NULL
);
GO

-- Creating table 'NotificationRecurringScheduleItem'
CREATE TABLE [dbo].[NotificationRecurringScheduleItem] (
    [Notifications_Id] int  NOT NULL,
    [NotificationRecurringScheduleItem_Notification_Id] int  NOT NULL
);
GO

-- Creating table 'ScheduleItemNotification'
CREATE TABLE [dbo].[ScheduleItemNotification] (
    [ScheduleItemNotification_Notification_Id] int  NOT NULL,
    [Notifications_Id] int  NOT NULL
);
GO

-- Creating table 'MenuCategoryCourse'
CREATE TABLE [dbo].[MenuCategoryCourse] (
    [MenuCategoryCourse_Course_Id] int  NOT NULL,
    [Courses_Id] int  NOT NULL
);
GO

-- Creating table 'CustomerDietaryRequirement'
CREATE TABLE [dbo].[CustomerDietaryRequirement] (
    [Customers_Id] int  NOT NULL,
    [DietaryRequirements_Id] int  NOT NULL
);
GO

-- Creating table 'CustomerFoodPreference'
CREATE TABLE [dbo].[CustomerFoodPreference] (
    [Customers_Id] int  NOT NULL,
    [FoodPreferences_Id] int  NOT NULL
);
GO

-- Creating table 'CustomerAllergy'
CREATE TABLE [dbo].[CustomerAllergy] (
    [Customers_Id] int  NOT NULL,
    [Allergies_Id] int  NOT NULL
);
GO

-- Creating table 'OrderModifier'
CREATE TABLE [dbo].[OrderModifier] (
    [Modifiers_Id] int  NOT NULL,
    [Orders_Id] int  NOT NULL
);
GO

-- --------------------------------------------------
-- Creating all PRIMARY KEY constraints
-- --------------------------------------------------

-- Creating primary key on [Id] in table 'Restaurants'
ALTER TABLE [dbo].[Restaurants]
ADD CONSTRAINT [PK_Restaurants]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Sessions'
ALTER TABLE [dbo].[Sessions]
ADD CONSTRAINT [PK_Sessions]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Services'
ALTER TABLE [dbo].[Services]
ADD CONSTRAINT [PK_Services]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Menus'
ALTER TABLE [dbo].[Menus]
ADD CONSTRAINT [PK_Menus]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'MenuCategories'
ALTER TABLE [dbo].[MenuCategories]
ADD CONSTRAINT [PK_MenuCategories]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'MenuItems'
ALTER TABLE [dbo].[MenuItems]
ADD CONSTRAINT [PK_MenuItems]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'MenuTags'
ALTER TABLE [dbo].[MenuTags]
ADD CONSTRAINT [PK_MenuTags]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'MenuGroups'
ALTER TABLE [dbo].[MenuGroups]
ADD CONSTRAINT [PK_MenuGroups]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Orders'
ALTER TABLE [dbo].[Orders]
ADD CONSTRAINT [PK_Orders]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Tables'
ALTER TABLE [dbo].[Tables]
ADD CONSTRAINT [PK_Tables]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Diners'
ALTER TABLE [dbo].[Diners]
ADD CONSTRAINT [PK_Diners]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Customers'
ALTER TABLE [dbo].[Customers]
ADD CONSTRAINT [PK_Customers]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Ratings'
ALTER TABLE [dbo].[Ratings]
ADD CONSTRAINT [PK_Ratings]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Floors'
ALTER TABLE [dbo].[Floors]
ADD CONSTRAINT [PK_Floors]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Layouts'
ALTER TABLE [dbo].[Layouts]
ADD CONSTRAINT [PK_Layouts]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Parties'
ALTER TABLE [dbo].[Parties]
ADD CONSTRAINT [PK_Parties]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Staffs'
ALTER TABLE [dbo].[Staffs]
ADD CONSTRAINT [PK_Staffs]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Roles'
ALTER TABLE [dbo].[Roles]
ADD CONSTRAINT [PK_Roles]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Permissions'
ALTER TABLE [dbo].[Permissions]
ADD CONSTRAINT [PK_Permissions]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'ModifierGroups'
ALTER TABLE [dbo].[ModifierGroups]
ADD CONSTRAINT [PK_ModifierGroups]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Categories'
ALTER TABLE [dbo].[Categories]
ADD CONSTRAINT [PK_Categories]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Countries'
ALTER TABLE [dbo].[Countries]
ADD CONSTRAINT [PK_Countries]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'BlackMarks'
ALTER TABLE [dbo].[BlackMarks]
ADD CONSTRAINT [PK_BlackMarks]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Devices'
ALTER TABLE [dbo].[Devices]
ADD CONSTRAINT [PK_Devices]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'DeviceRegistrationTokens'
ALTER TABLE [dbo].[DeviceRegistrationTokens]
ADD CONSTRAINT [PK_DeviceRegistrationTokens]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Resources'
ALTER TABLE [dbo].[Resources]
ADD CONSTRAINT [PK_Resources]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'ScheduleItems'
ALTER TABLE [dbo].[ScheduleItems]
ADD CONSTRAINT [PK_ScheduleItems]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Notifications'
ALTER TABLE [dbo].[Notifications]
ADD CONSTRAINT [PK_Notifications]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Events'
ALTER TABLE [dbo].[Events]
ADD CONSTRAINT [PK_Events]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'RecurringScheduleItems'
ALTER TABLE [dbo].[RecurringScheduleItems]
ADD CONSTRAINT [PK_RecurringScheduleItems]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'NotificationAcks'
ALTER TABLE [dbo].[NotificationAcks]
ADD CONSTRAINT [PK_NotificationAcks]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Courses'
ALTER TABLE [dbo].[Courses]
ADD CONSTRAINT [PK_Courses]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'AdhocNotifications'
ALTER TABLE [dbo].[AdhocNotifications]
ADD CONSTRAINT [PK_AdhocNotifications]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'AdhocNotificationAcks'
ALTER TABLE [dbo].[AdhocNotificationAcks]
ADD CONSTRAINT [PK_AdhocNotificationAcks]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Batches'
ALTER TABLE [dbo].[Batches]
ADD CONSTRAINT [PK_Batches]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'AuthenticationKeys'
ALTER TABLE [dbo].[AuthenticationKeys]
ADD CONSTRAINT [PK_AuthenticationKeys]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'TableLayouts'
ALTER TABLE [dbo].[TableLayouts]
ADD CONSTRAINT [PK_TableLayouts]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'CheckIns'
ALTER TABLE [dbo].[CheckIns]
ADD CONSTRAINT [PK_CheckIns]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Payments'
ALTER TABLE [dbo].[Payments]
ADD CONSTRAINT [PK_Payments]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Settings'
ALTER TABLE [dbo].[Settings]
ADD CONSTRAINT [PK_Settings]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'DefaultSettings'
ALTER TABLE [dbo].[DefaultSettings]
ADD CONSTRAINT [PK_DefaultSettings]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'DateConstraints'
ALTER TABLE [dbo].[DateConstraints]
ADD CONSTRAINT [PK_DateConstraints]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Printers'
ALTER TABLE [dbo].[Printers]
ADD CONSTRAINT [PK_Printers]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'FoodPreferences'
ALTER TABLE [dbo].[FoodPreferences]
ADD CONSTRAINT [PK_FoodPreferences]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Allergies'
ALTER TABLE [dbo].[Allergies]
ADD CONSTRAINT [PK_Allergies]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'DietaryRequirements'
ALTER TABLE [dbo].[DietaryRequirements]
ADD CONSTRAINT [PK_DietaryRequirements]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'CashUpDay'
ALTER TABLE [dbo].[CashUpDay]
ADD CONSTRAINT [PK_CashUpDay]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Adjustment'
ALTER TABLE [dbo].[Adjustment]
ADD CONSTRAINT [PK_Adjustment]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'AdjustmentTypes'
ALTER TABLE [dbo].[AdjustmentTypes]
ADD CONSTRAINT [PK_AdjustmentTypes]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'StaffAuthenticationKeys'
ALTER TABLE [dbo].[StaffAuthenticationKeys]
ADD CONSTRAINT [PK_StaffAuthenticationKeys]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'TaxTypes'
ALTER TABLE [dbo].[TaxTypes]
ADD CONSTRAINT [PK_TaxTypes]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Modifiers'
ALTER TABLE [dbo].[Modifiers]
ADD CONSTRAINT [PK_Modifiers]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Sessions_SeatedSession'
ALTER TABLE [dbo].[Sessions_SeatedSession]
ADD CONSTRAINT [PK_Sessions_SeatedSession]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Sessions_TakeAwaySession'
ALTER TABLE [dbo].[Sessions_TakeAwaySession]
ADD CONSTRAINT [PK_Sessions_TakeAwaySession]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Parties_WaitingList'
ALTER TABLE [dbo].[Parties_WaitingList]
ADD CONSTRAINT [PK_Parties_WaitingList]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Parties_Reservation'
ALTER TABLE [dbo].[Parties_Reservation]
ADD CONSTRAINT [PK_Parties_Reservation]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'DateConstraints_DayOfYearConstraint'
ALTER TABLE [dbo].[DateConstraints_DayOfYearConstraint]
ADD CONSTRAINT [PK_DateConstraints_DayOfYearConstraint]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'DateConstraints_DayOfWeekConstraint'
ALTER TABLE [dbo].[DateConstraints_DayOfWeekConstraint]
ADD CONSTRAINT [PK_DateConstraints_DayOfWeekConstraint]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'DateConstraints_AbsoluteDateConstraint'
ALTER TABLE [dbo].[DateConstraints_AbsoluteDateConstraint]
ADD CONSTRAINT [PK_DateConstraints_AbsoluteDateConstraint]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [Id] in table 'Adjustment_MewsAdjustment'
ALTER TABLE [dbo].[Adjustment_MewsAdjustment]
ADD CONSTRAINT [PK_Adjustment_MewsAdjustment]
    PRIMARY KEY CLUSTERED ([Id] ASC);
GO

-- Creating primary key on [MenuItemMenuTag_MenuTag_Id], [MenuTags_Id] in table 'MenuItemMenuTag'
ALTER TABLE [dbo].[MenuItemMenuTag]
ADD CONSTRAINT [PK_MenuItemMenuTag]
    PRIMARY KEY CLUSTERED ([MenuItemMenuTag_MenuTag_Id], [MenuTags_Id] ASC);
GO

-- Creating primary key on [MenuGroups_Id], [MenuItems_Id] in table 'MenuGroupMenuItem'
ALTER TABLE [dbo].[MenuGroupMenuItem]
ADD CONSTRAINT [PK_MenuGroupMenuItem]
    PRIMARY KEY CLUSTERED ([MenuGroups_Id], [MenuItems_Id] ASC);
GO

-- Creating primary key on [Tables_Id], [SeatedSessions_Id] in table 'TableSeatedSession'
ALTER TABLE [dbo].[TableSeatedSession]
ADD CONSTRAINT [PK_TableSeatedSession]
    PRIMARY KEY CLUSTERED ([Tables_Id], [SeatedSessions_Id] ASC);
GO

-- Creating primary key on [Staffs_Id], [Roles_Id] in table 'StaffRole'
ALTER TABLE [dbo].[StaffRole]
ADD CONSTRAINT [PK_StaffRole]
    PRIMARY KEY CLUSTERED ([Staffs_Id], [Roles_Id] ASC);
GO

-- Creating primary key on [Roles_Id], [Permissions_Id] in table 'RolePermission'
ALTER TABLE [dbo].[RolePermission]
ADD CONSTRAINT [PK_RolePermission]
    PRIMARY KEY CLUSTERED ([Roles_Id], [Permissions_Id] ASC);
GO

-- Creating primary key on [MenuItemModifierGroup_ModifierGroup_Id], [ModifierGroups_Id] in table 'MenuItemModifierGroup'
ALTER TABLE [dbo].[MenuItemModifierGroup]
ADD CONSTRAINT [PK_MenuItemModifierGroup]
    PRIMARY KEY CLUSTERED ([MenuItemModifierGroup_ModifierGroup_Id], [ModifierGroups_Id] ASC);
GO

-- Creating primary key on [Notifications_Id], [NotificationRecurringScheduleItem_Notification_Id] in table 'NotificationRecurringScheduleItem'
ALTER TABLE [dbo].[NotificationRecurringScheduleItem]
ADD CONSTRAINT [PK_NotificationRecurringScheduleItem]
    PRIMARY KEY CLUSTERED ([Notifications_Id], [NotificationRecurringScheduleItem_Notification_Id] ASC);
GO

-- Creating primary key on [ScheduleItemNotification_Notification_Id], [Notifications_Id] in table 'ScheduleItemNotification'
ALTER TABLE [dbo].[ScheduleItemNotification]
ADD CONSTRAINT [PK_ScheduleItemNotification]
    PRIMARY KEY CLUSTERED ([ScheduleItemNotification_Notification_Id], [Notifications_Id] ASC);
GO

-- Creating primary key on [MenuCategoryCourse_Course_Id], [Courses_Id] in table 'MenuCategoryCourse'
ALTER TABLE [dbo].[MenuCategoryCourse]
ADD CONSTRAINT [PK_MenuCategoryCourse]
    PRIMARY KEY CLUSTERED ([MenuCategoryCourse_Course_Id], [Courses_Id] ASC);
GO

-- Creating primary key on [Customers_Id], [DietaryRequirements_Id] in table 'CustomerDietaryRequirement'
ALTER TABLE [dbo].[CustomerDietaryRequirement]
ADD CONSTRAINT [PK_CustomerDietaryRequirement]
    PRIMARY KEY CLUSTERED ([Customers_Id], [DietaryRequirements_Id] ASC);
GO

-- Creating primary key on [Customers_Id], [FoodPreferences_Id] in table 'CustomerFoodPreference'
ALTER TABLE [dbo].[CustomerFoodPreference]
ADD CONSTRAINT [PK_CustomerFoodPreference]
    PRIMARY KEY CLUSTERED ([Customers_Id], [FoodPreferences_Id] ASC);
GO

-- Creating primary key on [Customers_Id], [Allergies_Id] in table 'CustomerAllergy'
ALTER TABLE [dbo].[CustomerAllergy]
ADD CONSTRAINT [PK_CustomerAllergy]
    PRIMARY KEY CLUSTERED ([Customers_Id], [Allergies_Id] ASC);
GO

-- Creating primary key on [Modifiers_Id], [Orders_Id] in table 'OrderModifier'
ALTER TABLE [dbo].[OrderModifier]
ADD CONSTRAINT [PK_OrderModifier]
    PRIMARY KEY CLUSTERED ([Modifiers_Id], [Orders_Id] ASC);
GO

-- --------------------------------------------------
-- Creating all FOREIGN KEY constraints
-- --------------------------------------------------

-- Creating foreign key on [RestaurantId] in table 'Sessions'
ALTER TABLE [dbo].[Sessions]
ADD CONSTRAINT [FK_RestaurantSession]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantSession'
CREATE INDEX [IX_FK_RestaurantSession]
ON [dbo].[Sessions]
    ([RestaurantId]);
GO

-- Creating foreign key on [RestaurantId] in table 'Services'
ALTER TABLE [dbo].[Services]
ADD CONSTRAINT [FK_RestaurantService]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantService'
CREATE INDEX [IX_FK_RestaurantService]
ON [dbo].[Services]
    ([RestaurantId]);
GO

-- Creating foreign key on [MenuId] in table 'MenuCategories'
ALTER TABLE [dbo].[MenuCategories]
ADD CONSTRAINT [FK_MenuMenuCategory]
    FOREIGN KEY ([MenuId])
    REFERENCES [dbo].[Menus]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_MenuMenuCategory'
CREATE INDEX [IX_FK_MenuMenuCategory]
ON [dbo].[MenuCategories]
    ([MenuId]);
GO

-- Creating foreign key on [MenuCategoryId] in table 'MenuGroups'
ALTER TABLE [dbo].[MenuGroups]
ADD CONSTRAINT [FK_MenuCategoryMenuGroup]
    FOREIGN KEY ([MenuCategoryId])
    REFERENCES [dbo].[MenuCategories]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_MenuCategoryMenuGroup'
CREATE INDEX [IX_FK_MenuCategoryMenuGroup]
ON [dbo].[MenuGroups]
    ([MenuCategoryId]);
GO

-- Creating foreign key on [MenuItemMenuTag_MenuTag_Id] in table 'MenuItemMenuTag'
ALTER TABLE [dbo].[MenuItemMenuTag]
ADD CONSTRAINT [FK_MenuItemMenuTag_MenuItem]
    FOREIGN KEY ([MenuItemMenuTag_MenuTag_Id])
    REFERENCES [dbo].[MenuItems]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating foreign key on [MenuTags_Id] in table 'MenuItemMenuTag'
ALTER TABLE [dbo].[MenuItemMenuTag]
ADD CONSTRAINT [FK_MenuItemMenuTag_MenuTag]
    FOREIGN KEY ([MenuTags_Id])
    REFERENCES [dbo].[MenuTags]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_MenuItemMenuTag_MenuTag'
CREATE INDEX [IX_FK_MenuItemMenuTag_MenuTag]
ON [dbo].[MenuItemMenuTag]
    ([MenuTags_Id]);
GO

-- Creating foreign key on [RestaurantId] in table 'MenuTags'
ALTER TABLE [dbo].[MenuTags]
ADD CONSTRAINT [FK_RestaurantMenuTag]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantMenuTag'
CREATE INDEX [IX_FK_RestaurantMenuTag]
ON [dbo].[MenuTags]
    ([RestaurantId]);
GO

-- Creating foreign key on [MenuItemId] in table 'Orders'
ALTER TABLE [dbo].[Orders]
ADD CONSTRAINT [FK_MenuItemOrder]
    FOREIGN KEY ([MenuItemId])
    REFERENCES [dbo].[MenuItems]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_MenuItemOrder'
CREATE INDEX [IX_FK_MenuItemOrder]
ON [dbo].[Orders]
    ([MenuItemId]);
GO

-- Creating foreign key on [DinerId] in table 'Orders'
ALTER TABLE [dbo].[Orders]
ADD CONSTRAINT [FK_DinerOrder]
    FOREIGN KEY ([DinerId])
    REFERENCES [dbo].[Diners]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_DinerOrder'
CREATE INDEX [IX_FK_DinerOrder]
ON [dbo].[Orders]
    ([DinerId]);
GO

-- Creating foreign key on [CustomerId] in table 'Ratings'
ALTER TABLE [dbo].[Ratings]
ADD CONSTRAINT [FK_CustomerRating]
    FOREIGN KEY ([CustomerId])
    REFERENCES [dbo].[Customers]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CustomerRating'
CREATE INDEX [IX_FK_CustomerRating]
ON [dbo].[Ratings]
    ([CustomerId]);
GO

-- Creating foreign key on [RestaurantId] in table 'MenuItems'
ALTER TABLE [dbo].[MenuItems]
ADD CONSTRAINT [FK_RestaurantMenuItem]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantMenuItem'
CREATE INDEX [IX_FK_RestaurantMenuItem]
ON [dbo].[MenuItems]
    ([RestaurantId]);
GO

-- Creating foreign key on [RestaurantId] in table 'Floors'
ALTER TABLE [dbo].[Floors]
ADD CONSTRAINT [FK_RestaurantFloor]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantFloor'
CREATE INDEX [IX_FK_RestaurantFloor]
ON [dbo].[Floors]
    ([RestaurantId]);
GO

-- Creating foreign key on [FloorId] in table 'Layouts'
ALTER TABLE [dbo].[Layouts]
ADD CONSTRAINT [FK_FloorLayout]
    FOREIGN KEY ([FloorId])
    REFERENCES [dbo].[Floors]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_FloorLayout'
CREATE INDEX [IX_FK_FloorLayout]
ON [dbo].[Layouts]
    ([FloorId]);
GO

-- Creating foreign key on [MenuGroups_Id] in table 'MenuGroupMenuItem'
ALTER TABLE [dbo].[MenuGroupMenuItem]
ADD CONSTRAINT [FK_MenuGroupMenuItem_MenuGroup]
    FOREIGN KEY ([MenuGroups_Id])
    REFERENCES [dbo].[MenuGroups]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating foreign key on [MenuItems_Id] in table 'MenuGroupMenuItem'
ALTER TABLE [dbo].[MenuGroupMenuItem]
ADD CONSTRAINT [FK_MenuGroupMenuItem_MenuItem]
    FOREIGN KEY ([MenuItems_Id])
    REFERENCES [dbo].[MenuItems]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_MenuGroupMenuItem_MenuItem'
CREATE INDEX [IX_FK_MenuGroupMenuItem_MenuItem]
ON [dbo].[MenuGroupMenuItem]
    ([MenuItems_Id]);
GO

-- Creating foreign key on [Tables_Id] in table 'TableSeatedSession'
ALTER TABLE [dbo].[TableSeatedSession]
ADD CONSTRAINT [FK_TableSeatedSession_Table]
    FOREIGN KEY ([Tables_Id])
    REFERENCES [dbo].[Tables]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating foreign key on [SeatedSessions_Id] in table 'TableSeatedSession'
ALTER TABLE [dbo].[TableSeatedSession]
ADD CONSTRAINT [FK_TableSeatedSession_SeatedSession]
    FOREIGN KEY ([SeatedSessions_Id])
    REFERENCES [dbo].[Sessions_SeatedSession]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_TableSeatedSession_SeatedSession'
CREATE INDEX [IX_FK_TableSeatedSession_SeatedSession]
ON [dbo].[TableSeatedSession]
    ([SeatedSessions_Id]);
GO

-- Creating foreign key on [PartySession_Session_Id] in table 'Sessions'
ALTER TABLE [dbo].[Sessions]
ADD CONSTRAINT [FK_PartySession]
    FOREIGN KEY ([PartySession_Session_Id])
    REFERENCES [dbo].[Parties]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_PartySession'
CREATE INDEX [IX_FK_PartySession]
ON [dbo].[Sessions]
    ([PartySession_Session_Id]);
GO

-- Creating foreign key on [Staffs_Id] in table 'StaffRole'
ALTER TABLE [dbo].[StaffRole]
ADD CONSTRAINT [FK_StaffRole_Staff]
    FOREIGN KEY ([Staffs_Id])
    REFERENCES [dbo].[Staffs]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating foreign key on [Roles_Id] in table 'StaffRole'
ALTER TABLE [dbo].[StaffRole]
ADD CONSTRAINT [FK_StaffRole_Role]
    FOREIGN KEY ([Roles_Id])
    REFERENCES [dbo].[Roles]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_StaffRole_Role'
CREATE INDEX [IX_FK_StaffRole_Role]
ON [dbo].[StaffRole]
    ([Roles_Id]);
GO

-- Creating foreign key on [Roles_Id] in table 'RolePermission'
ALTER TABLE [dbo].[RolePermission]
ADD CONSTRAINT [FK_RolePermission_Role]
    FOREIGN KEY ([Roles_Id])
    REFERENCES [dbo].[Roles]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating foreign key on [Permissions_Id] in table 'RolePermission'
ALTER TABLE [dbo].[RolePermission]
ADD CONSTRAINT [FK_RolePermission_Permission]
    FOREIGN KEY ([Permissions_Id])
    REFERENCES [dbo].[Permissions]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RolePermission_Permission'
CREATE INDEX [IX_FK_RolePermission_Permission]
ON [dbo].[RolePermission]
    ([Permissions_Id]);
GO

-- Creating foreign key on [CategoryId] in table 'Restaurants'
ALTER TABLE [dbo].[Restaurants]
ADD CONSTRAINT [FK_CategoryRestaurant]
    FOREIGN KEY ([CategoryId])
    REFERENCES [dbo].[Categories]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CategoryRestaurant'
CREATE INDEX [IX_FK_CategoryRestaurant]
ON [dbo].[Restaurants]
    ([CategoryId]);
GO

-- Creating foreign key on [RestaurantId] in table 'Restaurants'
ALTER TABLE [dbo].[Restaurants]
ADD CONSTRAINT [FK_HeadOffice]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_HeadOffice'
CREATE INDEX [IX_FK_HeadOffice]
ON [dbo].[Restaurants]
    ([RestaurantId]);
GO

-- Creating foreign key on [PrimaryCustomer_Id] in table 'Sessions'
ALTER TABLE [dbo].[Sessions]
ADD CONSTRAINT [FK_SessionCustomer]
    FOREIGN KEY ([PrimaryCustomer_Id])
    REFERENCES [dbo].[Customers]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_SessionCustomer'
CREATE INDEX [IX_FK_SessionCustomer]
ON [dbo].[Sessions]
    ([PrimaryCustomer_Id]);
GO

-- Creating foreign key on [CustomerId] in table 'BlackMarks'
ALTER TABLE [dbo].[BlackMarks]
ADD CONSTRAINT [FK_CustomerBlackMark]
    FOREIGN KEY ([CustomerId])
    REFERENCES [dbo].[Customers]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CustomerBlackMark'
CREATE INDEX [IX_FK_CustomerBlackMark]
ON [dbo].[BlackMarks]
    ([CustomerId]);
GO

-- Creating foreign key on [CountryId] in table 'Restaurants'
ALTER TABLE [dbo].[Restaurants]
ADD CONSTRAINT [FK_CountryRestaurant]
    FOREIGN KEY ([CountryId])
    REFERENCES [dbo].[Countries]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CountryRestaurant'
CREATE INDEX [IX_FK_CountryRestaurant]
ON [dbo].[Restaurants]
    ([CountryId]);
GO

-- Creating foreign key on [RestaurantId] in table 'Devices'
ALTER TABLE [dbo].[Devices]
ADD CONSTRAINT [FK_RestaurantDevice]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantDevice'
CREATE INDEX [IX_FK_RestaurantDevice]
ON [dbo].[Devices]
    ([RestaurantId]);
GO

-- Creating foreign key on [RestaurantId] in table 'DeviceRegistrationTokens'
ALTER TABLE [dbo].[DeviceRegistrationTokens]
ADD CONSTRAINT [FK_RestaurantDeviceRegistrationToken]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantDeviceRegistrationToken'
CREATE INDEX [IX_FK_RestaurantDeviceRegistrationToken]
ON [dbo].[DeviceRegistrationTokens]
    ([RestaurantId]);
GO

-- Creating foreign key on [ResourceId] in table 'Floors'
ALTER TABLE [dbo].[Floors]
ADD CONSTRAINT [FK_ResourceFloor]
    FOREIGN KEY ([ResourceId])
    REFERENCES [dbo].[Resources]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_ResourceFloor'
CREATE INDEX [IX_FK_ResourceFloor]
ON [dbo].[Floors]
    ([ResourceId]);
GO

-- Creating foreign key on [RestaurantId] in table 'Resources'
ALTER TABLE [dbo].[Resources]
ADD CONSTRAINT [FK_RestaurantResource]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantResource'
CREATE INDEX [IX_FK_RestaurantResource]
ON [dbo].[Resources]
    ([RestaurantId]);
GO

-- Creating foreign key on [RestaurantId] in table 'Menus'
ALTER TABLE [dbo].[Menus]
ADD CONSTRAINT [FK_RestaurantMenu]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantMenu'
CREATE INDEX [IX_FK_RestaurantMenu]
ON [dbo].[Menus]
    ([RestaurantId]);
GO

-- Creating foreign key on [MenuItemId] in table 'Ratings'
ALTER TABLE [dbo].[Ratings]
ADD CONSTRAINT [FK_MenuItemRating]
    FOREIGN KEY ([MenuItemId])
    REFERENCES [dbo].[MenuItems]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_MenuItemRating'
CREATE INDEX [IX_FK_MenuItemRating]
ON [dbo].[Ratings]
    ([MenuItemId]);
GO

-- Creating foreign key on [MenuItemModifierGroup_ModifierGroup_Id] in table 'MenuItemModifierGroup'
ALTER TABLE [dbo].[MenuItemModifierGroup]
ADD CONSTRAINT [FK_MenuItemModifierGroup_MenuItem]
    FOREIGN KEY ([MenuItemModifierGroup_ModifierGroup_Id])
    REFERENCES [dbo].[MenuItems]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating foreign key on [ModifierGroups_Id] in table 'MenuItemModifierGroup'
ALTER TABLE [dbo].[MenuItemModifierGroup]
ADD CONSTRAINT [FK_MenuItemModifierGroup_ModifierGroup]
    FOREIGN KEY ([ModifierGroups_Id])
    REFERENCES [dbo].[ModifierGroups]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_MenuItemModifierGroup_ModifierGroup'
CREATE INDEX [IX_FK_MenuItemModifierGroup_ModifierGroup]
ON [dbo].[MenuItemModifierGroup]
    ([ModifierGroups_Id]);
GO

-- Creating foreign key on [RestaurantId] in table 'ModifierGroups'
ALTER TABLE [dbo].[ModifierGroups]
ADD CONSTRAINT [FK_RestaurantModifierGroup]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantModifierGroup'
CREATE INDEX [IX_FK_RestaurantModifierGroup]
ON [dbo].[ModifierGroups]
    ([RestaurantId]);
GO

-- Creating foreign key on [SeatedSessionId] in table 'Diners'
ALTER TABLE [dbo].[Diners]
ADD CONSTRAINT [FK_SeatedSessionDiner]
    FOREIGN KEY ([SeatedSessionId])
    REFERENCES [dbo].[Sessions_SeatedSession]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_SeatedSessionDiner'
CREATE INDEX [IX_FK_SeatedSessionDiner]
ON [dbo].[Diners]
    ([SeatedSessionId]);
GO

-- Creating foreign key on [CustomerId] in table 'Diners'
ALTER TABLE [dbo].[Diners]
ADD CONSTRAINT [FK_CustomerDiner]
    FOREIGN KEY ([CustomerId])
    REFERENCES [dbo].[Customers]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CustomerDiner'
CREATE INDEX [IX_FK_CustomerDiner]
ON [dbo].[Diners]
    ([CustomerId]);
GO

-- Creating foreign key on [MenuId] in table 'Services'
ALTER TABLE [dbo].[Services]
ADD CONSTRAINT [FK_MenuService]
    FOREIGN KEY ([MenuId])
    REFERENCES [dbo].[Menus]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_MenuService'
CREATE INDEX [IX_FK_MenuService]
ON [dbo].[Services]
    ([MenuId]);
GO

-- Creating foreign key on [ServiceId] in table 'ScheduleItems'
ALTER TABLE [dbo].[ScheduleItems]
ADD CONSTRAINT [FK_ServiceScheduleItem]
    FOREIGN KEY ([ServiceId])
    REFERENCES [dbo].[Services]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_ServiceScheduleItem'
CREATE INDEX [IX_FK_ServiceScheduleItem]
ON [dbo].[ScheduleItems]
    ([ServiceId]);
GO

-- Creating foreign key on [ServiceId] in table 'Sessions_SeatedSession'
ALTER TABLE [dbo].[Sessions_SeatedSession]
ADD CONSTRAINT [FK_ServiceSeatedSession]
    FOREIGN KEY ([ServiceId])
    REFERENCES [dbo].[Services]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_ServiceSeatedSession'
CREATE INDEX [IX_FK_ServiceSeatedSession]
ON [dbo].[Sessions_SeatedSession]
    ([ServiceId]);
GO

-- Creating foreign key on [SessionId] in table 'Events'
ALTER TABLE [dbo].[Events]
ADD CONSTRAINT [FK_SessionEvent]
    FOREIGN KEY ([SessionId])
    REFERENCES [dbo].[Sessions]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_SessionEvent'
CREATE INDEX [IX_FK_SessionEvent]
ON [dbo].[Events]
    ([SessionId]);
GO

-- Creating foreign key on [ScheduleItemId] in table 'Events'
ALTER TABLE [dbo].[Events]
ADD CONSTRAINT [FK_ScheduleItemEvent]
    FOREIGN KEY ([ScheduleItemId])
    REFERENCES [dbo].[ScheduleItems]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_ScheduleItemEvent'
CREATE INDEX [IX_FK_ScheduleItemEvent]
ON [dbo].[Events]
    ([ScheduleItemId]);
GO

-- Creating foreign key on [RestaurantId] in table 'Events'
ALTER TABLE [dbo].[Events]
ADD CONSTRAINT [FK_RestaurantEvent]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantEvent'
CREATE INDEX [IX_FK_RestaurantEvent]
ON [dbo].[Events]
    ([RestaurantId]);
GO

-- Creating foreign key on [ServiceId] in table 'RecurringScheduleItems'
ALTER TABLE [dbo].[RecurringScheduleItems]
ADD CONSTRAINT [FK_ServiceRecurringScheduleItem]
    FOREIGN KEY ([ServiceId])
    REFERENCES [dbo].[Services]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_ServiceRecurringScheduleItem'
CREATE INDEX [IX_FK_ServiceRecurringScheduleItem]
ON [dbo].[RecurringScheduleItems]
    ([ServiceId]);
GO

-- Creating foreign key on [Notifications_Id] in table 'NotificationRecurringScheduleItem'
ALTER TABLE [dbo].[NotificationRecurringScheduleItem]
ADD CONSTRAINT [FK_NotificationRecurringScheduleItem_Notification]
    FOREIGN KEY ([Notifications_Id])
    REFERENCES [dbo].[Notifications]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating foreign key on [NotificationRecurringScheduleItem_Notification_Id] in table 'NotificationRecurringScheduleItem'
ALTER TABLE [dbo].[NotificationRecurringScheduleItem]
ADD CONSTRAINT [FK_NotificationRecurringScheduleItem_RecurringScheduleItem]
    FOREIGN KEY ([NotificationRecurringScheduleItem_Notification_Id])
    REFERENCES [dbo].[RecurringScheduleItems]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_NotificationRecurringScheduleItem_RecurringScheduleItem'
CREATE INDEX [IX_FK_NotificationRecurringScheduleItem_RecurringScheduleItem]
ON [dbo].[NotificationRecurringScheduleItem]
    ([NotificationRecurringScheduleItem_Notification_Id]);
GO

-- Creating foreign key on [ScheduleItemNotification_Notification_Id] in table 'ScheduleItemNotification'
ALTER TABLE [dbo].[ScheduleItemNotification]
ADD CONSTRAINT [FK_ScheduleItemNotification_ScheduleItem]
    FOREIGN KEY ([ScheduleItemNotification_Notification_Id])
    REFERENCES [dbo].[ScheduleItems]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating foreign key on [Notifications_Id] in table 'ScheduleItemNotification'
ALTER TABLE [dbo].[ScheduleItemNotification]
ADD CONSTRAINT [FK_ScheduleItemNotification_Notification]
    FOREIGN KEY ([Notifications_Id])
    REFERENCES [dbo].[Notifications]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_ScheduleItemNotification_Notification'
CREATE INDEX [IX_FK_ScheduleItemNotification_Notification]
ON [dbo].[ScheduleItemNotification]
    ([Notifications_Id]);
GO

-- Creating foreign key on [NotificationId] in table 'NotificationAcks'
ALTER TABLE [dbo].[NotificationAcks]
ADD CONSTRAINT [FK_NotificationNotificationAck]
    FOREIGN KEY ([NotificationId])
    REFERENCES [dbo].[Notifications]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_NotificationNotificationAck'
CREATE INDEX [IX_FK_NotificationNotificationAck]
ON [dbo].[NotificationAcks]
    ([NotificationId]);
GO

-- Creating foreign key on [SessionId] in table 'NotificationAcks'
ALTER TABLE [dbo].[NotificationAcks]
ADD CONSTRAINT [FK_SessionNotificationAck]
    FOREIGN KEY ([SessionId])
    REFERENCES [dbo].[Sessions]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_SessionNotificationAck'
CREATE INDEX [IX_FK_SessionNotificationAck]
ON [dbo].[NotificationAcks]
    ([SessionId]);
GO

-- Creating foreign key on [SessionId] in table 'Orders'
ALTER TABLE [dbo].[Orders]
ADD CONSTRAINT [FK_SessionOrder]
    FOREIGN KEY ([SessionId])
    REFERENCES [dbo].[Sessions]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_SessionOrder'
CREATE INDEX [IX_FK_SessionOrder]
ON [dbo].[Orders]
    ([SessionId]);
GO

-- Creating foreign key on [Diner_Id] in table 'Sessions_TakeAwaySession'
ALTER TABLE [dbo].[Sessions_TakeAwaySession]
ADD CONSTRAINT [FK_DinerTakeAwaySession]
    FOREIGN KEY ([Diner_Id])
    REFERENCES [dbo].[Diners]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_DinerTakeAwaySession'
CREATE INDEX [IX_FK_DinerTakeAwaySession]
ON [dbo].[Sessions_TakeAwaySession]
    ([Diner_Id]);
GO

-- Creating foreign key on [RestaurantId] in table 'Courses'
ALTER TABLE [dbo].[Courses]
ADD CONSTRAINT [FK_RestaurantCourse]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantCourse'
CREATE INDEX [IX_FK_RestaurantCourse]
ON [dbo].[Courses]
    ([RestaurantId]);
GO

-- Creating foreign key on [CourseId] in table 'Orders'
ALTER TABLE [dbo].[Orders]
ADD CONSTRAINT [FK_CourseOrder]
    FOREIGN KEY ([CourseId])
    REFERENCES [dbo].[Courses]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CourseOrder'
CREATE INDEX [IX_FK_CourseOrder]
ON [dbo].[Orders]
    ([CourseId]);
GO

-- Creating foreign key on [SessionId] in table 'AdhocNotifications'
ALTER TABLE [dbo].[AdhocNotifications]
ADD CONSTRAINT [FK_SessionAdhocNotification]
    FOREIGN KEY ([SessionId])
    REFERENCES [dbo].[Sessions]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_SessionAdhocNotification'
CREATE INDEX [IX_FK_SessionAdhocNotification]
ON [dbo].[AdhocNotifications]
    ([SessionId]);
GO

-- Creating foreign key on [AdhocNotificationId] in table 'AdhocNotificationAcks'
ALTER TABLE [dbo].[AdhocNotificationAcks]
ADD CONSTRAINT [FK_AdhocNotificationAdhocNotificationAck]
    FOREIGN KEY ([AdhocNotificationId])
    REFERENCES [dbo].[AdhocNotifications]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_AdhocNotificationAdhocNotificationAck'
CREATE INDEX [IX_FK_AdhocNotificationAdhocNotificationAck]
ON [dbo].[AdhocNotificationAcks]
    ([AdhocNotificationId]);
GO

-- Creating foreign key on [BatchId] in table 'Orders'
ALTER TABLE [dbo].[Orders]
ADD CONSTRAINT [FK_BatchOrder]
    FOREIGN KEY ([BatchId])
    REFERENCES [dbo].[Batches]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_BatchOrder'
CREATE INDEX [IX_FK_BatchOrder]
ON [dbo].[Orders]
    ([BatchId]);
GO

-- Creating foreign key on [RestaurantId] in table 'Batches'
ALTER TABLE [dbo].[Batches]
ADD CONSTRAINT [FK_RestaurantBatch]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantBatch'
CREATE INDEX [IX_FK_RestaurantBatch]
ON [dbo].[Batches]
    ([RestaurantId]);
GO

-- Creating foreign key on [LeadCustomer_Id] in table 'Parties'
ALTER TABLE [dbo].[Parties]
ADD CONSTRAINT [FK_PartyCustomer]
    FOREIGN KEY ([LeadCustomer_Id])
    REFERENCES [dbo].[Customers]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_PartyCustomer'
CREATE INDEX [IX_FK_PartyCustomer]
ON [dbo].[Parties]
    ([LeadCustomer_Id]);
GO

-- Creating foreign key on [CustomerId] in table 'AuthenticationKeys'
ALTER TABLE [dbo].[AuthenticationKeys]
ADD CONSTRAINT [FK_CustomerAuthenticationKey]
    FOREIGN KEY ([CustomerId])
    REFERENCES [dbo].[Customers]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CustomerAuthenticationKey'
CREATE INDEX [IX_FK_CustomerAuthenticationKey]
ON [dbo].[AuthenticationKeys]
    ([CustomerId]);
GO

-- Creating foreign key on [ServiceId] in table 'Courses'
ALTER TABLE [dbo].[Courses]
ADD CONSTRAINT [FK_ServiceCourse]
    FOREIGN KEY ([ServiceId])
    REFERENCES [dbo].[Services]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_ServiceCourse'
CREATE INDEX [IX_FK_ServiceCourse]
ON [dbo].[Courses]
    ([ServiceId]);
GO

-- Creating foreign key on [TableId] in table 'TableLayouts'
ALTER TABLE [dbo].[TableLayouts]
ADD CONSTRAINT [FK_TableTableLayout]
    FOREIGN KEY ([TableId])
    REFERENCES [dbo].[Tables]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_TableTableLayout'
CREATE INDEX [IX_FK_TableTableLayout]
ON [dbo].[TableLayouts]
    ([TableId]);
GO

-- Creating foreign key on [LayoutId] in table 'TableLayouts'
ALTER TABLE [dbo].[TableLayouts]
ADD CONSTRAINT [FK_LayoutTableLayout]
    FOREIGN KEY ([LayoutId])
    REFERENCES [dbo].[Layouts]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_LayoutTableLayout'
CREATE INDEX [IX_FK_LayoutTableLayout]
ON [dbo].[TableLayouts]
    ([LayoutId]);
GO

-- Creating foreign key on [LayoutId] in table 'Floors'
ALTER TABLE [dbo].[Floors]
ADD CONSTRAINT [FK_LayoutFloor]
    FOREIGN KEY ([LayoutId])
    REFERENCES [dbo].[Layouts]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_LayoutFloor'
CREATE INDEX [IX_FK_LayoutFloor]
ON [dbo].[Floors]
    ([LayoutId]);
GO

-- Creating foreign key on [Restaurant_Id] in table 'CheckIns'
ALTER TABLE [dbo].[CheckIns]
ADD CONSTRAINT [FK_CheckInRestaurant]
    FOREIGN KEY ([Restaurant_Id])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CheckInRestaurant'
CREATE INDEX [IX_FK_CheckInRestaurant]
ON [dbo].[CheckIns]
    ([Restaurant_Id]);
GO

-- Creating foreign key on [CheckInDiner_Diner_Id] in table 'Diners'
ALTER TABLE [dbo].[Diners]
ADD CONSTRAINT [FK_CheckInDiner]
    FOREIGN KEY ([CheckInDiner_Diner_Id])
    REFERENCES [dbo].[CheckIns]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CheckInDiner'
CREATE INDEX [IX_FK_CheckInDiner]
ON [dbo].[Diners]
    ([CheckInDiner_Diner_Id]);
GO

-- Creating foreign key on [Customer_Id] in table 'CheckIns'
ALTER TABLE [dbo].[CheckIns]
ADD CONSTRAINT [FK_CheckInCustomer]
    FOREIGN KEY ([Customer_Id])
    REFERENCES [dbo].[Customers]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CheckInCustomer'
CREATE INDEX [IX_FK_CheckInCustomer]
ON [dbo].[CheckIns]
    ([Customer_Id]);
GO

-- Creating foreign key on [MenuId] in table 'Restaurants'
ALTER TABLE [dbo].[Restaurants]
ADD CONSTRAINT [FK_MenuRestaurant]
    FOREIGN KEY ([MenuId])
    REFERENCES [dbo].[Menus]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_MenuRestaurant'
CREATE INDEX [IX_FK_MenuRestaurant]
ON [dbo].[Restaurants]
    ([MenuId]);
GO

-- Creating foreign key on [MenuId1] in table 'Services'
ALTER TABLE [dbo].[Services]
ADD CONSTRAINT [FK_MenuService1]
    FOREIGN KEY ([MenuId1])
    REFERENCES [dbo].[Menus]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_MenuService1'
CREATE INDEX [IX_FK_MenuService1]
ON [dbo].[Services]
    ([MenuId1]);
GO

-- Creating foreign key on [SessionId] in table 'Payments'
ALTER TABLE [dbo].[Payments]
ADD CONSTRAINT [FK_SessionPayment]
    FOREIGN KEY ([SessionId])
    REFERENCES [dbo].[Sessions]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_SessionPayment'
CREATE INDEX [IX_FK_SessionPayment]
ON [dbo].[Payments]
    ([SessionId]);
GO

-- Creating foreign key on [RestaurantId] in table 'Settings'
ALTER TABLE [dbo].[Settings]
ADD CONSTRAINT [FK_RestaurantSetting]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantSetting'
CREATE INDEX [IX_FK_RestaurantSetting]
ON [dbo].[Settings]
    ([RestaurantId]);
GO

-- Creating foreign key on [RestaurantId] in table 'Staffs'
ALTER TABLE [dbo].[Staffs]
ADD CONSTRAINT [FK_RestaurantStaff]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantStaff'
CREATE INDEX [IX_FK_RestaurantStaff]
ON [dbo].[Staffs]
    ([RestaurantId]);
GO

-- Creating foreign key on [Session_Id] in table 'Parties'
ALTER TABLE [dbo].[Parties]
ADD CONSTRAINT [FK_SeatedSessionParty]
    FOREIGN KEY ([Session_Id])
    REFERENCES [dbo].[Sessions_SeatedSession]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_SeatedSessionParty'
CREATE INDEX [IX_FK_SeatedSessionParty]
ON [dbo].[Parties]
    ([Session_Id]);
GO

-- Creating foreign key on [MenuCategoryCourse_Course_Id] in table 'MenuCategoryCourse'
ALTER TABLE [dbo].[MenuCategoryCourse]
ADD CONSTRAINT [FK_MenuCategoryCourse_MenuCategory]
    FOREIGN KEY ([MenuCategoryCourse_Course_Id])
    REFERENCES [dbo].[MenuCategories]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating foreign key on [Courses_Id] in table 'MenuCategoryCourse'
ALTER TABLE [dbo].[MenuCategoryCourse]
ADD CONSTRAINT [FK_MenuCategoryCourse_Course]
    FOREIGN KEY ([Courses_Id])
    REFERENCES [dbo].[Courses]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_MenuCategoryCourse_Course'
CREATE INDEX [IX_FK_MenuCategoryCourse_Course]
ON [dbo].[MenuCategoryCourse]
    ([Courses_Id]);
GO

-- Creating foreign key on [RestaurantId] in table 'Tables'
ALTER TABLE [dbo].[Tables]
ADD CONSTRAINT [FK_TableRestaurant]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_TableRestaurant'
CREATE INDEX [IX_FK_TableRestaurant]
ON [dbo].[Tables]
    ([RestaurantId]);
GO

-- Creating foreign key on [RestaurantId] in table 'DateConstraints'
ALTER TABLE [dbo].[DateConstraints]
ADD CONSTRAINT [FK_RestaurantDateConstraint]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantDateConstraint'
CREATE INDEX [IX_FK_RestaurantDateConstraint]
ON [dbo].[DateConstraints]
    ([RestaurantId]);
GO

-- Creating foreign key on [RestaurantId] in table 'Printers'
ALTER TABLE [dbo].[Printers]
ADD CONSTRAINT [FK_RestaurantPrinter]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantPrinter'
CREATE INDEX [IX_FK_RestaurantPrinter]
ON [dbo].[Printers]
    ([RestaurantId]);
GO

-- Creating foreign key on [RestaurantId] in table 'Notifications'
ALTER TABLE [dbo].[Notifications]
ADD CONSTRAINT [FK_RestaurantNotification]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantNotification'
CREATE INDEX [IX_FK_RestaurantNotification]
ON [dbo].[Notifications]
    ([RestaurantId]);
GO

-- Creating foreign key on [PrinterId] in table 'MenuItems'
ALTER TABLE [dbo].[MenuItems]
ADD CONSTRAINT [FK_PrinterMenuItem]
    FOREIGN KEY ([PrinterId])
    REFERENCES [dbo].[Printers]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_PrinterMenuItem'
CREATE INDEX [IX_FK_PrinterMenuItem]
ON [dbo].[MenuItems]
    ([PrinterId]);
GO

-- Creating foreign key on [Customers_Id] in table 'CustomerDietaryRequirement'
ALTER TABLE [dbo].[CustomerDietaryRequirement]
ADD CONSTRAINT [FK_CustomerDietaryRequirement_Customer]
    FOREIGN KEY ([Customers_Id])
    REFERENCES [dbo].[Customers]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating foreign key on [DietaryRequirements_Id] in table 'CustomerDietaryRequirement'
ALTER TABLE [dbo].[CustomerDietaryRequirement]
ADD CONSTRAINT [FK_CustomerDietaryRequirement_DietaryRequirement]
    FOREIGN KEY ([DietaryRequirements_Id])
    REFERENCES [dbo].[DietaryRequirements]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CustomerDietaryRequirement_DietaryRequirement'
CREATE INDEX [IX_FK_CustomerDietaryRequirement_DietaryRequirement]
ON [dbo].[CustomerDietaryRequirement]
    ([DietaryRequirements_Id]);
GO

-- Creating foreign key on [Customers_Id] in table 'CustomerFoodPreference'
ALTER TABLE [dbo].[CustomerFoodPreference]
ADD CONSTRAINT [FK_CustomerFoodPreference_Customer]
    FOREIGN KEY ([Customers_Id])
    REFERENCES [dbo].[Customers]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating foreign key on [FoodPreferences_Id] in table 'CustomerFoodPreference'
ALTER TABLE [dbo].[CustomerFoodPreference]
ADD CONSTRAINT [FK_CustomerFoodPreference_FoodPreference]
    FOREIGN KEY ([FoodPreferences_Id])
    REFERENCES [dbo].[FoodPreferences]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CustomerFoodPreference_FoodPreference'
CREATE INDEX [IX_FK_CustomerFoodPreference_FoodPreference]
ON [dbo].[CustomerFoodPreference]
    ([FoodPreferences_Id]);
GO

-- Creating foreign key on [Customers_Id] in table 'CustomerAllergy'
ALTER TABLE [dbo].[CustomerAllergy]
ADD CONSTRAINT [FK_CustomerAllergy_Customer]
    FOREIGN KEY ([Customers_Id])
    REFERENCES [dbo].[Customers]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating foreign key on [Allergies_Id] in table 'CustomerAllergy'
ALTER TABLE [dbo].[CustomerAllergy]
ADD CONSTRAINT [FK_CustomerAllergy_Allergy]
    FOREIGN KEY ([Allergies_Id])
    REFERENCES [dbo].[Allergies]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CustomerAllergy_Allergy'
CREATE INDEX [IX_FK_CustomerAllergy_Allergy]
ON [dbo].[CustomerAllergy]
    ([Allergies_Id]);
GO

-- Creating foreign key on [Party_Id] in table 'CheckIns'
ALTER TABLE [dbo].[CheckIns]
ADD CONSTRAINT [FK_CheckInParty]
    FOREIGN KEY ([Party_Id])
    REFERENCES [dbo].[Parties]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CheckInParty'
CREATE INDEX [IX_FK_CheckInParty]
ON [dbo].[CheckIns]
    ([Party_Id]);
GO

-- Creating foreign key on [ReceiptResourceId] in table 'Restaurants'
ALTER TABLE [dbo].[Restaurants]
ADD CONSTRAINT [FK_RestaurantsReceiptResource]
    FOREIGN KEY ([ReceiptResourceId])
    REFERENCES [dbo].[Resources]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantsReceiptResource'
CREATE INDEX [IX_FK_RestaurantsReceiptResource]
ON [dbo].[Restaurants]
    ([ReceiptResourceId]);
GO

-- Creating foreign key on [RestaurantId] in table 'CashUpDay'
ALTER TABLE [dbo].[CashUpDay]
ADD CONSTRAINT [FK_CashUpDayRestaurant]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CashUpDayRestaurant'
CREATE INDEX [IX_FK_CashUpDayRestaurant]
ON [dbo].[CashUpDay]
    ([RestaurantId]);
GO

-- Creating foreign key on [StaffId] in table 'CashUpDay'
ALTER TABLE [dbo].[CashUpDay]
ADD CONSTRAINT [FK_CashUpDayStaff]
    FOREIGN KEY ([StaffId])
    REFERENCES [dbo].[Staffs]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CashUpDayStaff'
CREATE INDEX [IX_FK_CashUpDayStaff]
ON [dbo].[CashUpDay]
    ([StaffId]);
GO

-- Creating foreign key on [CashUpDayId] in table 'Sessions'
ALTER TABLE [dbo].[Sessions]
ADD CONSTRAINT [FK_SessionCashUpDay]
    FOREIGN KEY ([CashUpDayId])
    REFERENCES [dbo].[CashUpDay]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_SessionCashUpDay'
CREATE INDEX [IX_FK_SessionCashUpDay]
ON [dbo].[Sessions]
    ([CashUpDayId]);
GO

-- Creating foreign key on [AdjustmentTypeId] in table 'Adjustment'
ALTER TABLE [dbo].[Adjustment]
ADD CONSTRAINT [FK_AdjustmentAdjustmentType]
    FOREIGN KEY ([AdjustmentTypeId])
    REFERENCES [dbo].[AdjustmentTypes]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_AdjustmentAdjustmentType'
CREATE INDEX [IX_FK_AdjustmentAdjustmentType]
ON [dbo].[Adjustment]
    ([AdjustmentTypeId]);
GO

-- Creating foreign key on [SessionId] in table 'Adjustment'
ALTER TABLE [dbo].[Adjustment]
ADD CONSTRAINT [FK_SessionAdjustment]
    FOREIGN KEY ([SessionId])
    REFERENCES [dbo].[Sessions]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_SessionAdjustment'
CREATE INDEX [IX_FK_SessionAdjustment]
ON [dbo].[Adjustment]
    ([SessionId]);
GO

-- Creating foreign key on [Restaurant_Id] in table 'StaffAuthenticationKeys'
ALTER TABLE [dbo].[StaffAuthenticationKeys]
ADD CONSTRAINT [FK_StaffAuthenticationKeyRestaurant]
    FOREIGN KEY ([Restaurant_Id])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_StaffAuthenticationKeyRestaurant'
CREATE INDEX [IX_FK_StaffAuthenticationKeyRestaurant]
ON [dbo].[StaffAuthenticationKeys]
    ([Restaurant_Id]);
GO

-- Creating foreign key on [StaffAuthenticationKeyStaff_StaffAuthenticationKey_Id] in table 'StaffAuthenticationKeys'
ALTER TABLE [dbo].[StaffAuthenticationKeys]
ADD CONSTRAINT [FK_StaffAuthenticationKeyStaff]
    FOREIGN KEY ([StaffAuthenticationKeyStaff_StaffAuthenticationKey_Id])
    REFERENCES [dbo].[Staffs]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_StaffAuthenticationKeyStaff'
CREATE INDEX [IX_FK_StaffAuthenticationKeyStaff]
ON [dbo].[StaffAuthenticationKeys]
    ([StaffAuthenticationKeyStaff_StaffAuthenticationKey_Id]);
GO

-- Creating foreign key on [CountryId] in table 'TaxTypes'
ALTER TABLE [dbo].[TaxTypes]
ADD CONSTRAINT [FK_CountryTaxType]
    FOREIGN KEY ([CountryId])
    REFERENCES [dbo].[Countries]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_CountryTaxType'
CREATE INDEX [IX_FK_CountryTaxType]
ON [dbo].[TaxTypes]
    ([CountryId]);
GO

-- Creating foreign key on [TaxTypeId] in table 'MenuItems'
ALTER TABLE [dbo].[MenuItems]
ADD CONSTRAINT [FK_TaxTypeMenuItem]
    FOREIGN KEY ([TaxTypeId])
    REFERENCES [dbo].[TaxTypes]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_TaxTypeMenuItem'
CREATE INDEX [IX_FK_TaxTypeMenuItem]
ON [dbo].[MenuItems]
    ([TaxTypeId]);
GO

-- Creating foreign key on [ModifierGroupId] in table 'Modifiers'
ALTER TABLE [dbo].[Modifiers]
ADD CONSTRAINT [FK_ModifierGroupModifier]
    FOREIGN KEY ([ModifierGroupId])
    REFERENCES [dbo].[ModifierGroups]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_ModifierGroupModifier'
CREATE INDEX [IX_FK_ModifierGroupModifier]
ON [dbo].[Modifiers]
    ([ModifierGroupId]);
GO

-- Creating foreign key on [TaxType_Id] in table 'Modifiers'
ALTER TABLE [dbo].[Modifiers]
ADD CONSTRAINT [FK_ModifierTaxType]
    FOREIGN KEY ([TaxType_Id])
    REFERENCES [dbo].[TaxTypes]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_ModifierTaxType'
CREATE INDEX [IX_FK_ModifierTaxType]
ON [dbo].[Modifiers]
    ([TaxType_Id]);
GO

-- Creating foreign key on [Modifiers_Id] in table 'OrderModifier'
ALTER TABLE [dbo].[OrderModifier]
ADD CONSTRAINT [FK_OrderModifier_Modifier]
    FOREIGN KEY ([Modifiers_Id])
    REFERENCES [dbo].[Modifiers]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating foreign key on [Orders_Id] in table 'OrderModifier'
ALTER TABLE [dbo].[OrderModifier]
ADD CONSTRAINT [FK_OrderModifier_Order]
    FOREIGN KEY ([Orders_Id])
    REFERENCES [dbo].[Orders]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_OrderModifier_Order'
CREATE INDEX [IX_FK_OrderModifier_Order]
ON [dbo].[OrderModifier]
    ([Orders_Id]);
GO

-- Creating foreign key on [DiscountReason_Id] in table 'Orders'
ALTER TABLE [dbo].[Orders]
ADD CONSTRAINT [FK_OrderAdjustmentType]
    FOREIGN KEY ([DiscountReason_Id])
    REFERENCES [dbo].[AdjustmentTypes]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_OrderAdjustmentType'
CREATE INDEX [IX_FK_OrderAdjustmentType]
ON [dbo].[Orders]
    ([DiscountReason_Id]);
GO

-- Creating foreign key on [StaffId] in table 'Orders'
ALTER TABLE [dbo].[Orders]
ADD CONSTRAINT [FK_StaffOrder]
    FOREIGN KEY ([StaffId])
    REFERENCES [dbo].[Staffs]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_StaffOrder'
CREATE INDEX [IX_FK_StaffOrder]
ON [dbo].[Orders]
    ([StaffId]);
GO

-- Creating foreign key on [StaffId] in table 'Adjustment'
ALTER TABLE [dbo].[Adjustment]
ADD CONSTRAINT [FK_StaffAdjustment]
    FOREIGN KEY ([StaffId])
    REFERENCES [dbo].[Staffs]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_StaffAdjustment'
CREATE INDEX [IX_FK_StaffAdjustment]
ON [dbo].[Adjustment]
    ([StaffId]);
GO

-- Creating foreign key on [RestaurantId] in table 'Parties'
ALTER TABLE [dbo].[Parties]
ADD CONSTRAINT [FK_RestaurantParty]
    FOREIGN KEY ([RestaurantId])
    REFERENCES [dbo].[Restaurants]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_RestaurantParty'
CREATE INDEX [IX_FK_RestaurantParty]
ON [dbo].[Parties]
    ([RestaurantId]);
GO

-- Creating foreign key on [PrinterId] in table 'Batches'
ALTER TABLE [dbo].[Batches]
ADD CONSTRAINT [FK_Batches_Printers]
    FOREIGN KEY ([PrinterId])
    REFERENCES [dbo].[Printers]
        ([Id])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_Batches_Printers'
CREATE INDEX [IX_FK_Batches_Printers]
ON [dbo].[Batches]
    ([PrinterId]);
GO

-- Creating foreign key on [Id] in table 'Sessions_SeatedSession'
ALTER TABLE [dbo].[Sessions_SeatedSession]
ADD CONSTRAINT [FK_SeatedSession_inherits_Session]
    FOREIGN KEY ([Id])
    REFERENCES [dbo].[Sessions]
        ([Id])
    ON DELETE CASCADE ON UPDATE NO ACTION;
GO

-- Creating foreign key on [Id] in table 'Sessions_TakeAwaySession'
ALTER TABLE [dbo].[Sessions_TakeAwaySession]
ADD CONSTRAINT [FK_TakeAwaySession_inherits_Session]
    FOREIGN KEY ([Id])
    REFERENCES [dbo].[Sessions]
        ([Id])
    ON DELETE CASCADE ON UPDATE NO ACTION;
GO

-- Creating foreign key on [Id] in table 'Parties_WaitingList'
ALTER TABLE [dbo].[Parties_WaitingList]
ADD CONSTRAINT [FK_WaitingList_inherits_Party]
    FOREIGN KEY ([Id])
    REFERENCES [dbo].[Parties]
        ([Id])
    ON DELETE CASCADE ON UPDATE NO ACTION;
GO

-- Creating foreign key on [Id] in table 'Parties_Reservation'
ALTER TABLE [dbo].[Parties_Reservation]
ADD CONSTRAINT [FK_Reservation_inherits_Party]
    FOREIGN KEY ([Id])
    REFERENCES [dbo].[Parties]
        ([Id])
    ON DELETE CASCADE ON UPDATE NO ACTION;
GO

-- Creating foreign key on [Id] in table 'DateConstraints_DayOfYearConstraint'
ALTER TABLE [dbo].[DateConstraints_DayOfYearConstraint]
ADD CONSTRAINT [FK_DayOfYearConstraint_inherits_DateConstraint]
    FOREIGN KEY ([Id])
    REFERENCES [dbo].[DateConstraints]
        ([Id])
    ON DELETE CASCADE ON UPDATE NO ACTION;
GO

-- Creating foreign key on [Id] in table 'DateConstraints_DayOfWeekConstraint'
ALTER TABLE [dbo].[DateConstraints_DayOfWeekConstraint]
ADD CONSTRAINT [FK_DayOfWeekConstraint_inherits_DateConstraint]
    FOREIGN KEY ([Id])
    REFERENCES [dbo].[DateConstraints]
        ([Id])
    ON DELETE CASCADE ON UPDATE NO ACTION;
GO

-- Creating foreign key on [Id] in table 'DateConstraints_AbsoluteDateConstraint'
ALTER TABLE [dbo].[DateConstraints_AbsoluteDateConstraint]
ADD CONSTRAINT [FK_AbsoluteDateConstraint_inherits_DateConstraint]
    FOREIGN KEY ([Id])
    REFERENCES [dbo].[DateConstraints]
        ([Id])
    ON DELETE CASCADE ON UPDATE NO ACTION;
GO

-- Creating foreign key on [Id] in table 'Adjustment_MewsAdjustment'
ALTER TABLE [dbo].[Adjustment_MewsAdjustment]
ADD CONSTRAINT [FK_MewsAdjustment_inherits_Adjustment]
    FOREIGN KEY ([Id])
    REFERENCES [dbo].[Adjustment]
        ([Id])
    ON DELETE CASCADE ON UPDATE NO ACTION;
GO

-- --------------------------------------------------
-- Script has ended
-- --------------------------------------------------
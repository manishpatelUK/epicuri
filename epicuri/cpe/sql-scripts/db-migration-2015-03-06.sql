BEGIN TRANSACTION
GO
	--Add nullable staff column to adjustment table	
	ALTER TABLE dbo.Adjustment ADD
		StaffId int NULL
	GO
	ALTER TABLE dbo.Adjustment ADD CONSTRAINT
		FK_Adjustment_Staffs FOREIGN KEY
		(
		StaffId
		) REFERENCES dbo.Staffs
		(
		Id
		) ON UPDATE  NO ACTION 
		 ON DELETE  NO ACTION 
	
	GO

	--Add StaffID and OrderTime fields to order table
	ALTER TABLE dbo.Orders ADD
		StaffId int NULL
	GO
	ALTER TABLE dbo.Orders ADD CONSTRAINT
		FK_Orders_Staffs FOREIGN KEY
		(
		StaffId
		) REFERENCES dbo.Staffs
		(
		Id
		) ON UPDATE  NO ACTION 
		 ON DELETE  NO ACTION 
	
	GO
	ALTER TABLE dbo.Orders ADD
		OrderTime datetime NULL
	GO



	--Add SupportsChange to adjustmenttype table
	ALTER TABLE dbo.AdjustmentTypes ADD
		SupportsChange int NOT NULL CONSTRAINT DF_AdjustmentTypes_SupportsChange DEFAULT 0
	GO

	--Set Supports Change for CASH
	UPDATE dbo.AdjustmentTypes SET SupportsChange=1 WHERE Id=1
	GO
COMMIT
GO





--Copy order time as session time
BEGIN TRANSACTION
	UPDATE dbo.Orders SET OrderTime = Sessions.StartTime
	FROM dbo.Orders
	INNER JOIN dbo.Sessions
	ON Orders.SessionId = Sessions.Id
	GO

COMMIT
BEGIN TRANSACTION
GO
	CREATE TABLE dbo.Tmp_Orders
		(
		Id int NOT NULL IDENTITY (1, 1),
		MenuItemId int NOT NULL,
		DinerId int NOT NULL,
		Note nvarchar(MAX) NOT NULL,
		SessionId int NOT NULL,
		Completed datetime NULL,
		CourseId int NULL,
		BatchId int NOT NULL,
		PriceOverride float(53) NULL,
		Deleted bit NOT NULL,
		Locked bit NOT NULL,
		InstantiatedFromId int NOT NULL,
		DiscountReason_Id int NULL,
		StaffId int NULL,
		OrderTime datetime NOT NULL
		)  ON [PRIMARY]
		 TEXTIMAGE_ON [PRIMARY]
	GO
	ALTER TABLE dbo.Tmp_Orders SET (LOCK_ESCALATION = TABLE)
	GO
	SET IDENTITY_INSERT dbo.Tmp_Orders ON
	GO
	IF EXISTS(SELECT * FROM dbo.Orders)
		 EXEC('INSERT INTO dbo.Tmp_Orders (Id, MenuItemId, DinerId, Note, SessionId, Completed, CourseId, BatchId, PriceOverride, Deleted, Locked, InstantiatedFromId, DiscountReason_Id, StaffId, OrderTime)
			SELECT Id, MenuItemId, DinerId, Note, SessionId, Completed, CourseId, BatchId, PriceOverride, Deleted, Locked, InstantiatedFromId, DiscountReason_Id, StaffId, OrderTime FROM dbo.Orders WITH (HOLDLOCK TABLOCKX)')
	GO
	SET IDENTITY_INSERT dbo.Tmp_Orders OFF
	GO
	ALTER TABLE dbo.OrderModifier
		DROP CONSTRAINT FK_OrderModifier_Order
	GO
	DROP TABLE dbo.Orders
	GO
	EXECUTE sp_rename N'dbo.Tmp_Orders', N'Orders', 'OBJECT' 
	GO
	ALTER TABLE dbo.Orders ADD CONSTRAINT
		PK_Orders PRIMARY KEY CLUSTERED 
		(
		Id
		) WITH( STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

	GO
	ALTER TABLE dbo.Orders ADD CONSTRAINT
		FK_BatchOrder FOREIGN KEY
		(
		BatchId
		) REFERENCES dbo.Batches
		(
		Id
		) ON UPDATE  NO ACTION 
		 ON DELETE  NO ACTION 
	
	GO
	ALTER TABLE dbo.Orders ADD CONSTRAINT
		FK_CourseOrder FOREIGN KEY
		(
		CourseId
		) REFERENCES dbo.Courses
		(
		Id
		) ON UPDATE  NO ACTION 
		 ON DELETE  NO ACTION 
	
	GO
	ALTER TABLE dbo.Orders ADD CONSTRAINT
		FK_DinerOrder FOREIGN KEY
		(
		DinerId
		) REFERENCES dbo.Diners
		(
		Id
		) ON UPDATE  NO ACTION 
		 ON DELETE  NO ACTION 
	
	GO
	ALTER TABLE dbo.Orders ADD CONSTRAINT
		FK_MenuItemOrder FOREIGN KEY
		(
		MenuItemId
		) REFERENCES dbo.MenuItems
		(
		Id
		) ON UPDATE  NO ACTION 
		 ON DELETE  NO ACTION 
	
	GO
	ALTER TABLE dbo.Orders ADD CONSTRAINT
		FK_OrderAdjustmentType FOREIGN KEY
		(
		DiscountReason_Id
		) REFERENCES dbo.AdjustmentTypes
		(
		Id
		) ON UPDATE  NO ACTION 
		 ON DELETE  NO ACTION 
	
	GO
	ALTER TABLE dbo.Orders ADD CONSTRAINT
		FK_SessionOrder FOREIGN KEY
		(
		SessionId
		) REFERENCES dbo.Sessions
		(
		Id
		) ON UPDATE  NO ACTION 
		 ON DELETE  NO ACTION 
	
	GO
	ALTER TABLE dbo.Orders ADD CONSTRAINT
		FK_Orders_Staffs FOREIGN KEY
		(
		StaffId
		) REFERENCES dbo.Staffs
		(
		Id
		) ON UPDATE  NO ACTION 
		 ON DELETE  NO ACTION 
	
	GO
COMMIT
BEGIN TRANSACTION
GO
ALTER TABLE dbo.OrderModifier ADD CONSTRAINT
	FK_OrderModifier_Order FOREIGN KEY
	(
	Orders_Id
	) REFERENCES dbo.Orders
	(
	Id
	) ON UPDATE  NO ACTION 
	 ON DELETE  NO ACTION 
	
GO
COMMIT
BEGIN TRANSACTION
ALTER TABLE [dbo].[Sessions] ADD [RemoveFromReports] bit  NOT NULL DEFAULT 0;
ALTER TABLE [dbo].[Orders] ADD [RemoveFromReports] bit  NOT NULL DEFAULT 0;
COMMIT

BEGIN TRANSACTION
GO
CREATE TABLE dbo.VoidReasons
	(
	Id int NOT NULL IDENTITY(1,1),
	StaffId int NOT NULL,
	SessionId int NOT NULL,
	VoidTime datetime NOT NULL,
	Reason nvarchar(MAX) NULL
	)  ON [PRIMARY]
	 TEXTIMAGE_ON [PRIMARY]
GO
ALTER TABLE dbo.VoidReasons ADD CONSTRAINT
	PK_VoidReasons PRIMARY KEY CLUSTERED 
	(
	Id
	) ON [PRIMARY]

GO
ALTER TABLE dbo.VoidReasons ADD CONSTRAINT
	FK_VoidReasons_Staff FOREIGN KEY
	(
	StaffId
	) REFERENCES dbo.Staffs
	(
	Id
	) ON UPDATE  NO ACTION 
	 ON DELETE  NO ACTION 
	 
ALTER TABLE dbo.VoidReasons ADD CONSTRAINT
	FK_VoidReasons_Session FOREIGN KEY
	(
	SessionId
	) REFERENCES dbo.Sessions
	(
	Id
	) ON UPDATE  NO ACTION 
	 ON DELETE  NO ACTION 
COMMIT

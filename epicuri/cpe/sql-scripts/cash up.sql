/*
CR Script
*/

USE [epicuri]
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO


/* Cash Up table CREATE */
CREATE TABLE [dbo].[CashUpDay] (
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[StaffId] [int]  NOT NULL,
	[StartTime] [datetime] NOT NULL,
	[EndTime] [datetime] NOT NULL,
	[Report] [nvarchar](max) NOT NULL,
	[WrapUp] [bit] NOT NULL,
	[RestaurantId] [int] NOT NULL
 CONSTRAINT [PK_CashUpDay] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO

ALTER TABLE [dbo].[CashUpDay] WITH CHECK ADD  CONSTRAINT [FK_CashUpDayStaff] FOREIGN KEY([StaffId])
REFERENCES [dbo].[Staffs] ([Id])
GO

ALTER TABLE [dbo].[CashUpDay] WITH CHECK ADD  CONSTRAINT [FK_CashUpDayRestaurant] FOREIGN KEY([RestaurantId])
REFERENCES [dbo].[Restaurants] ([Id])
GO





/* Add CashUp column to Session CREATE */
ALTER TABLE Sessions ADD CashUpDayId INT NULL

GO

ALTER TABLE [dbo].[Sessions] WITH CHECK ADD  CONSTRAINT [FK_SessionCashUpDay] FOREIGN KEY([CashUpDayId])
REFERENCES [dbo].[CashUpDay] ([Id])
GO




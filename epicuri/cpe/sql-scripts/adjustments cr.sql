/*
CR Script
*/

USE [epicuri]
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO


/* Adjustment Type table CREATE */
CREATE TABLE [dbo].[AdjustmentTypes](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[Name] [nvarchar](max) NOT NULL,
	[Type] [int] NOT NULL,
	[Deleted] [datetime] NULL,
 CONSTRAINT [PK_AdjustmentTypes] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO

/* Adjustment table CREATE */
CREATE TABLE [dbo].[Adjustment](
	[SessionId] [int] NOT NULL,
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[AdjustmentTypeId] [int] NOT NULL,
	[NumericalType] [int] NOT NULL,
	[Value] [float] NOT NULL,
	[Reference] [nvarchar](max) NULL,
	[Created] [datetime] NOT NULL,
	[Deleted] [datetime] NULL,
 CONSTRAINT [PK_Adjustment] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO

ALTER TABLE [dbo].[Adjustment]  WITH CHECK ADD  CONSTRAINT [FK_SessionAdjustment] FOREIGN KEY([SessionId])
REFERENCES [dbo].[Sessions] ([Id])
GO

ALTER TABLE [dbo].[Adjustment]  WITH CHECK ADD  CONSTRAINT [FK_AdjustmentAdjustmentType] FOREIGN KEY([AdjustmentTypeId])
REFERENCES [dbo].[AdjustmentTypes] ([Id])
GO



ALTER TABLE [dbo].[Orders] ADD
[DiscountReason_Id] int  NULL


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
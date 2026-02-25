ALTER TABLE Restaurants ADD MewsIntegration BIT NOT NULL DEFAULT 1
ALTER TABLE Restaurants ADD MewsAccessToken VARCHAR(100) NULL DEFAULT 1

ALTER TABLE Restaurants ADD ReceiptType INT NOT NULL DEFAULT 0

/****** Object:  Table [dbo].[Adjustment_Mews]    Script Date: 10/03/2015 12:41:12 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO

CREATE TABLE [dbo].[Adjustment_Mews](
	[Id] [int] NOT NULL,
	[FirstName] [varchar](max) NULL,
	[LastName] [varchar](max) NULL,
	[RoomNo] [varchar](max) NULL,
	[ChargeId] [varchar](max) NOT NULL,
 CONSTRAINT [PK_Adjustment_MewsAdjustment] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO

ALTER TABLE [dbo].[Adjustment_Mews]  WITH CHECK ADD  CONSTRAINT [FK_MewsAdjustment_inherits_Adjustment] FOREIGN KEY([Id])
REFERENCES [dbo].[Adjustment] ([Id])
GO

ALTER TABLE [dbo].[Adjustment_Mews] CHECK CONSTRAINT [FK_MewsAdjustment_inherits_Adjustment]
GO

SET IDENTITY_INSERT [dbo].[AdjustmentTypes] ON 

GO

INSERT [dbo].[AdjustmentTypes] ([Id], [Name], [Type], [Deleted], [SupportsChange]) VALUES (-1, N'Mews', 0, NULL, 0)
GO
SET IDENTITY_INSERT [dbo].[AdjustmentTypes] OFF
GO

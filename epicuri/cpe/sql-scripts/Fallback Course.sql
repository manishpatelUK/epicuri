USE [epicuri]
GO

SET IDENTITY_INSERT [dbo].[Courses] ON

GO

INSERT INTO [dbo].[Courses]
           ([ID], [Name])
     VALUES
           (-1, 'Fallback Course')
GO

SET IDENTITY_INSERT [dbo].[Courses] OFF

GO
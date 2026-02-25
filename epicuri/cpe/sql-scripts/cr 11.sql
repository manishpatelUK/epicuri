ALTER TABLE Restaurants ADD ISOCurrency VARCHAR(4) NOT NULL DEFAULT 'GBP'
ALTER TABLE Restaurants DROP FK_CurrencyRestaurant
ALTER TABLE Restaurants DROP Column CurrencyId

ALTER TABLE Countries DROP FK_CountryCurrency
ALTER TABLE Countries DROP Column DefaultCurrency_Id

DROP TABLE Currencies


ALTER TABLE Restaurants ADD IANATimezone VARCHAR(MAX) NOT NULL DEFAULT 'Europe/London'


INSERT [dbo].[DefaultSettings] ([Key], [Value], [Measure], [SettingDescription], [SortId]) 
VALUES ('TaxReferenceLabel', 'Our VAT Number',	'-', 'Localised string for tax reference (on receipt)',	99)
GO

INSERT [dbo].[DefaultSettings] ([Key], [Value], [Measure], [SettingDescription], [SortId]) 
VALUES ('TaxLabel', 'VAT',	'-', 'Localised string for tax reference in country (on receipt)',	99)
GO
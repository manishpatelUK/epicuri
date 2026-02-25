
checkPricesAndGetMenuItemIds:{[listOfIds;listOfPrices]
    (checkPrices[listOfIds;listOfPrices];select internalId,externalId,price from prices where externalId in listOfIds)
    };

checkPrices:{[listOfIds;listOfPrices]
    count[listOfIds]~count select from prices where ([] externalId;price) in ([]externalId:listOfIds;price:listOfPrices)
    };

getSessionInfo:{[id]
    select from sessionInfo where restaurantId=id
    };

if[not `prices in tables`.;prices:([] internalId:`int$(); externalId:`int$(); price:`int$(); name:`symbol$())];
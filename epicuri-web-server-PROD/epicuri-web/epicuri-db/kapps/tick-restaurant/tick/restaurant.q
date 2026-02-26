checkins:([] time:`timestamp$(); sym:`symbol$(); reservationId:`int$(); customerId:`int$(); partyId:`int$(); sessionId:`int$(); id:`int$());
checkinTables:([] time:`timestamp$(); sym:`symbol$(); id:`int$(); table:`int$()); // can have multiple tables, multiple rows
parties:([] time:`timestamp$(); sym:`symbol$(); reservationId:`int$(); numberOfPeople:`int$(); name:(); instantiatedFromId:`int$(); id:`int$());
reservations:([] ) //todo?

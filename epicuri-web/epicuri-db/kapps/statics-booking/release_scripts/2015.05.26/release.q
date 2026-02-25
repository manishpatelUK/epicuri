// add Dutch stuff to statics

\cd /home/epicuriuser/kapps/statics-booking
statics:get`:data/statics;

`statics upsert
        (`nl;
         "Terug";
         "Datum van reservatie en aantal gasten?";
         "Volgende";
         "Tijdstip van reservatie?";
         "Volgende";
         "We hebben nog een aantal details nodig...";
         "Reserveren!";
         "Naam";
         "Telefoonnummer";
         "Email";
         "Opmerkingen";
         "Bedankt! Tot zo!";
         "Geldig telefoonnummer is vereist";
         "Naam is vereist";
         "Reserveer een Tafel";
         "Houdt me op de hoogte van nieuws & aanbiedingen";
         "Bevestigd";
         "Gelieve ons te bellen voor grotere groepen" ;
         "Sorry, wij zijn op dat moment niet beschikbaar/open. Gelieve een reservatie te plaatsen op een andere datum/tijdstip.";
         "Kan geen reservatie maken in het verleden!";
         "Niet beschikbaar";
         "Gasten"
        );
save`:data/statics;

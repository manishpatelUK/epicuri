// add email stuff to statics

\cd /home/epicuriuser/kapps/statics-booking

toDinerList:(
    (`en;`diner;`subject;"You're all set for $restaurantName$");
    (`en;`diner;`rep;"Dear $dinerName$");
    (`en;`diner;`misc;"</p>");
    (`en;`diner;`rep;"Your table for $numberOfPeople$ at $restaurantName$ will be ready on $date$ at $time$. The reservation is held under: $dinerName$");
    (`en;`diner;`misc;"</p>");
    (`en;`diner;`misc;"To get there:");
    (`en;`diner;`misc;"</p>");
    (`en;`diner;`address1;"$addressLine1$");
    (`en;`diner;`address2;"$addressLine2$");
    (`en;`diner;`address3;"$addressLine3$");
    (`en;`diner;`address4;"$addressLine4$");
    (`en;`diner;`misc;"</p>");
    (`en;`diner;`rep;"$restaurantNumber$");
    (`en;`diner;`rep;"<a href='$restaurantEmail$'> $restaurantEmail$ </a>");
    (`en;`diner;`misc;"</p>");
    (`en;`diner;`misc;"Should your plans change, please let us know.");
    (`en;`diner;`misc;"</p>");
    (`en;`diner;`rep;"<html><h3>Using the web is so 2005!</h3></p>Why not make and manage your bookings using our <strong>free</strong> Epicuri guest app? Don't forget to check into the $restaurantName$ when you arrive, <strong>you'll get superpowers!</strong></html>");
    (`en;`diner;`misc;"</p>");
    (`en;`diner;`misc;"<html>Download for <a href='https://itunes.apple.com/gb/app/epicuri/id849250056'>iOS</a></html>");
    (`en;`diner;`misc;"<html>Download for <a href='https://play.google.com/store/apps/details?id=uk.co.epicuri.android&hl=en_GB'>Android</a></html>");
    (`nl;`diner;`subject;"Bevestiging van de reservering: $restaurantName$");
    (`nl;`diner;`rep;"Beste $dinerName$");
    (`nl;`diner;`misc;"</p>");
    (`nl;`diner;`rep;"Jouw tafel voor $numberOfPeople$ bij $restaurantName$ zal klaarstaan op $date$ $time$. De reservatie werd geboekt op naam van: $dinerName$");
    (`nl;`diner;`misc;"</p>");
    (`nl;`diner;`misc;"Om er te geraken:");
    (`nl;`diner;`misc;"</p>");
    (`nl;`diner;`address1;"$addressLine1$");
    (`nl;`diner;`address2;"$addressLine2$");
    (`nl;`diner;`address3;"$addressLine3$");
    (`nl;`diner;`address4;"$addressLine4$");
    (`nl;`diner;`misc;"</p>");
    (`nl;`diner;`rep;"$restaurantNumber$");
    (`nl;`diner;`rep;"<a href='$restaurantEmail$'> $restaurantEmail$ </a>");
    (`nl;`diner;`misc;"</p>");
    (`nl;`diner;`misc;"Gelieve ons op de hoogte te brengen wanneer uw plannen veranderen.");
    (`nl;`diner;`misc;"</p>");
    (`nl;`diner;`rep;"<html><h3>Het internet gebruiken is zo 2005!</h3></p>Waarom plaats en beheer je je reservaties niet door gebruik te maken van onze <strong>gratis</strong> Epicuri guest app? Vergeet niet om in te checken bij <name> wanneer je aankomt, <strong>je zal superkrachten ontvangen!</strong></html>");
    (`nl;`diner;`misc;"</p>");
    (`nl;`diner;`misc;"<html>Klik hier om te downloaden voor <a href='https://itunes.apple.com/gb/app/epicuri/id849250056'>iOS</a></html>");
    (`nl;`diner;`misc;"<html>Klik hier om te downloaden voor <a href='https://play.google.com/store/apps/details?id=uk.co.epicuri.android&hl=en_GB'>Android</a></html>")
    );

toRestaurantList:(
    (`en;`restaurant;`subject;"New reservation: $date$ $time$");
    (`en;`restaurant;`rep;"Hi $restaurantName$");
    (`en;`restaurant;`misc;"</p>");
    (`en;`restaurant;`misc;"You have a new table reservation!");
    (`en;`restaurant;`misc;"</p>");
    (`en;`restaurant;`rep;"Reservation Name: $dinerName$");
    (`en;`restaurant;`rep;"$date$ at $time$. Party of $numberOfPeople$");
    (`en;`restaurant;`notes;"<i>Notes: $notes$</i>");
    (`en;`restaurant;`rep;"Email: $dinerEmail$");
    (`en;`restaurant;`rep;"Phone: $dinerNumber$");
    (`en;`restaurant;`misc;"</p>");
    (`en;`restaurant;`misc;"The reservation is <strong>confirmed</strong> and in your Epicuri calendar.");
    (`en;`restaurant;`misc;"</p>");
    (`en;`restaurant;`misc;"Use your Epicuri app to manage bookings or to get a full up-to-the-second view of the day's reservations.");
    (`en;`restaurant;`misc;"</p>");
    (`en;`restaurant;`misc;"Best Regards");
    (`en;`restaurant;`misc;"</p>");
    (`en;`restaurant;`misc;"Epicuri Booking Manager");
    (`nl;`restaurant;`subject;"Nieuwe reservering $date$ $time$");
    (`nl;`restaurant;`rep;"Beste $restaurantName$");
    (`nl;`restaurant;`misc;"</p>");
    (`nl;`restaurant;`misc;"Je hebt een nieuwe tafelreservatie!");
    (`nl;`restaurant;`misc;"</p>");
    (`nl;`restaurant;`rep;"Naam Reservatie: $dinerName$");
    (`nl;`restaurant;`rep;"$date$ $time$, groep van $numberOfPeople$");
    (`nl;`restaurant;`notes;"<i>Aantekeningen: $notes$</i>");
    (`nl;`restaurant;`rep;"Email: $dinerEmail$");
    (`nl;`restaurant;`rep;"Telefoon: $dinerNumber$");
    (`nl;`restaurant;`misc;"</p>");
    (`nl;`restaurant;`misc;"De reservatie is <strong>bevestigd</strong> en geplaatst in je Epicuri kalender.");
    (`nl;`restaurant;`misc;"</p>");
    (`nl;`restaurant;`misc;"Gebruik je Epicuri app om reservaties te beheren of om al je dagelijkse reservaties onmiddellijk na te gaan.");
    (`nl;`restaurant;`misc;"</p>");
    (`nl;`restaurant;`misc;"Vriendelijke groeten");
    (`nl;`restaurant;`misc;"</p>");
    (`nl;`restaurant;`misc;"Epicuri Booking Manager")
    );

emailStatics:flip `language`recipient`identifier`text!flip[toDinerList,toRestaurantList];

save`:data/emailStatics;

exit 0

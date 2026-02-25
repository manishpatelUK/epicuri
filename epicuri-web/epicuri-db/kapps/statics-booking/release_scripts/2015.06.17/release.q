// update email statics for welcome message

\cd /home/epicuriuser/kapps/statics-booking

a:(
    (`en;`newdiner;`subject;"Welcome to Epicuri!");
    (`en;`newdiner;`misc;"<h1>You. Are. Awesome.</h1>");
    (`en;`newdiner;`misc;"</p>");
    (`en;`newdiner;`misc;"Thanks so much for signing up to Epicuri! Dont forget to check-in using your shiny new app at your local Epicuri restaurant and please use these new-found superpowers for good rather than evil. With great power comes great dining experiences.");
    (`en;`newdiner;`misc;"</p>");
    (`en;`newdiner;`misc;"...and if your favourite restaurant doesn't use Epicuri <strong>(what?!) </strong> <a href='http://epicuri.co.uk/associate-program'>tell us<a/> and then tell them it ain't the 1990's any more! Get with the program!");
    (`en;`newdiner;`misc;"</p>");
    (`en;`newdiner;`misc;"For now, you're good to go... Enjoy filling your belly full of yummy food!");
    (`en;`newdiner;`misc;"</p>");
    (`en;`newdiner;`misc;"From your friends at Epicuri");
    (`en;`newdiner;`misc;"</p>");
    (`en;`newdiner;`misc;"</p>");
    (`en;`newdiner;`misc;"<em>PS Wanna be friends with benefits?? Stick your face into our social media and every so often we pick out the funniest/loudest out of you food crazy bunch and send you out for a slap up meal!</em>");
    (`en;`newdiner;`misc;"</p>");
    (`en;`newdiner;`misc;"<a href='www.facebook.com/epicuriUK'>facebook.com/EpicuriUK</a>");
    (`en;`newdiner;`misc;"<a href='www.twitter.com/epicuriUK'>twitter.com/EpicuriUK</a>");
    (`en;`newdiner;`misc;"</p>");
    (`en;`newdiner;`misc;"By the way, we won't send you junk - but we may well keep you in the loop on major developments. If you prefer not to hear from us on those awesome pieces of news, please let us know on unsub@epicuri.co.uk."));

emailStatics:get[`:data/emailStatics],flip `language`recipient`identifier`text!flip[a];

save`:data/emailStatics;

exit 0
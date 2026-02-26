3 Internet applications
API:		MVC4 iOS and Android client applications
	
CPE:		MVC4 application for waiter applocaton

OnBoarding:	MVC4 application for onboarding / management


sub projects
Core:		contains entity model and connection

authentication:
try/catch authenticate at every function. user roles can be defined as a parameter, eg "Manager" etc.

adding new fields
add item to entity framework, edit model in API/CPE to have new field and add mapping in the constructor...

eg, adding FoodType to customer:
1) create propery in EF,
2) generate db from model
3) edit API/Model/Customer to have public String FoodType
4) add FoodType = customer.FoodType mapping

generating sample data

run unit tests (twice)
defaults
sample data
fortune house

uploading:
http://snip.so/VbED 
- upload obj/Debug/Packages/package.zip to server, 
- open iis manager, right click thinktouchsee.com
- click deploy > import application
- follow wizzard.
	- DO NOT recreate DB
	- Select Do not overwrite existing files
- copy web.config into the appropriate directory


static files:
static files are uploaded to OnBoarding/static. Epicuri user on the IIS server needs write permissions ot this folder.

This folder is then mapped to epicuri.thinktouchsee.com/static

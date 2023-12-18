# AutoFeeder Application

**AutoFeeder Application Specifications**

-	Developed in Kotlin using Android Studio.
-	Android 14 (SDK 34) Application.
-	App Size 17.95MBs.
-	Permission Requirements:
  	-	*INTERNET*
    -	*ACCESS_CORSE_LOCATION*
    -	*ACCESS_FINE_LOCATION*
    -	*BLUETOOTH*
    -	*BLUETOOTH_ADMIN*
    -	*BLUETOOTH_SCAN*
    -	*BLUETOOTH_CONNECT*

**AutoFeeder Setup Details**
- Scans for nearby BLE Devices.
-	Displays AutoFeeder devices with the MAC address in a list. 
-	Once AutoFeeder is selected, the user can input Wi-Fi SSID and password. 
-	AutoFeeder App prepopulates the RTDB with the AutoFeeder MAC Address. 
-	AutoFeeder App combines the Wi-Fi SSD, MAC Address, RTDB Secret Key, and Wi-Fi Password with unique characters not found in these parameters. 
-	AutoFeeder breaks this combination into chunks of 20 characters and sends the chunks over BLE.
-	The AutoFeeder then reconstructs the chunks and breaks the combination down into Wi-Fi credentials, MAC Address, and RTDB Secret key.
-	AutoFeeder connects to Wi-Fi and RTDB.

**AutoFeeder Login Details**
-	AutoFeeder requires users to create an account.
-	Using the AutoFeeder App, users can register for AutoFeeder with their email and password.
-	After the user has registered, users can log in to AutoFeeder.
-	Login is required for RTDB access.

**AutoFeeder Monitoring Details**
- Home Page:
  -	Displays percent of food in the AutoFeeder hopper.
  -	Displays percent of food in the AutoFeeder bowl.
  - Displays AutoFeeder status.
- Pet Info Page:
  -	Displays pet's current feeding profile. 

**AutoFeeder Control Details**
- Allows the user to manually feed their pet remotely.
-	“Feed Now” button signals AutoFeeder to feed by updating RTDB “freeFeed” to true.
-	After AutoFeeder has fed the pet, it updates RTDB “freeFeed” back to false. 

**AutoFeeder Feeding Schedule Setup Details**
-	“Pet Setup” allows user to configure a feeding profile for their pet.
-	Can select Free Feed or Schedule Feed.

-	Free Feed:
	- Sets pet’s name.
	- AutoFeeder feeds the pet once the bowl is empty.
- Schedule Feed:
  - Sets pet’s name.
  - Sets the time zone of the pet to ensure feeding schedule accuracy across different time zones.
  - Set the number of meals per day (1 to 4).
  -	Set amount of food per meal (¼, ⅓, ½, ⅔, ¾, and 1 cup).
  -	Set the time of meals (12-hour clock). 
-	Updates RTDB with feeding profile after clicking the “Done” button.
-	AutoFeeder then reads from RTDB to feed the pet as set up by the user. 

**AutoFeeder Application Notification Details**
-	Firebase Admins can send manual notifications to AutoFeeder users.
-	Allows easy communication methods between users and the AutoFeeder Team.
-	Can be used to notify users if RTDB is down for maintenance.
-	Can be implemented as Automatic Notifications with a paid version for Firebase. 


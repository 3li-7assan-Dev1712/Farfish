<h1>Welcome to Farfish!</h1>
Farfish is a social media app. With Farfish you can easily communicate
with your friends and family, sending photos, videos, audio and much more!
just download the app and sign in and enjoy with it.</br></br>
<h2>Technologies the app demonstrates:</h2></br>
The app uses the most of the jetpack libraries like: WorkManager, Navigation component
and I will use the rest of the libraries by the time.</br>
<h2>About the app</h2> </br>
As mentioned before it's a social media app, users use the app to communicate and chat
with one another in real time with high security functionality.</br>
the user have the option of updating and changing their information in the
user profile destinations, other users can also see their profile. </br>
<h2>Real time chat</h2>
users enjoy real time chat felling with all the functionality like seeing the user is writing
and read the brand new message all in real time.
users can easily know when the other user have read the message or not through the doubled blue tick
that indicate the target user has read the message, just like whatsapp and telegram.</br>
With the help of the magic of the WorkManager the app checks every three hours for the new messages and statues and send a notification if so,
in addition the app also checks for the old messages that are older than 3 months, deleting them to free up the realtime database.
in addition the app also checks for the old messages that are older than 3 months, deleting them to free up the realtime database.</br>
the reason that the app only save user's messages for 3 months is that the server can afford up to 20M messages, which is limited:)</br>
<h2>Telegram Emojis</h2>
the app uses a special library to provide telegram's emojis which really gives users a great user-experience,
and it is also a background compatible unit (API 4.1). </br>
The app support both English and Arabic as primary languages, and the
language will be selected according to user's phone language.
and it is also a background compatible unit (API 4.1). </br>
<h2>Filter Users!<h2>
This is one of the most enjoyable and complex part of the app functionality, it's simply let the user filter whom will be visible </br>
for him in the users destination, do to so the app go and takes a realtime permission asking for access the user's phone contacts
after the user grant this permission, the app reads those contacts save it in a SharedPreference, compare those phone numbers with
the those in the server, then display only the users with the same phone number. </br>
In addition, the user also have an option to disable the filter to use every public user, which will in tern get all
public users and replace them with common user list in a smooth and friendly animation, and that happens with
the help of the amazing combination of ListAdapter & DiffUtill class.
finally another cool feature this app has, is the robust interactivity, in all its destinations
using simple SnackBars to provide a reasoning feedback along with the solution, users notice that when they use the app.
</br></br>
..developed by Ali Hassan Ibrahim Al-Zubair..
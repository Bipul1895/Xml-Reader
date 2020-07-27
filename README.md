# Xml-Reader
Know about top 10 and 25 apps and songs on iTunes. I will keep you updated!
The application reads xml data from Apple's rss feed and parses the data to display it in a List View. 
Concepts Used : 1) Async Task : For reading data from the provided URL and stores it in the form of a string. Async task to prevent blocking of UI thread.
                2) HttpUrlConnection : Used built-in HTTP class to download data from internet.
                3) Xml Pull Parser : Used to parse xml data by identifying Start tag, End tag and text, etc 
                4) List View : Ultimately, the parsed data is displayed in a ListView. Undestood how listview requests for views from android if it needs. It is capable of reusing already inflated view to save both memory and time. Created custom adapter to achieve this.
                5) Created menu and menu groups to respond to select b/w free apps, paid apps, songs or whether user wants to view top 10 or top 25 list. 

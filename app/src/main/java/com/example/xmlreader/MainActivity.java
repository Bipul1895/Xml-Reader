package com.example.xmlreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ListView listApps;
    private static final String TAG = "MainActivity";

    //Notice in the below URL, after limit we have "%d" to take care of choosing b/w top 10 and top 25 apps.
    //We use String.format(str, int val) to use this functionality
    private String feedUrl="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private String feedcachedUrl="INVALIDATED";//to prevent download when user spams the same menu button
    private int feedLimit=10;

    //To restore data during orientation change
    public static final String STATE_URL="feedUrl";
    public static final String STATE_LIMIT="feedLimit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listApps=findViewById(R.id.xmlListView);

        //This is to restore the state of the application during orientation change
        //We restore state here because onRestoreInstanceState is called after onCreate, and we perform download in onCreate
        //In case you are wondering why we are not restoring value of feedCachedURL then know that
        //currently we are unable to save contents of our final result, when we rotate device, download must happen again
        //For that purpose feedCachedUrl should be invalidated, which is the default value
        if(savedInstanceState != null){
            feedUrl=savedInstanceState.getString(STATE_URL);
            feedLimit=savedInstanceState.getInt(STATE_LIMIT);
        }

        downloadURL(String.format(feedUrl, feedLimit));

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URL, feedUrl);
        outState.putInt(STATE_LIMIT, feedLimit);
        super.onSaveInstanceState(outState);
    }

    //For menu
    //Inflates menu resource file
    //Notice that in case of custom array adapter, inflation required context
    //Here we do not require context as our class (Main Activity) extends AppCompatActivity
    //Activity and AppCompatActivity are themselves the context of application
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu);

        //taking care of display of check mark on radio button "top 10" and "top 25" when the device is rotated
        //We will retrieve the value limit from the Bundle
        //Once we retrieve the value, the following code executes to check mark the correct limit value
        if(feedLimit == 10){
            menu.findItem(R.id.mnu10).setChecked(true);
        }
        else{
            menu.findItem(R.id.mnu25).setChecked(true);
        }

        return true;
    }

    //For menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();

        switch (id){
            case R.id.mnuFree:
                feedUrl="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;
            case R.id.mnuPaid:
                feedUrl="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;
            case R.id.mnuSongs:
                feedUrl="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.mnu10:
                if(!item.isChecked()){
                    item.setChecked(true);
                    feedLimit=10;
                    Log.d(TAG, "onOptionsItemSelected: setting feed limit to : " + feedLimit);
                } else {
                    Log.d(TAG, "onOptionsItemSelected: feed limit unchanged");
                }
                break;
            case R.id.mnu25:
                if(!item.isChecked()){
                    item.setChecked(true);//Notice that item will not show checked state automatically, we have to explicitly do that
                    feedLimit=25;
                    Log.d(TAG, "onOptionsItemSelected: setting feed limit to : " + feedLimit);
                } else {
                    Log.d(TAG, "onOptionsItemSelected: feed limit unchanged");
                }
                break;
            case R.id.mnuRefresh:
                feedcachedUrl="INVALIDATED";
                break;
            default://required when we nest menus
                return super.onOptionsItemSelected(item);
        }

        downloadURL(String.format(feedUrl, feedLimit));
        return true;
    }


    //Calls doInBackground of Async task
    //Creates an instance of Download Data class (extends Async task) and calls
    //doInBackground using "execute" method
    private void downloadURL(String feedUrl) {
        if(!feedcachedUrl.equalsIgnoreCase(feedUrl)) {
            Log.d(TAG, "downloadURL: starting AsyncTask");
            DownloadData downloadData = new DownloadData();
            downloadData.execute(feedUrl);//This creates the background thread, The parameter passed to this execute method will got to the doInBackground() method in DownloadData class
            feedcachedUrl=feedUrl;//Remember the last URL
            Log.d(TAG, "downloadURL: done");
        }
        else{
            Log.d(TAG, "downloadURL: URL not changed");
        }
    }

    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";
        //1st arg : Input async task takes, here URL of application feed
        //2nd arg : For displaying progress to the user
        //3rd arg : Return type : The result expected out from the doInBackground method, here a string of xml feed


        //Responsible for calling method to parse the data from ParseApplications class
        //Also creates an array adapter and displays data in ListView
        //The result of doInBackground method is received by this method as parameter string s
        //Calls parse() method of ParseApplications calls and the result if stored in
        //an arrayList named getApplications which is a private member of ParseApplications class
        //Runs on main thread
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parameter is " + s);
            if(s==null) return;

            //Let's do parsing
            ParseApplications parseApplications=new ParseApplications();
            parseApplications.parse(s);

            FeedAdapter feedAdapter=new FeedAdapter(MainActivity.this, R.layout.list_record, parseApplications.getApplications());
            listApps.setAdapter(feedAdapter);

        }


        //Runs on background thread
        //Calls method : downloadXML(String s)
        //This method receives URL as string parameter
        //Array of strings is used in case there are multiple URLs to download from
        @Override
        protected String doInBackground(String... strings) {
            String rssFeed=downloadXML(strings[0]);//will return null in case sth wrong happens
            if(rssFeed == null){
                Log.e(TAG, "doInBackground: Error Downloading");
            }
            return rssFeed;
        }

        //Since it is called by doInBackGround, this will also be on background thread
        private String downloadXML(String urlPath) {
            //We will append a lot to the result so we are using String Builder
            //We know that String in Java is immutable
            //xmlResult will store our XML in the form of a string
            StringBuilder xmlResult = new StringBuilder();

            //When dealing with data from external source, lots of things can wo wrong
            //Device may not be connected to internet
            //Connection may stop while data is downloading
            //URL is not valid, so there won't be any connection in the first place
            try {
                URL url = new URL(urlPath);
                HttpURLConnection connection=(HttpURLConnection) url.openConnection();

                int response=connection.getResponseCode();
                Log.d(TAG, "downloadXML: response code " + response);

                InputStream inputStream=connection.getInputStream();
                InputStreamReader inputStreamReader=new InputStreamReader(inputStream);//Input stream reader requires an object of input stream

                //Rather than reading one character at time, buffered reader reads a chunk of data and stores it in a buffer in RAM
                BufferedReader reader=new BufferedReader(inputStreamReader);//Buffered Reader requires an object of input stream reader

                //Could write the above 3 lines in 1 line
                //BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                //Now reading data
                int charsRead;//Number of characters read in one go
                char[] inputBuffer = new char[500];

                while (true){
                    charsRead=reader.read(inputBuffer);
                    //read method reads the chars in the char array : inputBuffer and returns the number of characters read to int : charsRead
                    if(charsRead < 0) break;
                    if(charsRead > 0){
                        xmlResult.append(String.valueOf(inputBuffer,0,charsRead));
                    }
                }
                reader.close();//It closes buff Reader, input stream reader and input stream as well
                return xmlResult.toString();//We put return statement here so that if exception is caught we can return null;

            } catch (MalformedURLException e) {
                Log.e(TAG, "downloadXML: Invalid URL" + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (SecurityException e) {
                Log.e(TAG, "downloadXML: Security Exception, Needs permission? " + e.getMessage());
            }

            return null;
        }

        /* Used to setup progress bar
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
         */

    }

}

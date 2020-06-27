package com.example.xmlreader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listApps=findViewById(R.id.xmlListView);

        downloadURL("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml");

    }

    //Calls doInBackground of Async task
    //Creates an instance of Download Data class (extends Async task) and calls
    //doInBackground using "execute" method
    private void downloadURL(String feedUrl) {
        Log.d(TAG, "downloadURL: starting AsyncTask");
        DownloadData downloadData = new DownloadData();
        downloadData.execute(feedUrl);//This creates the background thread, The parameter passed to this execute method will got to the doInBackground() method in DownloadData class
        Log.d(TAG, "downloadURL: done");
    }

    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";
        //1st arg : Input async task takes, here URL of application feed
        //2nd arg : For displaying progress to the user
        //3rd arg : Return type : The result expected out from the doInBackground method, here a string of xml feed


        //The result of doInBackground method is received by this method as parameter string s
        //Calls parse() method of ParseApplications calls and the result if stored in
        //an arrayList named getApplications which is a private member of ParseApplications class
        //Runs on main thread
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parameter is " + s);
            if(s==null) return;

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

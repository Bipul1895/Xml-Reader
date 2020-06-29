package com.example.xmlreader;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;//          Debug:
//        for(FeedEntry app:applications){
//            Log.d(TAG, "parse: ***************");
//            Log.d(TAG, "parse: " + app.toString());
//        }

//This class performs the work of parsing the string
public class ParseApplications {
    private static final String TAG = "ParseApplications";

    private ArrayList<FeedEntry> applications;

    //constructor
    public ParseApplications() {
        this.applications = new ArrayList<>();
    }

    public ArrayList<FeedEntry> getApplications() {
        return applications;
    }

    public boolean parse(String xmlData){
        boolean status=true;//tells if operation was successful
        FeedEntry currentRecord=null;
        boolean inEntry=false;//we need to extract "name" field from the entry but "name" field exits outside the entry as well, This variable is used to distinguish between the two
        String textValue="";//Stores the text, i.e. , text b/w the tags of xml

        try {
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp=factory.newPullParser();//xml pull parser need a factory object (XmlPullParserFactory) to get initialized
            xpp.setInput(new StringReader(xmlData));//xml pull parser expects a "reader" and not a string, so we provide our string to it in the form of StringReader
            int eventType=xpp.getEventType();//Event type denotes the "type" of xml tag that was encountered

            while (eventType != XmlPullParser.END_DOCUMENT){
                String tagName=xpp.getName();//Name of the xml tag encountered

                switch (eventType){
                    case XmlPullParser.START_TAG://One of the types of tag
                        if(tagName.equalsIgnoreCase("entry")){//we are writing the if statement like this and not the other way round because xml pull parser's getName() method can return NULL so we might get null pointer exception
                            inEntry=true;
                            currentRecord=new FeedEntry();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        textValue=xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if(inEntry){
                            if("name".equalsIgnoreCase(tagName)){
                                currentRecord.setName(textValue);
                            }
                            else if("artist".equalsIgnoreCase(tagName)){
                                currentRecord.setArtist(textValue);
                            }
                            else if("releaseDate".equalsIgnoreCase(tagName)){
                                currentRecord.setReleaseDate(textValue);
                            }
                            else if("summary".equalsIgnoreCase(tagName)){
                                currentRecord.setSummary(textValue);
                            }
                            else if("image".equalsIgnoreCase(tagName)){
                                currentRecord.setImageURL(textValue);
                            }
                            else if("entry".equalsIgnoreCase(tagName)){
                                applications.add(currentRecord);
                                inEntry=false;
                            }
                        }
                        break;
                }

                eventType=xpp.next();

            }

        } catch (XmlPullParserException e) {
            status=false;//problem parsing xml file
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

          Debug:
        for(FeedEntry app:applications){
            Log.d(TAG, "parse: ***************");
            Log.d(TAG, "parse: " + app.toString());
        }

        return status;

    }

}

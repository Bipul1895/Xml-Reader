package com.example.xmlreader;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class FeedAdapter extends ArrayAdapter {

    private static final String TAG = "FeedAdapter";

    //we need to get layout inflater and layout resource from the constructor
    //final objects should either be initialized when declared or inside the constructor
    private final LayoutInflater layoutInflater;
    private final int layoutResource;
    private List<FeedEntry> applications;

    public FeedAdapter(@NonNull Context context, int resource, List<FeedEntry> applications) {
        //context : for getting layout inflater
        //resource : for getting id of the resource file, here list_record.xml
        super(context, resource);
        this.layoutInflater=LayoutInflater.from(context);
        layoutResource=resource;
        this.applications=applications;
    }

    //We must override getCount() and getView() method
    //getCount() method tells ListView about the total number of items in the listView so it can position the scroll bar
    //getView() methods is called when listView asks the adapter to provide more View objects to display them
    //Note that we do not call these methods, they are called by the listView widget when required


    //For scroll bar
    @Override
    public int getCount() {
        return applications.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view=layoutInflater.inflate(layoutResource, parent, false);

        TextView tvName=view.findViewById(R.id.tvName);
        TextView tvArtist=view.findViewById(R.id.tvArtist);
        TextView tvSummary=view.findViewById(R.id.tvSummary);

        FeedEntry currentApp=applications.get(position);

        tvName.setText(currentApp.getName());
        tvArtist.setText(currentApp.getArtist());
        tvSummary.setText(currentApp.getSummary());

        Log.d(TAG, "getView: " + currentApp.getName());

        return view;

    }
}

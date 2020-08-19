package com.example.dailyfinance;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dailyfinance.mysqlite.DatabaseHandler;

import java.util.ArrayList;

public class List extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check which language to display
        DatabaseHandler db = new DatabaseHandler(this);
        String language = db.checkLanguage();
        Log.d("list", language);
        if(language.equals("English")) setContentView(R.layout.list);
        else setContentView(R.layout.list_lt);

        Profile profile = getProfile();

        final ArrayList<String> purchaseTitles = retrieveTitles(profile.getName());
        ArrayList<String> purchaseDates = retrieveDates(profile.getName());

        // add dates to titles
        ArrayList<String> listItems = new ArrayList<String>();
        for(int i = 0; i < purchaseTitles.size(); i++) {
            if(!purchaseTitles.get(i).contains(purchaseDates.get(i))) {
                // checking language
                if(language.equals("English")) listItems.add(purchaseTitles.get(i) + "\nDate: " + purchaseDates.get(i));
                else listItems.add(purchaseTitles.get(i) + "\nData: " + purchaseDates.get(i));
            } else {
                // remove seconds from the title
                listItems.add(purchaseTitles.get(i).substring(0, purchaseTitles.get(i).length() - 3));
            }
        }

        // simple list made with help from:
        // https://github.coventry.ac.uk/300CEM-1920OCTJAN/Teachingmaterials/tree/master/Week_05_AdapterViews_and_Fragments

        ListView listView = (ListView) findViewById(R.id.list);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                // Initialize a TextView for ListView each Item
                TextView tv = (TextView) view.findViewById(android.R.id.text1);

                // Set the text color of TextView (ListView Item)
                tv.setTextColor(Color.BLACK);

                // Generate ListView Item using TextView
                return view;
            }
        };
        listView.setAdapter(arrayAdapter);
        final Intent selectedPurchase = new Intent(this, SelectedPurchase.class);
        listView.setOnItemClickListener(

                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // send selected purchase title to the next activity
                        Bundle b = new Bundle();
                        b.putString("purchaseTitle", purchaseTitles.get(position));
                        selectedPurchase.putExtras(b);
                        startActivity(selectedPurchase);
                    }
                }
        );
    }

    public Profile getProfile(){
        // returns the selected profile
        DatabaseHandler db = new DatabaseHandler(this);
        int profileId = db.retrieveSelectedProfile();
        Profile profile = db.retrieveProfile(profileId);
        return profile;
    }

    public ArrayList<String> retrieveTitles(String name){
        // method that retrieves titles from 50 latest purchases
        DatabaseHandler db = new DatabaseHandler(this);
        return db.retrieve50Titles(name);
    }

    public ArrayList<String> retrieveDates(String name){
        // method that retrieves dates from 50 latest purchases
        DatabaseHandler db = new DatabaseHandler(this);
        return db.retrieve50Dates(name);
    }
}

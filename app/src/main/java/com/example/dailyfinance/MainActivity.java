package com.example.dailyfinance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.example.dailyfinance.mysqlite.DatabaseHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    String[] languages = {"English", "Lietuvių"};
    private Spinner spinnerLanguage;
    public static String staticLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check which language to display
        DatabaseHandler db = new DatabaseHandler(this);
        String dbLanguage = db.checkLanguage();
        Log.d("mainLanguage", dbLanguage);
        if(dbLanguage.equals("English")){
            setContentView(R.layout.activity_main);
        } else {
            setContentView(R.layout.activity_main_lt);
        }

        // check if there is a selected profile, if so - send user to AddPurchase activity
        checkIfSelectedProfile();

        // getting the instance of spinners and applying OnItemsSelectedListener to them
        spinnerLanguage = (Spinner) findViewById(R.id.language);

        // creating the ArrayAdapter instance having the languages list
        ArrayAdapter aaLanguages = new ArrayAdapter(this, android.R.layout.simple_spinner_item, languages);
        aaLanguages.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // setting the array adapter data on the spinner
        spinnerLanguage.setAdapter(aaLanguages);
        spinnerLanguage.setOnItemSelectedListener(this);

        if(dbLanguage.equals("English")){
            spinnerLanguage.setSelection(0);
        } else {
            spinnerLanguage.setSelection(1);
        }

        // add button for each profile
        profilesToButtons();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        // this method sets the selected language in the database

        DatabaseHandler db = new DatabaseHandler(this);
        staticLanguage = languages[position];
        db.setLanguage(staticLanguage);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

        // do nothing
    }

    @Override
    public void onBackPressed() {

        // this method does not let to go back to previous activity with back button

        moveTaskToBack(true);
    }

    public void save(View view){
        // reloads
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void checkIfSelectedProfile(){

        // his method retrieves a profile from database that has selected set to "yes", an automatic log-in in a way
        // if no profile is selected, does nothing

        DatabaseHandler db = new DatabaseHandler(this);
        int profileId = db.retrieveSelectedProfile();
        if(profileId == -1){
            // if no profile is selected - do nothing
        } else {
            // if there is a selected profile - add a date and send user to AddPurchase activity
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            Profile profile = db.retrieveProfile(profileId);
            db.addCurrentDate(profile.getName(), currentDate);

            Intent intent = new Intent (this, AddPurchase.class);
            startActivity(intent);
        }
    }

    public void goToCreateProfile (View view){

        // this method sends a user to "Create a profile" page

        Intent intent = new Intent (this, CreateProfile.class);
        startActivity(intent);
    }

    public void profilesToButtons() {

        // this method retrieves profiles from database and calls addProfileBbutton for each of of them

        DatabaseHandler db = new DatabaseHandler(this);
        Profile profile;

        // getting the number of profiles from table "profiles"
        ArrayList<Integer> profilesIds = db.getProfilesIds();

        // iterating through profiles and adding a button to the layout for each of them
        for(int i = 0; i < profilesIds.size(); i++){
            profile = db.retrieveProfile(profilesIds.get(i));
            addProfileButton(profilesIds.get(i), profile);
        }
    }

    public void addProfileButton(int id, final Profile profile) {

        // this method is to add a button for every registered profile

        //the layout on which you are working
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);

        // create new button
        Button btnTag = new Button(this);
        // set layout parameters to new button
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;

        btnTag.setLayoutParams(params);
        // add other parameters to button
        String text;
        // check language
        DatabaseHandler db = new DatabaseHandler(this);
        String language = db.checkLanguage();
        if(language.equals("English")) text = profile.getName() + " | Daily limit: " + profile.getDailyLimit() + " " + profile.getCurrency();
        else text = profile.getName() + " | Dienos limitas: " + profile.getDailyLimit() + " " + profile.getCurrency();

        btnTag.setText(text);
        btnTag.setId(id);
        btnTag.setBackgroundColor(Color.parseColor("#018577"));

        // add button to the layout
        layout.addView(btnTag);

        // add an onClick listener to this button
        //final DatabaseHandler db = new DatabaseHandler(this);

        btnTag.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addSel(profile);
                finish();
                startActivity(getIntent());
            }
        });
    }

    public void addSel(Profile profile){
        // removes selected from all profiles and adds only to the selected one
        DatabaseHandler db = new DatabaseHandler(this);
        db.updateAllSelected();
        db.addSelected(profile.getName());
    }
}

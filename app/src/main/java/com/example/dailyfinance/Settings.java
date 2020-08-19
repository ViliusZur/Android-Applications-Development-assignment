package com.example.dailyfinance;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dailyfinance.mysqlite.DatabaseHandler;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check which language to display
        DatabaseHandler db = new DatabaseHandler(this);
        String language = db.checkLanguage();
        Log.d("Settings", language);
        if(language.equals("English")) setContentView(R.layout.settings);
        else setContentView(R.layout.settings_lt);

    }

    public void changeProfile(View view) {

        // this method logs out from current profile

        DatabaseHandler db = new DatabaseHandler(this);

        if(db.retrieveSelectedProfile() == -1){
            // if no profile is selected - do nothing
        } else {
            // if there is a selected profile - make it not selected
            db.updateAllSelected();
            // send user to profile selection page
            Intent intent = new Intent (this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void deleteProfile(View view){
        // deletes current profile

        final DatabaseHandler db = new DatabaseHandler(this);

        // alert dialog box taken from: https://stackoverflow.com/questions/36747369/how-to-show-a-pop-up-in-android-studio-to-confirm-an-order
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setCancelable(true);

        // check language
        String language = db.checkLanguage();
        Log.d("Settings", language);
        if(language.equals("English")) {

            builder.setTitle("Delete");
            builder.setMessage("Are you sure you want to delete this profile?");
            builder.setPositiveButton("Confirm",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // if yes - delete profile

                            int profileId = db.retrieveSelectedProfile();
                            Profile profile = db.retrieveProfile(profileId);

                            db.deletePurchases(profile.getName());
                            db.deleteDailySpendings(profile.getName());
                            db.deleteDates(profile.getName());
                            db.deleteProfile(profile.getName());

                            sendToSelectProfile();
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // if no - do nothing
                }
            });

        } else {

            builder.setTitle("Ištrinti");
            builder.setMessage("Ar jūs tikrai norite ištrinti šį profilį?");
            builder.setPositiveButton("Taip",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // if yes - delete profile

                            int profileId = db.retrieveSelectedProfile();
                            Profile profile = db.retrieveProfile(profileId);

                            db.deletePurchases(profile.getName());
                            db.deleteDailySpendings(profile.getName());
                            db.deleteDates(profile.getName());
                            db.deleteProfile(profile.getName());

                            sendToSelectProfile();
                        }
                    });
            builder.setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // if no - do nothing
                }
            });

        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void sendToSelectProfile() {
        Intent intent = new Intent (this, MainActivity.class);
        startActivity(intent);
    }
}

package com.example.dailyfinance;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.example.dailyfinance.mysqlite.DatabaseHandler;


/*
    This class is for creating a new profile
 */
public class CreateProfile extends AppCompatActivity{

    private EditText name;
    private EditText dailyLimit;
    private EditText currency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check which language to display
        DatabaseHandler db = new DatabaseHandler(this);
        String language = db.checkLanguage();
        Log.d("createProfile", language);
        if(language.equals("English")) setContentView(R.layout.create_profile);
        else setContentView(R.layout.create_profile_lt);

        // getting details from create_profile.xml
        name = (EditText) findViewById(R.id.name);
        dailyLimit = (EditText) findViewById(R.id.dailyLimit);
        currency = (EditText) findViewById(R.id.currency);
    }

    public void ShowMessageDialog(String str){

        // this method outputs a dialog box with a string passed as an argument

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setMessage(str);
        builder.setCancelable(false);
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void register(View v){

        // registering a new profile to the database

        DatabaseHandler db = new DatabaseHandler(this);

        String aName = name.getText().toString();
        String aCurrency = currency.getText().toString();
        double aDailyLimit;
        if(dailyLimit.getText().toString().length() > 0) aDailyLimit = Double.parseDouble(dailyLimit.getText().toString());
        else aDailyLimit = 0;

        // check if all fields have entries
        // check language
        String language = db.checkLanguage();
        if(language.equals("English")) {
            if (aName.equals("") || aCurrency.equals("") || aDailyLimit == 0) {
                ShowMessageDialog("Fill in all fields");
                return;
            }
            // check if name is unique
            if (!db.isNameUnique(aName)) {
                ShowMessageDialog("Name is already taken");
                return;
            }
        } else {
            if (aName.equals("") || aCurrency.equals("") || aDailyLimit == 0) {
                ShowMessageDialog("Užpildykite visus laukus");
                return;
            }
            // check if name is unique
            if (!db.isNameUnique(aName)) {
                ShowMessageDialog("Profilis su tokius vardu jau egzistuoja");
                return;
            }
        }
        // update all profiles selected column to "no"
        db.updateAllSelected();
        // insert values to database
        db.registerProfile(new Register(aName, aDailyLimit, aCurrency, "yes"));

        // send user to "Add a purchase" page
        Intent intent = new Intent (this, MainActivity.class);
        startActivity(intent);
    }
}

package com.example.dailyfinance.mysqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.dailyfinance.Profile;
import com.example.dailyfinance.Purchase;
import com.example.dailyfinance.Register;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


/*
    This is where all methods directly connected
    to database should be.
    This is the data persistence layer.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE \"profiles\" (\n" +
                    "\t\"id\"\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                    "\t\"name\"\tTEXT NOT NULL UNIQUE,\n" +
                    "\t\"dailyLimit\"\tINTEGER NOT NULL,\n" +
                    "\t\"currency\"\tTEXT NOT NULL,\n" +
                    "\t\"selected\"\tTEXT NOT NULL\n" +
                    ");");
        db.execSQL("CREATE TABLE \"purchases\" (\n" +
                    "\t\"id\"\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                    "\t\"profileName\"\tTEXT NOT NULL,\n" +
                    "\t\"price\"\tINTEGER NOT NULL,\n" +
                    "\t\"title\"\tTEXT NOT NULL,\n" +
                    "\t\"description\"\tTEXT,\n" +
                    "\t\"photoURL\"\tTEXT,\n" +
                    "\t\"location\"\tTEXT,\n" +
                    "\t\"dateCreated\"\tTEXT NOT NULL,\n" +
                    "\t\"timeCreated\"\tTEXT NOT NULL\n" +
                    ");");
        db.execSQL("CREATE TABLE \"dailySpendings\" (\n" +
                    "\t\"id\"\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                    "\t\"profileName\"\tTEXT NOT NULL,\n" +
                    "\t\"price\"\tINTEGER NOT NULL,\n" +
                    "\t\"totalLeft\"\tINTEGER NOT NULL,\n" +
                    "\t\"time\"\tTEXT NOT NULL\n" +
                    ");");
        db.execSQL("CREATE TABLE \"languages\" (\n" +
                    "\t\"id\"\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                    "\t\"language\"\tTEXT NOT NULL\n" +
                    ");");
        db.execSQL("CREATE TABLE \"dates\" (\n" +
                    "\t\"id\"\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                    "\t\"profileName\"\tTEXT NOT NULL,\n" +
                    "\t\"date\"\tTEXT NOT NULL\n" +
                    ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public DatabaseHandler(Context context){
        super(context, "DailyFinanceDB", null, 1);
    }

    public String checkLanguage(){
        // checks the language

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT language  FROM  languages WHERE id = (SELECT MAX(id) FROM languages)" , null);
        Log.d("cursor count", cursor.getCount() + "");
        if(cursor.getCount() == 0){
            return "English";
        } else {
            cursor.moveToFirst();
            String language = cursor.getString(cursor.getColumnIndex("language"));
            return language;
        }
    }

    public int retrieveSelectedProfile(){

        // this method retrieves a profile from database that has selected set to "yes", an automatic log-in in a way

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT id  FROM  profiles WHERE selected = 'yes'" , null);

        if(cursor.getCount() == 1) {
            // if we have a profile with selected set to "yes"
            cursor.moveToFirst();
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            cursor.close();
            sqLiteDatabase.close();
            return id;
        } else {
            cursor.close();
            sqLiteDatabase.close();
            return -1;
        }
    }

    public ArrayList<Integer> getProfilesIds() {

        // this method is for getting all ids of profiles registered

        ArrayList<Integer> ids = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT id  FROM  profiles" , null);

        // add ids to an array
        while (cursor.moveToNext()) {
            ids.add(cursor.getInt(cursor.getColumnIndex("id")));
        }
        sqLiteDatabase.close();
        cursor.close();
        return ids;
    }

    public Profile retrieveProfile(int id) {

        // this method is for retrieving a profile from the database

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT *  FROM  profiles WHERE id = '"+id+"'" , null);

        cursor.moveToFirst();
        String name = cursor.getString(cursor.getColumnIndex("name"));
        int dailyLimit = cursor.getInt(cursor.getColumnIndex("dailyLimit"));
        String currency = cursor.getString(cursor.getColumnIndex("currency"));
        String selected = cursor.getString(cursor.getColumnIndex("selected"));

        sqLiteDatabase.close();
        cursor.close();

        return new Profile(name, dailyLimit, currency, selected);
    }

    public void updateAllSelected() {

        // if there is a selected profile - change selected to "no"
        SQLiteDatabase updateDatabase = getReadableDatabase();
        ContentValues updateValues = new ContentValues();
        updateValues.put("selected", "no");
        String whereClause = "selected=?";
        String whereArgs[] = {"yes"};
        updateDatabase.update("profiles", updateValues, whereClause, whereArgs);
        printAllProfiles();
        updateDatabase.close();
    }

    public void printAllProfiles(){

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT *  FROM  profiles", null);
        Log.d("size of cursor", "" + cursor.getCount());
        try {
            while (cursor.moveToNext()) {
                Log.d("name", cursor.getString(cursor.getColumnIndex("name")));
                Log.d("selected", cursor.getString(cursor.getColumnIndex("selected")));
            }
        } finally {
            cursor.close();
        }
    }

    public void addSelected(String name) {

        // this method makes a profile selected

        SQLiteDatabase updateDatabase = getReadableDatabase();
        ContentValues updateValues = new ContentValues();
        updateValues.put("selected", "yes");
        String whereClause = "name=?";
        String whereArgs[] = {name};
        updateDatabase.update("profiles", updateValues, whereClause, whereArgs);
        updateDatabase.close();
    }

    public boolean isNameUnique(String aName){

        // this method compares the name to all profile names
        aName = aName.toUpperCase();
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String sql = "SELECT * FROM  profiles WHERE name = ?";

        String whereArgs[] = {aName};
        Cursor cursor = sqLiteDatabase.rawQuery(sql, whereArgs);

        if(cursor.getCount() > 0) {
            cursor.close();
            sqLiteDatabase.close();
            return false;
        } else {
            cursor.close();
            sqLiteDatabase.close();
            return true;
        }
    }

    public void registerProfile(Register profile) {

        // this method is for registering a new profile to the database


        ContentValues putValues = new ContentValues();

        // insert values to database
        SQLiteDatabase putDatabase = getReadableDatabase();
        putValues.put("name", profile.getName().toUpperCase());
        putValues.put("dailyLimit", profile.getDailyLimit());
        putValues.put("currency", profile.getCurrency());
        putValues.put("selected", profile.getSelected());

        long result = putDatabase.insert("profiles", null, putValues);

        if (result > 0) {
            Log.d("dbhelper", "registered successfully");
            addTotalLeft(profile.getName(), 0, profile.getDailyLimit());
        } else {
            Log.d("dbhelper", "failed to register");
        }

        putDatabase.close();
    }

    public void addTotalLeft(String name, double price, double totalLeft) {

        // method that adds a new entry to the database table dailySpendings

        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        ContentValues putValues = new ContentValues();

        putValues.put("profileName", name);
        putValues.put("price", price);
        putValues.put("totalLeft", totalLeft);
        putValues.put("time", currentTime);

        sqLiteDatabase.insert("dailySpendings", null, putValues);
        sqLiteDatabase.close();
    }

    public double retrieveTotalLeftWithName(String name) {

        // this method retrieves totalLeft from database dailySpendings table for specfic profile

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT totalLeft FROM dailySpendings WHERE profileName = '"+name+"' AND id = (SELECT MAX(id) FROM dailySpendings WHERE profileName = '"+name+"');" , null);

        if(cursor.getCount() == 0){
            int profileId = retrieveSelectedProfile();
            Profile profile = retrieveProfile(profileId);
            addTotalLeft(profile.getName(), 0, profile.getDailyLimit());
            return retrieveTotalLeftWithName(name);
        } else {
            cursor.moveToFirst();
            double totalLeft = cursor.getDouble(cursor.getColumnIndex("totalLeft"));
            sqLiteDatabase.close();
            cursor.close();
            return totalLeft;
        }
    }

    public void setLanguage(String aLanguage){

        // this method sets selected language in the database

        SQLiteDatabase putDatabase = getReadableDatabase();
        ContentValues putValues = new ContentValues();
        putValues.put("language", aLanguage);
        putDatabase.insert("languages", null, putValues);
        putDatabase.close();
    }

    public void addPurchase(Purchase purchase){

        // this method adds a new purchase to the database

        // get current date and time
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        ContentValues putValues = new ContentValues();

        putValues.put("profileName", purchase.getName());
        putValues.put("price", purchase.getPrice());
        putValues.put("title", purchase.getTitle());
        putValues.put("description", purchase.getDescription());
        putValues.put("photoURL", purchase.getImage());
        putValues.put("location", purchase.getLocation());
        putValues.put("dateCreated", currentDate);
        putValues.put("timeCreated", currentTime);

        sqLiteDatabase.insert("purchases", null, putValues);
        sqLiteDatabase.close();
    }

    public void addCurrentDate(String name, String date) {

        // this method adds current date to the database

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        ContentValues putValues = new ContentValues();

        putValues.put("profileName", name);
        putValues.put("date", date);

        sqLiteDatabase.insert("dates", null, putValues);
        sqLiteDatabase.close();
    }

    public long checkIfNewDay(String name, String currentDate) {

        // this method checks if current date is bigger than the biggest last added date for a specific profile

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String sql = "SELECT date FROM dates WHERE id = (SELECT MAX(id) FROM dates WHERE profileName = ?)";

        String whereArgs[] = {name};
        Cursor cursor = sqLiteDatabase.rawQuery(sql, whereArgs);

        cursor.moveToFirst();
        String date = cursor.getString(cursor.getColumnIndex("date"));
        // check the difference in days between two dates
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date date1 = myFormat.parse(date);
            Date date2 = myFormat.parse(currentDate);
            long diff = date2.getTime() - date1.getTime();
            return (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void deletePurchases(String name) {

        // deletes purchases for a specific profile

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String whereClause = "profileName = ?";
        String whereArgs[] = {name};
        sqLiteDatabase.delete("purchases", whereClause, whereArgs);
    }

    public void deleteDailySpendings(String name) {

        // deletes dailySpendings for a specific profile

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String whereClause = "profileName = ?";
        String whereArgs[] = {name};
        sqLiteDatabase.delete("dailySpendings", whereClause, whereArgs);
    }

    public void deleteDates(String name) {

        // deltes dates for specific profile

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String whereClause = "profileName = ?";
        String whereArgs[] = {name};
        sqLiteDatabase.delete("dates", whereClause, whereArgs);
    }

    public void deleteProfile(String name){

        // deletes profile from database

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String whereClause = "name = ?";
        String whereArgs[] = {name};
        sqLiteDatabase.delete("profiles", whereClause, whereArgs);
    }

    public ArrayList<String> retrieve50Titles(String name){

        ArrayList<String> results = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String sql = "SELECT title FROM purchases WHERE profileName = ? ORDER BY id DESC LIMIT 50";

        String[] args = {name};

        Cursor cursor = sqLiteDatabase.rawQuery(sql, args);

        try {
            while (cursor.moveToNext()) {
               results.add(cursor.getString(cursor.getColumnIndex("title")));
            }
        } finally {
            cursor.close();
        }

        return results;
    }

    public ArrayList<String> retrieve50Dates(String name){

        ArrayList<String> results = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String sql = "SELECT dateCreated FROM purchases WHERE profileName = ? ORDER BY id DESC LIMIT 50";

        String[] args = {name};

        Cursor cursor = sqLiteDatabase.rawQuery(sql, args);

        try {
            while (cursor.moveToNext()) {
                results.add(cursor.getString(cursor.getColumnIndex("dateCreated")));
            }
        } finally {
            cursor.close();
        }

        return results;
    }

    public Purchase retrievePurchase(String purchaseTitle) {

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String sql = "SELECT * FROM purchases WHERE title = ?";
        String[] args = {purchaseTitle};
        Cursor cursor = sqLiteDatabase.rawQuery(sql, args);

        cursor.moveToFirst();
        String profileName = cursor.getString(cursor.getColumnIndex("profileName"));
        double price = cursor.getDouble(cursor.getColumnIndex("price"));
        String title = cursor.getString(cursor.getColumnIndex("title"));
        String description = cursor.getString(cursor.getColumnIndex("description"));
        String photoURL = cursor.getString(cursor.getColumnIndex("photoURL"));
        String location = cursor.getString(cursor.getColumnIndex("location"));
        String dateCreated = cursor.getString(cursor.getColumnIndex("dateCreated"));
        String timeCreated = cursor.getString(cursor.getColumnIndex("timeCreated"));

        return new Purchase(profileName, price, title, description, photoURL, location, dateCreated, timeCreated);
    }
}

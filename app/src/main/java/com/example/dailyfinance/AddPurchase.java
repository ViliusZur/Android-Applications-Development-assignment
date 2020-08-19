package com.example.dailyfinance;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.dailyfinance.mysqlite.DatabaseHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddPurchase extends AppCompatActivity implements OnMapReadyCallback {

    private EditText price;
    private EditText title;
    private EditText description;
    private FusedLocationProviderClient mFusedLocationClient;
    private double currentLatitude;
    private double currentLongitude;
    private ImageView mImageView;
    private Uri imageUri;
    private String imagePath;
    private LinearLayout photoMapLayout;
    private LinearLayout locationLayout;

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check which language to display
        DatabaseHandler db = new DatabaseHandler(this);
        String language = db.checkLanguage();
        Log.d("addPurchase", language);
        if(language.equals("English")) setContentView(R.layout.add_purchases);
        else setContentView(R.layout.add_purchases_lt);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // check if its a new day
        checkIfNewDay();

        // add a new date to the database
        addCurrentDate();

        // retrieve selected profile
        Profile profile = retrieveProfile();

        TextView name = (TextView) findViewById(R.id.name);
        name.setText(profile.getName());

        // add a TextView for daily counter
        addDailyCounter(profile);

        // code related to capturing images with camera
        imagePath = "";

        mImageView = (ImageView) findViewById(R.id.imageView);
        Button mCaptureBtn = (Button) findViewById(R.id.imageButton);

        // button click
        mCaptureBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // if system's OS is >= Marshmallow, request runtime permission
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED) {
                        // permission not enabled - request it
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                        // show popup to request permission
                        requestPermissions(permission, PERMISSION_CODE);
                    }
                    else {
                        // permission already granted
                        openCamera();
                    }
                }
                else {
                    // system OS < Marshmallow
                    openCamera();
                }
            }
        });

        // this code is run when add location button is pressed
        Button mLocationBtn = (Button) findViewById(R.id.locationButton);
        photoMapLayout = (LinearLayout) findViewById(R.id.showImageLocation);
        locationLayout = (LinearLayout) findViewById(R.id.locationLayout);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // button click
        mLocationBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                locationPermissions();
            }
        });
    }

    public void locationPermissions(){
        // if system's OS is >= Marshmallow, request runtime permission
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                // permission not enabled - request it
                String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};
                // show popup to request permission
                requestPermissions(permission, PERMISSION_CODE);
            }
            else {
                // permission already granted
                getLastLocation();
            }
        }
        else {
            // system OS < Marshmallow
            getLastLocation();
        }
    }

    public void getLastLocation(){
        mFusedLocationClient.getLastLocation().addOnCompleteListener(
                new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();
                            displayMap();
                        }
                    }
                }
        );
    }

    public void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            currentLatitude = mLastLocation.getLatitude();
            currentLongitude = mLastLocation.getLongitude();
            displayMap();
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // and move the map's camera to the same location.
        ViewGroup.LayoutParams params = photoMapLayout.getLayoutParams();
        params.height = 400;
        photoMapLayout.setLayoutParams(params);
        params = locationLayout.getLayoutParams();
        params.height = 400;
        locationLayout.setLayoutParams(params);
        float zoomLevel = 16.0f;
        LatLng currentPosition = new LatLng(currentLatitude, currentLongitude);
        googleMap.addMarker(new MarkerOptions().position(currentPosition).title("Marker in current position"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, zoomLevel));
    }

    @Override
    public void onBackPressed() {

        // this method does not let to go back to previous activity with back button

        moveTaskToBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_components, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // this method is for when the user select a menu item from top toolbar

        switch (item.getItemId()) {
            case R.id.settings:
                // User chose the "Settings" item, show the app settings UI
                Intent settings = new Intent(this, Settings.class);
                this.startActivity(settings);
                break;

            case R.id.search:
                // User chose the "List" action, list the DB
                Intent list = new Intent(this, List.class);
                this.startActivity(list);
                break;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
        return true;
    }

    public void displayMap(){
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public Profile retrieveProfile() {

        // this method retrieves a selected profile from the database

        DatabaseHandler db = new DatabaseHandler(this);
        int profileID = db.retrieveSelectedProfile();
        Profile profile = db.retrieveProfile(profileID);

        return profile;
    }

    public void addDailyCounter(Profile profile){

        // this method adds a dailyCounter view to the layout

        DatabaseHandler db = new DatabaseHandler(this);
        double totalLeft = db.retrieveTotalLeftWithName(profile.getName());
        // format titalLeft to 2 digits after decimal
        totalLeft = Double.parseDouble(new DecimalFormat("##.##").format(totalLeft));
        LinearLayout myLayout = findViewById(R.id.dailyLimit);

        // adding parameters to the view
        TextView dailyCounter  = new TextView(this);
        new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        dailyCounter.setGravity(Gravity.CENTER);
        if(totalLeft > 0) dailyCounter.setTextColor(Color.parseColor("#00cc00"));
        if(totalLeft <= 0) dailyCounter.setTextColor(Color.parseColor("#ff0000"));
        dailyCounter.setTextSize(50);
        dailyCounter.setBackgroundResource(R.drawable.border);
        dailyCounter.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);


        // add other parameters to the view
        String text = totalLeft + " " + profile.getCurrency();
        int id = 1;
        dailyCounter.setText(text);
        dailyCounter.setId(id);

        // add button to the layout
        myLayout.addView(dailyCounter);
    }

    private void openCamera() {

        ContentValues values = new ContentValues();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        values.put(MediaStore.Images.Media.TITLE, timeStamp);
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Daily Finance app");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        // camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // handling permission result
        // this is called when user presses allow or deny on permission request

        switch (requestCode) {
            case PERMISSION_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // permission from popup was granted
                    // if request sent for camera permission
                    if(permissions[0] == Manifest.permission.CAMERA) openCamera();
                    // if request sent for location permission
                    if(permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION){
                        getLastLocation();
                    }
                }
                else {
                    // permission from popup was denied
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // called when image was captured from camera

        if(resultCode == RESULT_OK){
            // set the image captured to our image view
            ViewGroup.LayoutParams params = photoMapLayout.getLayoutParams();
            params.height = 400;
            photoMapLayout.setLayoutParams(params);
            mImageView.setImageURI(imageUri);
            imagePath = getRealPathFromURI(imageUri);
        }
    }

    public String getRealPathFromURI(Uri uri) {

        // this method gets the path of an image from a uri

        String path = "";
        if (getContentResolver() != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }

    public void ShowMessageDialog(String str){

        // this method outputs a dialog box with a string passed as an argument

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    public void addPurchase(View v) {

        // this methods adds a purchase to the database

        DatabaseHandler db = new DatabaseHandler(this);

        // getting details
        price = (EditText) findViewById(R.id.price);
        title = (EditText) findViewById(R.id.title);
        description = (EditText) findViewById(R.id.descriptionEditText);

        // assign values to variables
        double aPrice;
        if(price.getText().toString().length() > 0) aPrice = Double.parseDouble(price.getText().toString());
        else aPrice = 0;
        String aTitle = title.getText().toString();
        String aDescription = description.getText().toString();

        // check if price field has an entry
        if(aPrice == 0){
            ShowMessageDialog("Fill in the price");
            return;
        }
        // check if title and description fields are empty, if so - put date and time as the title
        if(aTitle.equals("")){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd | HH:mm:ss", Locale.getDefault());
            aTitle = sdf.format(new Date());
        }

        // retrieve how much money is left for the day
        double totalLeft = db.retrieveTotalLeftWithName(retrieveProfile().getName());

        // insert values to database
        String location = "lat: " + currentLatitude + " lon: " + currentLongitude;
        db.addPurchase(new Purchase(retrieveProfile().getName(), aPrice, aTitle, aDescription, imagePath, location, "", ""));

        // add a new entry to dailySpendings
        totalLeft -= aPrice;
        db.addTotalLeft(retrieveProfile().getName(), aPrice, totalLeft);

        // reload
        Intent intent = new Intent (this, AddPurchase.class);
        startActivity(intent);
    }

    public void addMoney(View v) {

        // this method adds money to the daily spendings

        DatabaseHandler db = new DatabaseHandler(this);

        // getting details
        price = (EditText) findViewById(R.id.price);
        title = (EditText) findViewById(R.id.title);
        description = (EditText) findViewById(R.id.descriptionEditText);

        // assign values to variables
        double aPrice;
        if(price.getText().toString().length() > 0) aPrice = Double.parseDouble(price.getText().toString());
        else aPrice = 0;
        String aTitle = title.getText().toString();
        String aDescription = description.getText().toString();

        // check if price field has an entry
        if(aPrice == 0){

            ShowMessageDialog("Fill in the price");
            return;
        }
        // check if title and description fields are empty, if so - put date and time as the title
        if(aTitle.equals("")){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd | HH:mm:ss", Locale.getDefault());
            aTitle = sdf.format(new Date());
            aTitle = "Added Money " + aTitle;
        }

        // retrieve how much money is left for the day
        double totalLeft = db.retrieveTotalLeftWithName(retrieveProfile().getName());

        // insert values to database
        String location = "lat: " + currentLatitude + " lon: " + currentLongitude;
        db.addPurchase(new Purchase(retrieveProfile().getName(), aPrice, aTitle, aDescription, imagePath, location, "", ""));

        // add a new entry to dailySpendings
        totalLeft += aPrice;
        db.addTotalLeft(retrieveProfile().getName(), aPrice, totalLeft);

        // reload
        Intent intent = new Intent (this, AddPurchase.class);
        startActivity(intent);
    }

    public void checkIfNewDay() {

        // this method checks if a new day has started

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DatabaseHandler db = new DatabaseHandler(this);
        long difference = db.checkIfNewDay(retrieveProfile().getName(), currentDate);
        if(difference > 0){
            double moneyForNextDay = db.retrieveTotalLeftWithName(retrieveProfile().getName()) + (retrieveProfile().getDailyLimit() * difference);
            db.deleteDailySpendings(retrieveProfile().getName());
            db.addTotalLeft(retrieveProfile().getName(), 0, moneyForNextDay);
        }
    }

    public void addCurrentDate() {

        // this method adds current date to the databsae

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DatabaseHandler db = new DatabaseHandler(this);
        db.addCurrentDate(retrieveProfile().getName(), currentDate);
    }
}

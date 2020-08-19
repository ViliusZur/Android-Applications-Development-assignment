package com.example.dailyfinance;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dailyfinance.mysqlite.DatabaseHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.io.File;
import java.text.DecimalFormat;

public class SelectedPurchase extends AppCompatActivity implements OnMapReadyCallback {

    private TextView title;
    private TextView price;
    private TextView description;
    private TextView date;
    private TextView time;
    private ImageView photo;
    private LinearLayout photoLayout;
    private SupportMapFragment map;
    private String aLocation;
    private LinearLayout locationLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check which language to display
        DatabaseHandler db = new DatabaseHandler(this);
        String language = db.checkLanguage();
        Log.d("SelectedPurchase", language);
        if(language.equals("English")) setContentView(R.layout.selected_purchase);
        else setContentView(R.layout.selected_purchase_lt);

        // retrieve purchase title from previous activity
        Bundle b = getIntent().getExtras();
        String purchaseTitle = "";
        if(b != null){
            purchaseTitle = b.getString("purchaseTitle");
        }

        // retrieve the selected purchase
        Purchase selectedPurchase = db.retrievePurchase(purchaseTitle);
        // retrieve selected profile
        int profileId = db.retrieveSelectedProfile();
        Profile profile = db.retrieveProfile(profileId);

        title = (TextView) findViewById(R.id.title);
        price = (TextView) findViewById(R.id.price);
        description = (TextView) findViewById(R.id.description);
        date = (TextView) findViewById(R.id.date);
        time = (TextView) findViewById(R.id.time);
        photo = (ImageView) findViewById(R.id.photo);
        photoLayout = (LinearLayout) findViewById(R.id.photoLayout);
        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        locationLayout = (LinearLayout) findViewById(R.id.locationLayout);

        displayData(selectedPurchase, profile, language);
    }

    public void displayData(Purchase purchase, Profile profile, String language){
        //display all gathered data

        String aTitle = purchase.getTitle();

        double aPrice = purchase.getPrice();
        aPrice = Double.parseDouble(new DecimalFormat("##.##").format(aPrice));

        String aDescription = purchase.getDescription();
        String aDate = purchase.getDateCreated();

        String aTime = purchase.getTimeCreated();
        aTime = aTime.substring(0, aTime.length() - 3);

        String aPhotoPath = purchase.getImage();
        aLocation = purchase.getLocation();

        title.setText(aTitle);

        // check language
        if(language.equals("English")) {
            String priceText = "Price: " + aPrice + " " + profile.getCurrency();
            price.setText(priceText);
            aDescription = "Description:\n" + aDescription;
            description.setText(aDescription);

            if (!aTitle.contains(aDate)) {
                aDate = "Date: " + aDate;
                date.setText(aDate);
            }
            if (!aTitle.contains(aTime)) {
                aTime = "Time: " + aTime;
                time.setText(aTime);
            }
        } else {
            String priceText = "Kaina: " + aPrice + " " + profile.getCurrency();
            price.setText(priceText);
            aDescription = "Aprašymas:\n" + aDescription;
            description.setText(aDescription);

            if (!aTitle.contains(aDate)) {
                aDate = "Data: " + aDate;
                date.setText(aDate);
            }
            if (!aTitle.contains(aTime)) {
                aTime = "Laikas: " + aTime;
                time.setText(aTime);
            }
        }
        // get photo and display it if it exists
        File imgFile = new  File(aPhotoPath);

        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ViewGroup.LayoutParams params = photoLayout.getLayoutParams();
            params.height = 400;
            photoLayout.setLayoutParams(params);
            photo.setImageBitmap(myBitmap);
        }

        // display map if coordinatess exist
        if(!aLocation.equals("lat: 0.0 lon: 0.0")){
            map.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // adds map to the screen with a location

        ViewGroup.LayoutParams params = locationLayout.getLayoutParams();
        params.height = 400;
        locationLayout.setLayoutParams(params);

        String[] parts = aLocation.split(" ");
        double lat = Double.parseDouble(parts[1]);
        double lon = Double.parseDouble(parts[3]);

        float zoomLevel = 16.0f;
        LatLng currentPosition = new LatLng(lat, lon);
        googleMap.addMarker(new MarkerOptions().position(currentPosition).title("Marker in current position"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, zoomLevel));
    }
}

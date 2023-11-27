package com.example.weatherapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    // UI elements
    Switch manualLocationSwitch;
    EditText countryEditText;
    EditText cityEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize UI elements inside the onCreate method
        manualLocationSwitch = findViewById(R.id.manualLocationSwitch);
        countryEditText = findViewById(R.id.CoyntryEditText);
        cityEditText = findViewById(R.id.cityEditText);

        // Retrieve the boolean value from the Intent
        Intent intent = getIntent();
        boolean isManualLocationSelected = intent.getBooleanExtra("isManualLocationSelected", false);

        // Set the Switch state based on the retrieved value
        manualLocationSwitch.setChecked(isManualLocationSelected);

        // Enable or disable EditText fields based on the Switch state
        countryEditText.setEnabled(isManualLocationSelected);
        cityEditText.setEnabled(isManualLocationSelected);

        // Set up a listener for the manualLocationSwitch
        manualLocationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Enable or disable EditText fields based on the Switch state
            countryEditText.setEnabled(isChecked);
            cityEditText.setEnabled(isChecked);
        });
    }

    // Button click handler to go back to the main activity
    public void backToMain(View view) {
        boolean isManualLocationSelected = manualLocationSwitch.isChecked();

        // Retrieve the user's selected home and away currencies
        TextView userCityTextView = findViewById(R.id.cityEditText);
        String userCity = userCityTextView.getText().toString();
        TextView userCountryTextView = findViewById(R.id.CoyntryEditText);
        String userCountry = userCountryTextView.getText().toString();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("userCountryEdit", userCountry);
        intent.putExtra("userCityEdit", userCity);
        intent.putExtra("isManualLocationSelected", isManualLocationSelected);

        startActivity(intent);
    }

    // Button click handler to open Google Maps with the specified location
    public void openGoogleMaps(View view) {
        Intent intent = getIntent();
        double lat = intent.getDoubleExtra("Latitude", 0.0);
        double lon = intent.getDoubleExtra("Longitude", 0.0);

        // Build the Google Maps URL
        String googleMapsUrl = "https://www.google.com/maps/place/" + lat + "," + lon;

        // Now you can use the googleMapsUrl string to open the location in a web browser or an intent
        // For example, you can use an Intent to open the URL in a browser
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsUrl));
        startActivity(browserIntent);
    }
}
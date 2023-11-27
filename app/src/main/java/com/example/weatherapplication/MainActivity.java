package com.example.weatherapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    public String CityText; // Variable to store the "City: " string

    private boolean userManualLocationCheck = false;
    public String CountryText; // Variable to store the "Country: " string

    private String city = "";

    private double latitude;
    private double longitude;

    private double temperature;

    private String countryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize string variables with localized strings
        CityText = getString(R.string.city_text);
        CountryText = getString(R.string.country_text);

        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        userManualLocationCheck = intent.getBooleanExtra("isManualLocationSelected", false);
        city = intent.getStringExtra("userCityEdit");
        countryName = intent.getStringExtra("userCountryEdit");
        TextView cityTextView = findViewById(R.id.cityTextView);
        TextView countryTextView = findViewById(R.id.CountryTextView);

        if (savedInstanceState != null) {
            city = savedInstanceState.getString("CITY_VALUE");
            temperature = savedInstanceState.getDouble("TEMPERATURE_VALUE");
            userManualLocationCheck = savedInstanceState.getBoolean("MANUAL_LOCATION_CHECK_VALUE");
            countryName = savedInstanceState.getString("COUNTRY_VALUE");
            cityTextView.setText(CityText + city);
            countryTextView.setText(CountryText + countryName);
        }

        if (!userManualLocationCheck) {
            getLocation();
        } else {
            cityTextView.setText(CityText + city);
            countryTextView.setText(CountryText + countryName);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putDouble("TEMPERATURE_VALUE", temperature);
        bundle.putString("CITY_VALUE", city);
        bundle.putBoolean("MANUAL_LOCATION_CHECK_VALUE", userManualLocationCheck);
        bundle.putString("COUNTRY_VALUE", countryName);
    }

    public void getLocationInformation(View view) {
        userManualLocationCheck = false;
        getLocation();
    }

    public void getLocation() {
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions
            requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            return;
        }

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Register to listen for location changes
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                String baseUrl = "https://api.bigdatacloud.net/data/reverse-geocode-client";
                String locationURL = baseUrl + "?latitude=" + latitude + "&longitude=" + longitude + "&localityLanguage=en";
                getLocationInformation(locationURL);
            }
        });
    }

    public void getLocationInformation(String URL) {
        // Retrieve location information using Volley library
        StringRequest request = new StringRequest(Request.Method.GET, URL, response -> {
            parseLocationJasonData(response);
        }, error -> {
            showErrorMessage("Unable to get location. Check internet and try again.");
        });

        Volley.newRequestQueue(this).add(request);
    }

    private void parseLocationJasonData(String response) {
        // Parse JSON data for location information
        try {
            JSONObject countryJSON = new JSONObject(response);
            countryName = countryJSON.getString("countryName");
            city = countryJSON.getString("city");

            TextView countryTextView = findViewById(R.id.CountryTextView);
            TextView cityTextView = findViewById(R.id.cityTextView);
            countryTextView.setText(CountryText + countryName);
            cityTextView.setText(CityText + city);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void getWeather(View view) {
        // Retrieve weather information using OpenWeatherMap API
        String baseUrlStart = "https://api.openweathermap.org/data/2.5/weather?q=";
        String baseUrlEnd = "&appid=1124ca64182ff9c03f6bc13a1ed5122c&units=metric";
        String URL = baseUrlStart + city + baseUrlEnd;

        StringRequest request = new StringRequest(Request.Method.GET, URL, response -> {
            // Successful response, parse and handle weather data
            parseWeatherJasonData(response);
        }, error -> {
            // Handle network errors, server errors, etc.
            if (error.networkResponse != null) {
                String msg;

                // Handle errors with HTTP status codes (e.g., 404 Not Found)
                int statusCode = error.networkResponse.statusCode;
                String errorMessage = new String(error.networkResponse.data);

                if (statusCode == 404) {
                    // Handle 404 error (e.g., show a message to the user)
                    showErrorMessage("Resource not found. Please try again.");
                } else if (statusCode == 500) {
                    // Handle 500 error (e.g., show a message to the user)
                    showErrorMessage("Internal Server Error. Please try again later.");
                }

                // Handle error based on status code and error message
            } else {
                // Handle other types of errors (e.g., timeout, no internet connection)
                showErrorMessage("Check your internet connection and try again");
            }
        });

        // Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(request);
    }


    private void parseWeatherJasonData(String response) {
        // Parse JSON data for weather information
        try {
            JSONObject weatherJSON = new JSONObject(response);
            String weather = weatherJSON.getJSONArray("weather").getJSONObject(0).getString("main");
            temperature = weatherJSON.getJSONObject("main").getDouble("temp");

            TextView temperatureTextView = findViewById(R.id.temperatureTextView);
            temperatureTextView.setText(getString(R.string.temperature_label) + temperature + " C");

            TextView weatherTextView = findViewById(R.id.descriptionTextView);
            weatherTextView.setText(getString(R.string.description_label) + weather);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void startSettingsActivity(View view) {
        // Start SettingsActivity with necessary information
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("isManualLocationSelected", userManualLocationCheck);
        intent.putExtra("Latitude", latitude);
        intent.putExtra("Longitude", longitude);
        startActivity(intent);
    }

    private void showErrorMessage(String msg) {
        // Display a toast message to inform the user about the internet connection issue
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


}
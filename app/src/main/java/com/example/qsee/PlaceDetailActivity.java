package com.example.qsee;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PlaceDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail); // Set your layout XML here

        // Retrieve place details from an Intent or another source
        String placeName = getIntent().getStringExtra("placeName");
        String placeAddress = getIntent().getStringExtra("placeAddress");
        double placeRating = getIntent().getDoubleExtra("placeRating", 0.0);

        // Populate UI elements with place details
        TextView nameTextView = findViewById(R.id.placeNameTextView);
        TextView addressTextView = findViewById(R.id.placeAddressTextView);
        TextView ratingTextView = findViewById(R.id.placeRatingTextView);

        nameTextView.setText(placeName);
        addressTextView.setText(placeAddress);
        ratingTextView.setText(String.valueOf(placeRating));
    }
}
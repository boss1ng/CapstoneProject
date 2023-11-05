package com.example.qsee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

public class PlaceDetailFragment extends Fragment {

    public PlaceDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_detail, container, false);

        // Retrieve the selected location details from the arguments
        Bundle args = getArguments();
        if (args != null) {
            String placeName = args.getString("placeName", "");
            String placeAddress = args.getString("placeAddress", "");
            String placeDescription = args.getString("placeDescription", "");
            String placeRating = args.getString("placeRating", "");
            String placeLink = args.getString("placeLink", "");
            String placePrice = args.getString("placePrice", "");
            String isUserInQuezonCity = args.getString("isUserInQuezonCity", "");

            // Populate UI elements with place details
            TextView nameTextView = view.findViewById(R.id.placeNameTextView);
            TextView addressTextView = view.findViewById(R.id.placeAddressTextView);
            TextView descriptionTextView = view.findViewById(R.id.placeDescriptionTextView);
            TextView ratingTextView = view.findViewById(R.id.placeRatingTextView);
            Button directionsButton = view.findViewById(R.id.directionsButton);
            ImageView reportButton = view.findViewById(R.id.reportButton);
            ImageView imageViewLocation = view.findViewById(R.id.imageViewLocation);
            TextView priceTextView = view.findViewById(R.id.placePriceTextView);

            if (nameTextView != null) {
                nameTextView.setText(placeName);
            }

            if (addressTextView != null) {
                addressTextView.setText(placeAddress);
            }

            if (descriptionTextView != null) {
                descriptionTextView.setText(placeDescription);
            }

            if (ratingTextView != null) {
                double doubleRating = Double.parseDouble(placeRating);
                ratingTextView.setText(String.valueOf(doubleRating));
            }

            if (imageViewLocation != null) {
                Picasso.get().load(placeLink).into(imageViewLocation);
            }

            if (priceTextView != null) {
                priceTextView.setText(placePrice);
            }

            if (directionsButton != null) {
                directionsButton.setEnabled("true".equals(isUserInQuezonCity));
            }
        }

        return view;
    }
}

package com.example.qsee;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.squareup.picasso.Picasso;

public class PlaceDetailDialogFragment extends DialogFragment {


    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create a new Dialog instance
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Set a custom layout for the dialog
        dialog.setContentView(R.layout.fragment_place_detail);

        // Customize the width of the dialog (75% of screen width)
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 1);
        dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

        return dialog;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the custom layout for this dialog fragment
        View view = inflater.inflate(R.layout.fragment_place_detail, container, false);

        // Retrieve place details from arguments
        String placeName = getArguments().getString("placeName");
        String placeAddress = getArguments().getString("placeAddress");
        String placeDescription = getArguments().getString("placeDescription");
        String placeRating = getArguments().getString("placeRating");
        Double doubleRating = Double.parseDouble(placeRating);
        String placeLink = getArguments().getString("placeLink");
        String placePrice = getArguments().getString("placePrice");

        // Populate UI elements with place details
        TextView nameTextView = view.findViewById(R.id.placeNameTextView);
        TextView addressTextView = view.findViewById(R.id.placeAddressTextView);
        TextView descriptionTextView = view.findViewById(R.id.placeDescriptionTextView);
        TextView ratingTextView = view.findViewById(R.id.placeRatingTextView);
        Button directionsButton = view.findViewById(R.id.directionsButton);
        Button reportButton = view.findViewById(R.id.reportButton);
        ImageView imageViewLocation = view.findViewById(R.id.imageViewLocation);
        TextView priceTextView = view.findViewById(R.id.placePriceTextView);

        nameTextView.setText(placeName);
        addressTextView.setText(placeAddress);
        descriptionTextView.setText(placeDescription);
        ratingTextView.setText(String.valueOf(doubleRating));
        Picasso.get()
                .load(placeLink)
                .into(imageViewLocation);
        priceTextView.setText(placePrice);

        // Add a click listener to the Directions button
        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                // Open Google Maps for directions
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + placeAddress);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
                 */

                //view.setVisibility(View.GONE);

                dismiss(); // Dismiss the dialog

            }
        });

        // Add a click listener to the Report button
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle report button click here
                // You can implement the report functionality as needed
                // For example, open a dialog for reporting the place
            }
        });

        return view;
    }



}

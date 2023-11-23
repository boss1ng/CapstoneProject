package com.example.qsee;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

public class PlaceDialogSearch extends DialogFragment {


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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the custom layout for this dialog fragment
        View view = inflater.inflate(R.layout.fragment_place_detail, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Retrieve place details from arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");


        // Populate UI elements with place details
        TextView nameTextView = view.findViewById(R.id.placeNameTextView);
        TextView addressTextView = view.findViewById(R.id.placeAddressTextView);
        TextView descriptionTextView = view.findViewById(R.id.placeDescriptionTextView);
        TextView ratingTextView = view.findViewById(R.id.placeRatingTextView);
        Button directionsButton = view.findViewById(R.id.directionsButton);
        TextView contactView = view.findViewById(R.id.placeContactTextView);
            // Change the text of the button
            directionsButton.setText("Go to Maps");
        ImageView reportButton = view.findViewById(R.id.reportButton);
        reportButton.setImageResource(R.drawable.report_dialog);
        ImageView imageViewLocation = view.findViewById(R.id.imageViewLocation);
        TextView priceTextView = view.findViewById(R.id.placePriceTextView);

        String placeName = getArguments().getString("placeName");
        String placeAddress = getArguments().getString("placeAddress");
        String placeDescription;
        if (getArguments().getString("placeDescription").equals("-"))
            placeDescription = " ";
        else
            placeDescription = getArguments().getString("placeDescription");

        String placeRating = getArguments().getString("placeRating");


        String placeLink = getArguments().getString("placeLink");

        String placePrice;
        if (getArguments().getString("placePrice").equals("-")) {
            priceTextView.setVisibility(View.INVISIBLE);
        }
        else {
            placePrice = getArguments().getString("placePrice");
            priceTextView.setText(placePrice);
        }
        String placeContact;
        if (!getArguments().getString("contact").equals("")) {
            contactView.setVisibility(View.VISIBLE);
            placeContact = getArguments().getString("contact");
            contactView.setText(placeContact);
        }

        Double currentUserLat = getArguments().getDouble("userLatitude");
        Double currentUserLong = getArguments().getDouble("userLongitude");
        String destinationLat = getArguments().getString("destinationLatitude");
        String destinationLong = getArguments().getString("destinationLongitude");

        // Toast.makeText(getContext(), String.valueOf(destinationLat), Toast.LENGTH_LONG).show();

        nameTextView.setText(placeName);
        addressTextView.setText(placeAddress);
        descriptionTextView.setText(placeDescription);

            if (placeRating != null) {
                try {
                    Double doubleRating = Double.parseDouble(placeRating);
                    DecimalFormat df = new DecimalFormat(doubleRating == 0 ? "0" : "#0.0");
                    String formattedRating = df.format(doubleRating);
                    ratingTextView.setText(String.valueOf(formattedRating));
                    Picasso.get().load(placeLink).into(imageViewLocation);
                } catch (NumberFormatException e) {
                    // Handle the exception or log an error message
                    e.printStackTrace();
                }
            } else {
                // Handle the case where placeRating is null
                // You might want to set a default value or display a message
            }

            String isUserInQuezonCity = getArguments().getString("isUserInQuezonCity");
        //Toast.makeText(getContext(), isUserInQuezonCity, Toast.LENGTH_LONG).show();

        if (isUserInQuezonCity.equals("true"))
            directionsButton.setEnabled(true);
        else
            directionsButton.setEnabled(true);
        //directionsButton.setEnabled(false);


        // Add a click listener to the Directions button
        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the current dialog
                dismiss();

                // Create an instance of your MapsFragment
                MapsFragment mapsFragment = new MapsFragment();

                // Pass latitude and longitude as arguments
                Bundle args = new Bundle();
                args.putDouble("placeLatitude", Double.parseDouble(destinationLat));
                args.putDouble("placeLongitude", Double.parseDouble(destinationLong));
                args.putString("userId", userID);
                mapsFragment.setArguments(args);

                // Replace the current fragment with MapsFragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, mapsFragment); // Replace "R.id.fragment_container" with your actual container ID
                transaction.addToBackStack(null); // If you want to add this transaction to the back stack
                transaction.commit();
            }
        });


        // Add a click listener to the Report button
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle report button click here
                // You can implement the report functionality as needed
                // For example, open a dialog for reporting the place

                MapsFragmentReportDialog mapsFragmentReportDialog = new MapsFragmentReportDialog();

                if (getBundle != null) {
                    String userID = getBundle.getString("userId");
                    String placeName = getBundle.getString("placeName");

                    // Use Bundle to pass values
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", userID);
                    bundle.putString("placeName", placeName);
                    mapsFragmentReportDialog.setArguments(bundle);
                }

                mapsFragmentReportDialog.show(getChildFragmentManager(), "MapsFragmentReportDialog");
            }
        });
        }
        return view;
    }



}

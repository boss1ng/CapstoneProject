package com.example.qsee;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        // Re-enable the ImageButton
        ImageButton filterMenuBar = getParentFragment().getView().findViewById(R.id.filterMenuBar);
        filterMenuBar.setEnabled(true);

        /*
        BottomNavigationView bottomNavigationView = getParentFragment().getView().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setVisibility(View.GONE);

        LinearLayout linearLayout = getParentFragment().getView().findViewById(R.id.filterMenu);
        linearLayout.setVisibility(View.GONE);

        FragmentContainerView fragmentContainerView = getParentFragment().getView().findViewById(R.id.mapsFragment);
        fragmentContainerView.setVisibility(View.GONE);
        */

        /*
        ImageButton filterMenuBar = getParentFragment().getView().findViewById(R.id.filterMenuBar);
        filterMenuBar.setVisibility(View.VISIBLE);
        */
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
            String placeName = getBundle.getString("placeName");
            //Toast.makeText(getContext(), userID, Toast.LENGTH_LONG).show();
        }

        // Populate UI elements with place details
        TextView nameTextView = view.findViewById(R.id.placeNameTextView);
        TextView addressTextView = view.findViewById(R.id.placeAddressTextView);
        TextView descriptionTextView = view.findViewById(R.id.placeDescriptionTextView);
        TextView ratingTextView = view.findViewById(R.id.placeRatingTextView);
        Button directionsButton = view.findViewById(R.id.directionsButton);
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
        Double doubleRating = Double.parseDouble(placeRating);
        String placeLink = getArguments().getString("placeLink");

        String placePrice;
        if (getArguments().getString("placePrice").equals("-")) {
            priceTextView.setVisibility(View.INVISIBLE);
        }
        else {
            placePrice = getArguments().getString("placePrice");
            priceTextView.setText(placePrice);
        }

        Double currentUserLat = getArguments().getDouble("userLatitude");
        Double currentUserLong = getArguments().getDouble("userLongitude");
        String destinationLat = getArguments().getString("destinationLatitude");
        String destinationLong = getArguments().getString("destinationLongitude");

        // Toast.makeText(getContext(), String.valueOf(destinationLat), Toast.LENGTH_LONG).show();

        nameTextView.setText(placeName);
        addressTextView.setText(placeAddress);
        descriptionTextView.setText(placeDescription);
        // Format the rating to one decimal place
        DecimalFormat df = new DecimalFormat(doubleRating == 0 ? "0" : "#0.0");
        String formattedRating = df.format(doubleRating);
        ratingTextView.setText(String.valueOf(formattedRating));
        Picasso.get()
                .load(placeLink)
                .into(imageViewLocation);


        String isUserInQuezonCity = getArguments().getString("isUserInQuezonCity");
        //Toast.makeText(getContext(), isUserInQuezonCity, Toast.LENGTH_LONG).show();

        if (isUserInQuezonCity.equals("true"))
            directionsButton.setEnabled(true);
        else    // IF RESTRICTION TO QUEZON CITY IS REMOVED, change false to true.
            directionsButton.setEnabled(false);
        //directionsButton.setEnabled(false);


        // Add a click listener to the Directions button
        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss(); // Dismiss the dialog

                // In the fragment or activity where you want to navigate
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                MapsFragmentConfirmation fragmentConfirmation = new MapsFragmentConfirmation();

                if (getBundle != null) {
                    String userID = getBundle.getString("userId");

                    // Use Bundle to pass values
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", userID);
                    bundle.putString("placeName", placeName);
                    bundle.putDouble("userCurrentLatitude", currentUserLat);
                    bundle.putDouble("userCurrentLongitude", currentUserLong);
                    bundle.putString("destinationLatitude", destinationLat);
                    bundle.putString("destinationLongitude", destinationLong);
                    fragmentConfirmation.setArguments(bundle);
                }

                //ConstraintLayout constraintLayout = getParentFragment().getView().findViewById(R.id.fragment_container);
                //constraintLayout.setVisibility(View.GONE);

///*
                BottomNavigationView bottomNavigationView = getParentFragment().getView().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setVisibility(View.GONE);

                LinearLayout linearLayout = getParentFragment().getView().findViewById(R.id.filterMenu);
                linearLayout.setVisibility(View.GONE);

                FragmentContainerView fragmentContainerView = getParentFragment().getView().findViewById(R.id.mapsFragment);
                fragmentContainerView.setVisibility(View.GONE);
//*/

                // Replace the current fragment with the receiving fragment
                transaction.replace(R.id.fragment_container, fragmentConfirmation);
                transaction.addToBackStack(null);
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

        return view;
    }



}

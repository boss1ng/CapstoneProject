package com.example.qsee;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
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
import androidx.fragment.app.FragmentTransaction;

public class MapsFragmentArrivedDialog extends DialogFragment {


    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create a new Dialog instance
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Set a custom layout for the dialog
        dialog.setContentView(R.layout.fragment_maps_arrived_dialog);

        // Customize the width of the dialog (75% of screen width)
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 1);
        dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

        return dialog;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the custom layout for this dialog fragment
        View view = inflater.inflate(R.layout.fragment_maps_arrived_dialog, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String placeName = getBundle.getString("placeName");
            TextView textView = view.findViewById(R.id.placeNameTextView);
            textView.setText(placeName);
        }

        ImageView imageView = view.findViewById(R.id.imageMarker);
        imageView.setImageResource(R.drawable.red_marker);

        Button button = view.findViewById(R.id.btnDone);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss(); // Dismiss the dialog

                // In the fragment or activity where you want to navigate
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                MapsFragment mapsFragment = new MapsFragment();

                //BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
                //bottomNavigationView.setVisibility(View.GONE);

                /*
                LinearLayout llFilter = getView().findViewById(R.id.filterMenu);
                llFilter.setVisibility(View.GONE);

                LinearLayout llLoc = getView().findViewById(R.id.layoutLocation);
                llLoc.setVisibility(View.GONE);

                LinearLayout llButt = getView().findViewById(R.id.layoutButtons);
                llButt.setVisibility(View.GONE);
                 */

                // Replace the current fragment with the receiving fragment
                transaction.replace(R.id.fragment_container_arrived, mapsFragment);
                //transaction.remove(mapsFragmentArrived);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });


        return view;
    }

}

/*

    Okay na ang routing from [] to any BottomNavigationView
    1. MapsFragment -> PlaceDetailDialogFragment
    2. MapsFragment -> MapsFragmentConfirmation
    3. MapsFragment -> MapsFragmentConfirmation (Cancel)
    4. MapsFragment -> MapsFragmentConfirmation (Directions)
    5. MapsFragment -> MapsFragmentRoute
    6. MapsFragment -> FilterCategories
    7. DONE Routing (Back to MapsFragment) -> FilterCategories

    Hindi PA okay ang routing [] to any BottomNavigationView:
    8. MapsFragment -> MapsFragmentRoute -> BottomNavigationView
    9. DONE Routing (Back to MapsFragment) -> BottomNavigationView
    10. DONE Routing (Back to MapsFragment) -> FilterCategories -> BottomNavigationView

    Hindi na dapat mag "Qsee has stopped" if walang error sa
        BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setVisibility(View.GONE);
    Pero nireremove ko naman siya per page eh kaya gumagana sa 1-7. Ewan ko dito.



    About sa unstable connection, resulting to pause and not able to refresh anymore, check if Permission is included in the loop.

    Add Stop Routing.

 */
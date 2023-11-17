package com.example.qsee;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FilterCategories extends DialogFragment {

    private Bitmap screenshotMapView;

    ListView listView;
    CategoryAdapter adapter;

    public FilterCategories(Bitmap screenshotMap) {
        this.screenshotMapView = screenshotMap;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create a new Dialog instance
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Set a custom layout for the dialog
        dialog.setContentView(R.layout.fragment_filter_categories);

        // Customize the width of the dialog (75% of screen width)
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // Remove any margin
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_categories, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //String categoryName = "Accomodations+Shopping";

        //ImageView imageView = view.findViewById(R.id.screenshotRootView);
        //imageView.setImageBitmap(screenshotRootView);

        ImageView imageView1 = view.findViewById(R.id.screenshotMapView);
        imageView1.setImageBitmap(screenshotMapView);

        // Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            //Toast.makeText(getContext(), userID, Toast.LENGTH_LONG).show();
        }

        // For Reading the Database
        // Initialize Firebase Database reference
        // Reference to the "Location" node in Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Category");
        List categories = new ArrayList<>();
        listView = view.findViewById(R.id.listView);
        adapter = new CategoryAdapter(getContext(), categories);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        if (getBundle != null) {
            String categoryName = getBundle.getString("categoryName");

            if (categoryName != null) {
                //Toast.makeText(view.getContext(), categoryName, Toast.LENGTH_LONG).show();

                if (categoryName.contains("+")) {
                    Toast.makeText(view.getContext(), categoryName, Toast.LENGTH_LONG).show();

                    // Split the string by the '+' character
                    String[] preSelectedCategories = categoryName.split("\\+");

                    // Get the count of the resulting substrings
                    int numberOfCategories = preSelectedCategories.length;
                    String numCat = String.valueOf(numberOfCategories);
                    //Toast.makeText(view.getContext(), numCat, Toast.LENGTH_LONG).show();

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            categories.clear(); // Clear existing data
                            int position = 0; // Initialize position counter

                            for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                                String firebaseCategoryName = categorySnapshot.child("Category").getValue(String.class);
                                categories.add(firebaseCategoryName);

                                for (int i = 0; i < numberOfCategories; i++) {
                                    // Decide whether to check this item
                                    if (firebaseCategoryName.equals(preSelectedCategories[i])) {
                                        adapter.setItemChecked(position, true);
                                    }
                                }
                                position++;
                            }


                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle any errors here
                        }
                    });
                }

                else {
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            categories.clear(); // Clear existing data
                            int position = 0; // Initialize position counter

                            for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                                String firebaseCategoryName = categorySnapshot.child("Category").getValue(String.class);
                                categories.add(firebaseCategoryName);

                                // Decide whether to check this item
                                if (firebaseCategoryName.equals(categoryName)) {
                                    adapter.setItemChecked(position, true);
                                }
                                position++;
                            }
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle any errors here
                        }
                    });
                }
            }

            else {
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                            String categoryName = categorySnapshot.child("Category").getValue(String.class);
                            categories.add(categoryName);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle any errors here
                    }
                });
            }
        }

        else {

        }



/*
        // Customize the dialog's appearance and position
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            // window.setGravity(Gravity.TOP | Gravity.START);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            /
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT; // Adjust as needed
            layoutParams.gravity = Gravity.START; // Optional: Set gravity to your preference
            window.setAttributes(layoutParams);
             /
        }
*/

        ImageButton backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                // Assuming 'adapter' is an instance of CategoryAdapter
                ArrayList<String> selectedCategories = adapter.getCheckedItems();

                // Concatenate selected categories
                String concatenatedCategories = "";
                if (!selectedCategories.isEmpty()) {
                    concatenatedCategories = TextUtils.join("+", selectedCategories);
                }

                else
                    Log.d(TAG, "selectedCategories EMPTY");

                Log.d(TAG, "SELECTED CATEGORIES: " + concatenatedCategories);

                // Prepare the bundle to pass to the next fragment
                Bundle bundle = new Bundle();
                Bundle getBundle = getArguments();
                if (getBundle != null) {
                    String userID = getBundle.getString("userId");
                    bundle.putString("isVisited", "YES");
                    bundle.putString("userId", userID);
                    bundle.putString("categoryName", concatenatedCategories);
                }

                // Create and set arguments for the next fragment
                MapsFragment mapsFragment = new MapsFragment();
                mapsFragment.setArguments(bundle);

                // Replace the current fragment with the new fragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, mapsFragment);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });


        return view;
    }

}

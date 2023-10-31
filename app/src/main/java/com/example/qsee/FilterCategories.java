package com.example.qsee;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create a new Dialog instance
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Set a custom layout for the dialog
        dialog.setContentView(R.layout.fragment_filter_categories);

        // Customize the width of the dialog (75% of screen width)
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_categories, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //String categoryName = "Accomodations+Shopping";

        // Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            Toast.makeText(getContext(), userID, Toast.LENGTH_SHORT).show();
        }

        // For Reading the Database
        // Initialize Firebase Database reference
        // Reference to the "Location" node in Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Category");
        List categories = new ArrayList<>();
        ListView listView = view.findViewById(R.id.listView);
        ArrayAdapter<Object> adapter= new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice, categories);

        // Retrieve selected categories from Bundle arguments
        //Bundle getBundle = getArguments();
        if (getBundle != null) {
            String categoryName = getBundle.getString("categoryName");
            //Toast.makeText(view.getContext(), categoryName, Toast.LENGTH_SHORT).show();

            if (categoryName != null) {
                //Toast.makeText(view.getContext(), categoryName, Toast.LENGTH_SHORT).show();

                if (categoryName.contains("+")) {
                    //Toast.makeText(view.getContext(), categoryName, Toast.LENGTH_LONG).show();

                    // Split the string by the '+' character
                    String[] preSelectedCategories = categoryName.split("\\+");

                    // Get the count of the resulting substrings
                    int numberOfCategories = preSelectedCategories.length;
                    String numCat = String.valueOf(numberOfCategories);
                    Toast.makeText(view.getContext(), numCat, Toast.LENGTH_LONG).show();

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                                String categoryName = categorySnapshot.child("Category").getValue(String.class);
                                categories.add(categoryName);
                            }

                            // Now, you have the list of categories, you can display them as checkboxes.
                            ArrayAdapter<Object> adapter= new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice, categories);
                            listView.setAdapter(adapter);
                            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                            for (int i = 0; i < numberOfCategories; i++) {
                                // Autocheck items based on a condition
                                for (int y = 0; y < listView.getCount(); y++) {
                                    String item = (String) listView.getItemAtPosition(y);

                                    if (item != null && preSelectedCategories[i].equals(item)) {
                                        listView.setItemChecked(y, true);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle any errors here
                        }
                    });
                }

                else {
                    // Toast.makeText(view.getContext(), categoryName, Toast.LENGTH_SHORT).show();

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                                String categoryName = categorySnapshot.child("Category").getValue(String.class);
                                categories.add(categoryName);
                            }

                            // Now, you have the list of categories, you can display them as checkboxes.
                            ArrayAdapter<Object> adapter= new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice, categories);
                            listView.setAdapter(adapter);
                            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                            for (int y = 0; y < listView.getCount(); y++) {
                                String item = (String) listView.getItemAtPosition(y);

                                if (item != null && categoryName.equals(item)) {
                                    listView.setItemChecked(y, true);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle any errors here
                        }
                    });
                }
            }

            else {
                //Toast.makeText(view.getContext(), categoryName, Toast.LENGTH_LONG).show();
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                            String categoryName = categorySnapshot.child("Category").getValue(String.class);
                            categories.add(categoryName);
                        }

                        // Now, you have the list of categories, you can display them as checkboxes.
                        ArrayAdapter<Object> adapter= new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice, categories);
                        listView.setAdapter(adapter);
                        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
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

                    // Now, you have the list of categories, you can display them as checkboxes.
                    ArrayAdapter<Object> adapter= new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice, categories);
                    listView.setAdapter(adapter);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle any errors here
                }
            });
        }



        // Customize the dialog's appearance and position
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                // window.setGravity(Gravity.TOP | Gravity.START);
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

            /*
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT; // Adjust as needed
            layoutParams.gravity = Gravity.START; // Optional: Set gravity to your preference
            window.setAttributes(layoutParams);
             */
        }



        ImageButton backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  dismiss();

                  // In the fragment or activity where you want to navigate
                  FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                  MapsFragment mapsFragment = new MapsFragment();

                  // Retrieve the selected categories
                  SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                  ArrayList<String> selectedItems = new ArrayList<>();

                  // Concatenate selected categories
                  String selectedItem = "";

                  // Use Bundle to pass values
                  Bundle bundle = new Bundle();
                  for (int i = 0; i < checkedItems.size(); i++) {
                      int position = checkedItems.keyAt(i); // Get the position of the checked item
                      boolean isChecked = checkedItems.valueAt(i); // Get whether the item is checked

                      if (isChecked) {
                          String category = (String) categories.get(position); // Replace with your list of categories
                          //category += "+";
                          selectedItems.add(category); // Add selected category to the list
                      }
                  }

                  // Concatenate the selected categories into a single string
                  if (!selectedItems.isEmpty()) {
                      selectedItem = TextUtils.join("+", selectedItems);
                  }

                  // Retrieve selected categories from Bundle arguments
                  Bundle getBundle = getArguments();

                  if (getBundle != null) {
                      String userID = getBundle.getString("userId");
                      bundle.putString("isVisited", "YES");
                      bundle.putString("userId", userID);
                      bundle.putString("categoryName", selectedItem);
                      mapsFragment.setArguments(bundle);
                  }

                  // Replace the current fragment with the receiving fragment
                  transaction.replace(R.id.fragment_container, mapsFragment);
                  transaction.addToBackStack(null);
                  transaction.commit();

              }
        });

        return view;
    }

}

package com.example.qsee;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class AddGroupItineraryFragment extends Fragment {

    private LinearLayout dynamicFormContainer;
    private int formCount = 0;
    private int currentMaxFormCount = 5;
    private TextView dayView;
    private String userId;
    private RecyclerView recyclerView;
    private ItineraryAdapter itineraryAdapter;
    private List<View> formViews = new ArrayList<>();
    // Define a global variable to store the selected dates
    private List<String> selectedDates = new ArrayList<>();
    private List<String> locationsList;

    private String Itinerary1 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup1.png?alt=media&token=3bc087ae-5ed1-426f-a126-b09b35248dd0";
    private String Itinerary2 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup2.png?alt=media&token=8860d08b-40f6-4a56-942b-7cb2075304e3";
    private String Itinerary3 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup3.png?alt=media&token=75c15895-e0c7-40b3-b2d7-2830fc7398f2";
    private String Itinerary4 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup4.png?alt=media&token=200ad3a5-5b72-446d-8291-938673f1061c";
    private String Itinerary5 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup5.png?alt=media&token=0739f2d7-05f9-4ea3-96aa-a204a66a7beb";
    private String Itinerary6 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup6.png?alt=media&token=bd749ca2-c997-4302-bd14-d34600fc6b7c";
    private String Itinerary7 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup7.png?alt=media&token=54940844-fded-4278-94cc-0cedd98cfa90";
    private String Itinerary8 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup8.png?alt=media&token=f1534918-4510-48b4-aaa2-46d9b32f3b46";
    private String Itinerary9 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup9.png?alt=media&token=eb600104-3f9c-4b64-b407-1c0b9c22b5a2";
    private String Itinerary10 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup10.png?alt=media&token=a0a4b8d8-fbcf-4ee3-9f02-47924a417b92";
    private String Itinerary11 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup11.png?alt=media&token=4fa71607-d156-4440-b1d8-5803c7bca366";
    private String Itinerary12 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup12.png?alt=media&token=b8a99e22-76a3-48ce-b96f-556ed4014fc5";
    private String Itinerary13 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup13.png?alt=media&token=bfe1916a-3415-4c7e-bc7f-6abb89418eae";
    private String Itinerary14 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup14.png?alt=media&token=3e0f0fa1-84c1-466e-b947-b7c75599af6c";
    private String Itinerary15 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup15.png?alt=media&token=02646a6b-904a-400c-bf3f-7539e5297fdd";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_group_itinerary, container, false);
    }

    private String getRandomGroup() {
        // Create an array of your URLs
        String[] groupUrls = {Itinerary1, Itinerary2, Itinerary3, Itinerary4, Itinerary5, Itinerary6, Itinerary7, Itinerary8, Itinerary9, Itinerary10, Itinerary11, Itinerary12, Itinerary13, Itinerary14, Itinerary15};

        // Use Random to get a random index from the array
        Random random = new Random();
        int randomIndex = random.nextInt(groupUrls.length);

        // Return the randomly chosen URL
        return groupUrls[randomIndex];
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Retrieve the userId from the arguments
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getString("userId");
        }
        loadLocationsList();

        // Replace this with the actual reference to the TextInputLayout for the itinerary name
        TextInputLayout itineraryNameTextInput = view.findViewById(R.id.textInputLayout);
        TextInputLayout groupNameTextInput = view.findViewById(R.id.textInputLayout6);

        // Find the AutoCompleteTextView for the group name
        AutoCompleteTextView groupNameAutoComplete = (AutoCompleteTextView) groupNameTextInput.getEditText();

        // Query the "Groups" node in Firebase to find the group the user belongs to
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> groupNames = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (int i = 1; i <= 50; i++) { // Assuming there are up to 50 members
                        String member = snapshot.child("member" + i).getValue(String.class);
                        if (member != null && member.equals(userId)) {
                            // Assuming the group name is stored as a child of each group node
                            String groupName = snapshot.child("groupName").getValue(String.class);
                            if (groupName != null) {
                                groupNames.add(groupName);
                            }
                            break;
                        }
                    }
                }

                // Set up the AutoCompleteTextView adapter with the retrieved group names
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, groupNames);
                groupNameAutoComplete.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that may occur during the database operation
                Log.e("FirebaseError", "Error retrieving group information: " + databaseError.getMessage());
            }
        });


        dynamicFormContainer = view.findViewById(R.id.dynamicFormContainer);
        dayView = view.findViewById(R.id.dayView);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference itineraryRef = database.getReference("Itinerary");

        // Initialize the RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView); // Make sure to replace R.id.recyclerView with the actual ID of your RecyclerView in the XML layout
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the itineraryAdapter
        itineraryAdapter = new ItineraryAdapter();

        // Set the itineraryAdapter to the RecyclerView
        recyclerView.setAdapter(itineraryAdapter);


        // Define a local TextInputLayout variable
        TextInputLayout dateInput = view.findViewById(R.id.dateInput);
        EditText dateEditText = dateInput.getEditText();
        if (dateEditText != null) {
            dateEditText.setOnClickListener(v -> {
                int position = formViews.indexOf(view); // Get the index of the current form
                Calendar minDate = Calendar.getInstance();
                if (position == -1){
                    selectedDates.clear();
                }
                if (position > 0 && selectedDates.size() > position - 1) {
                    String previousDate = selectedDates.get(position - 1);
                    String[] parts = previousDate.split("-");
                    minDate.set(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[0]));
                    minDate.add(Calendar.DAY_OF_MONTH, 1); // Add one day to the minimum date
                }
                showDatePicker(dateInput, minDate, position); // Pass the local dateInput variable, the minimum date, and the position
            });
        }

        // Add RecyclerView for each day
        @SuppressLint("CutPasteId") RecyclerView dayRecyclerView = view.findViewById(R.id.recyclerView);
        dayRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ItineraryAdapter dayItineraryAdapter = new ItineraryAdapter();
        dayRecyclerView.setAdapter(dayItineraryAdapter);
        ItineraryItem defaultItem = new ItineraryItem("", "", "");
        dayItineraryAdapter.addItem(defaultItem);
        ImageView addActivityButton = view.findViewById(R.id.addActivity);
        ImageView deleteActivityButton = view.findViewById(R.id.remActivity);

        final int[] addCounter = {0};

        addActivityButton.setOnClickListener(v -> {
            if (addCounter[0] < 2) {
                ItineraryItem newItem = new ItineraryItem("", "", ""); // Create a new ItineraryItem
                dayItineraryAdapter.addItem(newItem); // Add the new item to the RecyclerView
                addCounter[0]++;
            } else {
                Toast.makeText(getContext(), "Maximum activities for a day reached.\nYou can add more in the edit option later.", Toast.LENGTH_LONG).show();
            }
        });

        deleteActivityButton.setOnClickListener(v -> {
            if (addCounter[0] > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you want to delete an activity?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    int lastIndex = dayItineraryAdapter.getItemCount() - 1;
                    if (lastIndex >= 0) {
                        dayItineraryAdapter.removeItem(lastIndex);
                        addCounter[0]--;
                    }
                });
                builder.setNegativeButton("No", (dialog, which) -> {
                    // If user cancels the deletion, dismiss the dialog
                    dialog.dismiss();
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });




        // Find the "Add Itinerary" button in the default form layout
        ImageView addItineraryButton = view.findViewById(R.id.addItinerary);
        if (addItineraryButton != null) {
            addItineraryButton.setOnClickListener(v -> {
                if (formCount < 5) {
                    formCount++;
                    addNewForm(formCount); // Increment the day count by 1
                    if (formCount == 4) {
                        addItineraryButton.setVisibility(View.GONE); // Hide the "Add Itinerary" button if formCount reaches 5
                        Toast.makeText(getContext(), "Maximum number of days reached", Toast.LENGTH_LONG).show(); // Show a toast when the maximum number of days is reached
                    }
                    if (formCount > 0) {
                        ImageButton deleteButton = view.findViewById(R.id.deleteForm);
                        deleteButton.setVisibility(View.GONE);
                        addItineraryButton.setVisibility(View.GONE);
                        dayView.setText("Day 1 of " + (formCount+1));
                    }
                }
            });
        }
        else {
            Log.e("AddItineraryFragment", "addItinerary ImageView not found");
        }
        ImageButton deleteButton = view.findViewById(R.id.deleteForm);
        deleteButton.setOnClickListener(v -> {
            int index = formViews.size() - 1; // Index of the latest form view
            if (index >= 0) {
                dynamicFormContainer.removeView(formViews.get(index));
                formViews.remove(index);
                formCount--;
                currentMaxFormCount = formCount; // Update the current maximum form count
                if (formCount > 0) {
                    dayView.setText("Day 1 of " + (formCount + 1));
                    assert addItineraryButton != null;
                    addItineraryButton.setVisibility(View.VISIBLE);
                }
                else {
                    dayView.setText("Day 1 of 1");
                }

                if (formCount <= 0) {
                    deleteButton.setVisibility(View.GONE);
                    assert addItineraryButton != null;
                    addItineraryButton.setVisibility(View.VISIBLE);
                }

                // Update the day count for each remaining form
                for (int i = 0; i < formViews.size(); i++) {
                    View formView = formViews.get(i);
                    TextView formDayView = formView.findViewById(R.id.dayView);
                    String currentDayCount = formDayView.getText().toString();
                    int currentDay = Integer.parseInt(currentDayCount.split(" ")[1]);
                    formDayView.setText("Day " + (currentDay) + " of " + (currentMaxFormCount+1));
                }
            }
        });


        Button cancelButton = view.findViewById(R.id.cancelBt);
        cancelButton.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack();
        });

        Button submitItineraryButton = view.findViewById(R.id.submit_itinerary);

        submitItineraryButton.setOnClickListener(v -> {
            String itineraryName = Objects.requireNonNull(itineraryNameTextInput.getEditText()).getText().toString();
            String groupName = Objects.requireNonNull(groupNameTextInput.getEditText().getText().toString());
            groupsRef.child(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

            if (itineraryName.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields for Day 1", Toast.LENGTH_LONG).show();
            } else {
                itineraryRef.orderByChild("iterName").equalTo(itineraryName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(getContext(), "Itinerary name already exists. Please use a different name.", Toast.LENGTH_LONG).show();
                        } else {
                            boolean allFormsFilled = true;
                            RecyclerView defaultDayRecyclerView = view.findViewById(R.id.recyclerView);
                            LinearLayoutManager defaultLayoutManager = (LinearLayoutManager) defaultDayRecyclerView.getLayoutManager();
                            // Validation for default form
                            TextInputLayout defaultDateInputLayout = view.findViewById(R.id.dateInput);
                            String defaultDate = Objects.requireNonNull(defaultDateInputLayout.getEditText()).getText().toString();


                            if (defaultDate.isEmpty()) {
                                Toast.makeText(getContext(), "Please fill in all fields for Day 1", Toast.LENGTH_LONG).show();
                                allFormsFilled = false;
                            } else {
                                for (int j = 0; j < 5; j++) {
                                    assert defaultLayoutManager != null;
                                    View itemView = defaultLayoutManager.getChildAt(j);

                                    if (itemView != null) {
                                        TextInputLayout timeInputLayout = itemView.findViewById(R.id.timeInput);
                                        TextInputLayout activityInputLayout = itemView.findViewById(R.id.activityInput);
                                        TextInputLayout locationInputLayout = itemView.findViewById(R.id.locationInput);
                                        TextInputLayout originInputLayout = itemView.findViewById(R.id.originInput);

                                        String time = Objects.requireNonNull(timeInputLayout.getEditText()).getText().toString();
                                        String activity = Objects.requireNonNull(activityInputLayout.getEditText()).getText().toString();
                                        String location = Objects.requireNonNull(locationInputLayout.getEditText()).getText().toString();
                                        String origin = Objects.requireNonNull(originInputLayout.getEditText()).getText().toString();

                                        if (time.isEmpty() || activity.isEmpty() || location.isEmpty() || origin.isEmpty()) {
                                            Toast.makeText(getContext(), "Please fill in all fields for Day 1", Toast.LENGTH_LONG).show();
                                            allFormsFilled = false;
                                            break;
                                        }
                                        else if (validateLocationsInAllForms(defaultDayRecyclerView)) {
                                            continue;
                                        } else {
                                            allFormsFilled = false;
                                            break;
                                        }
                                    }
                                }
                            }

                            // Check forms for other days
                            for (int i = 0; i < formViews.size(); i++) {
                                View currentFormView = formViews.get(i);
                                TextInputLayout dateInputLayout = currentFormView.findViewById(R.id.dateInput);
                                String date = Objects.requireNonNull(dateInputLayout.getEditText()).getText().toString();

                                if (date.isEmpty()) {
                                    Toast.makeText(getContext(), "Please fill in all fields for Day " + (i + 2), Toast.LENGTH_LONG).show();
                                    allFormsFilled = false;
                                    break;
                                }

                                RecyclerView currentDayRecyclerView = currentFormView.findViewById(R.id.recyclerView);
                                LinearLayoutManager layoutManager = (LinearLayoutManager) currentDayRecyclerView.getLayoutManager();

                                for (int j = 0; j < Objects.requireNonNull(layoutManager).getChildCount(); j++) {
                                    View itemView = layoutManager.getChildAt(j);

                                    if (itemView != null) {
                                        TextInputLayout timeInputLayout = itemView.findViewById(R.id.timeInput);
                                        TextInputLayout activityInputLayout = itemView.findViewById(R.id.activityInput);
                                        TextInputLayout locationInputLayout = itemView.findViewById(R.id.locationInput);
                                        TextInputLayout originInputLayout = itemView.findViewById(R.id.originInput);

                                        String time = Objects.requireNonNull(timeInputLayout.getEditText()).getText().toString();
                                        String activity = Objects.requireNonNull(activityInputLayout.getEditText()).getText().toString();
                                        String location = Objects.requireNonNull(locationInputLayout.getEditText()).getText().toString();
                                        String origin = Objects.requireNonNull(originInputLayout.getEditText()).getText().toString();

                                        if (time.isEmpty() || activity.isEmpty() || location.isEmpty() || origin.isEmpty()) {
                                            Toast.makeText(getContext(), "Please fill in all fields for Day " + (i + 2), Toast.LENGTH_LONG).show();
                                            allFormsFilled = false;
                                            break;
                                        }
                                        else if (validateLocationsInAllForms(currentDayRecyclerView)) {
                                            continue;
                                        } else {
                                            allFormsFilled = false;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (allFormsFilled) {
                                DatabaseReference userRef = itineraryRef.child(itineraryName);
                                userRef.child("admin").setValue(userId);
                                userRef.child("iterName").setValue(itineraryName);
                                userRef.child("groupName").setValue(groupName);

                                // Image of the Group
                                // Call getRandomGroup to get a random URL among 15 choices
                                String randomUrl = getRandomGroup();
                                userRef.child("IterPhoto").setValue(randomUrl);

                                // Saving logic for default form
                                DatabaseReference defaultDayRef = userRef.child("Day1");
                                defaultDayRef.child("date").setValue(defaultDate);
                                for (int j = 0; j < Objects.requireNonNull(defaultLayoutManager).getChildCount(); j++) {
                                    View itemView = Objects.requireNonNull(defaultLayoutManager).getChildAt(j);

                                    if (itemView != null) {
                                        TextInputLayout timeInputLayout = itemView.findViewById(R.id.timeInput);
                                        TextInputLayout activityInputLayout = itemView.findViewById(R.id.activityInput);
                                        TextInputLayout locationInputLayout = itemView.findViewById(R.id.locationInput);
                                        TextInputLayout originInputLayout = itemView.findViewById(R.id.originInput);

                                        String standardTime = Objects.requireNonNull(timeInputLayout.getEditText()).getText().toString();
                                        String militaryTime = convertToMilitaryTime(standardTime); // convert to military time

                                        String time = Objects.requireNonNull(timeInputLayout.getEditText()).getText().toString();
                                        String activity = Objects.requireNonNull(activityInputLayout.getEditText()).getText().toString();
                                        String location = Objects.requireNonNull(locationInputLayout.getEditText()).getText().toString();
                                        String origin = Objects.requireNonNull(originInputLayout.getEditText()).getText().toString();

                                        DatabaseReference itemRef = defaultDayRef.child(militaryTime);
                                        itemRef.child("status").setValue("incomplete");
                                        itemRef.child("activity").setValue(activity);
                                        itemRef.child("location").setValue(location);
                                        itemRef.child("origin").setValue(origin);

                                        DatabaseReference originReference = FirebaseDatabase.getInstance().getReference("Location");
                                        originReference.orderByChild("Location").equalTo(origin).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                                                    // Retrieve latitude and longitude for the origin
                                                    String firebaseOrigLat = locationSnapshot.child("Latitude").getValue(String.class);
                                                    String firebaseOrigLong = locationSnapshot.child("Longitude").getValue(String.class);

                                                    itemRef.child("originLat").setValue(firebaseOrigLat);
                                                    itemRef.child("originLong").setValue(firebaseOrigLong);
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                        DatabaseReference destReference = FirebaseDatabase.getInstance().getReference("Location");
                                        destReference.orderByChild("Location").equalTo(location).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                                                    // Retrieve latitude and longitude for the origin
                                                    String firebaseOrigLat = locationSnapshot.child("Latitude").getValue(String.class);
                                                    String firebaseOrigLong = locationSnapshot.child("Longitude").getValue(String.class);

                                                    itemRef.child("locationLat").setValue(firebaseOrigLat);
                                                    itemRef.child("locationLong").setValue(firebaseOrigLong);
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }

                                // Saving logic for days beyond Day 1
                                for (int i = 0; i < formViews.size(); i++) {
                                    View currentFormView = formViews.get(i);
                                    TextInputLayout dateInputLayout = currentFormView.findViewById(R.id.dateInput);
                                    String date = Objects.requireNonNull(dateInputLayout.getEditText()).getText().toString();

                                    RecyclerView currentDayRecyclerView = currentFormView.findViewById(R.id.recyclerView);
                                    LinearLayoutManager layoutManager = (LinearLayoutManager) currentDayRecyclerView.getLayoutManager();

                                    for (int j = 0; j < Objects.requireNonNull(layoutManager).getChildCount(); j++) {
                                        View itemView = layoutManager.getChildAt(j);

                                        if (itemView != null) {
                                            TextInputLayout timeInputLayout = itemView.findViewById(R.id.timeInput);
                                            TextInputLayout activityInputLayout = itemView.findViewById(R.id.activityInput);
                                            TextInputLayout locationInputLayout = itemView.findViewById(R.id.locationInput);
                                            TextInputLayout originInputLayout = itemView.findViewById(R.id.originInput);

                                            String standardTime = Objects.requireNonNull(timeInputLayout.getEditText()).getText().toString();
                                            String militaryTime = convertToMilitaryTime(standardTime); // convert to military time

                                            String activity = Objects.requireNonNull(activityInputLayout.getEditText()).getText().toString();
                                            String location = Objects.requireNonNull(locationInputLayout.getEditText()).getText().toString();
                                            String origin = Objects.requireNonNull(originInputLayout.getEditText()).getText().toString();

                                            DatabaseReference dayRef = userRef.child("Day" + (i + 2));
                                            dayRef.child("date").setValue(date);
                                            DatabaseReference itemRef = dayRef.child(militaryTime);
                                            itemRef.child("status").setValue("incomplete");
                                            itemRef.child("activity").setValue(activity);
                                            itemRef.child("location").setValue(location);
                                            itemRef.child("origin").setValue(origin);

                                            DatabaseReference originReference = FirebaseDatabase.getInstance().getReference("Location");
                                            originReference.orderByChild("Location").equalTo(origin).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                                                        // Retrieve latitude and longitude for the origin
                                                        String firebaseOrigLat = locationSnapshot.child("Latitude").getValue(String.class);
                                                        String firebaseOrigLong = locationSnapshot.child("Longitude").getValue(String.class);

                                                        itemRef.child("originLat").setValue(firebaseOrigLat);
                                                        itemRef.child("originLong").setValue(firebaseOrigLong);
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                            DatabaseReference destReference = FirebaseDatabase.getInstance().getReference("Location");
                                            destReference.orderByChild("Location").equalTo(location).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                                                        // Retrieve latitude and longitude for the origin
                                                        String firebaseOrigLat = locationSnapshot.child("Latitude").getValue(String.class);
                                                        String firebaseOrigLong = locationSnapshot.child("Longitude").getValue(String.class);

                                                        itemRef.child("locationLat").setValue(firebaseOrigLat);
                                                        itemRef.child("locationLong").setValue(firebaseOrigLong);
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    }
                                }

                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                try {
                                    Date date = sdf.parse(defaultDate);
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(date);

                                    Intent intent = new Intent(Intent.ACTION_INSERT)
                                            .setData(CalendarContract.Events.CONTENT_URI)
                                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.getTimeInMillis())
                                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.getTimeInMillis() + 60 * 60 * 1000) // 1 hour
                                            .putExtra(CalendarContract.Events.TITLE, itineraryName)
                                            .putExtra(CalendarContract.Events.ALL_DAY, true);

                                    startActivityForResult(intent, 1);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                Toast.makeText(getContext(), "Itinerary Saved.", Toast.LENGTH_LONG).show();
                                FragmentManager fragmentManager = getParentFragmentManager();
                                fragmentManager.popBackStack();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
                    } else {
                        Toast.makeText(getContext(), "Group with the name " + groupName + " does not exist.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle potential errors that may occur during the database query
                    Log.e("GroupCheck", "Error checking group name: " + databaseError.getMessage());
                }
            });
        });
    }

    private String convertToMilitaryTime(String standardTime) {
        try {
            SimpleDateFormat standardFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            Date date = standardFormat.parse(standardTime);
            if (date != null) {
                SimpleDateFormat militaryFormat = new SimpleDateFormat("HH:mm", Locale.US);
                return militaryFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ""; // Return empty string if conversion fails
    }


    @SuppressLint("SetTextI18n")
    private void addNewForm(int dayCount) {
        final int[] currentMaxFormCount = {formCount + 1};
        if (dayCount < currentMaxFormCount[0]) {
            View newFormView = LayoutInflater.from(getContext()).inflate(R.layout.dynamic_form_layout, dynamicFormContainer, false);

            // Find the dayView in the new form layout
            TextView dayView = newFormView.findViewById(R.id.dayView);

            if (dayCount > 0) {
                // Set the day count with proper numbering
                String dayCountText = "Day " + (dayCount + 1) + " of " + currentMaxFormCount[0];
                dayView.setText(dayCountText);
            } else {
                // Update the dayView for the default form
                dayView.setText("Day 1 of " + currentMaxFormCount[0]);
            }

            ImageButton deleteButton = newFormView.findViewById(R.id.deleteForm);
            ImageView addItineraryButton = newFormView.findViewById(R.id.addItinerary);

            // Set the delete button visible on the new form
            deleteButton.setVisibility(View.VISIBLE);

            // Check if the day count has reached 5 to hide the addItineraryButton
            if (dayCount == 4) {
                addItineraryButton.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Maximum number of days reached", Toast.LENGTH_LONG).show();
            }


            TextInputLayout dateInput = newFormView.findViewById(R.id.dateInput);
            EditText dateEditText = dateInput.getEditText();
            if (dateEditText != null) {
                dateEditText.setOnClickListener(v -> {
                    int position = formViews.indexOf(newFormView); // Get the index of the current form
                    Calendar minDate = Calendar.getInstance();
                    if (position > 0 && selectedDates.size() > position - 1) {
                        String previousDate = selectedDates.get(position - 1);
                        String[] parts = previousDate.split("-");
                        minDate.set(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[0]));
                        minDate.add(Calendar.DAY_OF_MONTH, 1); // Add one day to the minimum date
                    }
                    showDatePicker(dateInput, minDate, position); // Pass the local dateInput variable, the minimum date, and the position
                });
            }

            addItineraryButton.setOnClickListener(v -> {
                if (formCount < 5) {
                    formCount++;
                    addNewForm(formCount); // Increment the day count by 1
                    updateDayView(formCount);
                    if (formCount > 0) {
                        // Update the dayView
                        dayView.setText("Day " + (dayCount+1) + " of " + (currentMaxFormCount[0] + 1));
                    }
                }
            });
            deleteButton.setOnClickListener(v -> {
                int index = formViews.indexOf(newFormView);
                if (index >= 0) {
                    // Remove the corresponding date from the selectedDates list
                    if (index < selectedDates.size()) {
                        selectedDates.remove(index);
                    }
                    dynamicFormContainer.removeView(newFormView);
                    formViews.remove(index);
                    formCount--;
                    updateDayView(formCount);
                    currentMaxFormCount[0] = formCount;

                    // Update the day count for each form after deletion
                    for (int i = 0; i < formViews.size(); i++) {
                        View formView = formViews.get(i);
                        TextView formDayView = formView.findViewById(R.id.dayView);
                        String currentDayCount = formDayView.getText().toString();
                        int currentDay = Integer.parseInt(currentDayCount.split(" ")[1]);
                        formDayView.setText("Day " + (currentDay ) + " of " + (currentMaxFormCount[0] +1));
                        dayView.setText("Day 1 of " + currentMaxFormCount[0]);
                    }

                    // Show the buttons in the previous day's view
                    if (index - 1 >= 0) {
                        View previousView = formViews.get(index - 1);
                        ImageView addItineraryBtn = previousView.findViewById(R.id.addItinerary);
                        ImageButton deleteBtn = previousView.findViewById(R.id.deleteForm);
                        addItineraryBtn.setVisibility(View.VISIBLE);
                        deleteBtn.setVisibility(View.VISIBLE);
                    }
                    if(formCount==0){
                        addItineraryVisible();
                    }
                }
            });




            RecyclerView dayRecyclerView = newFormView.findViewById(R.id.recyclerView);
            dayRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            ItineraryAdapter dayItineraryAdapter = new ItineraryAdapter();
            dayRecyclerView.setAdapter(dayItineraryAdapter);
            ItineraryItem defaultItem = new ItineraryItem("", "", "");
            dayItineraryAdapter.addItem(defaultItem);
            ImageView addActivityButton = newFormView.findViewById(R.id.addActivity);
            ImageView deleteActivityButton = newFormView.findViewById(R.id.remActivity);

            final int[] addCounter = {0};

            addActivityButton.setOnClickListener(v -> {
                if (addCounter[0] < 2) {
                    ItineraryItem newItem = new ItineraryItem("", "", ""); // Create a new ItineraryItem
                    dayItineraryAdapter.addItem(newItem); // Add the new item to the RecyclerView
                    addCounter[0]++;
                } else {
                    Toast.makeText(getContext(), "Maximum activities for a day reached.\nYou can add more in the edit option later.", Toast.LENGTH_LONG).show();
                }
            });

            deleteActivityButton.setOnClickListener(v -> {
                if (addCounter[0] > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("Are you sure you want to delete an activity?");
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        int lastIndex = dayItineraryAdapter.getItemCount() - 1;
                        if (lastIndex >= 0) {
                            dayItineraryAdapter.removeItem(lastIndex);
                            addCounter[0]--;
                        }
                    });
                    builder.setNegativeButton("No", (dialog, which) -> {
                        // If user cancels the deletion, dismiss the dialog
                        dialog.dismiss();
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });


            dynamicFormContainer.addView(newFormView);
            formViews.add(newFormView);

            // Hide the buttons for previous forms
            for (int i = 0; i < formViews.size() - 1; i++) {
                View formView = formViews.get(i);
                ImageView addItineraryBtn = formView.findViewById(R.id.addItinerary);
                ImageButton deleteBtn = formView.findViewById(R.id.deleteForm);
                addItineraryBtn.setVisibility(View.GONE);
                deleteBtn.setVisibility(View.GONE);
            }

            // Update the day count for each existing form
            for (int i = 0; i < formViews.size(); i++) {
                View formView = formViews.get(i);
                TextView formDayView = formView.findViewById(R.id.dayView);
                String currentDayCount = formDayView.getText().toString();
                int currentDay = Integer.parseInt(currentDayCount.split(" ")[1]);
                formDayView.setText("Day " + currentDay + " of " + currentMaxFormCount[0]);
            }
            // Scroll to the newly added form
            newFormView.getParent().requestChildFocus(newFormView, newFormView);
        }
    }


    private void showDatePicker(TextInputLayout textInputLayout, final Calendar minDate, final int formIndex) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int monthOfYear, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + selectedYear;
                        boolean dateAllowed = true;
                        if (textInputLayout.getEditText() != null) {
                            for (String date : selectedDates) {
                                String[] parts = date.split("-");
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[0]));
                                if (selectedYear < calendar.get(Calendar.YEAR) ||
                                        (selectedYear == calendar.get(Calendar.YEAR) && monthOfYear < calendar.get(Calendar.MONTH)) ||
                                        (selectedYear == calendar.get(Calendar.YEAR) && monthOfYear == calendar.get(Calendar.MONTH) && dayOfMonth <= calendar.get(Calendar.DAY_OF_MONTH))) {
                                    Toast.makeText(getContext(), "Please select a date after " + date, Toast.LENGTH_LONG).show();
                                    dateAllowed = false;
                                    break;
                                }
                            }
                            if (dateAllowed) {
                                textInputLayout.getEditText().setText(selectedDate);
                                if (formIndex >= 0 && selectedDates.size() > formIndex) {
                                    // If the selected date list already contains the index, update the value
                                    selectedDates.set(formIndex, selectedDate);
                                } else {
                                    // If the selected date list does not contain the index, add the date to the list
                                    selectedDates.add(selectedDate);
                                }

                                // Automatically pre-select the next day if it exists
                                if (formIndex < formViews.size() - 1) {
                                    View nextFormView = formViews.get(formIndex + 1);
                                    TextInputLayout nextDateInputLayout = nextFormView.findViewById(R.id.dateInput);
                                    EditText nextDateEditText = nextDateInputLayout.getEditText();
                                    if (nextDateEditText != null) {
                                        Calendar nextDay = Calendar.getInstance();
                                        nextDay.set(selectedYear, monthOfYear, dayOfMonth + 1); // Setting the next day
                                        nextDateEditText.setText((nextDay.get(Calendar.DAY_OF_MONTH)) + "-"
                                                + (nextDay.get(Calendar.MONTH) + 1) + "-"
                                                + nextDay.get(Calendar.YEAR));
                                    }
                                }
                            }
                        }
                    }
                }, year, month, day);

        if (minDate != null) {
            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        }

        datePickerDialog.show();
    }



    @SuppressLint("SetTextI18n")
    private void updateDayView(int formCount) {
        if (formCount > 0) {
            dayView.setText("Day 1 of " + (formCount + 1));
        } else {
            dayView.setText("Day 1 of 1");
        }
    }

    private void addItineraryVisible() {
        ImageView addItineraryButton = requireView().findViewById(R.id.addItinerary); // Change this to the ID of your add itinerary button
        if (formCount == 0) {
            addItineraryButton.setVisibility(View.VISIBLE);
        } else {
            addItineraryButton.setVisibility(View.GONE);
        }
    }
    private void loadLocationsList() {
        DatabaseReference locationsRef = FirebaseDatabase.getInstance().getReference().child("Location");
        locationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                locationsList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String location = snapshot.child("Location").getValue(String.class);
                    locationsList.add(location);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }
    private boolean validateLocationsInAllForms(View view) {
        // Check default form
        if (!validateFormLocations(view.findViewById(R.id.recyclerView))) {
            return false;
        }

        // Check all dynamic forms
        for (View formView : formViews) {
            RecyclerView dayRecyclerView = formView.findViewById(R.id.recyclerView);
            if (!validateFormLocations(dayRecyclerView)) {
                return false;
            }
        }

        return true;
    }

    private boolean validateFormLocations(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        for (int i = 0; i < layoutManager.getItemCount(); i++) {
            View itemView = layoutManager.getChildAt(i);
            if (itemView != null) {
                TextInputLayout locationInputLayout = itemView.findViewById(R.id.locationInput);
                TextInputLayout originInputLayout = itemView.findViewById(R.id.originInput);

                String location = Objects.requireNonNull(locationInputLayout.getEditText()).getText().toString();
                String origin = Objects.requireNonNull(originInputLayout.getEditText()).getText().toString();

                if (!locationsList.contains(location) || !locationsList.contains(origin)) {
                    Toast.makeText(getContext(), "Invalid origin or destination in one of the forms", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }
        return true;
    }


}
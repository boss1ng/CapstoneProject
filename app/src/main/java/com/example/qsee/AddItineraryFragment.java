package com.example.qsee;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class AddItineraryFragment extends Fragment {

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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_itinerary, container, false);
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

        // Replace this with the actual reference to the TextInputLayout for the itinerary name
        TextInputLayout itineraryNameTextInput = view.findViewById(R.id.textInputLayout);

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
            if (addCounter[0] < 3) {
                ItineraryItem newItem = new ItineraryItem("", "", ""); // Create a new ItineraryItem
                dayItineraryAdapter.addItem(newItem); // Add the new item to the RecyclerView
                addCounter[0]++;
            } else {
                Toast.makeText(getContext(), "Maximum activities for a day reached.\nYou can add more in the edit option later.", Toast.LENGTH_SHORT).show();
            }
        });

        deleteActivityButton.setOnClickListener(v -> {
            if (addCounter[0] > 0) {
                int lastIndex = dayItineraryAdapter.getItemCount() - 1;
                if (lastIndex >= 0) {
                    dayItineraryAdapter.removeItem(lastIndex);
                    addCounter[0]--;
                }
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
                        Toast.makeText(getContext(), "Maximum number of days reached", Toast.LENGTH_SHORT).show(); // Show a toast when the maximum number of days is reached
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

            if (itineraryName.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields for Day 1", Toast.LENGTH_SHORT).show();
            } else {
                itineraryRef.orderByChild("iterName").equalTo(itineraryName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(getContext(), "Itinerary name already exists. Please use a different name.", Toast.LENGTH_SHORT).show();
                        } else {
                            boolean allFormsFilled = true;
                            RecyclerView defaultDayRecyclerView = view.findViewById(R.id.recyclerView);
                            LinearLayoutManager defaultLayoutManager = (LinearLayoutManager) defaultDayRecyclerView.getLayoutManager();
                            // Validation for default form
                            TextInputLayout defaultDateInputLayout = view.findViewById(R.id.dateInput);
                            String defaultDate = Objects.requireNonNull(defaultDateInputLayout.getEditText()).getText().toString();

                            if (defaultDate.isEmpty()) {
                                Toast.makeText(getContext(), "Please fill in all fields for Day 1", Toast.LENGTH_SHORT).show();
                                allFormsFilled = false;
                            } else {
                                for (int j = 0; j < 5; j++) {
                                    assert defaultLayoutManager != null;
                                    View itemView = defaultLayoutManager.getChildAt(j);
                                    Log.d("Kinuha ko ang","eto" + defaultLayoutManager.getChildAt(j));

                                    if (itemView != null) {
                                        TextInputLayout timeInputLayout = itemView.findViewById(R.id.timeInput);
                                        TextInputLayout activityInputLayout = itemView.findViewById(R.id.activityInput);
                                        TextInputLayout locationInputLayout = itemView.findViewById(R.id.locationInput);

                                        String time = Objects.requireNonNull(timeInputLayout.getEditText()).getText().toString();
                                        String activity = Objects.requireNonNull(activityInputLayout.getEditText()).getText().toString();
                                        String location = Objects.requireNonNull(locationInputLayout.getEditText()).getText().toString();

                                        if (time.isEmpty() || activity.isEmpty() || location.isEmpty()) {
                                            Toast.makeText(getContext(), "Please fill in all fields for Day 1", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(getContext(), "Please fill in all fields for Day " + (i + 2), Toast.LENGTH_SHORT).show();
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

                                        String time = Objects.requireNonNull(timeInputLayout.getEditText()).getText().toString();
                                        String activity = Objects.requireNonNull(activityInputLayout.getEditText()).getText().toString();
                                        String location = Objects.requireNonNull(locationInputLayout.getEditText()).getText().toString();

                                        if (time.isEmpty() || activity.isEmpty() || location.isEmpty()) {
                                            Toast.makeText(getContext(), "Please fill in all fields for Day " + (i + 2), Toast.LENGTH_SHORT).show();
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

                                // Saving logic for default form
                                DatabaseReference defaultDayRef = userRef.child("Day1");
                                defaultDayRef.child("date").setValue(defaultDate);
                                for (int j = 0; j < Objects.requireNonNull(defaultLayoutManager).getChildCount(); j++) {
                                    View itemView = Objects.requireNonNull(defaultLayoutManager).getChildAt(j);

                                    if (itemView != null) {
                                        TextInputLayout timeInputLayout = itemView.findViewById(R.id.timeInput);
                                        TextInputLayout activityInputLayout = itemView.findViewById(R.id.activityInput);
                                        TextInputLayout locationInputLayout = itemView.findViewById(R.id.locationInput);

                                        String standardTime = Objects.requireNonNull(timeInputLayout.getEditText()).getText().toString();
                                        String militaryTime = convertToMilitaryTime(standardTime); // convert to military time

                                        String time = Objects.requireNonNull(timeInputLayout.getEditText()).getText().toString();
                                        String activity = Objects.requireNonNull(activityInputLayout.getEditText()).getText().toString();
                                        String location = Objects.requireNonNull(locationInputLayout.getEditText()).getText().toString();

                                        DatabaseReference itemRef = defaultDayRef.child(militaryTime);
                                        itemRef.child("status").setValue("incomplete");
                                        itemRef.child("activity").setValue(activity);
                                        itemRef.child("location").setValue(location);
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

                                            String standardTime = Objects.requireNonNull(timeInputLayout.getEditText()).getText().toString();
                                            String militaryTime = convertToMilitaryTime(standardTime); // convert to military time

                                            String activity = Objects.requireNonNull(activityInputLayout.getEditText()).getText().toString();
                                            String location = Objects.requireNonNull(locationInputLayout.getEditText()).getText().toString();

                                            DatabaseReference dayRef = userRef.child("Day" + (i + 2));
                                            dayRef.child("date").setValue(date);
                                            DatabaseReference itemRef = dayRef.child(militaryTime);
                                            itemRef.child("status").setValue("incomplete");
                                            itemRef.child("activity").setValue(activity);
                                            itemRef.child("location").setValue(location);
                                        }
                                    }
                                }


                                Toast.makeText(getContext(), "Itinerary saved to Firebase", Toast.LENGTH_SHORT).show();
                                FragmentManager fragmentManager = getParentFragmentManager();
                                fragmentManager.popBackStack();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
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
                Toast.makeText(getContext(), "Maximum number of days reached", Toast.LENGTH_SHORT).show();
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
                if (addCounter[0] < 3) {
                    ItineraryItem newItem = new ItineraryItem("", "", ""); // Create a new ItineraryItem
                    dayItineraryAdapter.addItem(newItem); // Add the new item to the RecyclerView
                    addCounter[0]++;
                } else {
                    Toast.makeText(getContext(), "Maximum activities for a day reached.\nYou can add more in the edit option later.", Toast.LENGTH_SHORT).show();
                }
            });

            deleteActivityButton.setOnClickListener(v -> {
                if (addCounter[0] > 0) {
                    int lastIndex = dayItineraryAdapter.getItemCount() - 1;
                    if (lastIndex >= 0) {
                        dayItineraryAdapter.removeItem(lastIndex);
                        addCounter[0]--;
                    }
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
                                    Toast.makeText(getContext(), "Please select a date after " + date, Toast.LENGTH_SHORT).show();
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


}
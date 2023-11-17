package com.example.qsee;

import android.app.TimePickerDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ActivityViewHolder> {

    private List<ItineraryItem> itemList;

    public ItineraryAdapter() {
        this.itemList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_layout, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ItineraryItem item = itemList.get(position);
        holder.bind(item);

        holder.timeInputLayout.getEditText().setOnClickListener(v -> {
            showTimePicker(holder.timeInputLayout);
        });
    }


    private void showTimePicker(TextInputLayout textInputLayout) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(textInputLayout.getContext(),
                (TimePicker view, int selectedHour, int selectedMinute) -> {
                    String selectedTime;
                    if (selectedHour >= 12) {
                        if (selectedHour > 12) {
                            selectedHour -= 12;
                        }
                        selectedTime = String.format("%02d:%02d PM", selectedHour, selectedMinute);
                    } else {
                        if (selectedHour == 0) {
                            selectedHour = 12;
                        }
                        selectedTime = String.format("%02d:%02d AM", selectedHour, selectedMinute);
                    }
                    if (textInputLayout.getEditText() != null) {
                        textInputLayout.getEditText().setText(selectedTime);
                    }
                }, hour, minute, false);

        timePickerDialog.show();
    }



    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void addItem(ItineraryItem item) {
        itemList.add(item);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position > 0 && position < itemList.size()) {
            itemList.remove(position);
            notifyItemRemoved(position);
        }
    }


    public List<ItineraryItem> getItemList() {
        return itemList;
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        private TextInputLayout timeInputLayout;
        private TextInputLayout activityInputLayout;
        private TextInputLayout locationInputLayout; // Changed to TextInputLayout
        private TextInputLayout originInputLayout;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            timeInputLayout = itemView.findViewById(R.id.timeInput);
            activityInputLayout = itemView.findViewById(R.id.activityInput);
            locationInputLayout = itemView.findViewById(R.id.locationInput); // Changed to TextInputLayout
            originInputLayout = itemView.findViewById(R.id.originInput);

        }

        public void bind(ItineraryItem item) {
            TextInputEditText timeEditText = (TextInputEditText) timeInputLayout.getEditText();
            TextInputEditText activityEditText = (TextInputEditText) activityInputLayout.getEditText();

            if (timeEditText != null) {
                if (timeEditText.getText().toString().isEmpty()) {
                    timeEditText.setText(item.getTime());
                }
            }

            if (activityEditText != null) {
                if (activityEditText.getText().toString().isEmpty()) {
                    activityEditText.setText(item.getActivity());
                }
            }

            // Getting text from AutoCompleteTextView directly
            if (originInputLayout.getEditText() instanceof AutoCompleteTextView) {
                AutoCompleteTextView originAutoCompleteTextView = (AutoCompleteTextView) originInputLayout.getEditText(); // Get the AutoCompleteTextView
                String locationText = originAutoCompleteTextView.getText().toString(); // Get the text from AutoCompleteTextView

                // Fetch data from Firebase Realtime Database
                DatabaseReference locationsRef = FirebaseDatabase.getInstance().getReference().child("Location");
                locationsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> locationsList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String Location = snapshot.child("Location").getValue(String.class);
                            locationsList.add(Location);
                        }

                        // Set up the adapter for the AutoCompleteTextView
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(originAutoCompleteTextView.getContext(), android.R.layout.simple_dropdown_item_1line, locationsList);
                        originAutoCompleteTextView.setAdapter(adapter);

                        originAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                                // Not used in this case
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                                // Check if the entered text is not present in the locationsList
                                String locationText = charSequence.toString();
                                if (!locationsList.contains(locationText)) {
                                    originAutoCompleteTextView.setError("Invalid location"); // Optionally, you can show an error message
                                } else {
                                    originAutoCompleteTextView.setError(null); // Clear the error if the location is valid
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                // Not used in this case
                            }
                        });

                        originAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if (!hasFocus) {
                                    // Check if the entered text is not present in the locationsList
                                    String locationText = originAutoCompleteTextView.getText().toString();
                                    if (!locationsList.contains(locationText)) {
                                        originAutoCompleteTextView.setText(""); // Clear the input if it doesn't match any item
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle any errors
                    }
                });

                if (locationText.isEmpty()) {
                    originAutoCompleteTextView.setText(item.getLocation());
                }
            }

            // Getting text from AutoCompleteTextView directly
            if (locationInputLayout.getEditText() instanceof AutoCompleteTextView) {
                AutoCompleteTextView locationAutoCompleteTextView = (AutoCompleteTextView) locationInputLayout.getEditText(); // Get the AutoCompleteTextView
                String locationText = locationAutoCompleteTextView.getText().toString(); // Get the text from AutoCompleteTextView

                // Fetch data from Firebase Realtime Database
                DatabaseReference locationsRef = FirebaseDatabase.getInstance().getReference().child("Location");
                locationsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> locationsList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String Location = snapshot.child("Location").getValue(String.class);
                            locationsList.add(Location);
                        }

                        // Set up the adapter for the AutoCompleteTextView
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(locationAutoCompleteTextView.getContext(), android.R.layout.simple_dropdown_item_1line, locationsList);
                        locationAutoCompleteTextView.setAdapter(adapter);

                        locationAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                                // Not used in this case
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                                // Check if the entered text is not present in the locationsList
                                String locationText = charSequence.toString();
                                if (!locationsList.contains(locationText)) {
                                    locationAutoCompleteTextView.setError("Invalid location"); // Optionally, you can show an error message
                                } else {
                                    locationAutoCompleteTextView.setError(null); // Clear the error if the location is valid
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                // Not used in this case
                            }
                        });

                        locationAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if (!hasFocus) {
                                    // Check if the entered text is not present in the locationsList
                                    String locationText = locationAutoCompleteTextView.getText().toString();
                                    if (!locationsList.contains(locationText)) {
                                        locationAutoCompleteTextView.setText(""); // Clear the input if it doesn't match any item
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle any errors
                    }
                });

                if (locationText.isEmpty()) {
                    locationAutoCompleteTextView.setText(item.getLocation());
                }
            }
        }

    }
}

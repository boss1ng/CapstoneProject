package com.example.qsee;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpdateItineraryFragment extends Fragment {

    private String userId;
    private String locationName;

    public static UpdateItineraryFragment newInstance(String userId, String locationName) {
        UpdateItineraryFragment fragment = new UpdateItineraryFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("locationName", locationName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            locationName = getArguments().getString("locationName");
        }
    }

    private void setLocNameFromFirebase(String locationName) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Itinerary");
        databaseReference.orderByChild("iterName").equalTo(locationName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Date startDate = null;
                Date endDate = null;
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String iterName = childSnapshot.getKey(); // Assuming iterName is the key
                    TextView locNameTextView = requireView().findViewById(R.id.locName);
                    String capitalizedLocName = iterName.substring(0, 1).toUpperCase() + iterName.substring(1);
                    locNameTextView.setText(capitalizedLocName);

                    for (int i = 1; i <= 5; i++) {
                        String dayKey = "Day" + i;
                        List<Itinerary> itineraryList = new ArrayList<>();
                        String dateText = "";
                        for (DataSnapshot timeSnapshot : childSnapshot.child(dayKey).getChildren()) {
                            if (timeSnapshot.getKey().equals("date")) {
                                String date = timeSnapshot.getValue(String.class);
                                SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                try {
                                    Date currentDate = parser.parse(date);
                                    if (startDate == null || currentDate.before(startDate)) {
                                        startDate = currentDate;
                                    }
                                    if (endDate == null || currentDate.after(endDate)) {
                                        endDate = currentDate;
                                    }
                                    SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
                                    dateText = formatter.format(currentDate).toUpperCase();
                                } catch (java.text.ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // Rest of the code remains unchanged
                                String timeString = timeSnapshot.getKey(); // Assuming time is the key under DayX
                                SimpleDateFormat parser = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                Date time = null;
                                try {
                                    time = parser.parse(timeString);
                                } catch (java.text.ParseException e) {
                                    e.printStackTrace();
                                }
                                if (time != null) {
                                    SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                                    String formattedTime = formatter.format(time);
                                    String location = timeSnapshot.child("location").getValue(String.class);
                                    String activity = timeSnapshot.child("activity").getValue(String.class);
                                    itineraryList.add(new Itinerary(formattedTime, location, activity));
                                }
                            }
                        }

                        int recyclerViewId = getResources().getIdentifier("day" + i + "Recycler", "id", requireActivity().getPackageName());
                        RecyclerView recyclerView = requireView().findViewById(recyclerViewId);
                        // Inside UpdateItineraryFragment
                        EditItineraryAdapter adapter = new EditItineraryAdapter(getContext(), itineraryList, userId, iterName);
                        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                        recyclerView.setAdapter(adapter);

                        // Hide the day title if there are no items in the RecyclerView
                        TextView dayTitleTextView = requireView().findViewById(getResources().getIdentifier("dayTitle" + i, "id", requireActivity().getPackageName()));
                        ImageView dayOpt = requireView().findViewById(getResources().getIdentifier("optD" + i, "id", requireActivity().getPackageName()));
                        if (itineraryList.isEmpty()) {
                            dayTitleTextView.setVisibility(View.GONE);
                            dayOpt.setVisibility(View.GONE);
                        } else {
                            dayTitleTextView.setVisibility(View.VISIBLE);
                            dayOpt.setVisibility(View.VISIBLE);
                            if (!dateText.isEmpty()) {
                                dayTitleTextView.setText(dateText);
                            }
                        }
                    }
                }

                if (startDate != null && endDate != null) {
                    SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                    String formattedStartDate = formatter.format(startDate);
                    String formattedEndDate = formatter.format(endDate);
                    TextView dateDurationTextView = requireView().findViewById(R.id.dateDuration);
                    dateDurationTextView.setText(formattedStartDate + " to " + formattedEndDate);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that occur during the query
                Log.e("FirebaseError", "Error: " + databaseError.getMessage());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_itineraryview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (locationName != null) {
            setLocNameFromFirebase(locationName);
        }

        ImageView optD1 = view.findViewById(R.id.optD1);
        ImageView optD2 = view.findViewById(R.id.optD2);
        ImageView optD3 = view.findViewById(R.id.optD3);
        ImageView optD4 = view.findViewById(R.id.optD4);
        ImageView optD5 = view.findViewById(R.id.optD5);

        optD1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action for optD1 click
                // For example, open the AddItineraryActivity fragment for Day 1
                AddItineraryActivity addItineraryActivity = new AddItineraryActivity();

                // Create a bundle to pass data
                Bundle args = new Bundle();
                args.putString("iterName", locationName); // Pass the iterName
                args.putString("day", "Day 1"); // Pass the day
                addItineraryActivity.setArguments(args);

                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, addItineraryActivity);
                transaction.addToBackStack(null); // Optional, to add the transaction to the back stack
                transaction.commit();
            }
        });

        optD1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action for optD1 click
                // For example, open the AddItineraryActivity fragment for Day 1
                AddItineraryActivity addItineraryActivity = new AddItineraryActivity();

                // Create a bundle to pass data
                Bundle args = new Bundle();
                args.putString("iterName", locationName); // Pass the iterName
                args.putString("day", "Day1"); // Pass the day
                addItineraryActivity.setArguments(args);

                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, addItineraryActivity);
                transaction.addToBackStack(null); // Optional, to add the transaction to the back stack
                transaction.commit();
            }
        });

        optD2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action for optD1 click
                // For example, open the AddItineraryActivity fragment for Day 1
                AddItineraryActivity addItineraryActivity = new AddItineraryActivity();

                // Create a bundle to pass data
                Bundle args = new Bundle();
                args.putString("iterName", locationName); // Pass the iterName
                args.putString("day", "Day2"); // Pass the day
                addItineraryActivity.setArguments(args);

                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, addItineraryActivity);
                transaction.addToBackStack(null); // Optional, to add the transaction to the back stack
                transaction.commit();
            }
        });

        optD3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action for optD1 click
                // For example, open the AddItineraryActivity fragment for Day 1
                AddItineraryActivity addItineraryActivity = new AddItineraryActivity();

                // Create a bundle to pass data
                Bundle args = new Bundle();
                args.putString("iterName", locationName); // Pass the iterName
                args.putString("day", "Day3"); // Pass the day
                addItineraryActivity.setArguments(args);

                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, addItineraryActivity);
                transaction.addToBackStack(null); // Optional, to add the transaction to the back stack
                transaction.commit();
            }
        });

        optD4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action for optD1 click
                // For example, open the AddItineraryActivity fragment for Day 1
                AddItineraryActivity addItineraryActivity = new AddItineraryActivity();

                // Create a bundle to pass data
                Bundle args = new Bundle();
                args.putString("iterName", locationName); // Pass the iterName
                args.putString("day", "Day4"); // Pass the day
                addItineraryActivity.setArguments(args);

                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, addItineraryActivity);
                transaction.addToBackStack(null); // Optional, to add the transaction to the back stack
                transaction.commit();
            }
        });

        optD5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action for optD1 click
                // For example, open the AddItineraryActivity fragment for Day 1
                AddItineraryActivity addItineraryActivity = new AddItineraryActivity();

                // Create a bundle to pass data
                Bundle args = new Bundle();
                args.putString("iterName", locationName); // Pass the iterName
                args.putString("day", "Day5"); // Pass the day
                addItineraryActivity.setArguments(args);

                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, addItineraryActivity);
                transaction.addToBackStack(null); // Optional, to add the transaction to the back stack
                transaction.commit();
            }
        });
    }
}

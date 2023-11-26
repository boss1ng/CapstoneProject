package com.example.qsee;

import static android.app.PendingIntent.getActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditItineraryAdapter extends RecyclerView.Adapter<EditItineraryAdapter.EditItineraryViewHolder> {
    private List<Itinerary> dataList;
    private String userId;
    private String iterName;

    private Context context; // Define the context variable

    // Constructor for the adapter
    public EditItineraryAdapter(Context context, List<Itinerary> dataList, String userId, String iterName) {
        this.context = context; // Set the context
        this.dataList = dataList;
        this.userId = userId;
        this.iterName = iterName;
    }


    @NonNull
    @Override
    public EditItineraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itinerary_list_item, parent, false);
        return new EditItineraryViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull EditItineraryViewHolder holder, int position) {
        // Bind data to your views here
        Itinerary data = dataList.get(position);
        holder.timeTextView.setText(data.getTime());
        // Assuming holder.locationTextView is your TextView
        String location = data.getLocation();
        String activity = data.getActivity();

        // Creating a SpannableString with the location in bold and activity in italic
        SpannableString spannableString = new SpannableString(location + "\n" + activity);

        // Setting the span for bold text
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, location.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Setting the span for italic text
        StyleSpan italicSpan = new StyleSpan(Typeface.ITALIC);
        spannableString.setSpan(italicSpan, location.length() + 1, location.length() + 1 + activity.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Reducing the size of the activity text
        float relativeSize = 0.8f; // Adjust the size as needed
        spannableString.setSpan(new RelativeSizeSpan(relativeSize), location.length() + 1, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Setting the formatted text to the TextView
        holder.locationTextView.setText(spannableString);

        // Call the method to load and set the location icon
        holder.bindLocationIcon(location);


    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // Create a ViewHolder
    public class EditItineraryViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        TextView locationTextView;
        ImageView editButton;
        ImageView deleteButton;
        ImageView locationIcon;


        public EditItineraryViewHolder(View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.locationTime); // Replace with your TextView ID
            locationTextView = itemView.findViewById(R.id.locationName); // Replace with your TextView ID
            editButton = itemView.findViewById(R.id.edit);
            locationIcon = itemView.findViewById(R.id.locationIcon);

            // Initialize other views
            deleteButton = itemView.findViewById(R.id.delete); // Replace with your delete button ID

            // Set click listener for delete button
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Itinerary clickedItem = dataList.get(position);
                        deleteItemFromDatabase(clickedItem, position);
                    }
                }
            });

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditActivityFragment editActivityFragment = new EditActivityFragment();

                    Bundle bundle = new Bundle();
                    bundle.putString("time", dataList.get(getAdapterPosition()).getTime()); // Assuming you have a getTime method in your Itinerary model
                    bundle.putString("location", dataList.get(getAdapterPosition()).getLocation());// Assuming you have a getLocation method in your Itinerary model
                    bundle.putString("iterName", iterName);
                    editActivityFragment.setArguments(bundle);

                    FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.fragment_container, editActivityFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });

        }
        public void bindLocationIcon(String location) {
            // Query the Firebase database to find the location
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Location");
            Query query = databaseReference.orderByChild("Location").equalTo(location);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Location data found
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String link = snapshot.child("Link").getValue(String.class);

                            // Use Picasso to load the image and set it into locationIcon
                            Picasso.get().load(link).into(locationIcon);
                        }
                    } else {
                        // Location data not found
                        // You can set a default image or handle the case as needed
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle the error if any
                }
            });
        }
    }
    // Method to delete item from the database
    private void deleteItemFromDatabase(Itinerary itinerary, int position) {
        String time = itinerary.getTime(); // Retrieve time from the itinerary
        String location = itinerary.getLocation(); // Retrieve location from the itinerary


        // Convert the retrieved time to military time format
        try {
            SimpleDateFormat givenFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            SimpleDateFormat militaryFormat = new SimpleDateFormat("HH:mm", Locale.US);

            assert time != null;
            Date date = givenFormat.parse(time);
            assert date != null;
            time = militaryFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.d("DeleteItem", "Deleting item - Time: " + time + ", Location: " + location);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to delete this item?");
        String finalTime = time;
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Itinerary");
                assert iterName != null;
                Query activityQuery = databaseReference.child(iterName);
                activityQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot daySnapshot : dataSnapshot.getChildren()) {
                            for (DataSnapshot timeSnapshot : daySnapshot.getChildren()) {
                                String retrievedLoc = timeSnapshot.child("location").getValue(String.class);
                                if (timeSnapshot.getKey().equals(finalTime) && retrievedLoc.equals(location)) {
                                    // Remove the old timeSnapshot
                                    timeSnapshot.getRef().removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    dataList.remove(position);
                                                    notifyItemRemoved(position);
                                                    Toast.makeText(context, "Activity deleted.", Toast.LENGTH_LONG).show();

                                                    TextView dayTitleTextView = null;
                                                    ImageView optD = null;
                                                    switch (daySnapshot.getKey()) {
                                                        case "Day1":
                                                            dayTitleTextView = ((Activity) context).findViewById(R.id.dayTitle1);
                                                            optD = ((Activity) context).findViewById(R.id.optD1);
                                                            break;
                                                        case "Day2":
                                                            dayTitleTextView = ((Activity) context).findViewById(R.id.dayTitle2);
                                                            optD = ((Activity) context).findViewById(R.id.optD2);
                                                            break;
                                                        case "Day3":
                                                            dayTitleTextView = ((Activity) context).findViewById(R.id.dayTitle3);
                                                            optD = ((Activity) context).findViewById(R.id.optD3);
                                                            break;
                                                        case "Day4":
                                                            dayTitleTextView = ((Activity) context).findViewById(R.id.dayTitle4);
                                                            optD = ((Activity) context).findViewById(R.id.optD4);
                                                            break;
                                                        case "Day5":
                                                            dayTitleTextView = ((Activity) context).findViewById(R.id.dayTitle5);
                                                            optD = ((Activity) context).findViewById(R.id.optD5);
                                                            break;
                                                        default:
                                                            break;
                                                    }

                                                    // Check if the daySnapshot has only the date and no other child nodes
                                                    if (daySnapshot.getChildrenCount() == 2) {
                                                        if (dayTitleTextView != null) {
                                                            dayTitleTextView.setVisibility(View.GONE);
                                                            optD.setVisibility(View.GONE);
                                                            // Remove the day from the database
                                                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Itinerary").child(iterName);
                                                            databaseReference.child(daySnapshot.getKey()).removeValue();
                                                        }
                                                    }

                                                    // Dapat madedelete din yung Itinerary sa list after mawala yung last day/activity sa itinerary
                                                    // Check if iterName is not null and there are no more children in dataSnapshot
                                                    if (iterName != null && !dataSnapshot.hasChildren()) {
                                                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Itinerary");
                                                        databaseReference.child(iterName).removeValue();
                                                        dataList.remove(position);
                                                        notifyItemRemoved(position);
                                                        //Toast.makeText(context, "Activity deleted", Toast.LENGTH_LONG).show();
                                                        Toast.makeText(context, "Last activity removed, The Itinerary will be deleted.", Toast.LENGTH_LONG).show();

                                                        // Pop back the fragment when the itinerary is deleted
                                                        FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                                                        fragmentManager.popBackStack();
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context, "Item not deleted successfully.", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            }
                        }
                        if (dataSnapshot.getChildrenCount() == 3){
                            // Remove the day from the database
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Itinerary");
                            databaseReference.child(iterName).removeValue();

                            Toast.makeText(context, "Last activity removed, The Itinerary will be deleted.", Toast.LENGTH_LONG).show();

                            // Redirect to ProfileFragment
                            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                            ProfileFragment profileFragment = new ProfileFragment();
                            // Use Bundle to pass values
                            Bundle bundle = new Bundle();
                            bundle.putString("userId", userId);
                            bundle.putString("fromItinerary", "2");
                            profileFragment.setArguments(bundle);
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.replace(R.id.fragment_container, profileFragment); // Replace R.id.fragment_container with your actual container ID
                            transaction.commit();
                        } else if (dataSnapshot.getChildrenCount() == 4 && dataSnapshot.hasChild("groupName")) {
                            // Remove the day from the database
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Itinerary");
                            databaseReference.child(iterName).removeValue();

                            Toast.makeText(context, "Last activity removed, The Itinerary will be deleted.", Toast.LENGTH_LONG).show();

                            // Redirect to ProfileFragment
                            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                            ProfileFragment profileFragment = new ProfileFragment();
                            // Use Bundle to pass values
                            Bundle bundle = new Bundle();
                            bundle.putString("userId", userId);
                            bundle.putString("fromItinerary", "2");
                            profileFragment.setArguments(bundle);
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.replace(R.id.fragment_container, profileFragment); // Replace R.id.fragment_container with your actual container ID
                            transaction.commit();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle any errors that occur during the query
                        Log.e("FirebaseError", "Error: " + databaseError.getMessage());
                    }
                });
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked No button
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }




}


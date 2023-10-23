package com.example.qsee;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

    @Override
    public void onBindViewHolder(@NonNull EditItineraryViewHolder holder, int position) {
        // Bind data to your views here
        Itinerary data = dataList.get(position);
        // Example of setting data to TextViews
        holder.timeTextView.setText(data.getTime());
        holder.locationTextView.setText(data.getLocation());
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


        public EditItineraryViewHolder(View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.locationTime); // Replace with your TextView ID
            locationTextView = itemView.findViewById(R.id.locationName); // Replace with your TextView ID
            editButton = itemView.findViewById(R.id.edit);

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
                                                    Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context, "Item not deleted successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }
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


package com.example.qsee;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<Location> locationList;
    private String userId;
    private Context context;

    public LocationAdapter(List<Location> locationList, String userId, Context context) {
        this.locationList = locationList;
        this.userId = userId;
        this.context = context;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_list_item, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Location location = locationList.get(position);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference itineraryRef = database.getReference("Itinerary");

        itineraryRef.child(location.getLocationName()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String adminId = dataSnapshot.child("admin").getValue(String.class);
                if (adminId != null) {
                    DatabaseReference usersRef = database.getReference("MobileUsers");
                    usersRef.child(adminId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String username = dataSnapshot.child("username").getValue(String.class);
                            if (username != null) {
                                // Perform necessary actions with the username
                                // For example, you can pass it to your AESUtils for decryption
                                String decryptedUsername = AESUtils.decrypt(username);

                                // Adding the "Owner: " prefix
                                String displayText = "Owner: " + decryptedUsername;

                                // Apply formatting to the text
                                SpannableString spannableString = new SpannableString(location.getLocationName() + "\n" + displayText);
                                spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, location.getLocationName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                spannableString.setSpan(new StyleSpan(Typeface.ITALIC), location.getLocationName().length() + 1, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                // Set the formatted text to the TextView
                                holder.locationNameTextView.setText(spannableString);

                                // Adjust the text size for the username
                                int usernameStart = spannableString.toString().indexOf(displayText);
                                int usernameEnd = usernameStart + displayText.length();
                                spannableString.setSpan(new RelativeSizeSpan(0.8f), usernameStart, usernameEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Adjust the size as needed
                                holder.locationNameTextView.setText(spannableString);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Handle error
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });


        // Set click listener for options icon
        holder.optionsIcon.setOnClickListener(v -> showPopupMenu(holder.optionsIcon, position));
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.options_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.edit) {
                // Handle edit option here
                editLocation(locationList.get(position));
                return true;
            } else if (id == R.id.delete) {
                // Handle delete option here
                showDeleteConfirmationDialog(position);
                return true;
            } else {
                return false;
            }
        });

        popup.show();
    }

    private void editLocation(Location location) {
        // Create the fragment you want to replace in the fragment_container
        UpdateItineraryFragment updateItineraryFragment = new UpdateItineraryFragment();

        // Pass the userId to the fragment
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        bundle.putString("locationName", location.getLocationName());
        updateItineraryFragment.setArguments(bundle);

        // Get the parent FragmentActivity's FragmentManager
        FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();

        // Begin the fragment transaction
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Replace the fragment_container with the new fragment
        transaction.replace(R.id.fragment_container, updateItineraryFragment);
        transaction.addToBackStack(null); // Optional: Add transaction to back stack
        transaction.commit();
    }

    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete this location?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Delete the item from the database and the list
                    Location location = locationList.get(position);
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Itinerary");
                    databaseReference.child(location.getLocationName()).removeValue();

                    locationList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, locationList.size());

                    // Display a toast message
                    Toast.makeText(context, "Itinerary deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }


    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView locationNameTextView;
        ImageView optionsIcon;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationNameTextView = itemView.findViewById(R.id.locationName);
            optionsIcon = itemView.findViewById(R.id.options);
        }
    }
}


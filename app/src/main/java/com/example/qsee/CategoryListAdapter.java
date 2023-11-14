package com.example.qsee;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.ViewHolder> {
    private Context context;
    private List<LocationItem> locationItems;
    private FragmentManager fragmentManager;
    boolean isUserInQuezonCity = true;
    private String userId;

    public CategoryListAdapter(Context context, FragmentManager fragmentManager, String userId) {
        this.context = context;
        this.locationItems = new ArrayList<>();
        this.fragmentManager = fragmentManager; // Initialize the FragmentManager
        this.userId = userId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout and create a new ViewHolder
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationItem locationItem = locationItems.get(position);
        holder.locationTextView.setText(locationItem.getLocation());

        String imageUrl = locationItem.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).into(holder.locationImage);
        } else {
            // Handle cases where imageUrl is empty or null
            // You can set a placeholder image or hide the ImageView as needed
        }

        holder.locationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Location");

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String location = snapshot.child("Location").getValue(String.class);
                            if (location != null && location.equals(locationItem.getLocation())) {
                                String placeAddress = snapshot.child("Address").getValue(String.class);
                                String placeRating = snapshot.child("AverageRate").getValue(String.class);
                                String placeDescription = snapshot.child("Description").getValue(String.class);
                                String placeLink = snapshot.child("Link").getValue(String.class);
                                String highestPrice = snapshot.child("HighestPrice").getValue(String.class);
                                String lowestPrice = snapshot.child("LowestPrice").getValue(String.class);
                                String latitude = snapshot.child("Latitude").getValue(String.class);
                                String longitude = snapshot.child("Longitude").getValue(String.class);
                                String placePrice = "₱" + lowestPrice + " - ₱" + highestPrice;

                                if (lowestPrice != null && highestPrice != null) {
                                    if (lowestPrice.equals("") || highestPrice.equals("")) {
                                        lowestPrice = "-";
                                        highestPrice = "-";
                                        placePrice = "-";
                                    }
                                    else {
                                        placePrice = "₱" + lowestPrice + " - ₱" + highestPrice;
                                    }
                                }

                                Bundle args = new Bundle();
                                args.putString("userId",userId);
                                args.putString("placeName", locationItem.getLocation());
                                args.putString("placeAddress", placeAddress);
                                args.putString("placeRating", placeRating);
                                args.putString("placeDescription", placeDescription);
                                args.putString("placeLink", placeLink);
                                args.putString("placePrice", placePrice);
                                args.putString("destinationLatitude", latitude);
                                args.putString("destinationLongitude", longitude);

                                args.putString("isUserInQuezonCity", String.valueOf(isUserInQuezonCity));

                                PlaceDialogSearch placeDialogSearch = new PlaceDialogSearch();
                                placeDialogSearch.setArguments(args);

                                placeDialogSearch.show(fragmentManager, "PlaceDialogSearch");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle any errors or onCancelled events here
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return locationItems.size();
    }

    public void setItems(List<LocationItem> items) {
        locationItems.clear();
        locationItems.addAll(items);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView locationTextView;
        ImageView locationImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            locationTextView = itemView.findViewById(R.id.locationName); // Replace with your TextView ID
            locationImage = itemView.findViewById(R.id.locationImage);
        }
    }
}


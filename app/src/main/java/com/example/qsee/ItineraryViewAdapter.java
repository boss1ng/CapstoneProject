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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ItineraryViewAdapter extends RecyclerView.Adapter<ItineraryViewAdapter.ItineraryViewHolder> {
    private List<Itinerary> dataList;
    private String userId;
    private String iterName;

    private Context context; // Define the context variable

    // Constructor for the adapter
    public ItineraryViewAdapter(Context context, List<Itinerary> dataList, String userId, String iterName) {
        this.context = context; // Set the context
        this.dataList = dataList;
        this.userId = userId;
        this.iterName = iterName;
    }


    @NonNull
    @Override
    public ItineraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itineraryview_list_item, parent, false);

        return new ItineraryViewHolder(view);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ItineraryViewHolder holder, int position) {
        // Bind data to your views here
        Itinerary data = dataList.get(position);
        holder.timeTextView.setText(data.getTime());

        // Assuming holder.locationTextView is your TextView
        String location = data.getLocation();
        String activity = data.getActivity();

        // Creating a SpannableString with the location in bold and activity in italic
        SpannableString spannableString = new SpannableString(location + "\n" + activity);
        // Load a random image from Firebase Storage into the locationIcon ImageView (Di pa gumagana)
        //loadRandomImageIntoImageView(holder.locationIcon);

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


    }
    /*
    // Load an image from Firebase Storage and display it in the locationIcon ImageView
    private void loadRandomImageIntoImageView(ImageView locationIcon) {
        // Replace 'specific-image.jpg' with the actual image file name
        try {

        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("Icons/birds.png");

        imageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // Load the specific image into the ImageView using Glide
                    RequestOptions requestOptions = new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL); // Adjust caching strategy as needed
                    Glide.with(context)
                            .load(uri)
                            .apply(requestOptions)
                            .into(locationIcon);
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to get the download URL
                    Log.e("FirebaseStorage", "Failed to get download URL: " + e.getMessage());
                });
        } catch (Exception e) {
            Log.e("FirebaseStorage", "Error loading image: " + e.getMessage());
        }
    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }

     */

    // Create a ViewHolder
    public class ItineraryViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        TextView locationTextView;
        ImageView locationIcon;


        public ItineraryViewHolder(View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.locationTime); // Replace with your TextView ID
            locationTextView = itemView.findViewById(R.id.locationName); // Replace with your TextView ID
            locationIcon = itemView.findViewById(R.id.locationIcon); //for image replace --- not working
        }
    }
}


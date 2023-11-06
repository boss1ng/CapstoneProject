package com.example.qsee;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItineraryViewFragment extends Fragment {

    private String userId;
    private String locationName;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 101;

    public static ItineraryViewFragment newInstance(String userId, String locationName) {
        ItineraryViewFragment fragment = new ItineraryViewFragment();
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
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Date startDate = null;
                Date endDate = null;
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String iterName = childSnapshot.getKey(); // Assuming iterName is the key
                    TextView locNameTextView = requireView().findViewById(R.id.locName);
                    String capitalizedLocName = iterName.substring(0, 1).toUpperCase() + iterName.substring(1);
                    locNameTextView.setText(capitalizedLocName);

                    String groupName = childSnapshot.child("groupName").getValue(String.class);

                    if (groupName != null){
                        locNameTextView.setText(capitalizedLocName+ " - "+groupName);
                    }

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
                        ItineraryViewAdapter adapter = new ItineraryViewAdapter(getContext(), itineraryList, userId, iterName);
                        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                        recyclerView.setAdapter(adapter);

                        // Hide the day title if there are no items in the RecyclerView
                        TextView dayTitleTextView = requireView().findViewById(getResources().getIdentifier("dayTitle" + i, "id", requireActivity().getPackageName()));
                        if (itineraryList.isEmpty()) {
                            dayTitleTextView.setVisibility(View.GONE);
                        } else {
                            dayTitleTextView.setVisibility(View.VISIBLE);
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
        // Handle the back button click to pop the back stack
        ImageView backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pop the back stack when the back button is clicked
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Add the download button functionality
        ImageView downloadBtn = view.findViewById(R.id.downloadBtn);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for the permission
                if (ContextCompat.checkSelfPermission(requireContext(), WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted, request it
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                } else {
                    // Permission has already been granted, proceed with the PDF generation and saving
                    generatePDF();
                }

            }
        });

    }
    private void generatePDF() {
        // Get the root view
        final View rootView = requireView();

        // Get the height of the whole content in the ScrollView
        int height = 0;
        for (int i = 0; i < ((ViewGroup) rootView).getChildCount(); i++) {
            height += ((ViewGroup) rootView).getChildAt(i).getHeight();
        }
        int width = rootView.getWidth();

        // Create a bitmap and a canvas to draw the content
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        rootView.draw(canvas);

        // Create a PDF document
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas pdfCanvas = page.getCanvas();
        Paint paint = new Paint();
        pdfCanvas.drawBitmap(bitmap, 0, 0, paint);
        document.finishPage(page);

        // Define the file path for the PDF
        String fileName = locationName + " - " + "itinerary.pdf";
        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsFolder, fileName);
        String filePath = file.getAbsolutePath();

        // Create a file output stream
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            document.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Close the document
        document.close();

        // Show a toast indicating that the PDF has been saved
        Toast.makeText(requireContext(), "Itinerary saved to Downloads", Toast.LENGTH_SHORT).show();
    }
}

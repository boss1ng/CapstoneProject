package com.example.qsee;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

interface TaskCompletedCallback {
    void onTaskCompleted(String response);
}

public class ItineraryViewFragment extends Fragment implements TaskCompletedCallback {
    private String userId;
    private String locationName;
    private PdfDocument document;

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 101;
    private static final int REQUEST_CODE_SAVE_PDF = 102;

    private String responseString = null;
    private String jsonResponseString = null;
    private HttpURLConnection urlConnection = null;

    public PdfPTable table;
    public Document pdfDocument;
    private int pendingTasks = 0; // To track pending async tasks

    final String[] originLatitude = {""};
    final String[] originLongitude = {""};
    final String[] destLatitude = {""};
    final String[] destLongitude = {""};

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
                //requireActivity().getSupportFragmentManager().popBackStack();

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                ProfileFragment profileFragment = new ProfileFragment();

                // Use Bundle to pass values
                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                bundle.putString("fromItinerary", "2");
                profileFragment.setArguments(bundle);

                TextView textView2 = view.findViewById(R.id.locName);
                textView2.setVisibility(View.GONE);
                TextView textView3 = view.findViewById(R.id.dateDuration);
                textView3.setVisibility(View.GONE);
                ImageView imageView = view.findViewById(R.id.backButton);
                imageView.setVisibility(View.GONE);
                ImageView imageView1 = view.findViewById(R.id.downloadBtn);
                imageView1.setVisibility(View.GONE);

                TextView textView4 = view.findViewById(R.id.dayTitle1);
                textView4.setVisibility(View.GONE);
                TextView textView5 = view.findViewById(R.id.dayTitle2);
                textView5.setVisibility(View.GONE);
                TextView textView6 = view.findViewById(R.id.dayTitle3);
                textView6.setVisibility(View.GONE);
                TextView textView7 = view.findViewById(R.id.dayTitle4);
                textView7.setVisibility(View.GONE);
                TextView textView8 = view.findViewById(R.id.dayTitle5);
                textView8.setVisibility(View.GONE);

                RecyclerView day1Recycler = view.findViewById(R.id.day1Recycler);
                day1Recycler.setVisibility(View.GONE);
                RecyclerView day2Recycler = view.findViewById(R.id.day2Recycler);
                day2Recycler.setVisibility(View.GONE);
                RecyclerView day3Recycler = view.findViewById(R.id.day3Recycler);
                day3Recycler.setVisibility(View.GONE);
                RecyclerView day4Recycler = view.findViewById(R.id.day4Recycler);
                day4Recycler.setVisibility(View.GONE);
                RecyclerView day5Recycler = view.findViewById(R.id.day5Recycler);
                day5Recycler.setVisibility(View.GONE);

                transaction.replace(R.id.fragment_container, profileFragment);
                transaction.addToBackStack(null);
                transaction.commit();
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
                    //generatePDF();

                    downloadBtn.setEnabled(false);
                    backButton.setEnabled(false);
                    createPdfWithTable();
                    downloadBtn.setEnabled(true);
                    backButton.setEnabled(true);
                }
            }
        });
    }

    // Custom Page Event Helper
    class BackgroundImageHelper extends PdfPageEventHelper {
        private Image backgroundImage;

        public BackgroundImageHelper(Context context) throws Exception {
            // Load the drawable resource
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.logopdf);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

            // Convert bitmap to byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bitmapData = stream.toByteArray();

            // Create an Image object from the byte array
            this.backgroundImage = Image.getInstance(bitmapData);
            this.backgroundImage.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
            this.backgroundImage.setAbsolutePosition(0, 0);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                writer.getDirectContentUnder().addImage(backgroundImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Convert a hexadecimal color string to BaseColor
    private static BaseColor hexToBaseColor(String hexColor) {
        int red = Integer.parseInt(hexColor.substring(1, 3), 16);
        int green = Integer.parseInt(hexColor.substring(3, 5), 16);
        int blue = Integer.parseInt(hexColor.substring(5, 7), 16);
        return new BaseColor(red, green, blue);
    }

    @Override
    public void onTaskCompleted(String response) {

        // Get the directory for the user's public documents directory.
        File pdfFolder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "QSee Itinerary");

        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
            Log.d(TAG, "PDF Directory created");
        }

        //String fileName = ServerValue.TIMESTAMP + "_Itinerary.pdf";
        File pdfFile = new File(pdfFolder, locationName + ".pdf");

        // Create a new Document
        Document document = new Document(PageSize.A4);

        // Set margins (left, right, top, bottom)
        document.setMargins(0, 0, 72, 36); // 1/2 inch margins

        try {
            //PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));

            // Set the page event helper
            BackgroundImageHelper backgroundImageHelper = new BackgroundImageHelper(getContext());
            writer.setPageEvent(backgroundImageHelper);

            // Open the document for writing
            document.open();

            // Create a Font with a larger size
            Font largeFont = new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.BOLD);
            Font tableHeader = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
            Font activityHeader = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Itinerary");
            databaseReference.orderByChild("iterName").equalTo(locationName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Date startDate = null;
                    Date endDate = null;
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String iterName = childSnapshot.getKey(); // Assuming iterName is the key
                        String documentTitle = "Itinerary Plan for " + iterName;

                        try {
                            // Add a title to the document
                            // Create a Paragraph with the text "Itinerary Plan", centered and using the larger font
                            Paragraph paragraph = new Paragraph(documentTitle, largeFont);
                            paragraph.setAlignment(Paragraph.ALIGN_CENTER);

                            // Add the paragraph to the document
                            document.add(paragraph);

                            // Alternatively, you can create a new Paragraph to add a line break
                            Paragraph lineBreak = new Paragraph("\n");
                            document.add(lineBreak);

                            // Add content to the document
                            // Create a table with five columns
                            final PdfPTable table = new PdfPTable(5);

                            for (int i = 1; i <= 5; i++) {
                                String dayKey = "Day" + i;
                                String dateText = "";
                                String date = childSnapshot.child(dayKey).child("date").getValue(String.class);
                                SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

                                if (date != null) {
                                    try {
                                        Date currentDate = parser.parse(date);
                                        if (startDate == null || currentDate.before(startDate)) {
                                            startDate = currentDate;
                                        }
                                        if (endDate == null || currentDate.after(endDate)) {
                                            endDate = currentDate;
                                        }
                                        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
                                        dateText = formatter.format(currentDate);

                                    } catch (java.text.ParseException e) {
                                        e.printStackTrace();
                                    }
                                } else
                                    break;

                                // Create a cell with the text "Merged Columns" that spans all five columns
                                PdfPCell cell = new PdfPCell(new Paragraph("Day " + i + " - " + dateText, tableHeader));
                                cell.setColspan(5); // Set the number of columns to span
                                cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                cell.setPadding(10);
                                cell.setBackgroundColor(hexToBaseColor("#3894A3"));

                                // Add the cell to the table
                                table.addCell(cell);

                                PdfPCell timeCell = new PdfPCell(new Paragraph("Time", activityHeader));
                                timeCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                timeCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                timeCell.setPadding(10);
                                table.addCell(timeCell);

                                PdfPCell activityCell = new PdfPCell(new Paragraph("Activity", activityHeader));
                                activityCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                activityCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                activityCell.setPadding(10);
                                table.addCell(activityCell);

                                PdfPCell originCell = new PdfPCell(new Paragraph("Origin", activityHeader));
                                originCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                originCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                originCell.setPadding(10);
                                table.addCell(originCell);

                                PdfPCell destCell = new PdfPCell(new Paragraph("Destination", activityHeader));
                                destCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                destCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                destCell.setPadding(10);
                                table.addCell(destCell);

                                PdfPCell routeCell = new PdfPCell(new Paragraph("Route Information", activityHeader));
                                routeCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                routeCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                routeCell.setPadding(10);
                                table.addCell(routeCell);

                                for (DataSnapshot timeSnapshot : childSnapshot.child(dayKey).getChildren()) {
                                    // Add headers to the table
                                    String key = timeSnapshot.getKey();
                                    String outputTime = null;

                                    // Create a SimpleDateFormat for parsing 24-hour time
                                    DateFormat inputFormat = new SimpleDateFormat("HH:mm");

                                    // Create a SimpleDateFormat for formatting 12-hour time with AM/PM
                                    DateFormat outputFormat = new SimpleDateFormat("hh:mm a");

                                    try {
                                        // Parse the input time
                                        Date dateParse = inputFormat.parse(key);

                                        // Format the date in 12-hour format with AM/PM
                                        outputTime = outputFormat.format(dateParse);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    if (key.contains("date")) {
                                        break;
                                    }

                                    else {
                                        PdfPCell specificTimeCell = new PdfPCell(new Paragraph(outputTime));
                                        specificTimeCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                        specificTimeCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                        table.addCell(specificTimeCell);

                                        PdfPCell specificActivityCell = new PdfPCell(new Paragraph(timeSnapshot.child("activity").getValue(String.class)));
                                        specificActivityCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                        specificActivityCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                        table.addCell(specificActivityCell);

                                        String origin = timeSnapshot.child("origin").getValue(String.class);
                                        PdfPCell specificOriginCell = new PdfPCell(new Paragraph(origin));
                                        specificOriginCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                        specificOriginCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                        table.addCell(specificOriginCell);

                                        String destination = timeSnapshot.child("location").getValue(String.class);
                                        PdfPCell specificDestCell = new PdfPCell(new Paragraph(destination));
                                        specificDestCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                        specificDestCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                        table.addCell(specificDestCell);

                                        //ROUTE INFORMATION
                                        //table.addCell("ROUTE");
                                        Log.d(TAG, "RESPONSE STRING" + response);
                                        StringBuilder distancesBuilder = new StringBuilder();

                                        try {
                                            if (response != null) {
                                                JSONObject jsonResponse = new JSONObject(response); // jsonResponseString is the JSON response you received

                                                // Check the status of the response
                                                String status = jsonResponse.getString("status");

                                                if (status.equals("OK")) {
                                                    Log.d(TAG, "STATUS" + status);

                                                    JSONArray routes = jsonResponse.getJSONArray("routes");

                                                    for (int h = 0; h < routes.length(); h++) {
                                                        JSONObject route = routes.getJSONObject(h);

                                                        JSONArray legs = route.getJSONArray("legs");

                                                        for (int j = 0; j < legs.length(); j++) {
                                                            JSONObject leg = legs.getJSONObject(j);

                                                            JSONArray steps = leg.getJSONArray("steps");

                                                            for (int k = 0; k < steps.length(); k++) {
                                                                JSONObject step = steps.getJSONObject(k);

                                                                // Extract information from the step
                                                                String distance = step.getJSONObject("distance").getString("text");
                                                                String duration = step.getJSONObject("duration").getString("text");
                                                                String htmlInstructions = step.getString("html_instructions");
                                                                // Remove HTML tags and display plain text instructions
                                                                String plainTextInstructions = Html.fromHtml(htmlInstructions).toString();

                                                                // Get the maneuver from your API response
                                                                // Retrieve maneuver if it's present, or provide a default value
                                                                String maneuverType = step.optString("maneuver", "No Maneuver");
                                                                //Toast.makeText(getContext(), maneuverType, Toast.LENGTH_LONG).show();

                                                                //String routeInstruction = "In " + duration + ", " + plainTextInstructions + " after " + distance + ".";
                                                                String routeInstruction = "After " + distance + ", " + plainTextInstructions + ".";

                                                                distancesBuilder.append(routeInstruction).append("\n\n"); // Append distance and a newline
                                                            }
                                                        }
                                                    }

                                                    if (distancesBuilder.length() > 0) {
                                                        // Route Information
                                                        //table.addCell("ROUTE");
                                                        //table.addCell(distance);
                                                        PdfPCell distanceCell = new PdfPCell(new Phrase(distancesBuilder.toString()));
                                                        table.addCell(distanceCell);
                                                        Log.d(TAG, "BE HAPPY NOW :)");
                                                    }

                                                } else {
                                                    // Handle the case when the API request returns a status other than "OK"
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            // Handle JSON parsing errors
                                        }
                                    }
                                }

                                PdfPCell transitionRow = new PdfPCell(new Paragraph(" "));
                                transitionRow.setColspan(5); // Set the number of columns to span
                                transitionRow.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                transitionRow.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                transitionRow.setBorder(Rectangle.NO_BORDER); // Remove borders from the cell

                                // Add the cell to the table
                                table.addCell(transitionRow);
                            }

                            // Add the table to the document
                            document.add(table);

                            // End of Table
                            document.add(lineBreak);
                        } catch (DocumentException e) {
                            throw new RuntimeException(e);
                        } finally {
                            // Close the document
                            if (document.isOpen()) {
                                document.close();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            Toast.makeText(getContext(), "Itinerary saved to /Documents/QSee Itinerary directory.", Toast.LENGTH_LONG).show();

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createPdfWithTable() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Itinerary");
        databaseReference.orderByChild("iterName").equalTo(locationName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Date startDate = null;
                Date endDate = null;
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String iterName = childSnapshot.getKey(); // Assuming iterName is the key
                    String documentTitle = "Itinerary Plan for " + iterName;

                    for (int i = 1; i <= 5; i++) {
                        String dayKey = "Day" + i;
                        String dateText = "";
                        String date = childSnapshot.child(dayKey).child("date").getValue(String.class);
                        SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

                        if (date != null) {
                            try {
                                Date currentDate = parser.parse(date);
                                if (startDate == null || currentDate.before(startDate)) {
                                    startDate = currentDate;
                                }
                                if (endDate == null || currentDate.after(endDate)) {
                                    endDate = currentDate;
                                }
                                SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
                                dateText = formatter.format(currentDate);

                            } catch (java.text.ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        else
                            break;

                        for (DataSnapshot timeSnapshot : childSnapshot.child(dayKey).getChildren()) {
                            String key = timeSnapshot.getKey();
                            if (key.contains("date")) {
                                break;
                            }

                            else {
                                String origin = timeSnapshot.child("origin").getValue(String.class);
                                String destination = timeSnapshot.child("location").getValue(String.class);

                                String originLat = timeSnapshot.child("originLat").getValue(String.class);
                                String originLong = timeSnapshot.child("originLong").getValue(String.class);

                                String destLat = timeSnapshot.child("locationLat").getValue(String.class);
                                String destLong = timeSnapshot.child("locationLong").getValue(String.class);

                                String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                                        "origin=" + originLat + "," + originLong +
                                        "&destination=" + destLat + "," + destLong +
                                        "&key=" + getResources().getString(R.string.google_maps_api_key);

                                new FetchDirectionsTask(ItineraryViewFragment.this).execute(url);

                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Show a toast indicating that the PDF has been saved
        Toast.makeText(getContext(), "Exporting...", Toast.LENGTH_LONG).show();
    }

    private class FetchDirectionsTask extends AsyncTask<String, Void, String> {
        private TaskCompletedCallback callback;

        public FetchDirectionsTask(TaskCompletedCallback callback) {
            this.callback = callback;
        }
        @Override
        protected String doInBackground(String... urls) {
            return getDirectionsData(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Process the result here
            // Update UI based on the fetched data
            responseString = result;
            //Log.d(TAG, "RESPONSE STRING" + responseString);
            callback.onTaskCompleted(result);
        }
    }

    private String getDirectionsData(String urlString) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonResponse = null;

        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder builder = new StringBuilder();
            if (inputStream == null) {
                // No data received, handle accordingly
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }

            if (builder.length() == 0) {
                // Stream was empty, no point in parsing
                return null;
            }
            jsonResponse = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return jsonResponse;
    }








    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SAVE_PDF && resultCode == Activity.RESULT_OK && data != null) {
            // Save the PDF content to the chosen location
            Uri uri = data.getData();
            savePdfToUri(uri);
        }
    }

    private void savePdfToUri(Uri uri) {
        try {
            ParcelFileDescriptor pfd = requireActivity().getContentResolver().openFileDescriptor(uri, "w");
            if (pfd != null) {
                FileOutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor());
                document.writeTo(outputStream);
                outputStream.close();
                pfd.close();

                // Close the document
                document.close();

                // Show a toast indicating that the PDF has been saved
                Toast.makeText(requireContext(), "Itinerary saved to chosen location.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
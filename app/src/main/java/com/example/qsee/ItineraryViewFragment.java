package com.example.qsee;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

import com.google.common.collect.Table;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ItineraryViewFragment extends Fragment {

    private String userId;
    private String locationName;
    private PdfDocument document;

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 101;
    private static final int REQUEST_CODE_SAVE_PDF = 102;

    private String responseString = null;

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
                    //generatePDF();

                    createPdfWithTable();
                }
            }
        });

    }
    private void generatePDF() {
        // Get the root view
        final View rootView = requireView();

        // Create a PDF document
        document = new PdfDocument();

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
        document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas pdfCanvas = page.getCanvas();
        Paint paint = new Paint();
        pdfCanvas.drawBitmap(bitmap, 0, 0, paint);
        document.finishPage(page);

        // Ask the user to choose a location to save the PDF
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "itinerary.pdf");

        startActivityForResult(intent, REQUEST_CODE_SAVE_PDF);
    }

    public void createPdfWithTable() {

        // Get the directory for the user's public documents directory.
        File pdfFolder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "QSee Itinerary");

        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
            Log.d(TAG, "PDF Directory created");
        }

        String fileName = ServerValue.TIMESTAMP + "_Itinerary.pdf";
        File pdfFile = new File(pdfFolder, fileName);

        // Create a new Document
        Document document = new Document(PageSize.A4);

        // Set margins (left, right, top, bottom)
        document.setMargins(0, 0, 36, 36); // 1/2 inch margins

        try {
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));

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
                            // Create a table with three columns
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
                                }

                                else
                                    break;

                                // Create a cell with the text "Merged Columns" that spans all four columns
                                PdfPCell cell = new PdfPCell(new Paragraph("Day 1 - " + dateText, tableHeader));
                                cell.setColspan(5); // Set the number of columns to span
                                cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically

                                // Add the cell to the table
                                table.addCell(cell);

                                PdfPCell timeCell = new PdfPCell(new Paragraph("Time", activityHeader));
                                timeCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                timeCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                table.addCell(timeCell);

                                PdfPCell activityCell = new PdfPCell(new Paragraph("Activity", activityHeader));
                                activityCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                activityCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                table.addCell(activityCell);

                                PdfPCell originCell = new PdfPCell(new Paragraph("Origin", activityHeader));
                                originCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                originCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                table.addCell(originCell);

                                PdfPCell destCell = new PdfPCell(new Paragraph("Destination", activityHeader));
                                destCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                destCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                table.addCell(destCell);

                                PdfPCell routeCell = new PdfPCell(new Paragraph("Route Information", activityHeader));
                                routeCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // Center the content
                                routeCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                table.addCell(routeCell);

                                boolean[] isOriginFound = {false};
                                boolean[] isDestFound = {false};

                                for (DataSnapshot timeSnapshot : childSnapshot.child(dayKey).getChildren()) {
                                    // Add headers to the table

                                    String key = timeSnapshot.getKey();

                                    if (key.contains("date")) {
                                        break;
                                    }

                                    else {

                                        PdfPCell specificTimeCell = new PdfPCell(new Paragraph(key));
                                        specificTimeCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                        table.addCell(specificTimeCell);

                                        PdfPCell specificActivityCell = new PdfPCell(new Paragraph(timeSnapshot.child("activity").getValue(String.class)));
                                        specificActivityCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                        table.addCell(specificActivityCell);

                                        String origin = timeSnapshot.child("origin").getValue(String.class);
                                        PdfPCell specificOriginCell = new PdfPCell(new Paragraph(origin));
                                        specificOriginCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                        table.addCell(specificOriginCell);

                                        String destination = timeSnapshot.child("location").getValue(String.class);
                                        PdfPCell specificDestCell = new PdfPCell(new Paragraph(destination));
                                        specificDestCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // Center vertically
                                        table.addCell(specificDestCell);

                                        //table.addCell(urlRequest);
                                        // Add here the Directions API URL Request.
                                        // The latitude and longitude of the origin and destination will be queried from the Locations from realtime database.

                                        // Assuming you have a DatabaseReference for the "Locations" node
                                        DatabaseReference locationsReference = FirebaseDatabase.getInstance().getReference("Location");

                                        // Query the "Locations" node to retrieve latitude and longitude for the origin
                                        locationsReference.orderByChild("Location").equalTo(origin).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                                                    // Retrieve latitude and longitude for the origin
                                                    String firebaseOrigLat = locationSnapshot.child("Latitude").getValue(String.class);
                                                    String firebaseOrigLong = locationSnapshot.child("Longitude").getValue(String.class);

                                                    originLatitude[0] = firebaseOrigLat;
                                                    originLongitude[0] = firebaseOrigLong;


                                                    Log.d(TAG, "Origin Latitude: " + originLatitude[0]);
                                                    Log.d(TAG, "Origin Longitude: " + originLongitude[0]);

                                                    // Now you have the latitude and longitude for the origin, you can construct the Directions API URL

                                                    // Similarly, query the "Locations" node for destination latitude and longitude
                                                    locationsReference.orderByChild("Location").equalTo(destination).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                                                                // Retrieve latitude and longitude for the destination
                                                                String firebaseDestLat = locationSnapshot.child("Latitude").getValue(String.class);
                                                                String firebaseDestLong = locationSnapshot.child("Longitude").getValue(String.class);

                                                                destLatitude[0] = firebaseDestLat;
                                                                destLongitude[0] = firebaseDestLong;

                                                                Log.d(TAG, "destination Latitude: " + destLatitude[0]);
                                                                Log.d(TAG, "destination Longitude: " + destLongitude[0]);

                                                                // not working here
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {
                                                            // Handle the error if the query fails
                                                        }
                                                    });
                                                }

                                                // not working here
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                // Handle the error if the query fails
                                            }
                                        });

                                        // Now you have the latitude and longitude for the destination, you can construct the Directions API URL
                                        // Now you have the latitude and longitude for both origin and destination
                                        // Construct the URL for directions
                                        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                                                "origin=" + originLatitude[0] + "," + originLongitude[0] +
                                                "&destination=" + destLatitude[0] + "," + destLongitude[0] +
                                                "&key=" + getResources().getString(R.string.google_maps_api_key);

                                        Log.e(TAG, url);

                                        // Use the 'url' as needed
                                        table.addCell(url);
                                    }
                                }
                            }

                            // Add the table to the document
                            document.add(table);

                            // End of Table
                            document.add(lineBreak);
                        }

                        catch (DocumentException e) {
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

            Log.d(TAG, "PDF created successfully at: " + pdfFile.getAbsolutePath());

            // Show a toast indicating that the PDF has been saved
            Toast.makeText(getContext(), "Itinerary saved to /Documents/QSee Itinerary directory.", Toast.LENGTH_LONG).show();

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }



    public String getResponseString(String urlString) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    public class DirectionsTask extends AsyncTask<Void, Void, String> {

        String passedUrl;
        PdfPTable passedtable;
        //PdfPTable table
        public DirectionsTask(String url) {
            passedUrl = url;
            //passedtable = table;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String jsonResponseString = null;
            HttpURLConnection urlConnection = null;

            try {
                URL urlRequest = new URL(passedUrl);
                urlConnection = (HttpURLConnection) urlRequest.openConnection();

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    jsonResponseString = response.toString();
                }

                else {
                    // Handle the case when the request returns an error
                }
            }

            catch (Exception e) {
                e.printStackTrace();
                // Handle exceptions
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return jsonResponseString;
        }

        @Override
        protected void onPostExecute(String jsonResponseString) {
            // Use the jsonResponseString in your application as needed

            try {

                JSONObject jsonResponse = new JSONObject(jsonResponseString); // jsonResponseString is the JSON response you received

                responseString = jsonResponseString;

                // Check the status of the response
                String status = jsonResponse.getString("status");


                if (status.equals("OK")) {
                    JSONArray routes = jsonResponse.getJSONArray("routes");

                    for (int i = 0; i < routes.length(); i++) {
                        JSONObject route = routes.getJSONObject(i);

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

                                // Route Information
                                //passedtable.addCell("INSERT");

                            }
                        }
                    }

                }

                else {
                    // Handle the case when the API request returns a status other than "OK"
                }
            }

            catch (JSONException e) {
                e.printStackTrace();
                // Handle JSON parsing errors
            }
        }
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
                Toast.makeText(requireContext(), "Itinerary saved to chosen location", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

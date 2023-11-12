package com.example.qsee;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

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
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
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

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

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
    private PdfDocument document;

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 101;
    private static final int REQUEST_CODE_SAVE_PDF = 102;

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

                    //String fileName = "itinerary.pdf";
                    //String filePath = getActivity().getFilesDir() + File.separator + fileName;
                    //createPdfWithTable(filePath);
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

    public static void createPdfWithTable(String filePath) {
        Document document = new Document(PageSize.A4);

        try {
            // Create a PdfWriter instance to write to the specified file path
            PdfWriter.getInstance(document, new FileOutputStream(new File(filePath)));
            document.open();

            // Add a title to the document
            document.add(new Paragraph("Table Example"));

            // Create a table with three columns
            PdfPTable table = new PdfPTable(3);

            // Add headers to the table
            table.addCell("Header 1");
            table.addCell("Header 2");
            table.addCell("Header 3");

            // Add data to the table
            table.addCell("Row 1, Col 1");
            table.addCell("Row 1, Col 2");
            table.addCell("Row 1, Col 3");

            table.addCell("Row 2, Col 1");
            table.addCell("Row 2, Col 2");
            table.addCell("Row 2, Col 3");

            // Add the table to the document
            document.add(table);

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            // Close the document
            if (document.isOpen()) {
                document.close();
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

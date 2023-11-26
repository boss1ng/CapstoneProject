package com.example.qsee;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {
    boolean isUserInQuezonCity = true;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private TextView textView;
    private TextView dateText;
    private TextView wC;
    private TextView rP;
    private JSONArray forecasts;
    private int currentDayIndex = 0; // Track the currently displayed day index.
    private final Map<String, List<Double>> dailyForecasts = new HashMap<>(); // Declare dailyForecasts as a class-level variable
    private String postUsername;
    private ImageView imageView12;
    private ImageView imageView13;
    private ImageView imageView14;
    private ImageView imageView15;
    private ImageView imageView16;
    private ImageView imageView17;


    private RecyclerView feedRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList; // Your data source

    public HomeFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        LinearLayout linearLayout = view.findViewById(R.id.recycleLinear);
        /*imageView12 = view.findViewById(R.id.imageView12);
        imageView13 = view.findViewById(R.id.imageView13);
        imageView14 = view.findViewById(R.id.imageView14);
        imageView15 = view.findViewById(R.id.imageView15);
        imageView16 = view.findViewById(R.id.imageView16);
        imageView17 = view.findViewById(R.id.imageView17);*/

        /*Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/sliders%2Fslider_1.jpg?alt=media&token=83a13343-b5bd-42bd-98ca-a234e13be324").into(imageView12);
        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/sliders%2Fslider_2.jpg?alt=media&token=dd113988-1a86-40e1-9e42-e8947e798b67").into(imageView13);
        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/sliders%2Fslider_3.jpg?alt=media&token=8a9a8d6b-5736-4b81-b2a2-579a1b08d1c6").into(imageView14);
        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/sliders%2Fslider_4.jpg?alt=media&token=0c163b54-da16-4514-9869-5262e8824108").into(imageView15);
        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/sliders%2Fslider_5.jpg?alt=media&token=36192c6c-0818-4ee5-9b6c-dc3ee1809a71").into(imageView16);
        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/sliders%2Fslider_6.jpg?alt=media&token=24bf286a-554f-4f74-8016-8c9eabe0b7de").into(imageView17);*/

        feedRecyclerView = view.findViewById(R.id.feedRecyclerView);

        // Apply the ItemDecoration with the desired spacing (in pixels)
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing); // Define your desired spacing in dimens.xml
        feedRecyclerView.addItemDecoration(new ItemSpacingDecoration(spacingInPixels));
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Initialize your data list and adapter
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);
        feedRecyclerView.setAdapter(postAdapter);

        // Initialize the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Request location permission if not granted
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        }

        /*textView = view.findViewById(R.id.textView);
        dateText = view.findViewById(R.id.date);
        wC = view.findViewById(R.id.weatherCondition);
        rP = view.findViewById(R.id.rainPercentage);

        ImageView previousImageView = view.findViewById(R.id.previousButton);
        ImageView nextImageView = view.findViewById(R.id.nextButton);*/

        // Set click listeners for previous and next ImageViews
        /*previousImageView.setOnClickListener(v -> showPreviousDay());
        nextImageView.setOnClickListener(v -> showNextDay());

        // Automatically load the weather forecast for Quezon City
        getWeatherForecastByLocationName();

        // Initially, display the first day's information (dayIndex = 0)
        displayDay(currentDayIndex);*/

        // Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            //Toast.makeText(getContext(), userID, Toast.LENGTH_LONG).show();
        }

        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        // Set the default item as highlighted
        MenuItem defaultItem = bottomNavigationView.getMenu().findItem(R.id.action_home);
        defaultItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_home) {
                    loadFragment(new HomeFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout linearLayout = view.findViewById(R.id.parentLinearCont);
                    linearLayout.setVisibility(View.GONE);
                    FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingAddButton);
                    floatingActionButton.setVisibility(View.GONE);

                } else if (itemId == R.id.action_search) {
                    loadFragment(new SearchFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout linearLayout = view.findViewById(R.id.parentLinearCont);
                    linearLayout.setVisibility(View.GONE);
                    RecyclerView linearLayout1 = view.findViewById(R.id.feedRecyclerView);
                    linearLayout1.setVisibility(View.GONE);
                    FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingAddButton);
                    floatingActionButton.setVisibility(View.GONE);

                } else if (itemId == R.id.action_maps) {
                    loadFragment(new MapsFragment());
                    //BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout linearLayout = view.findViewById(R.id.parentLinearCont);
                    linearLayout.setVisibility(View.GONE);
                    RecyclerView linearLayout1 = view.findViewById(R.id.feedRecyclerView);
                    linearLayout1.setVisibility(View.GONE);
                    FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingAddButton);
                    floatingActionButton.setVisibility(View.GONE);

                } else if (itemId == R.id.action_quiz) {
                    loadFragment(new StartQuizFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout linearLayout = view.findViewById(R.id.parentLinearCont);
                    linearLayout.setVisibility(View.GONE);
                    RecyclerView linearLayout1 = view.findViewById(R.id.feedRecyclerView);
                    linearLayout1.setVisibility(View.GONE);
                    FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingAddButton);
                    floatingActionButton.setVisibility(View.GONE);

                } else if (itemId == R.id.action_profile) {
                    loadFragment(new ProfileFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout linearLayout = view.findViewById(R.id.parentLinearCont);
                    linearLayout.setVisibility(View.GONE);
                    RecyclerView linearLayout1 = view.findViewById(R.id.feedRecyclerView);
                    linearLayout1.setVisibility(View.GONE);
                    FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingAddButton);
                    floatingActionButton.setVisibility(View.GONE);

                }
                return true;
            }
        });

        // Find the FloatingActionButton by ID
        FloatingActionButton fab = view.findViewById(R.id.floatingAddButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Check if location permission is granted
                if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Request location permission if not granted
                    requestPermissions(new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, LOCATION_PERMISSION_REQUEST_CODE);
                }

                // Get the user's last known location and move the camera there
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

// RESTRICTION TO QUEZON CITY.
///*
                        Geocoder geocoder = new Geocoder(getContext());

                        try {
                            List<android.location.Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                            if (addresses != null && addresses.size() > 0) {
                                Address address = addresses.get(0);

                                // You can now extract address components
                                String completeAddress = address.getAddressLine(0); // Full street address
                                String city = address.getLocality();
                                String state = address.getAdminArea();
                                String postalCode = address.getPostalCode();
                                String country = address.getCountryName();

                                /*
                                https://quezoncity.com/000001a/000001b/qc+links/backups/quezon+city+zip+code+6.html
                                1105	1102	1106	1116	1109	1111	1119	1110	1115	1126	1120	1101	1117	1100	1121
                                1128	1104	1112	1123	1113	1125	1118	1122	1114	1127	1124	1103	1108	1107
                                 */

                                if (completeAddress.contains("Metro Manila")) {

                                    if (completeAddress.contains("1105") || completeAddress.contains("1102") || completeAddress.contains("1106") || completeAddress.contains("1116") ||
                                            completeAddress.contains("1109") || completeAddress.contains("1111") || completeAddress.contains("1119") || completeAddress.contains("1110") ||
                                            completeAddress.contains("1115") || completeAddress.contains("1126") || completeAddress.contains("1120") || completeAddress.contains("1101") ||
                                            completeAddress.contains("1117") || completeAddress.contains("1100") || completeAddress.contains("1121") || completeAddress.contains("1128") ||
                                            completeAddress.contains("1104") || completeAddress.contains("1112") || completeAddress.contains("1123") || completeAddress.contains("1113") ||
                                            completeAddress.contains("1125") || completeAddress.contains("1118") || completeAddress.contains("1122") || completeAddress.contains("1114") ||
                                            completeAddress.contains("1127") || completeAddress.contains("1124") || completeAddress.contains("1103") || completeAddress.contains("1108") ||
                                            completeAddress.contains("1107") || completeAddress.contains("Quezon City")) {

                                        isUserInQuezonCity = true;
                                    }

                                    else
                                        isUserInQuezonCity = false;
                                }

                                else
                                    isUserInQuezonCity = false;

                            } else {
                                // Geocoder couldn't find an address for the given latitude and longitude
                            }
                        } catch (IOException e) {
                            // Handle geocoding errors (e.g., network issues, service not available)
                            throw new RuntimeException(e);
                        }
//*/

                        if (isUserInQuezonCity) {
                            // The user is within Quezon City
                            // Create an instance of the AddGlimpseFragment
                            AddGlimpseFragment addGlimpseFragment = new AddGlimpseFragment();

                            if (getBundle != null) {
                                String userID = getBundle.getString("userId");

                                // Create a Bundle to pass the userId as an argument
                                Bundle args = new Bundle();
                                args.putString("userId", userID); // Replace "your_user_id_here" with the actual user ID
                                args.putString("fromHome", "From Home Fragment");
                                addGlimpseFragment.setArguments(args);
                            }

                            // Show the AddGlimpseFragment as a dialog
                            FragmentManager fragmentManager = getChildFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            addGlimpseFragment.show(transaction, "add_glimpse_dialog"); // You can provide a tag for the dialog
                        }

                        else {
                            // The user is outside Quezon City
                            fab.setEnabled(false);

                            Toast.makeText(getContext(), "You are outside Quezon City.", Toast.LENGTH_LONG).show();

                            // Create a Handler to introduce a delay
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Display the second Toast with LENGTH_LONG duration after a delay
                                    Toast.makeText(getContext(), "You won't be able to post.", Toast.LENGTH_LONG).show();
                                }
                            }, 3500); // 2000 milliseconds (2 seconds) delay
                        }
                    }
                });
            }
        });

        // For Reading the Database
        // Initialize Firebase Database reference
        // Reference to the "Location" node in Firebase

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        DatabaseReference mobileUsersReference = FirebaseDatabase.getInstance().getReference().child("MobileUsers");

        // Create a list to store the retrieved data as maps
        List<Map<String, String>> dataMapList = new ArrayList<>();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    linearLayout.setVisibility(View.VISIBLE);
                    dataMapList.clear();
                    postList.clear();
                    for (DataSnapshot placeSnapshot : snapshot.getChildren()) {

                        // Extract user posts data
                        String postCaption = placeSnapshot.child("caption").getValue(String.class);
                        String postImage = placeSnapshot.child("imageUrl").getValue(String.class);
                        Long postTimestamp = placeSnapshot.child("timestamp").getValue(Long.class);
                        String postUserId = placeSnapshot.child("userId").getValue(String.class);
                        String postUsername = placeSnapshot.child("username").getValue(String.class);
                        String postProfilePic = placeSnapshot.child("profilePictureUrl").getValue(String.class);

                        String stringTimeStamp = String.valueOf(postTimestamp);

                        // Create a data map and add the values
                        Map<String, String> dataMap = new HashMap<>();

                        dataMap.put("caption", postCaption);
                        dataMap.put("imageUrl", postImage);
                        dataMap.put("timestamp", stringTimeStamp);
                        dataMap.put("userId", postUserId);
                        dataMap.put("postUsername", postUsername);
                        dataMap.put("postProfilePic",postProfilePic);

                        // Add the data map to the list
                        dataMapList.add(dataMap);
                    }

                    // Now, dataMapList contains the data in reverse order as maps
                    // You can iterate over it to access the data
                    for (Map<String, String> dataMap : dataMapList) {
                        String caption = dataMap.get("caption");
                        String imageUrl = dataMap.get("imageUrl");
                        String timestamp = dataMap.get("timestamp");
                        String username = dataMap.get("postUsername");
                        String profurl = dataMap.get("postProfilePic");
                        Log.d("PostDebug", "Adding post with timestamp: " + timestamp);


                        if (timestamp != null && !timestamp.equalsIgnoreCase("null") && !timestamp.isEmpty()) {
                            long timestampLong = Long.parseLong(timestamp);
                            String timeAgo = getTimeAgo(timestampLong);
                            // Create a new Post object and add it to the list
                            Post newPost = new Post(username, profurl, imageUrl, caption, timeAgo);
                            postList.add(newPost);
                        }

                                        // Sort the postList based on converted timestamps in descending order (latest first)
                        postList.sort((post1, post2) -> {
                            long timestampMillis1 = convertTimeAgoToMillis(post1.getPostTime());
                            long timestampMillis2 = convertTimeAgoToMillis(post2.getPostTime());
                            return Long.compare(timestampMillis2, timestampMillis1);
                        });

                        postAdapter.notifyDataSetChanged();



                    }
                }
                else {
                    Toast.makeText(getContext(), "No Activity Posted.", Toast.LENGTH_LONG).show();
                    linearLayout.setVisibility(View.GONE);
                }
                // Update the RecyclerView adapter with the retrieved user groups
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }

    public String getTimeAgo(Long timestamp) {

        //long convertedTimestamp = Long.parseLong(timestamp);

        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - timestamp;

        // Define time intervals in milliseconds
        long minuteInMillis = 60 * 1000;
        long hourInMillis = 60 * minuteInMillis;
        long dayInMillis = 24 * hourInMillis;
        long weekInMillis = 7 * dayInMillis;

        if (timeDifference < minuteInMillis) {
            // Less than a minute ago
            return "just now";
        } else if (timeDifference < hourInMillis) {
            // Minutes ago
            int minutesAgo = (int) (timeDifference / minuteInMillis);
            return minutesAgo + " " + (minutesAgo == 1 ? "min" : "mins") + " ago";
        } else if (timeDifference < dayInMillis) {
            // Hours ago
            int hoursAgo = (int) (timeDifference / hourInMillis);
            return hoursAgo + " " + (hoursAgo == 1 ? "hour" : "hours") + " ago";
        } else if (timeDifference < weekInMillis) {
            // Days ago
            int daysAgo = (int) (timeDifference / dayInMillis);
            return daysAgo + " " + (daysAgo == 1 ? "day" : "days") + " ago";
        } else {
            // More than a week ago, return the date
            Date date = new Date(timestamp);
            // Format the date as needed, e.g., using SimpleDateFormat
            // SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            // return dateFormat.format(date);
            return date.toString();
        }
    }

    private void loadFragment(Fragment fragment) {
        //Bundle bundle = new Bundle();
        //bundle.putString("userId", userId);
        //fragment.setArguments(bundle);

        // Use Bundle to pass values
        Bundle bundle = new Bundle();

        // Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            bundle.putString("userId", userID);
            fragment.setArguments(bundle);
        }

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    /*@SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void displayDay(int dayIndex) {
        if (dayIndex >= 0 && dayIndex < dailyForecasts.size()) {
            List<String> sortedDates = new ArrayList<>(dailyForecasts.keySet());
            Collections.sort(sortedDates);

            if (dayIndex < sortedDates.size()) {
                // Define a custom comparator to sort the dates in ascending order
                sortedDates.sort(new Comparator<String>() {
                    final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.US);

                    @Override
                    public int compare(String date1, String date2) {
                        try {
                            Date d1 = dateFormat.parse(date1);
                            Date d2 = dateFormat.parse(date2);
                            assert d1 != null;
                            return d1.compareTo(d2);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0; // Handle parsing error gracefully
                        }
                    }
                });

                String date = sortedDates.get(dayIndex);
                List<Double> temperatures = dailyForecasts.get(date);
                assert temperatures != null;
                double averageTemperature = calculateAverage(temperatures);


                // Extract and display the weather condition
                String weatherCondition = getWeatherCondition(date, forecasts);

                // Extract and display the rain percentage
                String rainPercentage = getRainPercentage(date);

                // Display the daily forecast for the selected day
                dateText.setText(date + "\n");
                wC.setText(weatherCondition + "\n");
                rP.setText(rainPercentage + "%\n");
                textView.setText(String.format("%.2f", averageTemperature) + "°C\n");
            }
        }
    }

    private String getRainPercentage(String date) {
        try {
            for (int i = 0; i < forecasts.length(); i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                String dateTime = forecast.getString("dt_txt");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat compareFormatter = new SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.US);

                Date forecastDate = dateFormatter.parse(dateTime);
                assert forecastDate != null;
                String formattedDate = compareFormatter.format(forecastDate);

                if (formattedDate.equals(date)) {
                    JSONObject rainObject = forecast.optJSONObject("rain");
                    if (rainObject != null) {
                        double rainVolume3h = 0.0; // Default value if rain data is not available
                        if (rainObject.has("3h")) {
                            // Extract the rain volume for the last 3 hours (in mm)
                            rainVolume3h = rainObject.getDouble("3h");
                        }

                        // Calculate the rain percentage based on the rain volume
                        int rainPercentage = (int) ((rainVolume3h) * 100);

                        // Clamp the rain percentage to a maximum of 100%
                        if (rainPercentage > 100) {
                            rainPercentage = 100;
                        }

                        // Get the rain icon based on the rain percentage
                        String rainIcon = getRainIcon(rainPercentage);

                        // Construct the output string with the icon and percentage
                        return rainIcon + " " + rainPercentage;
                    }
                }
            }
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
        return "☀️0"; // Default value if rain percentage cannot be determined
    }

    // Define a function to map rain percentages to icons
    private String getRainIcon(int rainPercentage) {
        if (rainPercentage <= 0) {
            return "☀️"; // No Rain icon (Clear weather)
        } else if (rainPercentage < 20) {
            return "☔️"; // Light rain icon
        } else if (rainPercentage < 60) {
            return "🌧️"; // Moderate rain icon
        } else {
            return "⛈️"; // Heavy rain icon
        }
    }


    private String getWeatherCondition(String date, JSONArray forecasts) {
        try {
            for (int i = 0; i < forecasts.length(); i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                String dateTime = forecast.getString("dt_txt");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat compareFormatter = new SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.US);

                Date forecastDate = dateFormatter.parse(dateTime);
                assert forecastDate != null;
                String formattedDate = compareFormatter.format(forecastDate);

                if (formattedDate.equals(date)) {
                    JSONArray weatherArray = forecast.getJSONArray("weather");
                    if (weatherArray.length() > 0) {
                        JSONObject weather = weatherArray.getJSONObject(0);
                        String description = weather.getString("description");

                        // Map weather descriptions to icons here
                        return mapWeatherToIcon(description);
                    }
                }
            }
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
        return "Not Available"; // Default value if weather icon cannot be determined
    }

    // Define a function to map weather descriptions to icons
    private String mapWeatherToIcon(String description) {
        // You can implement your own mapping logic here.
        // For example, you can use a switch statement or if-else statements to map descriptions to icons.
        // Here's a simple example using a few common weather descriptions:
        switch (description.toLowerCase()) {
            case "clear sky":
                return "☀️";

            case "few clouds":
                return "🌤️";

            case "scattered clouds":
                return "⛅";

            case "broken clouds":
            case "overcast clouds":
                return "☁️";

            case "light rain":
            case "moderate rain":
            case "heavy intensity rain":
            case "very heavy rain":
                return "🌦️";

            case "rain":
            case "light intensity drizzle":
            case "drizzle":
            case "heavy intensity drizzle":
            case "light intensity drizzle rain":
            case "drizzle rain":
            case "heavy intensity drizzle rain":
            case "shower rain and drizzle":
            case "heavy shower rain and drizzle":
            case "shower drizzle":
            case "extreme rain":
                return "🌧️";


            case "thunderstorm":
            case "thunderstorm with light rain":
            case "thunderstorm with rain":
            case "thunderstorm with heavy rain":
            case "light thunderstorm":
            case "heavy thunderstorm":
            case "ragged thunderstorm":
            case "thunderstorm with light drizzle":
            case "thunderstorm with drizzle":
            case "thunderstorm with heavy drizzle":
                return "⛈️";

            case "snow":
                return "🌨️";

            case "mist":
            case "smoke":
            case "haze":
            case "sand/dust whirls":
            case "fog":
            case "sand":
            case "dust":
            case "volcanic ash":
            case "squalls":
            case "tornado":
                return "🌫️";

            default:
                return "❓"; // If the description doesn't match any known icon, return a question mark or handle it as needed.
        }
    }

    private void getWeatherForecastByLocationName() {
        // Replace with your API key
        String api_key = "4f37738f182c49802496e7f263ff8797";
        String weather_url = "https://api.openweathermap.org/data/2.5/forecast?q=" +
                "Quezon City" + "&appid=" + api_key;

        // Instantiate the RequestQueue.
        com.android.volley.RequestQueue queue = Volley.newRequestQueue(requireContext());

        // Request a string response from the provided URL.
        @SuppressLint("SetTextI18n") StringRequest stringRequest = new StringRequest(Request.Method.GET, weather_url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        // Assign the list of forecasts from the JSON to the class-level variable
                        forecasts = jsonObject.getJSONArray("list");

                        // Clear previous text
                        textView.setText("");

                        // Group forecasts by day
                        dailyForecasts.clear(); // Clear existing data

                        // Get the current date in the local time zone
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeZone(TimeZone.getTimeZone("Philippines/Manila")); // Set your desired time zone
                        Date currentDate = calendar.getTime();

                        // Format the current date and time as "yyyy-MM-dd HH:mm:ss"
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        for (int i = 0; i < forecasts.length(); i++) {
                            JSONObject forecast = forecasts.getJSONObject(i);

                            // Extract the date and time
                            String dateTime = forecast.getString("dt_txt");

                            // Parse the date string into a Date object
                            Date forecastDateTime = dateTimeFormat.parse(dateTime);

                            // Check if the forecast date and time are greater than or equal to the current date and time
                            if (forecastDateTime != null && forecastDateTime.compareTo(currentDate) >= 0) {
                                String date = new SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.US).format(forecastDateTime);

                                JSONObject main = forecast.getJSONObject("main");
                                double temperatureKelvin = main.getDouble("temp");
                                double temperatureCelsius = temperatureKelvin - 273.15;

                                // Add the temperature to the daily forecast list
                                if (!dailyForecasts.containsKey(date)) {
                                    dailyForecasts.put(date, new ArrayList<>());
                                }
                                Objects.requireNonNull(dailyForecasts.get(date)).add(temperatureCelsius);
                            }
                        }

                        // After fetching and processing the data, display the first day's information
                        displayDay(currentDayIndex);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        textView.setText("JSON Parsing Error");
                    } catch (ParseException e) {
                        e.printStackTrace();
                        textView.setText("Date Parsing Error");
                    }
                },
                error -> textView.setText("Error fetching data."));

        queue.add(stringRequest);
    }

    private void showPreviousDay() {
        if (currentDayIndex > 0) {
            currentDayIndex--;
            displayDay(currentDayIndex);
        }
    }

    private void showNextDay() {
        if (currentDayIndex < 4) { // Assuming you want to show 5 days of forecasts
            currentDayIndex++;
            displayDay(currentDayIndex);
        }
    }

    private double calculateAverage(List<Double> numbers) {
        double sum = 0;
        for (double num : numbers) {
            sum += num;
        }
        return sum / numbers.size();
    }
    */

    public long convertTimeAgoToMillis(String timeAgo) {
        // Assuming "timeAgo" is in the format "X unit(s) ago"
        if ("just now".equalsIgnoreCase(timeAgo.trim())) {
            // "just now" case means the timestamp is already in milliseconds
            return System.currentTimeMillis();
        }

        String[] parts = timeAgo.split(" ");

        if (parts.length >= 2) {
            int value;
            try {
                value = Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                // Handle the case where the value is not a valid integer
                return -1;
            }

            String unit = parts[1].toLowerCase();

            long multiplier;
            switch (unit) {
                case "min":
                case "mins":
                    multiplier = 60 * 1000L;
                    break;
                case "hour":
                case "hours":
                    multiplier = 60 * 60 * 1000L;
                    break;
                case "day":
                case "days":
                    multiplier = 24 * 60 * 60 * 1000L;
                    break;
                // Add more cases for other units if needed

                default:
                    // Unknown unit or unsupported unit, return -1 or handle accordingly
                    return -1;
            }

            // Calculate the timestamp in milliseconds
            return System.currentTimeMillis() - value * multiplier;
        }

        // Invalid format, return -1 or handle accordingly
        return -1;
    }


}

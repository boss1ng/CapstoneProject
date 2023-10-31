package com.example.qsee;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsInstructions extends DialogFragment {


    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /*
        // Retrieve the screen resolution
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) requireContext().getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;

            ScrollView scrollView = new ScrollView(getActivity());
            scrollView.setLayoutParams(new ScrollView.LayoutParams(
                    screenWidth - 50, screenHeight - 100
            ));
        }
         */

        setCancelable(true);

        // Create a new Dialog instance
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Set a custom layout for the dialog
        dialog.setContentView(R.layout.fragment_maps_instructions);

        // Customize the width of the dialog (75% of screen width)
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 1);
        dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));



        return dialog;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the custom layout for this dialog fragment
        View view = inflater.inflate(R.layout.fragment_maps_instructions, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //setCancelable(true); // Allow canceling on outside click

        Button btnStopRoute = view.findViewById(R.id.btnStop);
        btnStopRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss(); // Dismiss the dialog

                // In the fragment or activity where you want to navigate
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                MapsFragment mapsFragment = new MapsFragment();

                /*
                BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setVisibility(View.GONE);

                LinearLayout llFilter = getView().findViewById(R.id.filterMenu);
                llFilter.setVisibility(View.GONE);

                LinearLayout llLoc = getView().findViewById(R.id.directionsCont);
                llLoc.setVisibility(View.GONE);

                LinearLayout llButt = getView().findViewById(R.id.overviewCont);
                llButt.setVisibility(View.GONE);
                 */

                /*
                    // Retrieve selected categories from Bundle arguments
                    Bundle getBundle = getArguments();

                    if (getBundle != null) {
                        String isStop = getBundle.getString("isStop");
                        if (isStop.equals("STOP")) {

                            Toast.makeText(getContext(), "STOPPED", Toast.LENGTH_SHORT).show();

                            BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
                            bottomNavigationView.setVisibility(View.GONE);

                            LinearLayout llFilter = getView().findViewById(R.id.filterMenu);
                            llFilter.setVisibility(View.GONE);

                            LinearLayout llLoc = getView().findViewById(R.id.directionsCont);
                            llLoc.setVisibility(View.GONE);

                            LinearLayout llButt = getView().findViewById(R.id.overviewCont);
                            llButt.setVisibility(View.GONE);
                        }
                    }
                 */

                // Use Bundle to pass values
                Bundle bundle = new Bundle();
                bundle.putString("isStop", "STOP");
                mapsFragment.setArguments(bundle);

                // Replace the current fragment with the receiving fragment
                transaction.replace(R.id.fragment_container, mapsFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();

        TextView textView = view.findViewById(R.id.placeNameTextView);
        textView.setText("ROUTE INFORMATION");

        if (getBundle != null) {
            String placeName = getBundle.getString("placeName");

            Double passedCurrentUserLocationLat = getBundle.getDouble("userCurrentLatitude");
            Double passedCurrentUserLocationLong = getBundle.getDouble("userCurrentLongitude");
            String destinationLatitude = getBundle.getString("destinationLatitude");
            String destinationLongitude = getBundle.getString("destinationLongitude");

            Double destLatitude = Double.parseDouble(destinationLatitude);
            Double destLongitude = Double.parseDouble(destinationLongitude);

            // Use Google Directions API to request directions
            String apiKey = getString(R.string.google_maps_api_key);
            String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + passedCurrentUserLocationLat + "," + passedCurrentUserLocationLong +
                    "&destination=" + destLatitude + "," + destLongitude +
                    "&key=" + apiKey;

            String url1 = "https://maps.googleapis.com/maps/api/directions/json?origin=14.6440,121.0481&destination=14.6485387,121.0499399&mode=driving&key=AIzaSyAwTBhjMDtD74Nvqz7eUbN81v93SLhM3IU";

            // Create an instance of DirectionsTask and execute it
            DirectionsTask directionsTask = new DirectionsTask(url);
            directionsTask.execute();
        }



        /*
        LinearLayout dynamicLayoutContainer = view.findViewById(R.id.instructionsCont); // Replace with your container ID

        int numberOfIterations = 5; // Set the desired number of iterations

        for (int i = 0; i < numberOfIterations; i++) {
            // Create a new LinearLayout for each iteration
            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            // Create an ImageView
            ImageView imageView = new ImageView(getActivity());
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            //imageView.setImageResource(R.drawable.your_image); // Set your image resource

            // Create two TextViews
            TextView textView1 = new TextView(getActivity());
            textView1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            textView1.setText("Text1");

            TextView textView2 = new TextView(getActivity());
            textView2.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            textView2.setText("Text2");

            // Add the ImageView and TextViews to the LinearLayout
            linearLayout.addView(imageView);
            linearLayout.addView(textView1);
            linearLayout.addView(textView2);

            // Add the new LinearLayout to the container
            dynamicLayoutContainer.addView(linearLayout);
        }
         */

        return view;
    }

    public class DirectionsTask extends AsyncTask<Void, Void, String> {

        String passedUrl;
        public DirectionsTask(String url) {
            passedUrl = url;
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

                // Check the status of the response
                String status = jsonResponse.getString("status");
                if (status.equals("OK")) {
                    JSONArray routes = jsonResponse.getJSONArray("routes");

                    LinearLayout dynamicLayoutContainer = getView().findViewById(R.id.instructionsCont); // Replace with your container ID

                    int numberOfIterations = 10; // Set the desired number of iterations

                    for (int i = 0; i < routes.length(); i++) {
                        JSONObject route = routes.getJSONObject(i);

                        JSONArray legs = route.getJSONArray("legs");

                        for (int j = 0; j < legs.length(); j++) {
                            JSONObject leg = legs.getJSONObject(j);

                            JSONArray steps = leg.getJSONArray("steps");

                            for (int k = 0; k < steps.length(); k++) {
                                JSONObject step = steps.getJSONObject(k);

                                // Create a new LinearLayout for each iteration
                                LinearLayout linearLayout = new LinearLayout(getActivity());

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

                                // Create a variable to store the drawable resource ID
                                int drawableResource = R.drawable.straight; // Default drawable

                                // Map maneuver types to corresponding drawable resource IDs
                                switch (maneuverType) {
                                    case "keep-left":
                                        drawableResource = R.drawable.keep_left;
                                        break;
                                    case "keep-right":
                                        drawableResource = R.drawable.keep_right;
                                        break;
                                    case "ferry":
                                        drawableResource = R.drawable.ferry;
                                        break;
                                    case "ferry-train":
                                        drawableResource = R.drawable.ferry_train;
                                        break;
                                    case "fork-left":
                                        drawableResource = R.drawable.fork_left;
                                        break;
                                    case "fork-right":
                                        drawableResource = R.drawable.fork_right;
                                        break;
                                    case "merge":
                                        drawableResource = R.drawable.merge;
                                        break;
                                    case "ramp-left":
                                        drawableResource = R.drawable.ramp_left;
                                        break;
                                    case "ramp-right":
                                        drawableResource = R.drawable.ramp_right;
                                        break;
                                    case "roundabout-left":
                                        drawableResource = R.drawable.roundabout_left;
                                        break;
                                    case "roundabout-right":
                                        drawableResource = R.drawable.roundabout_right;
                                        break;
                                    case "straight":
                                        drawableResource = R.drawable.straight;
                                        break;
                                    case "turn-right":
                                        drawableResource = R.drawable.turn_right;
                                        break;
                                    case "turn-left":
                                        drawableResource = R.drawable.turn_left;
                                        break;
                                    case "turn-sharp-right":
                                        drawableResource = R.drawable.turn_sharp_right;
                                        break;
                                    case "turn-sharp-left":
                                        drawableResource = R.drawable.turn_sharp_left;
                                        break;
                                    case "turn-slight-right":
                                        drawableResource = R.drawable.turn_slight_right;
                                        break;
                                    case "turn-slight-left":
                                        drawableResource = R.drawable.turn_slight_left;
                                        break;
                                    case "uturn-right":
                                        drawableResource = R.drawable.uturn_right;
                                        break;
                                    case "uturn-left":
                                        drawableResource = R.drawable.uturn_left;
                                        break;

                                    default:
                                        drawableResource = R.drawable.straight;
                                        break;
                                }

                                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                                ));
                                linearLayout.setPadding(25, 25, 25, 25);
                                linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                                // Create an ImageView
                                ImageView imageView = new ImageView(getActivity());
                                imageView.setLayoutParams(new LinearLayout.LayoutParams(
                                        200, 200
                                ));
                                imageView.setImageResource(drawableResource); // Set your image resource
                                imageView.setPadding(0, 0, 25, 0);
                                //android:layout_gravity="center_vertical"
                                //android:layout_weight="1"

                                View horizontalLine = new View(getActivity());
                                horizontalLine.setLayoutParams(new ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        1 // Height of the horizontal line in pixels or dp
                                ));
                                horizontalLine.setBackgroundColor(Color.WHITE); // Set the color of the line

                                    LinearLayout linearLayoutVertical = new LinearLayout(getActivity());
                                    linearLayoutVertical.setLayoutParams(new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                                    ));
                                    linearLayoutVertical.setOrientation(LinearLayout.VERTICAL);

                                    // Create two TextViews
                                    TextView textView1 = new TextView(getActivity());
                                    textView1.setLayoutParams(new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                                    ));
                                    //textView1.setText("Text1");
                                    //textView1.setText(distance);
                                    // Extract the numerical value from the distance string
                                    double distanceValue = Double.parseDouble(distance.replaceAll("[^0-9.]+", ""));

                                    if (distance.contains("km") && distanceValue < 1.0) {
                                        // Convert the distance to meters
                                        int meters = (int) (distanceValue * 1000);
                                        String distanceInMeters = meters + " m";
                                        // Use distanceInMeters as needed
                                        textView1.setText(distanceInMeters);
                                    } else {
                                        // Use the original distance string (it's already in meters or more than 1 km)
                                        textView1.setText(distance);
                                    }

                                    textView1.setTextColor(Color.WHITE); // Set the text color to white
                                    textView1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD)); // Make it bold
                                    textView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // Set font size to 16sp
                                    textView1.setPadding(0, 0, 0, 10);

                                    TextView textView2 = new TextView(getActivity());
                                    textView2.setLayoutParams(new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                                    ));
                                    //textView2.setText("Text2");
                                    textView2.setText(plainTextInstructions);
                                    textView2.setTextColor(Color.WHITE); // Set the text color to white



                                // Add the ImageView and TextViews to the LinearLayout
                                linearLayout.addView(imageView);
                                linearLayout.addView(linearLayoutVertical);
                                    linearLayoutVertical.addView(textView1);
                                    linearLayoutVertical.addView(textView2);

                                // Add the new LinearLayout to the container
                                dynamicLayoutContainer.addView(linearLayout);
                                dynamicLayoutContainer.addView(horizontalLine);
                            }
                        }
                    }

                    /*
                    for (int i = 0; i < routes.length(); i++) {

                        // Create a new LinearLayout for each iteration
                        LinearLayout linearLayout = new LinearLayout(getActivity());

                        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        linearLayout.setPadding(25, 25, 25, 25);
                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                        // Create an ImageView
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                                200, 200
                        ));
                        imageView.setImageResource(R.drawable.straight); // Set your image resource
                        imageView.setPadding(0, 0, 25, 0);
                        //android:layout_gravity="center_vertical"
                        //android:layout_weight="1"

                            LinearLayout linearLayoutVertical = new LinearLayout(getActivity());
                            linearLayoutVertical.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                            ));
                            linearLayoutVertical.setOrientation(LinearLayout.VERTICAL);

                            // Create two TextViews
                            TextView textView1 = new TextView(getActivity());
                            textView1.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                            ));
                            textView1.setText("Text1");
                            textView1.setTextColor(Color.WHITE); // Set the text color to white
                            textView1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD)); // Make it bold
                            textView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // Set font size to 16sp
                            textView1.setPadding(0, 0, 0, 10);

                            TextView textView2 = new TextView(getActivity());
                            textView2.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                            ));
                            textView2.setText("Text2");
                            textView2.setTextColor(Color.WHITE); // Set the text color to white

                        // Add the ImageView and TextViews to the LinearLayout
                        linearLayout.addView(imageView);
                        linearLayout.addView(linearLayoutVertical);
                        linearLayoutVertical.addView(textView1);
                        linearLayoutVertical.addView(textView2);

                        // Add the new LinearLayout to the container
                        dynamicLayoutContainer.addView(linearLayout);


                        //
                        // Inflate the XML layout
                        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_maps_instructions, dynamicLayoutContainer, false);

                        // Find and set values for views within the inflated layout
                        ImageView imageView = view.findViewById(R.id.imageViewDirections);
                        TextView textViewDistance = view.findViewById(R.id.textViewDistance);
                        TextView textViewDirection = view.findViewById(R.id.textViewDirection);
                        Button btnDone = view.findViewById(R.id.btnDone);

                        // Customize the views or set data as needed
                        imageView.setImageResource(R.drawable.straight);
                        textViewDistance.setText("Distance " + i);
                        textViewDirection.setText("Direction " + i);
                        btnDone.setText("Button " + i);

                        // Add the inflated layout to the container
                        dynamicLayoutContainer.addView(view);
                        //

                    }
                     */


                } else {
                    // Handle the case when the API request returns a status other than "OK"
                }
            }

            catch (JSONException e) {
                e.printStackTrace();
                // Handle JSON parsing errors
            }

        }
    }
}

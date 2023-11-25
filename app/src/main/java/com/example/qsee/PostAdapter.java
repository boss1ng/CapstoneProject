package com.example.qsee;

import static com.android.volley.VolleyLog.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private List<Post> postList; // Assuming Post is a class you've defined for your data

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            // Inflate header layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_layout, parent, false);
            return new HeaderViewHolder(view, view.getContext());
        } else {
            // Inflate item layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_layout, parent, false);
            return new PostViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            // Handle header data binding if needed
        } else {
            // Adjust position by -1 to account for the header
            Post post = postList.get(position - 1);
            PostViewHolder postViewHolder = (PostViewHolder) holder;
            if (post.getCaption().isEmpty()) {
                postViewHolder.infoBelow.setVisibility(View.GONE);
                postViewHolder.userName.setText(post.getUserName());
                Picasso.get().load(post.getPostImageUrl()).into(postViewHolder.postImage);
                Picasso.get().load(post.getUserProfileImageUrl()).into(postViewHolder.userProfileImage);
                postViewHolder.postTime.setText(post.getPostTime());
            } else {
                postViewHolder.infoBelow.setVisibility(View.VISIBLE);
                postViewHolder.userName.setText(post.getUserName());
                Picasso.get().load(post.getPostImageUrl()).into(postViewHolder.postImage);
                postViewHolder.postCaption.setText(post.getCaption());
                Picasso.get().load(post.getUserProfileImageUrl()).into(postViewHolder.userProfileImage);
                postViewHolder.usernameBelow.setText(post.getUserName());
                postViewHolder.postTime.setText(post.getPostTime());
            }
        }
    }

    @Override
    public int getItemCount() {
        // +1 for header
        return postList.size() + 1;
    }

    // ViewHolder for header
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        // Initialize your header views here if needed
        private Context context;
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
        private PostAdapter postAdapter;
        private List<Post> postList; // Your data source
        HeaderViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            // Initialize header views
            textView = itemView.findViewById(R.id.textView);
            dateText = itemView.findViewById(R.id.date);
            wC = itemView.findViewById(R.id.weatherCondition);
            rP = itemView.findViewById(R.id.rainPercentage);
            imageView12 = itemView.findViewById(R.id.imageView12);
            imageView13 = itemView.findViewById(R.id.imageView13);
            imageView14 = itemView.findViewById(R.id.imageView14);
            imageView15 = itemView.findViewById(R.id.imageView15);
            imageView16 = itemView.findViewById(R.id.imageView16);
            imageView17 = itemView.findViewById(R.id.imageView17);

            loadImages();

            ImageView previousImageView = itemView.findViewById(R.id.previousButton);
            ImageView nextImageView = itemView.findViewById(R.id.nextButton);

            // Set click listeners for previous and next ImageViews
            previousImageView.setOnClickListener(v -> showPreviousDay());
            nextImageView.setOnClickListener(v -> showNextDay());

            // Automatically load the weather forecast for Quezon City
            getWeatherForecastByLocationName();

            // Initially, display the first day's information (dayIndex = 0)
            displayDay(currentDayIndex);

            // Retrieve selected categories from Bundle arguments
            //Bundle getBundle = getArguments();


        }
        @SuppressLint({"SetTextI18n", "DefaultLocale"})
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
                    Log.d("HeaderFragment", "Setting text for dateText: " + date);
                    dateText.setText(date);

                    wC.setText(weatherCondition + "\n");
                    rP.setText(rainPercentage + "%\n");
                    textView.setText(String.format("%.2f", averageTemperature) + "°C\n");
                }
            }
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

                Log.d(TAG,"NEXT BUTTON");
            }
        }

        private double calculateAverage(List<Double> numbers) {
            double sum = 0;
            for (double num : numbers) {
                sum += num;
            }
            return sum / numbers.size();
        }
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

        private void loadImages(){
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/sliders%2Fslider_1.jpg?alt=media&token=83a13343-b5bd-42bd-98ca-a234e13be324").into(imageView12);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/sliders%2Fslider_2.jpg?alt=media&token=dd113988-1a86-40e1-9e42-e8947e798b67").into(imageView13);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/sliders%2Fslider_3.jpg?alt=media&token=8a9a8d6b-5736-4b81-b2a2-579a1b08d1c6").into(imageView14);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/sliders%2Fslider_4.jpg?alt=media&token=0c163b54-da16-4514-9869-5262e8824108").into(imageView15);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/sliders%2Fslider_5.jpg?alt=media&token=36192c6c-0818-4ee5-9b6c-dc3ee1809a71").into(imageView16);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/sliders%2Fslider_6.jpg?alt=media&token=24bf286a-554f-4f74-8016-8c9eabe0b7de").into(imageView17);


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
            com.android.volley.RequestQueue queue = Volley.newRequestQueue(context);

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


    }



    // ViewHolder for items (existing ViewHolder)
}

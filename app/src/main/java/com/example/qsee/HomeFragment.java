package com.example.qsee;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.qsee.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class HomeFragment extends Fragment {

    private String api_key = "4f37738f182c49802496e7f263ff8797"; // Replace with your API key
    private TextView textView;
    private TextView dateText;
    private TextView wC;
    private TextView rP;
    private EditText locationEditText;
    private Button getWeatherButton;
    private Button previousButton;
    private Button nextButton;
    private JSONArray forecasts;
    private int currentDayIndex = 0; // Track the currently displayed day index.
    private Map<String, List<Double>> dailyForecasts = new HashMap<>(); // Declare dailyForecasts as a class-level variable

    public HomeFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        textView = view.findViewById(R.id.textView);
        dateText = view.findViewById(R.id.date);
        wC = view.findViewById(R.id.weatherCondition);
        rP = view.findViewById(R.id.rainPercentage);

        ImageView previousImageView = view.findViewById(R.id.previousButton);
        ImageView nextImageView = view.findViewById(R.id.nextButton);

        // Set click listeners for previous and next ImageViews
        previousImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreviousDay();
            }
        });

        nextImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextDay();
            }
        });

        // Automatically load the weather forecast for Quezon City
        getWeatherForecastByLocationName("Quezon City");

        return view;
    }

    private void displayDay(int dayIndex) {
        if (dailyForecasts != null && dayIndex >= 0 && dayIndex < dailyForecasts.size()) {
            List<String> sortedDates = new ArrayList<>(dailyForecasts.keySet());
            Collections.sort(sortedDates);

            String date = sortedDates.get(dayIndex);
            List<Double> temperatures = dailyForecasts.get(date);
            double averageTemperature = calculateAverage(temperatures);

            // Extract and display the weather condition
            String weatherCondition = getWeatherCondition(date, forecasts);

            // Extract and display the rain percentage
            int rainPercentage = getRainPercentage(date);

            // Display the daily forecast for the selected day
            dateText.setText(date + "\n"); // Display day in the format "Thursday, MMMM dd yyyy"
            wC.setText(weatherCondition + "\n");
            rP.setText(rainPercentage + "%\n");
            textView.setText(String.format("%.2f", averageTemperature) + "°C\n");
        }
    }

    private int getRainPercentage(String date) {
        try {
            for (int i = 0; i < forecasts.length(); i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                String dateTime = forecast.getString("dt_txt");
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat compareFormatter = new SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.US);

                Date forecastDate = dateFormatter.parse(dateTime);
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
                        int rainPercentage = (int) ((rainVolume3h / 1.0) * 100);

                        // Clamp the rain percentage to a maximum of 100%
                        if (rainPercentage > 100) {
                            rainPercentage = 100;
                        }

                        return rainPercentage;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1; // Default value if rain percentage cannot be determined
    }

    private String getWeatherCondition(String date, JSONArray forecasts) {
        try {
            for (int i = 0; i < forecasts.length(); i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                String dateTime = forecast.getString("dt_txt");
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat compareFormatter = new SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.US);

                Date forecastDate = dateFormatter.parse(dateTime);
                String formattedDate = compareFormatter.format(forecastDate);

                if (formattedDate.equals(date)) {
                    JSONArray weatherArray = forecast.getJSONArray("weather");
                    if (weatherArray.length() > 0) {
                        JSONObject weather = weatherArray.getJSONObject(0);
                        return weather.getString("description");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Not available"; // Default value if weather condition cannot be determined
    }

    private void getWeatherForecastByLocationName(String locationName) {
        String weather_url = "https://api.openweathermap.org/data/2.5/forecast?q=" +
                locationName + "&appid=" + api_key;

        // Instantiate the RequestQueue.
        com.android.volley.RequestQueue queue = Volley.newRequestQueue(requireContext());

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, weather_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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
                            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
                                    dailyForecasts.get(date).add(temperatureCelsius);
                                }
                            }

                            // Display the daily forecasts for the next 5 days including today in ascending order
                            List<String> sortedDates = new ArrayList<>(dailyForecasts.keySet());
                            Collections.sort(sortedDates);

                            int dayCount = 1; // Initialize day count to 1

                            for (String date : sortedDates) {
                                if (dayCount <= 5) { // Display 5 days, including today
                                    List<Double> temperatures = dailyForecasts.get(date);
                                    double averageTemperature = calculateAverage(temperatures);

                                    // Extract and display the weather condition
                                    String weatherCondition = getWeatherCondition(date, forecasts);

                                    // Extract and display the rain percentage
                                    int rainPercentage = getRainPercentage(date);

                                    // Append the daily forecast to the TextView


                                    dayCount++;
                                } else {
                                    break;
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            textView.setText("JSON Parsing Error");
                        } catch (ParseException e) {
                            e.printStackTrace();
                            textView.setText("Date Parsing Error");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        textView.setText("Error fetching data.");
                    }
                });

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
}

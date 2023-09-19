package com.example.qsee;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private String api_key = "4f37738f182c49802496e7f263ff8797"; // Replace with your API key
    private TextView textView;
    private EditText locationEditText;
    private Button getWeatherButton;
    private Button previousButton;
    private Button nextButton;
    private int currentDayIndex = 0; // Track the currently displayed day index.
    private Map<String, List<Double>> dailyForecasts = new HashMap<>(); // Declare dailyForecasts as a class-level variable

    public HomeFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        textView = view.findViewById(R.id.textView);
        previousButton = view.findViewById(R.id.previousButton);
        nextButton = view.findViewById(R.id.nextButton);

        // Set click listeners for previous and next buttons
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreviousDay();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextDay();
            }
        });

        // Automatically load the weather forecast for Quezon City
        getWeatherForecastByLocationName("Quezon City");

        return view;
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

    private void displayDay(int dayIndex) {
        if (dailyForecasts != null && dayIndex >= 0 && dayIndex < dailyForecasts.size()) {
            List<String> sortedDates = new ArrayList<>(dailyForecasts.keySet());
            Collections.sort(sortedDates); // Sort the dates in ascending order

            String date = sortedDates.get(dayIndex);
            List<Double> temperatures = dailyForecasts.get(date);
            double averageTemperature = calculateAverage(temperatures);

            // Display the daily forecast for the selected day
            textView.setText("Date: " + date + "\n");
            textView.append("Average Temperature: " + String.format("%.2f", averageTemperature) + "°C\n\n");
        }
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

                            // Get the list of forecasts from the JSON
                            JSONArray forecasts = jsonObject.getJSONArray("list");

                            // Clear previous text
                            textView.setText("");

                            // Group forecasts by day
                            dailyForecasts.clear(); // Clear existing data
                            for (int i = 0; i < forecasts.length(); i++) {
                                JSONObject forecast = forecasts.getJSONObject(i);

                                // Extract the date, temperature, and time
                                String dateTime = forecast.getString("dt_txt");
                                String date = dateTime.split(" ")[0];
                                JSONObject main = forecast.getJSONObject("main");
                                double temperatureKelvin = main.getDouble("temp");
                                double temperatureCelsius = temperatureKelvin - 273.15;

                                // Add the temperature to the daily forecast list
                                if (!dailyForecasts.containsKey(date)) {
                                    dailyForecasts.put(date, new ArrayList<>());
                                }
                                dailyForecasts.get(date).add(temperatureCelsius);
                            }

                            // Display the daily forecasts for the next 5 days
                            int dayCount = 0;
                            for (String date : dailyForecasts.keySet()) {
                                if (dayCount >= 5) {
                                    break;
                                }
                                List<Double> temperatures = dailyForecasts.get(date);
                                double averageTemperature = calculateAverage(temperatures);

                                // Append the daily forecast to the TextView
                                textView.append("Date: " + date + "\n");
                                textView.append("Average Temperature: " + String.format("%.2f", averageTemperature) + "°C\n\n");

                                dayCount++;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            textView.setText("JSON Parsing Error");
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

    private double calculateAverage(List<Double> numbers) {
        double sum = 0;
        for (double num : numbers) {
            sum += num;
        }
        return sum / numbers.size();
    }
}
package com.example.qsee;

public class ItineraryItem {
    private String time;
    private String activity;
    private String location;

    public ItineraryItem(String time, String activity, String location) {
        this.time = time;
        this.activity = activity;
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}


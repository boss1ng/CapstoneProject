package com.example.qsee;

public class Itinerary {
    private String time;
    private String location;
    private String activity;

    public Itinerary(String time, String location, String activity) {
        this.time = time;
        this.location = location;
        this.activity = activity;
    }



    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }
    public String getActivity(){
        return activity;
    }
}


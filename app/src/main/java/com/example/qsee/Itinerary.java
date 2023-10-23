package com.example.qsee;

public class Itinerary {
    private String time;
    private String location;

    public Itinerary(String time, String location) {
        this.time = time;
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }
}


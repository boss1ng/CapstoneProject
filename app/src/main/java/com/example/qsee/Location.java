package com.example.qsee;

public class Location {
    private String locationName;
    private String locationAddress;

    public Location() {
        // Default constructor required for calls to DataSnapshot.getValue(Location.class)
    }

    public Location(String locationName, String locationAddress) {
        this.locationName = locationName;
        this.locationAddress = locationAddress;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }
}



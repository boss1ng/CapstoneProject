package com.example.qsee;

public class Location {
    private String locationName;
    private String locationAddress;
    private String admin;

    public Location() {
        // Default constructor required for calls to DataSnapshot.getValue(Location.class)
    }

    public Location(String locationName, String locationAddress, String admin) {
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.admin = admin;
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

    public String getLocationAdmin(){
        return admin;
    }

    public void setLocationAdmin(String admin){
        this.admin = admin;
    }
}



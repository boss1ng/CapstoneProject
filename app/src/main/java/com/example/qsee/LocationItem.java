package com.example.qsee;

public class LocationItem {
    private String location;
    private String imageUrl;

    public LocationItem(String location, String imageUrl) {
        this.location = location;
        this.imageUrl = imageUrl;
    }

    public String getLocation() {
        return location;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

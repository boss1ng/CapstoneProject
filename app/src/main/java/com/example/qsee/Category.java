package com.example.qsee;

public class Category {
    private String key; // Firebase key
    private String name; // Category name
    private String iconLink; // URL to the category icon

    public Category(String key, String name, String iconLink) {
        this.key = key;
        this.name = name;
        this.iconLink = iconLink;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getIconLink() {
        return iconLink;
    }

    // You may add more properties and methods as needed
}


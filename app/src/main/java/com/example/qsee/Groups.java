package com.example.qsee;

public class Groups {
    private String groupName;
    private String admin; // Change "createdBy" to "admin"

    public Groups() {
        // Default constructor is required by Firebase
    }

    public Groups(String groupName, String admin) { // Change the constructor parameter name
        this.groupName = groupName;
        this.admin = admin; // Change "createdBy" to "admin"
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getAdmin() { // Change the getter method name
        return admin;
    }

    public void setAdmin(String admin) { // Change the setter method name
        this.admin = admin;
    }
}

package com.example.qsee;

import java.util.Random;

public class User {
    private String firstName;
    private String lastName;
    private String contactNumber;
    private String birthdate;
    private String username;
    private String password;
    private String userId;

    public void generateRandomUserId() {
        Random random = new Random();
        StringBuilder userIdBuilder = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            int randomDigit = random.nextInt(10); // Generate a random digit (0-9)
            userIdBuilder.append(randomDigit); // Append the digit to the User ID
        }

        this.userId = userIdBuilder.toString(); // Set the generated User ID
    }


    // Constructor (you can create an empty constructor if needed)
    public User() {
        // Default constructor required for Firebase Realtime Database
    }

    // Getter and Setter for FirstName
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // Getter and Setter for LastName
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Getter and Setter for ContactNumber
    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    // Getter and Setter for Birthdate
    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    // Getter and Setter for Username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Getter and Setter for Password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    // Getter and Setter for UserId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}


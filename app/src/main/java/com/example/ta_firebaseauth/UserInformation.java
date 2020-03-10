package com.example.ta_firebaseauth;

// Saving new information in Firebase Database
// Used in ProfileActivity
public class UserInformation {
    private String age;
    private String address;

    public UserInformation() {
    }

    public UserInformation(String age, String address) {
        this.age = age;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}

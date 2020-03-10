package com.example.ta_firebaseauth;

// A class for saving registration information in Firebase Database
// Used in RegisterActivity
public class SaveRegInformation {
    private String name;
    private String email;
    private String password;
    private String phone;
    private String instance;
    //private String occupation;

    private String status;

    public SaveRegInformation() {
    }

    public SaveRegInformation(String name, String email, String password, String phone, String instance) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.instance = instance;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {

        this.phone = phone;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    /*
    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }
    */

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

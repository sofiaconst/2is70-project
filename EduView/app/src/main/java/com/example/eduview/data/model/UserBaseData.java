package com.example.eduview.data.model;

public class UserBaseData {
    public String firstName;
    public String lastName;
    public UserRole role;
    public String pfp;

    public UserBaseData(String firstName, String lastName, UserRole role, String pfp) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.pfp = pfp;
    }
}

package com.example.eduview.Classes;

import static com.example.eduview.Resources.UserRole.*;

import com.example.eduview.Resources.UserRole;

public class User {
    private String id;
    private String firstName;
    private String lastName;
    private UserRole role;
    private String profileImageURL; //Store as URL?
//    private String bio; //We are removing Bio

    //Empty Constructor needed for Firebase
    public User() {}

    //User profile Creation constructor
    public User(String id, String firstName, String lastName, UserRole role) {

    }

    /*
    Getters
     */
    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    public UserRole getRole() {
        return role;
    }

    /*
    Setters
     */
    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }

    /*
    Methods
     */
    public boolean isTeacher() {
        return this.role == UserRole.TEACHER;
    }

    public boolean isStudent() {
        return this.role == UserRole.STUDENT;
    }

    public boolean isParent() {
        return this.role == UserRole.PARENT;
    }
}

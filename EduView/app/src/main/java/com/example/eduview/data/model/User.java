package com.example.eduview.data.model;

public class User {
    private String userid;
    private String firstName;
    private String lastName;
    private UserRole role;
    private String profileImageURL; //Store as URL?
//    private String bio; //We are removing Bio

    //Empty Constructor needed for Firebase
    public User() {}

    //User profile Creation constructor
    public User(String userid, String firstName, String lastName, UserRole role) {
        this.userid = userid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    /*
    Getters
     */
    public String getId() {
        return userid;
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

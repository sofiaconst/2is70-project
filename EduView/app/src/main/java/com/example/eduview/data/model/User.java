package com.example.eduview.data.model;

import com.google.firebase.database.PropertyName;

public class User {
    private String userId;
    @PropertyName("first_name")
    private String firstName;
    @PropertyName("last_name")
    private String lastName;
    @PropertyName("role")
    private UserRole role;
    @PropertyName("pfp")
    private String profileImageURL; //Store as URL?
    private String bio;
    private String email;

    //Empty Constructor needed for Firebase
    public User() {}

    //User profile Creation constructor
    public User(String userId, String firstName, String lastName, UserRole role) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.bio = "";
    }

    public boolean isTeacher() {
        return this.role == UserRole.TEACHER;
    }
    public boolean isStudent() {
        return this.role == UserRole.STUDENT;
    }

    public boolean isParent() {
        return this.role == UserRole.PARENT;
    }

    // Getters
    public String getUserId() {
        return userId;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {return lastName;}
  
    public String getProfileImageURL() {return profileImageURL;}
    public UserRole getRole() {
        return role;
    }
    public String getBio() { return bio; }
    public String getEmail() { return email; }

    // Setters
    public void setProfileImageURL(String profileImageURL) {this.profileImageURL = profileImageURL;}
    public void setBio(String bio) { this.bio = bio; }
    public void setEmail(String email) { this.email = email; }
}

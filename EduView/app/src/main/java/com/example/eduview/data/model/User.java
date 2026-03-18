package com.example.eduview.data.model;

import com.google.firebase.database.Exclude;
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
    private String profilePictureName;

    //Empty Constructor needed for Firebase
    public User() {}

    //User profile Creation constructor
    public User(String userId, String firstName, String lastName, UserRole role) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.profilePictureName = ProfilePicture.DEFAULT.name();
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
    public UserRole getRole() { return role; }
    
    @PropertyName("pfp")
    public String getProfilePictureName() {
        return profilePictureName;
    }

    @PropertyName("pfp")
    public void setProfilePictureName(String profilePictureName) {
        this.profilePictureName = profilePictureName;
    }

    @Exclude
    public ProfilePicture getProfilePicture() {
        if (profilePictureName == null) return ProfilePicture.DEFAULT;
        try {
            return ProfilePicture.valueOf(profilePictureName);
        } catch (IllegalArgumentException e) {
            return ProfilePicture.DEFAULT;
        }
    }

    @Exclude
    public void setProfilePicture(ProfilePicture profilePicture) {
        this.profilePictureName = profilePicture != null ? profilePicture.name() : ProfilePicture.DEFAULT.name();
    }

    @Exclude
    public int getProfilePictureResourceId() {
        return getProfilePicture().getDrawableId();
    }
}

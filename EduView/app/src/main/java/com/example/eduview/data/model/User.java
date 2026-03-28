package com.example.eduview.data.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

/**
 * Represents a user in the app.
 * This class is extended by user types Teacher, Student, and Parent.
 */
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
    @PropertyName("email")
    private String email;

    /**
     * Empty Constructor needed for Firebase
     */
    public User() {}

    /**
     * Creates a new user with profile information.
     *
     * @param userId user ID
     * @param firstName user's first name
     * @param lastName user's last name
     * @param email user's email address
     * @param role role of the user
     */
    public User(String userId, String firstName, String lastName, String email, UserRole role) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.profilePictureName = ProfilePicture.DEFAULT.name();
    }

    /**
     * Checks if the user is a teacher.
     * @return true if the user is a teacher
     */
    public boolean isTeacher() {
        return this.role == UserRole.TEACHER;
    }

    /**
     * Checks if the user is a student.
     * @return true if the user is a student
     */
    public boolean isStudent() {
        return this.role == UserRole.STUDENT;
    }

    /**
     * Checks if the user is a parent.
     * @return true if the user is a parent
     */
    public boolean isParent() {
        return this.role == UserRole.PARENT;
    }

    // Getters
    /**
     * Returns the user's ID.
     * @return user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the user's first name.
     * @return first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the user's last name.
     * @return last name
     */
    public String getLastName() {return lastName;}

    /**
     * Returns the user's role.
     * @return user role
     */
    public UserRole getRole() { return role; }

    /**
     * Returns the user's email address.
     * @return email
     */
    public String getEmail() { return email; }


    /**
     * Returns the stored profile picture name. Mapped to "pfp" in Firebase.
     *
     * @return profile picture name
     */
    @PropertyName("pfp")
    public String getProfilePictureName() {
        return profilePictureName;
    }

    /**
     * Converts the stored profile picture name into a ProfilePicture enum.
     *
     * @return ProfilePicture enum value
     */
    @Exclude
    public ProfilePicture getProfilePicture() {
        if (profilePictureName == null) return ProfilePicture.DEFAULT;
        try {
            return ProfilePicture.valueOf(profilePictureName);
        } catch (IllegalArgumentException e) {
            return ProfilePicture.DEFAULT;
        }
    }

    /**
     * Returns the drawable resource ID for the user's profile picture.
     *
     * @return drawable resource ID
     */
    @Exclude
    public int getProfilePictureResourceId() {
        return getProfilePicture().getDrawableId();
    }

    // Setters

    @PropertyName("pfp")
    public void setProfilePictureName(String profilePictureName) {
        this.profilePictureName = profilePictureName;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    @Exclude
    public void setProfilePicture(ProfilePicture profilePicture) {
        this.profilePictureName = profilePicture != null ? profilePicture.name() : ProfilePicture.DEFAULT.name();
    }



}

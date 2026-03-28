package com.example.eduview.data.model;

/**
 * Essentially the User class but used for parsing in User Repository.
 */
public class UserBaseData {
    public String firstName;
    public String lastName;
    public UserRole role;
    public String pfp;
    public String email;
    public String bio;

    public UserBaseData(String firstName, String lastName, UserRole role, String pfp) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.pfp = pfp;
    }
}

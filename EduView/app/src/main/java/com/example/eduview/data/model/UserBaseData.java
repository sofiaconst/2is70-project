package com.example.eduview.data.model;

/**
 * Essentially the User class but used for parsing and testing in User Repository.
 */
public class UserBaseData {
    public String firstName;
    public String lastName;
    public UserRole role;
    public String pfp;
    public String email;
    public String bio;

    /**
     * Creates a UserBaseData instance with basic information.
     * @param firstName first name of user
     * @param lastName last name of user
     * @param role role of user
     * @param pfp profile picture of user
     */
    public UserBaseData(String firstName, String lastName, UserRole role, String pfp) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.pfp = pfp;
    }
}

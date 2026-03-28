package com.example.eduview.data.model;

import static com.example.eduview.data.model.UserRole.PARENT;

import com.google.firebase.database.PropertyName;

import java.util.List;

/**
 * Represents a parent (which is a user) in the system.
 * Contains information about the parent like their children, name, email and ID.
 */
public class Parent extends User {
    private List<String> children;


    /**
     * Empty Constructor needed for Firebase
     */
    public Parent() {}

    /**
     * Creates a parent with their children.
     *
     * @param id parent user ID
     * @param firstName parent's first name
     * @param lastName parent's last name
     * @param email parent's email address
     * @param children list of child (student) IDs
     */
    public Parent(String id, String firstName, String lastName, String email, List<String> children){
        super(id, firstName, lastName, email, PARENT);
        this.children = children;
    }

    /**
     * Returns the list of child IDs associated with the parent.
     *
     * @return list of child IDs
     */
    @PropertyName("children")
    public List<String> getChildrenIDs() {
        return children;
    }


}

package com.example.eduview.Classes;

import static com.example.eduview.Resources.UserRole.PARENT;

import java.util.List;

public class Parent extends User {
    private String email;
    private List<String> childID;


    //Empty Constructor needed for Firebase
    public Parent() {}

    public Parent(String id, String firstName, String lastName, String email, List<String> childID ){
        super(id, firstName, lastName, PARENT);
        this.email = email;
        this.childID = childID;
    }

    /*
    Getters
     */
    public String getEmail() {
        return email;
    }
    public List<String> getChildID() {
        return childID;
    }
}

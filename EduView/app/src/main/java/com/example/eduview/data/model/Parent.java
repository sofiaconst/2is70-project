package com.example.eduview.data.model;

import static com.example.eduview.data.model.UserRole.PARENT;

import java.util.List;

public class Parent extends User {
    private String email;
    private List<String> children;


    //Empty Constructor needed for Firebase
    public Parent() {}

    public Parent(String id, String firstName, String lastName, String email, List<String> children){
        super(id, firstName, lastName, PARENT);
        this.email = email;
        this.children = children;
    }

    public String getEmail() {
        return email;
    }
    public List<String> getChildrenIDs() {
        return children;
    }

}

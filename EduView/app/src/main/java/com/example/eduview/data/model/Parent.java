package com.example.eduview.data.model;

import static com.example.eduview.data.model.UserRole.PARENT;

import com.google.firebase.database.PropertyName;

import java.util.List;

public class Parent extends User {
    private List<String> children;


    //Empty Constructor needed for Firebase
    public Parent() {}

    public Parent(String id, String firstName, String lastName, String email, List<String> children){
        super(id, firstName, lastName, email, PARENT);
        this.children = children;
    }

    @PropertyName("children")
    public List<String> getChildrenIDs() {
        return children;
    }


}

package com.example.eduview.data.model;

import static com.example.eduview.data.model.UserRole.STUDENT;
import static com.example.eduview.data.model.UserRole.TEACHER;

import com.google.firebase.database.PropertyName;

public class Teacher extends User {
    private String email;

    @PropertyName("classroom")
    private String classID;

    //Empty Constructor needed for Firebase
    public Teacher() {}

    public Teacher(String id, String firstName, String lastName, String email, String classID){
        super(id, firstName, lastName, TEACHER);
        this.email = email;
        this.classID = classID;
    }

    public Teacher(String id, String firstName, String lastName, String classID){
        super(id, firstName, lastName, TEACHER);
        this.email = null;
        this.classID = classID;
    }

    /*
    Getters
     */
    public String getEmail() {
        return email;
    }

    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

}

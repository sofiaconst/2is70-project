package com.example.eduview.data.model;

import static com.example.eduview.data.model.UserRole.STUDENT;
import static com.example.eduview.data.model.UserRole.TEACHER;

import com.google.firebase.database.PropertyName;

public class Teacher extends User {

    private String email;
    private String classroom;

    //Empty Constructor needed for Firebase
    public Teacher() {}

    public Teacher(String id, String firstName, String lastName, String email, String classroom){
        super(id, firstName, lastName, TEACHER);
        this.email = email;
        this.classroom = classroom;
    }

    public Teacher(String id, String firstName, String lastName, String classroom){
        super(id, firstName, lastName, TEACHER);
        this.email = null;
        this.classroom = classroom;
    }

    /*
    Getters
     */
    public String getEmail() {
        return email;
    }

    public String getClassID() {
        return classroom;
    }

    public void setClassID(String classID) {
        this.classroom = classID;
    }

}

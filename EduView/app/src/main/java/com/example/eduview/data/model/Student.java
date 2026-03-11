package com.example.eduview.data.model;

import static com.example.eduview.data.model.UserRole.STUDENT;

import com.google.firebase.database.PropertyName;

public class Student extends User {
    @PropertyName("classroom")
    private String classID;

    //Empty Constructor needed for Firebase
    public Student() {}

    public Student(String id, String firstName, String lastName, String classID){
        super(id, firstName, lastName, STUDENT);
        this.classID = classID;
    }


    /*
        Setters
         */
    public void setClassID(String classID) {
        this.classID = classID;
    }


    public String getClassId() {
        return classID;
    }
}

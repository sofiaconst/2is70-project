package com.example.eduview.data.model;

import static com.example.eduview.data.model.UserRole.STUDENT;

import com.google.firebase.database.PropertyName;

public class Student extends User {
//    @PropertyName("classroom")
    private String classroom;

    //Empty Constructor needed for Firebase
    public Student() {}

    public Student(String id, String firstName, String lastName, String classroom){
        super(id, firstName, lastName, STUDENT);
        this.classroom = classroom;
    }


    /*
        Setters
         */
    public void setClassID(String classID) {
        this.classroom = classID;
    }


    public String getClassId() {
        return classroom;
    }
}

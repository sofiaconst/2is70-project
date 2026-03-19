package com.example.eduview.data.model;

import static com.example.eduview.data.model.UserRole.STUDENT;

import com.google.firebase.database.PropertyName;

public class Student extends User {
    @PropertyName("classroom")
    private String classroomId;
    //Empty Constructor needed for Firebase
    public Student() {}

    public Student(String id, String firstName, String lastName, String classroom){
        super(id, firstName, lastName, STUDENT);
        this.classroomId = classroom;
    }


    /*
        Setters
         */
    public void setClassID(String classID) {
        this.classroomId = classID;
    }


    public String getClassId() {
        return classroomId;
    }
}

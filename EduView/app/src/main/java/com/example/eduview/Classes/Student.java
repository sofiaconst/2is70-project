package com.example.eduview.Classes;

import static com.example.eduview.Resources.UserRole.STUDENT;

public class Student extends User {
    private String classID;

    //Empty Constructor needed for Firebase
    public Student() {}

    public Student(String id, String firstName, String lastName, String classID){
        super(id, firstName, lastName, STUDENT);
        this.classID = classID;
    }

    /*
    Getters
     */
    public String getClassID() {
        return classID;
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

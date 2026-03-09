package com.example.eduview.Classes;

import static com.example.eduview.Resources.UserRole.STUDENT;

public class Student extends User {
    private String classID;
    private String parentID;

    //Empty Constructor needed for Firebase
    public Student() {}

    public Student(String id, String firstName, String lastName, String parentID){
        super(id, firstName, lastName, STUDENT);
        this.parentID = parentID;
    }

    /*
    Getters
     */
    public String getClassID() {
        return classID;
    }

    public String getParentID() {
        return parentID;
    }

    /*
        Setters
         */
    public void setClassID(String classID) {
        this.classID = classID;
    }



}

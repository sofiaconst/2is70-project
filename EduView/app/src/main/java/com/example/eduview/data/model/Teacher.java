package com.example.eduview.data.model;

import static com.example.eduview.data.model.UserRole.TEACHER;

public class Teacher extends User {

    private String classroom;

    //Empty Constructor needed for Firebase
    public Teacher() {}

    public Teacher(String id, String firstName, String lastName, String email, String classroom){
        super(id, firstName, lastName, email, TEACHER);
        this.classroom = classroom;
    }

    public Teacher(String id, String firstName, String lastName, String classroom){
        super(id, firstName, lastName, null, TEACHER);
        this.classroom = classroom;
    }

    /*
    Getters
     */
    public String getClassId() {
        return classroom;
    }

    public void setClassID(String classID) {
        this.classroom = classID;
    }

}

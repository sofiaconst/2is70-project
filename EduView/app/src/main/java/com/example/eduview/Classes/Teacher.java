package com.example.eduview.Classes;

import static com.example.eduview.Resources.UserRole.TEACHER;

public class Teacher extends User {
    private String email;
    private String classID;

    //Empty Constructor needed for Firebase
    public Teacher() {}

    public Teacher(String id, String firstName, String lastName, String email, String classID){
        super(id, firstName, lastName, TEACHER);
        this.email = email;
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

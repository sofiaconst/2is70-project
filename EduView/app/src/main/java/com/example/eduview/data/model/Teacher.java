package com.example.eduview.data.model;

import static com.example.eduview.data.model.UserRole.TEACHER;

import com.google.firebase.database.PropertyName;

public class Teacher extends User {
    private String email;
    @PropertyName("classroom")
    private String classroomId;

    //Empty Constructor needed for Firebase
    public Teacher() {}

    public Teacher(String id, String firstName, String lastName, String email, String classID){
        super(id, firstName, lastName, TEACHER);
        this.email = email;
        this.classroomId = classID;
    }

    public Teacher(String id, String firstName, String lastName, String classID){
        super(id, firstName, lastName, TEACHER);
        this.email = null;
        this.classroomId = classID;
    }

    public String getEmail() {
        return email;
    }

    public String getClassId() {
        return classroomId;
    }

    public void setClassId(String classID) {
        this.classroomId = classID;
    }

}

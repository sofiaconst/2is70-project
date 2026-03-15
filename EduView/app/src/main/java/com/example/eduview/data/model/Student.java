package com.example.eduview.data.model;

import static com.example.eduview.data.model.UserRole.STUDENT;

import com.google.firebase.database.PropertyName;

public class Student extends User {
    @PropertyName("classroom")
    private String classroomId;

    //Empty Constructor needed for Firebase
    public Student() {}

    public Student(String id, String firstName, String lastName, String classroomId){
        super(id, firstName, lastName, STUDENT);
        this.classroomId = classroomId;
    }

    public String getClassId() {
        return classroomId;
    }

    // Setters
    public void setClassroomId(String classroomId) {
        this.classroomId = classroomId;
    }
}

package com.example.eduview.data.model;

import static com.example.eduview.data.model.UserRole.STUDENT;

import com.google.firebase.database.PropertyName;

/**
 * Represents a student (which is a user) in the system.
 * Contains information about the student like their classroom, name and ID.
 */
public class Student extends User {
    @PropertyName("classroom")
    private String classroomId;
    //Empty Constructor needed for Firebase
    public Student() {}

    /**
     * Creates a new user as a student with an ID, first name, last name, email and the ID of their
     * classroom.
     * @param id the student's user ID
     * @param firstName the first name of the student
     * @param lastName the last name of the student
     * @param classroom the classroom ID of the student
     */
    public Student(String id, String firstName, String lastName, String email, String classroom){
        super(id, firstName, lastName, email, STUDENT);
        this.classroomId = classroom;
    }


    /*
        Setters
     */
    public void setClassId(String classID) {
        this.classroomId = classID;
    }


    public String getClassId() {
        return classroomId;
    }
}

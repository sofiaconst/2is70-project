package com.example.eduview.data.model;

import static com.example.eduview.data.model.UserRole.TEACHER;

/**
 * Represents a teacher (which is a user) in the system.
 * Contains information about the teacher like their classroom, name, email and ID.
 */
public class Teacher extends User {

    private String classroom;

    // Empty Constructor needed for Firebase
    public Teacher() {}

    /**
     * Creates a teacher with their ID, first name, last name, email and classroom..
     *
     * @param id the teacher's user ID
     * @param firstName teacher's first name
     * @param lastName teacher's last name
     * @param email teacher's email address
     * @param classroom ID of the classroom managed by the teacher
     */
    public Teacher(String id, String firstName, String lastName, String email, String classroom){
        super(id, firstName, lastName, email, TEACHER);
        this.classroom = classroom;
    }

    /**
     * Creates a teacher without an email address.
     *
     * @param id the teacher's user ID
     * @param firstName teacher's first name
     * @param lastName teacher's last name
     * @param classroom ID of the classroom managed by the teacher
     */
    public Teacher(String id, String firstName, String lastName, String classroom){
        super(id, firstName, lastName, null, TEACHER);
        this.classroom = classroom;
    }

    /**
     * Returns the ID of the classroom managed by the teacher.
     * @return ID classroom of the classroom managed by the teacher
     */
    public String getClassId() {
        return classroom;
    }

    public void setClassID(String classID) {
        this.classroom = classID;
    }

}

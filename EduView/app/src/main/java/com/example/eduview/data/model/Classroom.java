package com.example.eduview.data.model;

import com.google.firebase.database.PropertyName;

import java.util.List;

/**
 * Represents a classroom in the system.
 * Contains information about the teacher and enrolled students.
 */
public class Classroom {
    private String id;
    private String name;
    @PropertyName("teacher")
    private String teacherId;
    private List<String> studentIds;

    public Classroom() { }

    /**
     * Creates a new classroom with a new ID and name for the classroom and teacher ID of the
     * teacher that controls the classroom and ID's of the students in it.
     * @param id the ID of the classroom
     * @param name the name of the classroom
     * @param teacherId the ID of the teacher controlling the classroom
     * @param studentIds list of ID's of the students in the classroom
     */
    public Classroom(String id, String name, String teacherId, List<String> studentIds) {
        this.id = id;
        this.name = name;
        this.teacherId = teacherId;
        this.studentIds = studentIds;
    }

    /**
     * Returns the classroom ID.
     * @return the classroom ID
     */
    public String getId() { return id; }

    /**
     * Returns the name of the classroom.
     * @return the name of the classroom
     */
    public String getName() { return name; }

    /**
     * Returns the teacher's ID.
     * @return the teacher's ID.
     */
    @PropertyName("teacher")
    public String getTeacherId() { return teacherId; }
    public List<String> getStudentIds() { return studentIds; }
    @PropertyName("teacher")
    public void setTeacherId(String teacherId) {this.teacherId = teacherId;}
}
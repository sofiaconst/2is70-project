package com.example.eduview.data.model;

import java.util.List;

public class Classroom {
    private String id;
    private String name;
    private String teacherId;
    private List<String> studentIds;

    public Classroom() { }

    public Classroom(String id, String name, String teacherId, List<String> studentIds) {
        this.id = id;
        this.name = name;
        this.teacherId = teacherId;
        this.studentIds = studentIds;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getTeacherId() { return teacherId; }
    public List<String> getStudentIds() { return studentIds; }
}
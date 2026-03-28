package com.example.eduview.data.model;

/**
 * Enum representing the roles of users.
 */
public enum UserRole {
    // User than can create posts and announcements and supervises posts created by students.
    TEACHER,
    // User that can create posts but do not get published without confirmation
    STUDENT,
    // User that can only see posts and cannot create.
    PARENT
}

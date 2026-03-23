package com.example.eduview.ui.profile.profileStates;

public class StudentProfileState {

    private final boolean isRegistered;
    private final String className;
    private final String teacherName;
    private final boolean isLoading;
    private final String errorMessage;

    public StudentProfileState(
            boolean isRegistered,
            String className,
            String teacherName,
            boolean isLoading,
            String errorMessage
    ) {
        this.isRegistered = isRegistered;
        this.className = className;
        this.teacherName = teacherName;
        this.isLoading = isLoading;
        this.errorMessage = errorMessage;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public String getClassName() {
        return className;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    // Optional: static helpers (VERY useful)
    public static StudentProfileState notRegistered() {
        return new StudentProfileState(false, null, null, false, null);
    }

    public static StudentProfileState loading() {
        return new StudentProfileState(true, null, null, true, null);
    }

    public static StudentProfileState success(String className, String teacherName) {
        return new StudentProfileState(true, className, teacherName, false, null);
    }

    public static StudentProfileState error(String message) {
        return new StudentProfileState(true, null, null, false, message);
    }
}
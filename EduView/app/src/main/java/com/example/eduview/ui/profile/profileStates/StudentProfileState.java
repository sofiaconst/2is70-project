package com.example.eduview.ui.profile.profileStates;

/**
 * Represents the UI state of a student's profile screen.
 * Contains information like the classroom name, teacher name.
 */
public class StudentProfileState {

    private final boolean isRegistered;
    private final String className;
    private final String teacherName;
    private final boolean isLoading;
    private final String errorMessage;

    /**
     * Creates a new state view for the student profile screen.
     *
     * @param isRegistered boolean for whether the student is in a class
     * @param className name of the class the student is in
     * @param teacherName the name of the student's teacher
     * @param isLoading whether the data is currently loading
     * @param errorMessage error message if an error occurred, otherwise null
     */
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

    /**
     * Indicates whether the student is currently registered to a class.
     * @return true if registered, false otherwise
     */
    public boolean isRegistered() {
        return isRegistered;
    }

    /**
     * Returns the name of the classroom.
     * @return classroom name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the name of the teacher of the student.
     * @return teacher name
     */
    public String getTeacherName() {
        return teacherName;
    }

    /**
     * Indicates whether the profile data is currently loading.
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return isLoading;
    }

    /**
     * Returns the error message if an error occurred.
     * @return error message or null if no error
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    // Helpers

    /**
     * Creates a not registered state.
     */
    public static StudentProfileState notRegistered() {
        return new StudentProfileState(false, null, null, false, null);
    }

    /**
     * Creates a loading state.
     */
    public static StudentProfileState loading() {
        return new StudentProfileState(true, null, null, true, null);
    }

    /**
     * Creates a success state with loaded data.
     */
    public static StudentProfileState success(String className, String teacherName) {
        return new StudentProfileState(true, className, teacherName, false, null);
    }

    /**
     * Creates an error state with a message.
     */
    public static StudentProfileState error(String message) {
        return new StudentProfileState(true, null, null, false, message);
    }
}
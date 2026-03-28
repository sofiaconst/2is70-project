package com.example.eduview.ui.profile.profileStates;

import android.graphics.Bitmap;

import com.example.eduview.data.model.Student;

import java.util.List;

/**
 * Represents the UI state of a teacher's profile screen.
 * Contains information like the classroom name, QR code, student list.
 */
public class TeacherProfileState {

    private final boolean isLoading;
    private final String className;
    private final Bitmap qrCode;
    private final List<Student> students;
    private final String errorMessage;

    /**
     * Creates a new state view for the teacher profile screen.
     *
     * @param className name of the classroom
     * @param qrBitmap QR code bitmap for classroom joining
     * @param students list of students in the classroom
     * @param isLoading whether the data is currently loading
     * @param errorMessage error message if an error occurred, otherwise null
     */
    public TeacherProfileState(
            String className,
            Bitmap qrBitmap,
            List<Student> students,
            boolean isLoading,
            String errorMessage
    ) {
        this.className = className;
        this.qrCode = qrBitmap;
        this.students = students;
        this.isLoading = isLoading;
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the name of the classroom.
     * @return classroom name
     */
    public String getClassName() { return className; }

    /**
     * Returns the QR code used for joining the classroom.
     * @return QR code bitmap for joining the classroom
     */
    public Bitmap getQrCode() { return qrCode; }

    /**
     * Returns the list of students in the classroom.
     * @return list of students in the classroom
     */
    public List<Student> getStudents() { return students; }

    /**
     * Indicates whether the profile data is currently loading.
     * @return true if loading, false otherwise
     */
    public boolean isLoading() { return isLoading; }

    /**
     * Returns the error message if an error occurred.
     * @return error message or null if no error
     */
    public String getErrorMessage() { return errorMessage; }

    // Helpers

    /**
     * Creates a loading state.
     */
    public static TeacherProfileState loading() {
        return new TeacherProfileState(null, null, null, true, null);
    }

    /**
     * Creates a success state with loaded data.
     */
    public static TeacherProfileState success(String className, Bitmap qr, List<Student> students) {
        return new TeacherProfileState(className, qr, students, false, null);
    }

    /**
     * Creates an error state with a message.
     */
    public static TeacherProfileState error(String msg) {
        return new TeacherProfileState(null, null, null, false, msg);
    }
}
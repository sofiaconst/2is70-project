package com.example.eduview.ui.profile.profileStates;

import android.graphics.Bitmap;

import com.example.eduview.data.model.Student;

import java.util.List;

public class TeacherProfileState {

    private final boolean isLoading;
    private final String className;
    private final Bitmap qrCode;
    private final List<Student> students;
    private final String errorMessage;

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

    public String getClassName() { return className; }
    public Bitmap getQrCode() { return qrCode; }
    public List<Student> getStudents() { return students; }
    public boolean isLoading() { return isLoading; }
    public String getErrorMessage() { return errorMessage; }

    public static TeacherProfileState loading() {
        return new TeacherProfileState(null, null, null, true, null);
    }

    public static TeacherProfileState success(String className, Bitmap qr, List<Student> students) {
        return new TeacherProfileState(className, qr, students, false, null);
    }

    public static TeacherProfileState error(String msg) {
        return new TeacherProfileState(null, null, null, false, msg);
    }
}
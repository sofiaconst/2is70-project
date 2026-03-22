package com.example.eduview.ui.profile.profileStates;

import android.graphics.Bitmap;

import com.example.eduview.data.model.Student;

import java.util.List;

public class TeacherProfileState {

    private final boolean isLoading;
    private final String classId;
    private final String className;
    private final Bitmap qrCode;
    private final List<Student> students;
    private final String errorMessage;

    public TeacherProfileState(boolean isLoading,
                               String classId,
                               String className,
                               Bitmap qrCode,
                               List<Student> students,
                               String errorMessage) {
        this.isLoading = isLoading;
        this.classId = classId;
        this.className = className;
        this.qrCode = qrCode;
        this.students = students;
        this.errorMessage = errorMessage;
    }

    public boolean isLoading() { return isLoading; }
    public String getClassId() { return classId; }
    public String getClassName() { return className; }
    public Bitmap getQrCode() { return qrCode; }
    public List<Student> getStudents() { return students; }
    public String getErrorMessage() { return errorMessage; }

    public static TeacherProfileState loading() {
        return new TeacherProfileState(true, null, null, null, null, null);
    }

    public static TeacherProfileState success(String classId, String className, Bitmap qr, List<Student> students) {
        return new TeacherProfileState(false, classId, className, qr, students, null);
    }

    public static TeacherProfileState error(String msg) {
        return new TeacherProfileState(false, null, null, null, null, msg);
    }
}
package com.example.eduview.ui.profile;

import android.graphics.Bitmap;

import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.UserRole;

import java.util.List;

public class ProfileUIState {

    public static class BaseUserUiState {
        public final String fullName;
        public final String firstName;
        public final String lastName;
        public final UserRole role;
        public final String profilePictureUrl;

        public BaseUserUiState(String firstName, String lastName, UserRole role, String profilePictureUrl) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.fullName = firstName + " " + lastName;
            this.role = role;
            this.profilePictureUrl = profilePictureUrl;
        }
    }

    public class StudentUiState {
        public final String classroomName; // "None" if not registered
        public final String teacherName;
        public final boolean isRegistered;

        public StudentUiState(String classroomName, String teacherName, boolean isRegistered) {
            this.classroomName = classroomName;
            this.teacherName = teacherName;
            this.isRegistered = isRegistered;
        }
    }

    public class TeacherUiState {
        public final String classroomName;
        public final Bitmap qrCodeBitmap;
        public final List<Student> students;

        public TeacherUiState(String classroomName, Bitmap qrCodeBitmap, List<Student> students) {
            this.classroomName = classroomName;
            this.qrCodeBitmap = qrCodeBitmap;
            this.students = students;
        }
    }

    public class ParentUiState {
        public final List<ChildUiModel> children;

        public ParentUiState(List<ChildUiModel> children) {
            this.children = children;
        }

        public class ChildUiModel {
            public final String fullName;
            public final String classroomName;

            public ChildUiModel(String fullName, String classroomName) {
                this.fullName = fullName;
                this.classroomName = classroomName;
            }
        }
    }
}
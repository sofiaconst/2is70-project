package com.example.eduview.data.repository;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.function.Consumer;

public class ClassroomRepository {
    private final DatabaseReference classroomsRef;
    private final DatabaseReference teachersRef;
    private final DatabaseReference studentsRef;

    public ClassroomRepository() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        classroomsRef = db.getReference("classrooms");
        teachersRef = db.getReference("teachers");
        studentsRef = db.getReference("students");
    }

    public void getClassroomName(String classId, Consumer<String> onSuccess, Consumer<Exception> onError) {
        classroomsRef.child(classId).child("name").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                onSuccess.accept(task.getResult().getValue(String.class));
            } else {
                onError.accept(new RuntimeException("Classroom name not found"));
            }
        });
    }

    // Gets teacher id for the classroom (NOT THE NAME)
    public void getClassroomTeacher(
            String classId, Consumer<String> onSuccess, Consumer<Exception> onError) {
        classroomsRef.child(classId).child("teacher").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                onSuccess.accept(task.getResult().getValue(String.class));
            } else {
                onError.accept(new RuntimeException("Teacher id not found for this classroom"));
            }
        });
    }

    public void getTeacherClassroom(String teacherId, Consumer<String> onSuccess, Consumer<Exception> onError) {
        teachersRef.child(teacherId).child("classroom").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                onSuccess.accept(task.getResult().getValue(String.class));
            } else {
                onError.accept(new RuntimeException("No classroom found for teacher"));
            }
        });
    }

    public void joinClassroom(String studentId, String classCode, Runnable onSuccess, Consumer<Exception> onError) {
        classroomsRef.child(classCode).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Register student in classrooms table: studentId: true
                classroomsRef.child(classCode).child("students").child(studentId).setValue(true)
                        .addOnSuccessListener(aVoid ->
                                // Register class in students table: classroom: classCode
                                studentsRef.child(studentId).child("classroom").setValue(classCode)
                                        .addOnSuccessListener(aVoid1 -> onSuccess.run())
                                        .addOnFailureListener(onError::accept)
                        )
                        .addOnFailureListener(onError::accept);
            } else {
                onError.accept(new RuntimeException("Invalid Class Code!"));
            }
        });
    }
}

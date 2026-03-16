package com.example.eduview.data.repository;

import com.example.eduview.data.model.Classroom;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ClassroomRepository {

    private final DatabaseReference classroomsRef;

    public ClassroomRepository() {
        classroomsRef = FirebaseDatabase.getInstance().getReference("classrooms");
    }

    // Fetch classroom by ID
    public void getClassroomName(String classId, ClassroomCallback<Classroom> classroomCallback) {
        classroomsRef.child(classId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Classroom classroom = task.getResult().getValue(Classroom.class);
                classroomCallback.onSuccess(classroom);
            } else {
                classroomCallback.onError(new RuntimeException("Classroom not found"));
            }
        });
    }

    // Add student to classroom
    public void joinClassroom(String studentId, String classId, ClassroomCallback<Void> classroomCallback) {
        classroomsRef.child(classId).child("students").child(studentId)
                .setValue(true)
                .addOnSuccessListener(aVoid -> classroomCallback.onSuccess(null))
                .addOnFailureListener(classroomCallback::onError);
    }

    // Generic callback interface
    public interface ClassroomCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
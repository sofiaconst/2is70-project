package com.example.eduview.data.repository;

import java.util.function.Consumer;

import com.example.eduview.data.model.Classroom;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ClassroomRepository {

    private final DatabaseReference classroomsRef;

    public ClassroomRepository() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        classroomsRef = db.getReference("classrooms");
        //teachersRef = db.getReference("teachers");
        //studentsRef = db.getReference("students");
        
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
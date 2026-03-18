package com.example.eduview.data.repository;

import com.example.eduview.data.model.Classroom;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassroomRepository {

    private final DatabaseReference rootRef;
    private final DatabaseReference classroomsRef;

    public ClassroomRepository() {
        rootRef = FirebaseDatabase.getInstance().getReference();
        classroomsRef = rootRef.child("classrooms");
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
        Map<String, Object> updates = new HashMap<>();
        updates.put("classrooms/" + classId + "/students/" + studentId, true);
        updates.put("students/" + studentId + "/classroom", classId);

        rootRef.updateChildren(updates)
                .addOnSuccessListener(unused -> classroomCallback.onSuccess(null))
                .addOnFailureListener(classroomCallback::onError);
    }

    public void getStudentIdsForClassroom(String classId, ClassroomCallback<List<String>> callback) {
        classroomsRef.child(classId).child("students").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError(new RuntimeException("Failed to fetch classroom students"));
                return;
            }

            DataSnapshot snapshot = task.getResult();
            List<String> studentIds = new ArrayList<>();

            for (DataSnapshot child : snapshot.getChildren()) {
                if (child.getKey() != null) {
                    studentIds.add(child.getKey());
                }
            }

            callback.onSuccess(studentIds);
        });
    }

    public void removeStudentFromClassroom(String classId, String studentId, ClassroomCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("classrooms/" + classId + "/students/" + studentId, null);
        updates.put("students/" + studentId + "/classroom", null);

        rootRef.updateChildren(updates)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    // Generic callback interface
    public interface ClassroomCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}

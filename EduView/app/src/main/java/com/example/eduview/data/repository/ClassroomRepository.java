package com.example.eduview.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eduview.data.model.Classroom;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for managing classroom data and student assignments.
 */
public class ClassroomRepository {

    private final DatabaseReference rootRef;
    private final DatabaseReference classroomsRef;

    /**
     * Default constructor initializing Firebase references.
     */
    public ClassroomRepository() {
        rootRef = FirebaseDatabase.getInstance().getReference();
        classroomsRef = rootRef.child("classrooms");
    }

    /**
     * Constructor used for testing with mocked references.
     */
    public ClassroomRepository(DatabaseReference rootRef, DatabaseReference classroomsRef) {
        this.rootRef = rootRef;
        this.classroomsRef = classroomsRef;
    }

    /**
     * Fetches detailed classroom information by ID.
     */
    public void getClassroomById(String classId, ClassroomCallback<Classroom> callback) {
        classroomsRef.child(classId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Classroom classroom = task.getResult().getValue(Classroom.class);
                callback.onSuccess(classroom);
            } else {
                callback.onError(new RuntimeException("Classroom not found"));
            }
        });
    }

    /**
     * Joins a student to a classroom using the class ID.
     */
    public void joinClassroom(String studentId, String classId, ClassroomCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("classrooms/" + classId + "/students/" + studentId, true);
        updates.put("students/" + studentId + "/classroom", classId);

        rootRef.updateChildren(updates)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    /**
     * Returns a LiveData stream of student IDs for a given classroom.
     * Uses a ValueEventListener for real-time updates when students join or leave.
     */
    public LiveData<List<String>> getLiveStudentIdsForClassroom(String classId) {
        MutableLiveData<List<String>> liveData = new MutableLiveData<>();
        
        classroomsRef.child(classId).child("students").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> studentIds = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (child.getKey() != null) {
                        studentIds.add(child.getKey());
                    }
                }
                liveData.postValue(studentIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ClassroomRepository", "Student ID listener cancelled", error.toException());
            }
        });
        
        return liveData;
    }

    /**
     * Removes a student from a classroom.
     */
    public void removeStudentFromClassroom(String classId, String studentId, ClassroomCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("classrooms/" + classId + "/students/" + studentId, null);
        updates.put("students/" + studentId + "/classroom", "");

        rootRef.updateChildren(updates)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    /**
     * Callback interface for classroom operations.
     */
    public interface ClassroomCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}

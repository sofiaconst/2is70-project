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

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
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
     *
     * @param rootRef root Firebase reference
     * @param classroomsRef Firebase reference pointing to classrooms
     */
    public ClassroomRepository(DatabaseReference rootRef, DatabaseReference classroomsRef) {
        this.rootRef = rootRef;
        this.classroomsRef = classroomsRef;
    }

    /**
     * Fetches detailed classroom information by ID.
     *
     * @param classId classroom ID to fetch
     * @param callback callback that returns the classroom or an error
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
     *
     * @param studentId ID of the student joining the classroom
     * @param classId classroom ID to join
     * @param callback callback indicating success or failure
     */
    public void joinClassroom(String studentId, String classId, ClassroomCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();

        // Add the student under the classroom's student list.
        updates.put("classrooms/" + classId + "/students/" + studentId, true);

        // Also store the classroom on the student's own record.
        updates.put("students/" + studentId + "/classroom", classId);

        rootRef.updateChildren(updates)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    /**
     * Returns a LiveData stream of student IDs for a given classroom.
     * Uses a ValueEventListener for real-time updates when students join or leave.
     *
     * @param classId classroom whose students should be observed
     * @return LiveData containing the current list of student IDs
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
     * Returns a LiveData stream of the classroom ID assigned to a student.
     * This is used to dynamically react when a student joins or is removed from a classroom.
     *
     * @param studentId ID of the student whose classroom should be observed
     * @return LiveData containing the student's current classroom ID, or an empty string/null if none
     */
    public LiveData<String> getLiveStudentClassroom(String studentId) {
        MutableLiveData<String> liveData = new MutableLiveData<>();

        // Observe the student's classroom field directly so the UI can react in real time.
        rootRef.child("students").child(studentId).child("classroom")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String classId = snapshot.getValue(String.class);
                        liveData.postValue(classId);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ClassroomRepository", "Student classroom listener cancelled", error.toException());
                    }
                });

        return liveData;
    }

    /**
     * Removes a student from a classroom.
     *
     * @param classId classroom ID from which the student will be removed
     * @param studentId ID of the student to remove
     * @param callback callback indicating success or failure
     */
    public void removeStudentFromClassroom(String classId, String studentId, ClassroomCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();

        // Remove the student from the classroom's student list.
        updates.put("classrooms/" + classId + "/students/" + studentId, null);

        // Clear the student's classroom value so the app knows they are no longer assigned.
        updates.put("students/" + studentId + "/classroom", "");

        rootRef.updateChildren(updates)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    /**
     * Callback interface for classroom operations.
     *
     * @param <T> type of result returned by the operation
     */
    public interface ClassroomCallback<T> {

        /**
         * Called when the operation succeeds.
         *
         * @param result result of the operation
         */
        void onSuccess(T result);

        /**
         * Called when the operation fails.
         *
         * @param e exception describing the error
         */
        void onError(Exception e);
    }
}
package com.example.eduview.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.model.UserBaseData;
import com.example.eduview.data.model.UserRole;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class UserRepository {
    private final DatabaseReference usersRef;
    private final DatabaseReference parentsRef;
    private final DatabaseReference studentsRef;
    private final DatabaseReference teachersRef;

    public UserRepository() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        usersRef = db.getReference("users");
        studentsRef = db.getReference("students");
        teachersRef = db.getReference("teachers");
        parentsRef = db.getReference("parents");
    }

    public void getUserById(String userId, UserCallback callback) {
        usersRef.child(userId).get().addOnCompleteListener(task -> {

            if (!task.isSuccessful()) {
                callback.onError(new RuntimeException("Failed to fetch user"));
                return;
            }

            DataSnapshot snapshot = task.getResult();

            if (!snapshot.exists()) {
                callback.onError(new RuntimeException("User not found"));
                return;
            }

            UserBaseData base;
            try {
                base = parseBaseUser(snapshot);
            } catch (Exception e) {
                callback.onError(e);
                return;
            }

            switch (base.role) {
                case STUDENT:
                    fetchStudent(userId, base, callback);
                    break;

                case TEACHER:
                    fetchTeacher(userId, base, callback);
                    break;

                case PARENT:
                    fetchParent(userId, base, callback);
                    break;

                default:
                    callback.onError(new RuntimeException("Invalid role"));
                    break;
            }
        });
    }

    private UserBaseData parseBaseUser(DataSnapshot snapshot) {
        String firstName = snapshot.child("first_name").getValue(String.class);
        String lastName = snapshot.child("last_name").getValue(String.class);
        String roleStr = snapshot.child("role").getValue(String.class);
        String pfp = snapshot.child("pfp").getValue(String.class);
        String email = snapshot.child("email").getValue(String.class);
        String bio = snapshot.child("bio").getValue(String.class);

        if (firstName == null || lastName == null || roleStr == null) {
            throw new RuntimeException("User information missing");
        }

        UserRole role;
        try {
            role = UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid user role: " + roleStr);
        }

        UserBaseData base = new UserBaseData(firstName, lastName, role, pfp);
        base.email = email;
        base.bio = bio;
        return base;
    }

    private void fetchStudent(String userId, UserBaseData base, UserCallback callback) {
        studentsRef.child(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError(new RuntimeException("Failed to fetch student"));
                return;
            }

            DataSnapshot snapshot = task.getResult();
            if (!snapshot.exists()) {
                callback.onError(new RuntimeException("Student not found"));
                return;
            }

            // Extract classroom ID or any other role-specific fields
            String classId = snapshot.child("classroom").getValue(String.class);

            // Create Student object using base info + role-specific info
            Student student = new Student(
                    userId,
                    base.firstName,
                    base.lastName,
                    classId
            );

            // Return via callback
            callback.onSuccess(student);
        });
    }

    private void fetchTeacher(String userId, UserBaseData base, UserCallback callback) {
        teachersRef.child(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError(new RuntimeException("Failed to fetch teacher"));
                return;
            }

            DataSnapshot snapshot = task.getResult();
            if (!snapshot.exists()) {
                callback.onError(new RuntimeException("Teacher not found"));
                return;
            }

            // Role-specific fields
            String classId = snapshot.child("classroom").getValue(String.class);
            String email = snapshot.child("email").getValue(String.class);

            // Create Teacher object
            Teacher teacher = new Teacher(
                    userId,
                    base.firstName,
                    base.lastName,
                    email,
                    classId
            );

            // Return via callback
            callback.onSuccess(teacher);
        });
    }

    private void fetchParent(String userId, UserBaseData base, UserCallback callback) {
        parentsRef.child(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError(new RuntimeException("Failed to fetch parent"));
                return;
            }

            DataSnapshot snapshot = task.getResult();
            if (!snapshot.exists()) {
                callback.onError(new RuntimeException("Parent not found"));
                return;
            }

            // Role-specific fields
            String email = snapshot.child("email").getValue(String.class);
            List<String> childrenIDs = getChildrenIds(snapshot); // assumes a helper method

            // Create Parent object
            Parent parent = new Parent(
                    userId,
                    base.firstName,
                    base.lastName,
                    email,
                    childrenIDs
            );

            Log.d("TESTER", Arrays.toString(parent.getChildrenIDs().toArray()));

            // Return via callback
            callback.onSuccess(parent);
        });
    }

    @NonNull
    private static List<String> getChildrenIds(DataSnapshot parentSnapshot) {
        DataSnapshot childrenSnapshot = parentSnapshot.child("children");
        List<String> childrenIDs = new ArrayList<>();

        if (childrenSnapshot.exists()) {
            for (DataSnapshot childSnapshot : childrenSnapshot.getChildren()) {

                String childId = childSnapshot.getValue(String.class);

                if (childId != null && !childId.trim().isEmpty()) {
                    childrenIDs.add(childId);
                }
            }
        }

        return childrenIDs;
    }

    public void updateProfilePicture(String userID, String imageUrl) {
        usersRef.child(userID).child("pfp").setValue(imageUrl);
    }

    public void updateClass(String userID, String classID) {
        studentsRef.child(userID).child("classroom").setValue(classID);
    }

    public void updateBio(String userId, String bio, Runnable onSuccess, Consumer<Exception> onError) {
        usersRef.child(userId).child("bio").setValue(bio)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onError::accept);
    }

    public void fetchChildrenOfParent(String parentID,
                                      Consumer<List<String>> onSuccess,
                                      Consumer<Exception> onError) {

        parentsRef.child(parentID).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                onError.accept(new RuntimeException("Failed to fetch parent data"));
                return;
            }

            DataSnapshot parentSnapshot = task.getResult();

            if (!parentSnapshot.exists()) {
                onError.accept(new RuntimeException("No user with that ID"));
                return;
            }

            List<String> childrenIDs = getChildrenIds(parentSnapshot);
            onSuccess.accept(childrenIDs);
        });
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onError(Exception e);
    }
}

package com.example.eduview.data.repository;

import androidx.annotation.NonNull;

import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.ProfilePicture;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.model.UserBaseData;
import com.example.eduview.data.model.UserRole;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
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

        if (firstName == null || lastName == null || roleStr == null) {
            throw new RuntimeException("User information missing");
        }

        UserRole role;
        try {
            role = UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid user role: " + roleStr);
        }

        return new UserBaseData(firstName, lastName, role, pfp);
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
            student.setProfilePictureName(base.pfp);

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
            teacher.setProfilePictureName(base.pfp);

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
            parent.setProfilePictureName(base.pfp);

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
                if (childSnapshot.getKey() != null) {
                    childrenIDs.add(childSnapshot.getKey());
                }
            }
        }

        return childrenIDs;
    }

    public void updateProfilePicture(String userID, ProfilePicture profilePicture) {
        usersRef.child(userID).child("pfp").setValue(profilePicture.name());
    }

    public void updateClass(String userID, String classID) {
        studentsRef.child(userID).child("classroom").setValue(classID);
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

    // Helper method to get students by their IDs
    public void getStudentsByIds(List<String> studentIds,
                                 java.util.function.Consumer<List<Student>> onSuccess,
                                 java.util.function.Consumer<Exception> onError) {

        if (studentIds == null || studentIds.isEmpty()) {
            onSuccess.accept(new ArrayList<>());
            return;
        }

        List<Student> students = new ArrayList<>();
        final int[] remaining = {studentIds.size()};
        final boolean[] failed = {false};

        for (String studentId : studentIds) {
            getUserById(studentId, new UserCallback() {
                @Override
                public void onSuccess(User user) {
                    if (failed[0]) return;

                    if (user instanceof Student) {
                        students.add((Student) user);
                    }

                    remaining[0]--;

                    if (remaining[0] == 0) {
                        onSuccess.accept(students);
                    }
                }

                @Override
                public void onError(Exception e) {
                    if (failed[0]) return;
                    failed[0] = true;
                    onError.accept(e);
                }
            });
        }
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onError(Exception e);
    }
}
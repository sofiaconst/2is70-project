package com.example.eduview.data.repository;

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

    public UserRepository(DatabaseReference usersRef,
                          DatabaseReference parentsRef,
                          DatabaseReference studentsRef,
                          DatabaseReference teachersRef) {
        this.usersRef = usersRef;
        this.parentsRef = parentsRef;
        this.studentsRef = studentsRef;
        this.teachersRef = teachersRef;
    }

    public void fetchUser(String userId, Consumer<User> onSuccess, Consumer<Exception> onError) {
        usersRef.child(userId).get().addOnCompleteListener(task -> {

            if (!task.isSuccessful()) {
                onError.accept(new RuntimeException("Failed to fetch user"));
                return;
            }

            DataSnapshot snapshot = task.getResult();

            if (!snapshot.exists()) {
                onError.accept(new RuntimeException("User not found"));
                return;
            }

            UserBaseData base;
            try {
                base = parseBaseUser(snapshot);
            } catch (Exception e) {
                onError.accept(e);
                return;
            }

            switch (base.role) {
                case STUDENT:
                    fetchStudent(userId, base, onSuccess, onError);
                    break;

                case TEACHER:
                    fetchTeacher(userId, base, onSuccess, onError);
                    break;

                case PARENT:
                    fetchParent(userId, base, onSuccess, onError);
                    break;

                default:
                    onError.accept(new RuntimeException("Invalid role"));
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

    private void fetchStudent(String userId,
                              UserBaseData base,
                              Consumer<User> onSuccess,
                              Consumer<Exception> onError) {

        studentsRef.child(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                onError.accept(new RuntimeException("Failed to fetch student"));
                return;
            }

            DataSnapshot snapshot = task.getResult();
            String classId = snapshot.child("classroom").getValue(String.class);

            Student student = new Student(
                    userId,
                    base.firstName,
                    base.lastName,
                    classId
            );

            finalizeUser(student, base.pfp, onSuccess);
        });
    }

    private void fetchTeacher(String userId,
                              UserBaseData base,
                              Consumer<User> onSuccess,
                              Consumer<Exception> onError) {

        teachersRef.child(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                onError.accept(new RuntimeException("Failed to fetch teacher"));
                return;
            }

            DataSnapshot snapshot = task.getResult();
            String classId = snapshot.child("classroom").getValue(String.class);
            String email = snapshot.child("email").getValue(String.class);

            Teacher teacher = new Teacher(
                    userId,
                    base.firstName,
                    base.lastName,
                    email,
                    classId
            );

            finalizeUser(teacher, base.pfp, onSuccess);
        });
    }

    private void fetchParent(String userId,
                             UserBaseData base,
                             Consumer<User> onSuccess,
                             Consumer<Exception> onError) {

        parentsRef.child(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                onError.accept(new RuntimeException("Failed to fetch parent"));
                return;
            }

            DataSnapshot snapshot = task.getResult();
            List<String> childrenIDs = getChildrenIds(snapshot);
            String email = snapshot.child("email").getValue(String.class);

            Parent parent = new Parent(
                    userId,
                    base.firstName,
                    base.lastName,
                    email,
                    childrenIDs
            );

            finalizeUser(parent, base.pfp, onSuccess);
        });
    }

    @NonNull
    private static List<String> getChildrenIds(DataSnapshot parentSnapshot) {
        DataSnapshot childrenSnapshot = parentSnapshot.child("children");
        List<String> childrenIDs = new ArrayList<>();

        for (DataSnapshot childSnapshot : childrenSnapshot.getChildren()) {
            childrenIDs.add(childSnapshot.getKey());
        }
        return childrenIDs;
    }

    private void finalizeUser(User user, String pfp, Consumer<User> onSuccess) {
        if (pfp != null) {
            user.setProfileImageURL(pfp);
        }
        onSuccess.accept(user);
    }

    public void updateProfilePicture(String userID, String imageUrl) {
        usersRef.child(userID).child("pfp").setValue(imageUrl);
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

    public interface UserCallback {
    }
}
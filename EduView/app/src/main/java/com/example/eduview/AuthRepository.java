package com.example.eduview;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference rootRef;

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    public void signUpUser(String firstName, String lastName, String email, String password, String role, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userId = firebaseAuth.getCurrentUser().getUid();
                    User user = new User(firstName, lastName, email, role);

                    rootRef.child("users").child(userId).setValue(user)
                        .addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful()) {
                                Log.d("AuthRepository", "User data successfully written to Firebase.");
                                callback.onSuccess();
                            } else {
                                Log.e("AuthRepository", "Failed to save user data to Firebase", dbTask.getException());
                                callback.onFailure(dbTask.getException());
                            }
                        });
                } else {
                    Log.e("AuthRepository", "Sign Up Failed", task.getException());
                    callback.onFailure(task.getException());
                }
            });
    }

    public void signUpTeacher(String firstName, String lastName, String email, String password, String className, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userId = firebaseAuth.getCurrentUser().getUid();

                    // Generate a unique classroom ID
                    String classroomId = rootRef.child("classrooms").push().getKey();

                    // 1. Store in users table
                    User user = new User(firstName, lastName, email, "Teacher");
                    rootRef.child("users").child(userId).setValue(user)
                        .addOnCompleteListener(userTask -> {
                            if (userTask.isSuccessful()) {
                                // 2. Store in teachers table
                                rootRef.child("teachers").child(userId).setValue(new Teacher(className, classroomId))
                                    .addOnCompleteListener(teacherTask -> {
                                        if (teacherTask.isSuccessful()) {
                                            // 3. Store in classrooms table
                                            Classroom classroom = new Classroom(className, firstName + " " + lastName);
                                            rootRef.child("classrooms").child(classroomId).setValue(classroom)
                                                .addOnCompleteListener(classroomTask -> {
                                                    if (classroomTask.isSuccessful()) {
                                                        callback.onSuccess();
                                                    } else {
                                                        callback.onFailure(classroomTask.getException());
                                                    }
                                                });
                                        } else {
                                            callback.onFailure(teacherTask.getException());
                                        }
                                    });
                            } else {
                                callback.onFailure(userTask.getException());
                            }
                        });
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }

    public void signUpParent(String firstName, String lastName, String email, String password, List<String> childIds, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String parentId = firebaseAuth.getCurrentUser().getUid();

                    // 1. Store in users table
                    User user = new User(firstName, lastName, email, "Parent");
                    rootRef.child("users").child(parentId).setValue(user)
                        .addOnCompleteListener(userTask -> {
                            if (userTask.isSuccessful()) {
                                // 2. Store in parents table
                                // Map child IDs to custom keys (student_1, student_2, etc.)
                                Map<String, String> childrenMap = new HashMap<>();
                                for (int i = 0; i < childIds.size(); i++) {
                                    childrenMap.put("student_" + (i + 1), childIds.get(i));
                                }

                                rootRef.child("parents").child(parentId).child("children").setValue(childrenMap)
                                    .addOnCompleteListener(parentTask -> {
                                        if (parentTask.isSuccessful()) {
                                            callback.onSuccess();
                                        } else {
                                            callback.onFailure(parentTask.getException());
                                        }
                                    });
                            } else {
                                callback.onFailure(userTask.getException());
                            }
                        });
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }

    public interface AuthCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public static class User {
        public String first_name;
        public String last_name;
        public String email;
        public String role;

        public User(String first_name, String last_name, String email, String role) {
            this.first_name = first_name;
            this.last_name = last_name;
            this.email = email;
            this.role = role;
        }
    }

    public static class Parent {
        public String first_name;
        public String last_name;
        public String email;
        public String role;
        public Map<String, String> children;

        public Parent(String first_name, String last_name, String email, String role) {
            this.first_name = first_name;
            this.last_name = last_name;
            this.email = email;
            this.role = role;
        }

        public void setChildren(Map<String, String> children) {
            this.children = children;
        }
    }

    public static class Student {
        public String first_name;
        public String last_name;
        public String parentId;

        public Student(String first_name, String last_name) {
            this.first_name = first_name;
            this.last_name = last_name;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }
    }

    public static class Teacher {
        public String classroom;
        public String classroomId;

        public Teacher(String classroom, String classroomId) {
            this.classroom = classroom;
            this.classroomId = classroomId;
        }
    }

    public static class Classroom {
        public String name;
        public String teacher;
        public String qrCode;
        public List<String> students;
        public Feed feed;

        public Classroom(String name, String teacher) {
            this.name = name;
            this.teacher = teacher;
            this.qrCode = "";
            this.students = new ArrayList<>();
            this.feed = new Feed();
        }
    }

    public static class Feed {
        public List<String> announcements;
        public List<String> pending;
        public List<String> published;

        public Feed() {
            this.announcements = new ArrayList<>();
            this.pending = new ArrayList<>();
            this.published = new ArrayList<>();
        }
    }
}
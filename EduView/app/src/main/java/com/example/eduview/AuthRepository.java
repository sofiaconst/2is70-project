package com.example.eduview;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference rootRef;

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    public void checkEmailExists(String email, Consumer<Boolean> callback) {
        rootRef.child("users").orderByChild("email").equalTo(email).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.accept(task.getResult().exists());
                } else {
                    callback.accept(false);
                }
            });
    }

    public interface Consumer<T> {
        void accept(T t);
    }

    public void signUpUser(String firstName, String lastName, String email, String password, String role, AuthCallback callback) {
        checkEmailExists(email, exists -> {
            if (exists) {
                callback.onFailure(new Exception("Email already exists"));
                return;
            }
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
        });
    }

    public void signUpTeacher(String firstName, String lastName, String email, String password, String className, AuthCallback callback) {
        checkEmailExists(email, exists -> {
            if (exists) {
                callback.onFailure(new Exception("Email already exists"));
                return;
            }
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
                                    rootRef.child("teachers").child(userId).setValue(new Teacher(classroomId))
                                        .addOnCompleteListener(teacherTask -> {
                                            if (teacherTask.isSuccessful()) {
                                                // 3. Store in classrooms table
                                                Classroom classroom = new Classroom(className, userId);
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
        });
    }

    public void signUpParent(String firstName, String lastName, String email, String password, List<ChildInfo> children, AuthCallback callback) {
        // First check all emails
        List<String> allEmails = new ArrayList<>();
        allEmails.add(email);
        for (ChildInfo child : children) {
            allEmails.add(child.email);
        }

        checkMultipleEmailsExist(allEmails, anyExists -> {
            if (anyExists) {
                callback.onFailure(new Exception("One or more emails already exist"));
                return;
            }

            // First, create the parent account
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String parentId = firebaseAuth.getCurrentUser().getUid();

                        // 1. Store Parent in users table
                        User parentUser = new User(firstName, lastName, email, "Parent");
                        rootRef.child("users").child(parentId).setValue(parentUser)
                            .addOnCompleteListener(userTask -> {
                                if (userTask.isSuccessful()) {
                                    // Now create accounts for each child
                                    signUpChildrenSequentially(children, password, 0, new ArrayList<>(), new ChildAuthCallback() {
                                        @Override
                                        public void onAllChildrenSignedUp(List<String> childUids) {
                                            // 2. Store in parents table with child IDs
                                            Map<String, String> childrenMap = new HashMap<>();
                                            for (int i = 0; i < childUids.size(); i++) {
                                                childrenMap.put("student_" + (i + 1), childUids.get(i));
                                            }

                                            rootRef.child("parents").child(parentId).child("children").setValue(childrenMap)
                                                .addOnCompleteListener(parentTask -> {
                                                    if (parentTask.isSuccessful()) {
                                                        // IMPORTANT: After creating children, the last child is logged in.
                                                        // We need to log back in as the parent so the app state is correct.
                                                        firebaseAuth.signInWithEmailAndPassword(email, password)
                                                            .addOnCompleteListener(reAuthTask -> {
                                                                if (reAuthTask.isSuccessful()) {
                                                                    callback.onSuccess();
                                                                } else {
                                                                    callback.onFailure(reAuthTask.getException());
                                                                }
                                                            });
                                                    } else {
                                                        callback.onFailure(parentTask.getException());
                                                    }
                                                });
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            callback.onFailure(e);
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
        });
    }

    private void checkMultipleEmailsExist(List<String> emails, Consumer<Boolean> callback) {
        AtomicInteger remaining = new AtomicInteger(emails.size());
        AtomicBoolean anyExists = new AtomicBoolean(false);

        for (String email : emails) {
            checkEmailExists(email, exists -> {
                if (exists) {
                    anyExists.set(true);
                }
                if (remaining.decrementAndGet() == 0) {
                    callback.accept(anyExists.get());
                }
            });
        }
    }

    private void signUpChildrenSequentially(List<ChildInfo> children, String password, int index, List<String> childUids, ChildAuthCallback callback) {
        if (index >= children.size()) {
            callback.onAllChildrenSignedUp(childUids);
            return;
        }

        ChildInfo child = children.get(index);
        firebaseAuth.createUserWithEmailAndPassword(child.email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String childUid = firebaseAuth.getCurrentUser().getUid();
                    childUids.add(childUid);

                    // 1. Store in users table
                    User childUser = new User(child.firstName, child.lastName, child.email, "Student");
                    
                    rootRef.child("users").child(childUid).setValue(childUser)
                        .addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful()) {
                                // 2. Store in students table
                                Map<String, Object> studentData = new HashMap<>();
                                studentData.put("classroom", "");
                                
                                rootRef.child("students").child(childUid).setValue(studentData)
                                    .addOnCompleteListener(studentTask -> {
                                        if (studentTask.isSuccessful()) {
                                            // Proceed to next child
                                            signUpChildrenSequentially(children, password, index + 1, childUids, callback);
                                        } else {
                                            callback.onError(studentTask.getException());
                                        }
                                    });
                            } else {
                                callback.onError(dbTask.getException());
                            }
                        });
                } else {
                    callback.onError(task.getException());
                }
            });
    }

    public void addChildToParent(String parentId, ChildInfo child, String parentPassword, AuthCallback callback) {
        String parentEmail = firebaseAuth.getCurrentUser().getEmail();

        checkEmailExists(child.email, exists -> {
            if (exists) {
                callback.onFailure(new Exception("Email already exists"));
                return;
            }
            firebaseAuth.createUserWithEmailAndPassword(child.email, parentPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String childUid = firebaseAuth.getCurrentUser().getUid();

                        // 1. Store in users table
                        User childUser = new User(child.firstName, child.lastName, child.email, "Student");
                        rootRef.child("users").child(childUid).setValue(childUser)
                            .addOnCompleteListener(userTask -> {
                                if (userTask.isSuccessful()) {
                                    // 2. Store in students table
                                    Map<String, Object> studentData = new HashMap<>();
                                    studentData.put("classroom", "");
                                    rootRef.child("students").child(childUid).setValue(studentData)
                                        .addOnCompleteListener(studentTask -> {
                                            if (studentTask.isSuccessful()) {
                                                // 3. Add to parent's children list
                                                rootRef.child("parents").child(parentId).child("children").get()
                                                    .addOnCompleteListener(getTask -> {
                                                        if (getTask.isSuccessful()) {
                                                            Map<String, String> childrenMap = (Map<String, String>) getTask.getResult().getValue();
                                                            if (childrenMap == null) childrenMap = new HashMap<>();
                                                            
                                                            int nextIndex = childrenMap.size() + 1;
                                                            childrenMap.put("student_" + nextIndex, childUid);

                                                            rootRef.child("parents").child(parentId).child("children").setValue(childrenMap)
                                                                .addOnCompleteListener(updateTask -> {
                                                                    // Re-authenticate as parent
                                                                    firebaseAuth.signInWithEmailAndPassword(parentEmail, parentPassword)
                                                                        .addOnCompleteListener(reAuthTask -> {
                                                                            if (reAuthTask.isSuccessful()) {
                                                                                callback.onSuccess();
                                                                            } else {
                                                                                callback.onFailure(reAuthTask.getException());
                                                                            }
                                                                        });
                                                                });
                                                        } else {
                                                            callback.onFailure(getTask.getException());
                                                        }
                                                    });
                                            } else {
                                                callback.onFailure(studentTask.getException());
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
        });
    }

    private interface ChildAuthCallback {
        void onAllChildrenSignedUp(List<String> childUids);
        void onError(Exception e);
    }

    public interface AuthCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public static class ChildInfo {
        public String firstName;
        public String lastName;
        public String email;

        public ChildInfo(String firstName, String lastName, String email) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }
    }

    public static class User {
        public String first_name;
        public String last_name;
        public String email;
        public String role;
        public String bio;
        public String pfp;

        public User(String first_name, String last_name, String email, String role) {
            this.first_name = first_name;
            this.last_name = last_name;
            this.email = email;
            this.role = role;
            this.bio = "";
            this.pfp = "";
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
        public String classroom;

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

        public Teacher(String classroom) {
            this.classroom = classroom;
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

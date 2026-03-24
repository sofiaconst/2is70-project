package com.example.eduview;

import android.util.Log;

import com.example.eduview.data.model.ProfilePicture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AuthService {

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference rootRef;

    public AuthService() {
        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    //Constructor for Testing
    public AuthService(FirebaseAuth firebaseAuth, DatabaseReference rootRef) {
        this.firebaseAuth = firebaseAuth;
        this.rootRef = rootRef;
    }

    public void signOut() {
        firebaseAuth.signOut();
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

    private Exception sanitizeError(Exception e) {
        if (e == null || e.getMessage() == null) return e;
        String msg = e.getMessage();
        String sanitized = msg.replace("email address", "username")
                             .replace("Email address", "Username")
                             .replace("email", "username")
                             .replace("Email", "Username");
        return new Exception(sanitized);
    }

    public void signUpUser(String firstName, String lastName, String email, String password, String role, AuthCallback callback) {
        checkEmailExists(email, exists -> {
            if (exists) {
                callback.onFailure(new Exception("Username already taken: " + email));
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
                                    firebaseAuth.signOut();
                                    callback.onSuccess();
                                } else {
                                    callback.onFailure(sanitizeError(dbTask.getException()));
                                }
                            });
                    } else {
                        callback.onFailure(sanitizeError(task.getException()));
                    }
                });
        });
    }

    public void signUpParent(String firstName, String lastName, String email, String password, List<ChildInfo> children, AuthCallback callback) {
        List<String> allEmails = new ArrayList<>();
        allEmails.add(email);
        for (ChildInfo child : children) {
            allEmails.add(child.email);
        }

        checkMultipleEmailsExistDetailed(allEmails, existingEmail -> {
            if (existingEmail != null) {
                callback.onFailure(new Exception("Username already taken: " + existingEmail));
                return;
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String parentId = firebaseAuth.getCurrentUser().getUid();

                            User parentUser = new User(firstName, lastName, email, "Parent");
                            rootRef.child("users").child(parentId).setValue(parentUser)
                                    .addOnCompleteListener(userTask -> {
                                        if (userTask.isSuccessful()) {
                                            signUpChildrenSequentially(children, password, 0, new ArrayList<>(), new ChildAuthCallback() {
                                                @Override
                                                public void onAllChildrenSignedUp(List<String> childUids) {
                                                    Map<String, String> childrenMap = new HashMap<>();
                                                    for (int i = 0; i < childUids.size(); i++) {
                                                        childrenMap.put("student_" + (i + 1), childUids.get(i));
                                                    }

                                                    rootRef.child("parents").child(parentId).child("children").setValue(childrenMap)
                                                            .addOnCompleteListener(parentTask -> {
                                                                if (parentTask.isSuccessful()) {
                                                                    firebaseAuth.signOut();
                                                                    callback.onSuccess();
                                                                } else {
                                                                    cleanupFailedParent(parentId, callback, parentTask.getException());
                                                                }
                                                            });
                                                }

                                                @Override
                                                public void onError(Exception e, String failedEmail) {
                                                    cleanupFailedParent(parentId, callback, new Exception("Username already taken: " + failedEmail));
                                                }
                                            });
                                        } else {
                                            cleanupFailedParent(parentId, callback, userTask.getException());
                                        }
                                    });
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                callback.onFailure(new Exception("Username already taken: " + email));
                            } else {
                                callback.onFailure(sanitizeError(task.getException()));
                            }
                        }
                    });
        });
    }

    private void cleanupFailedParent(String parentId, AuthCallback callback, Exception originalError) {
        // Remove from DB and delete Auth user so they can retry
        rootRef.child("users").child(parentId).removeValue();
        rootRef.child("parents").child(parentId).removeValue();
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.getCurrentUser().delete().addOnCompleteListener(task -> {
                callback.onFailure(sanitizeError(originalError));
            });
        } else {
            callback.onFailure(sanitizeError(originalError));
        }
    }

    private void checkMultipleEmailsExistDetailed(List<String> emails, Consumer<String> callback) {
        AtomicInteger remaining = new AtomicInteger(emails.size());
        final String[] foundEmail = {null};

        for (String email : emails) {
            checkEmailExists(email, exists -> {
                if (exists && foundEmail[0] == null) {
                    foundEmail[0] = email;
                }
                if (remaining.decrementAndGet() == 0) {
                    callback.accept(foundEmail[0]);
                }
            });
        }
    }

    public void signUpTeacher(String firstName, String lastName, String email, String password, String className, AuthCallback callback) {
        checkEmailExists(email, exists -> {
            if (exists) {
                callback.onFailure(new Exception("Username already taken: " + email));
                return;
            }
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        String classroomId = rootRef.child("classrooms").push().getKey();

                    User user = new User(firstName, lastName, email, "Teacher");
                    user.pfp = ProfilePicture.DEFAULT.name();
                    rootRef.child("users").child(userId).setValue(user)
                        .addOnCompleteListener(userTask -> {
                            if (userTask.isSuccessful()) {
                                rootRef.child("teachers").child(userId).setValue(new Teacher(classroomId))
                                    .addOnCompleteListener(teacherTask -> {
                                        if (teacherTask.isSuccessful()) {
                                            Classroom classroom = new Classroom(className, userId);
                                            rootRef.child("classrooms").child(classroomId).setValue(classroom)
                                                .addOnCompleteListener(classroomTask -> {
                                                    if (classroomTask.isSuccessful()) {
                                                        firebaseAuth.signOut();
                                                        callback.onSuccess();
                                                    } else {
                                                        cleanupFailedParent(userId, callback, classroomTask.getException());
                                                    }
                                                });
                                        } else {
                                            cleanupFailedParent(userId, callback, teacherTask.getException());
                                        }
                                    });
                            } else {
                                cleanupFailedParent(userId, callback, userTask.getException());
                            }
                        });
                } else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        callback.onFailure(new Exception("Username already taken: " + email));
                    } else {
                        callback.onFailure(sanitizeError(task.getException()));
                    }
                }
            });});
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

                    User childUser = new User(child.firstName, child.lastName, child.email, "Student");
                    childUser.bio = "";
                    childUser.pfp = ProfilePicture.DEFAULT.name();
                    
                    rootRef.child("users").child(childUid).setValue(childUser)
                        .addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful()) {
                                Map<String, Object> studentData = new HashMap<>();
                                studentData.put("classroom", "");
                                
                                rootRef.child("students").child(childUid).setValue(studentData)
                                    .addOnCompleteListener(studentTask -> {
                                        if (studentTask.isSuccessful()) {
                                            signUpChildrenSequentially(children, password, index + 1, childUids, callback);
                                        } else {
                                            callback.onError(studentTask.getException(), child.email);
                                        }
                                    });
                            } else {
                                callback.onError(dbTask.getException(), child.email);
                            }
                        });
                } else {
                    callback.onError(task.getException(), child.email);
                }
            });
    }

    public void addChildToParent(String parentId, ChildInfo child, String parentPassword, AuthCallback callback) {
        String parentEmail = firebaseAuth.getCurrentUser().getEmail();

        checkEmailExists(child.email, exists -> {
            if (exists) {
                callback.onFailure(new Exception("Username already taken: " + child.email));
                return;
            }
            firebaseAuth.signInWithEmailAndPassword(parentEmail, parentPassword)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        firebaseAuth.createUserWithEmailAndPassword(child.email, parentPassword)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String childUid = firebaseAuth.getCurrentUser().getUid();

                                    User childUser = new User(child.firstName, child.lastName, child.email, "Student");
                                    rootRef.child("users").child(childUid).setValue(childUser)
                                        .addOnCompleteListener(userTask -> {
                                            if (userTask.isSuccessful()) {
                                                Map<String, Object> studentData = new HashMap<>();
                                                studentData.put("classroom", "");
                                                rootRef.child("students").child(childUid).setValue(studentData)
                                                    .addOnCompleteListener(studentTask -> {
                                                        if (studentTask.isSuccessful()) {
                                                            rootRef.child("parents").child(parentId).child("children").get()
                                                                .addOnCompleteListener(getTask -> {
                                                                    if (getTask.isSuccessful()) {
                                                                        Map<String, String> childrenMap = (Map<String, String>) getTask.getResult().getValue();
                                                                        if (childrenMap == null) childrenMap = new HashMap<>();
                                                                        
                                                                        int nextIndex = childrenMap.size() + 1;
                                                                        childrenMap.put("student_" + nextIndex, childUid);

                                                                        rootRef.child("parents").child(parentId).child("children").setValue(childrenMap)
                                                                            .addOnCompleteListener(updateTask -> {
                                                                                firebaseAuth.signInWithEmailAndPassword(parentEmail, parentPassword)
                                                                                    .addOnCompleteListener(reAuthTask -> {
                                                                                        if (reAuthTask.isSuccessful()) {
                                                                                            callback.onSuccess();
                                                                                        } else {
                                                                                            callback.onFailure(sanitizeError(reAuthTask.getException()));
                                                                                        }
                                                                                    });
                                                                            });
                                                                    } else {
                                                                        callback.onFailure(sanitizeError(getTask.getException()));
                                                                    }
                                                                });
                                                        } else {
                                                            callback.onFailure(sanitizeError(studentTask.getException()));
                                                        }
                                                    });
                                            } else {
                                                callback.onFailure(sanitizeError(userTask.getException()));
                                            }
                                        });
                                } else {
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        callback.onFailure(new Exception("Username already taken: " + child.email));
                                    } else {
                                        callback.onFailure(sanitizeError(task.getException()));
                                    }
                                }
                            });
                    } else {
                        callback.onFailure(new Exception("Incorrect parent password"));
                    }
                });
        });
    }

    private interface ChildAuthCallback {
        void onAllChildrenSignedUp(List<String> childUids);
        void onError(Exception e, String failedEmail);
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

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

/**
 * Service class responsible for handling authentication and user creation logic.
 * This class uses Firebase Authentication and Firebase Realtime Database
 * operations to do authentication in the application. It supports signing users out,
 * checking whether usernames already exist, creating regular users,
 * creating parents with children, creating teachers with classrooms,
 * and adding a new child to an existing parent account.
 */
public class AuthService {

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference rootRef;

    /**
     * Default constructor.
     * Initializes Firebase authentication and database references
     * using the default Firebase instances.
     */
    public AuthService() {
        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Constructor used for testing with used dependencies.
     * Allows custom FirebaseAuth and DatabaseReference instances
     * to be injected instead of using the default references.
     */
    public AuthService(FirebaseAuth firebaseAuth, DatabaseReference rootRef) {
        this.firebaseAuth = firebaseAuth;
        this.rootRef = rootRef;
    }

    /**
     * Signs out the currently authenticated user.
     */
    public void signOut() {
        // Delegate sign-out directly to Firebase.
        firebaseAuth.signOut();
    }

    /**
     * Checks if an email already exists in the database.
     * In the app, email is used as the stored login identity, but it is
     * presented to the user as a username in many places.
     *
     * @param email the email to check
     * @param callback returns true if exists, false otherwise
     */
    public void checkEmailExists(String email, Consumer<Boolean> callback) {
        // Search the users collection by stored email value.
        rootRef.child("users").orderByChild("email").equalTo(email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Return whether a matching user exists.
                        callback.accept(task.getResult().exists());
                    } else {
                        // Treat failures as "not found".
                        callback.accept(false);
                    }
                });
    }

    /**
     * Custom Consumer interface to avoid Java version issues.
     */
    public interface Consumer<T> {
        void accept(T t);
    }

    /**
     * Replaces Firebase error messages to match app terminology (username instead of email).
     */
    private Exception sanitizeError(Exception e) {
        if (e == null || e.getMessage() == null) return e;

        String msg = e.getMessage();

        // Replace all occurrences of "email" with "username" for consistency in UI.
        String sanitized = msg.replace("email address", "username")
                .replace("Email address", "Username")
                .replace("email", "username")
                .replace("Email", "Username");

        // Wrap the adjusted text in a new exception object.
        return new Exception(sanitized);
    }

    /**
     * Signs up a generic user and stores their base profile in the database.
     * Checks if the username is already taken,
     * creates the Firebase authentication account,
     * stores the user data under /users,
     * and signs the user out after successful creation.
     *
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the email used for authentication
     * @param password the account password
     * @param role the role of the user
     * @param callback callback reporting success or failure
     */
    public void signUpUser(String firstName, String lastName, String email, String password, String role, AuthCallback callback) {
        // Verify that the username/email is not already in use.
        checkEmailExists(email, exists -> {
            if (exists) {
                callback.onFailure(new Exception("Username already taken: " + email));
                return;
            }

            // Create the authentication account in Firebase.
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            // Get UID from Firebase
                            String userId = firebaseAuth.getCurrentUser().getUid();

                            // Create database user object
                            User user = new User(firstName, lastName, email, role);

                            // Save user in database
                            rootRef.child("users").child(userId).setValue(user)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            // Sign out after registration so login stays explicit.
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

    /**
     * Registers a parent along with multiple children accounts.
     */
    public void signUpParent(String firstName, String lastName, String email, String password, List<ChildInfo> children, AuthCallback callback) {

        // Collect all emails to check for duplicates
        List<String> allEmails = new ArrayList<>();
        allEmails.add(email);
        for (ChildInfo child : children) {
            // Include every child email so conflicts are detected before creation starts.
            allEmails.add(child.email);
        }

        // Abort the full flow if any parent/child username is already taken.
        checkMultipleEmailsExistDetailed(allEmails, existingEmail -> {
            if (existingEmail != null) {
                callback.onFailure(new Exception("Username already taken: " + existingEmail));
                return;
            }

            // Create the parent authentication account first.
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            String parentId = firebaseAuth.getCurrentUser().getUid();

                            User parentUser = new User(firstName, lastName, email, "Parent");

                            // Save parent user
                            rootRef.child("users").child(parentId).setValue(parentUser)
                                    .addOnCompleteListener(userTask -> {
                                        if (userTask.isSuccessful()) {

                                            // Create children accounts
                                            signUpChildrenSequentially(children, password, 0, new ArrayList<>(), new ChildAuthCallback() {

                                                @Override
                                                public void onAllChildrenSignedUp(List<String> childUids) {

                                                    // Map children IDs into structured keys
                                                    Map<String, String> childrenMap = new HashMap<>();
                                                    for (int i = 0; i < childUids.size(); i++) {
                                                        // Store children in a numbered structure.
                                                        childrenMap.put("student_" + (i + 1), childUids.get(i));
                                                    }

                                                    // Save the parent to children relationship separately.
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
                                                    // If child creation fails midway, clean up the parent account too.
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

    /**
     * Cleans up partially created parent account if something fails.
     */
    private void cleanupFailedParent(String parentId, AuthCallback callback, Exception originalError) {

        // Remove parent data from database
        rootRef.child("users").child(parentId).removeValue();
        rootRef.child("parents").child(parentId).removeValue();

        // Delete Firebase user if still logged in
        if (firebaseAuth.getCurrentUser() != null) {
            // Delete the account so the user can retry cleanly.
            firebaseAuth.getCurrentUser().delete().addOnCompleteListener(task -> {
                callback.onFailure(sanitizeError(originalError));
            });
        } else {
            callback.onFailure(sanitizeError(originalError));
        }
    }

    /**
     * Checks multiple emails and returns the first one that already exists.
     */
    private void checkMultipleEmailsExistDetailed(List<String> emails, Consumer<String> callback) {

        // Track how many checks are still pending.
        AtomicInteger remaining = new AtomicInteger(emails.size());
        final String[] foundEmail = {null};

        for (String email : emails) {
            checkEmailExists(email, exists -> {

                // Store the first duplicate found
                if (exists && foundEmail[0] == null) {
                    foundEmail[0] = email;
                }

                // When all checks complete return result
                if (remaining.decrementAndGet() == 0) {
                    callback.accept(foundEmail[0]);
                }
            });
        }
    }

    /**
     * Registers a teacher and automatically creates a classroom.
     */
    public void signUpTeacher(String firstName, String lastName, String email, String password, String className, AuthCallback callback) {

        // Ensure the teacher username is not already used.
        checkEmailExists(email, exists -> {
            if (exists) {
                callback.onFailure(new Exception("Username already taken: " + email));
                return;
            }

            // Create the teacher in Firebase Auth.
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            String userId = firebaseAuth.getCurrentUser().getUid();

                            // Generate unique classroom ID
                            String classroomId = rootRef.child("classrooms").push().getKey();

                            User user = new User(firstName, lastName, email, "Teacher");
                            user.pfp = ProfilePicture.DEFAULT.name();

                            // Store the teacher's base user data.
                            rootRef.child("users").child(userId).setValue(user)
                                    .addOnCompleteListener(userTask -> {
                                        if (userTask.isSuccessful()) {

                                            // Save teacher info
                                            rootRef.child("teachers").child(userId).setValue(new Teacher(classroomId))
                                                    .addOnCompleteListener(teacherTask -> {
                                                        if (teacherTask.isSuccessful()) {

                                                            // Create classroom entry
                                                            Classroom classroom = new Classroom(className, userId);

                                                            // Save the linked classroom record.
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
                    });
        });
    }

    /**
     * Recursively signs up children one by one to avoid Firebase conflicts.
     */
    private void signUpChildrenSequentially(List<ChildInfo> children, String password, int index, List<String> childUids, ChildAuthCallback callback) {

        // All children processed?
        if (index >= children.size()) {
            callback.onAllChildrenSignedUp(childUids);
            return;
        }

        // Work on a single child.
        ChildInfo child = children.get(index);

        // Create the child's account before storing profile data.
        firebaseAuth.createUserWithEmailAndPassword(child.email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        String childUid = firebaseAuth.getCurrentUser().getUid();
                        childUids.add(childUid);

                        // Create user object for child
                        User childUser = new User(child.firstName, child.lastName, child.email, "Student");
                        childUser.bio = "";
                        childUser.pfp = ProfilePicture.DEFAULT.name();

                        // Save the base child record.
                        rootRef.child("users").child(childUid).setValue(childUser)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {

                                        // Initialize student data
                                        Map<String, Object> studentData = new HashMap<>();
                                        studentData.put("classroom", "");

                                        // Store the role-specific student node.
                                        rootRef.child("students").child(childUid).setValue(studentData)
                                                .addOnCompleteListener(studentTask -> {
                                                    if (studentTask.isSuccessful()) {

                                                        // Process next child
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

    /**
     * Adds a new child account to an existing parent.
     */
    public void addChildToParent(String parentId, ChildInfo child, String parentPassword, AuthCallback callback) {

        // Capture the currently logged in parent's email before auth state changes.
        String parentEmail = firebaseAuth.getCurrentUser().getEmail();

        // Do not continue if the new child's username already exists.
        checkEmailExists(child.email, exists -> {
            if (exists) {
                callback.onFailure(new Exception("Username already taken: " + child.email));
                return;
            }

            // Reauthenticate parent before operation
            firebaseAuth.signInWithEmailAndPassword(parentEmail, parentPassword)
                    .addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {

                            // The child signs in as that child in Firebase
                            firebaseAuth.createUserWithEmailAndPassword(child.email, parentPassword)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {

                                            String childUid = firebaseAuth.getCurrentUser().getUid();

                                            User childUser = new User(child.firstName, child.lastName, child.email, "Student");

                                            // Save the child's user data first.
                                            rootRef.child("users").child(childUid).setValue(childUser)
                                                    .addOnCompleteListener(userTask -> {
                                                        if (userTask.isSuccessful()) {

                                                            Map<String, Object> studentData = new HashMap<>();
                                                            studentData.put("classroom", "");

                                                            // Create the student data node.
                                                            rootRef.child("students").child(childUid).setValue(studentData)
                                                                    .addOnCompleteListener(studentTask -> {
                                                                        if (studentTask.isSuccessful()) {

                                                                            // Fetch current children list
                                                                            rootRef.child("parents").child(parentId).child("children").get()
                                                                                    .addOnCompleteListener(getTask -> {
                                                                                        if (getTask.isSuccessful()) {

                                                                                            Map<String, String> childrenMap =
                                                                                                    (Map<String, String>) getTask.getResult().getValue();

                                                                                            if (childrenMap == null) childrenMap = new HashMap<>();

                                                                                            int nextIndex = childrenMap.size() + 1;
                                                                                            childrenMap.put("student_" + nextIndex, childUid);

                                                                                            // Update parent-child relation
                                                                                            rootRef.child("parents").child(parentId).child("children").setValue(childrenMap)
                                                                                                    .addOnCompleteListener(updateTask -> {

                                                                                                        // Authenticate parent again
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

    /**
     * Callback used during sequential child creation.
     */
    private interface ChildAuthCallback {
        void onAllChildrenSignedUp(List<String> childUids);
        void onError(Exception e, String failedEmail);
    }

    /**
     * Authentication callback.
     */
    public interface AuthCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * Simple data holder for child signup information.
     */
    public static class ChildInfo {
        public String firstName;
        public String lastName;
        public String email;

        public ChildInfo(String firstName, String lastName, String email) {
            // Store the provided child signup data.
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }
    }

    /**
     * Database model representing a user.
     */
    public static class User {
        public String first_name;
        public String last_name;
        public String email;
        public String role;
        public String bio;
        public String pfp;

        public User(String first_name, String last_name, String email, String role) {
            // Assign common user fields used in the database.
            this.first_name = first_name;
            this.last_name = last_name;
            this.email = email;
            this.role = role;
        }
    }

    /**
     * Database model representing a parent user.
     */
    public static class Parent {
        public String first_name;
        public String last_name;
        public String email;
        public String role;
        public Map<String, String> children;

        public Parent(String first_name, String last_name, String email, String role) {
            // Initialize the parent's identity fields.
            this.first_name = first_name;
            this.last_name = last_name;
            this.email = email;
            this.role = role;
        }

        public void setChildren(Map<String, String> children) {
            // Replace the full children map with the provided one.
            this.children = children;
        }
    }

    /**
     * Database model representing a student.
     */
    public static class Student {
        public String first_name;
        public String last_name;
        public String parentId;
        public String classroom;

        public Student(String first_name, String last_name) {
            // Initialize the name fields.
            this.first_name = first_name;
            this.last_name = last_name;
        }

        public void setParentId(String parentId) {
            // Link this student to a parent by ID.
            this.parentId = parentId;
        }
    }

    /**
     * Database model representing a teacher.
     */
    public static class Teacher {
        public String classroom;

        public Teacher(String classroom) {
            // Store the classroom assigned to this teacher.
            this.classroom = classroom;
        }
    }

    /**
     * Database model representing a classroom.
     */
    public static class Classroom {
        public String name;
        public String teacher;
        public String qrCode;
        public List<String> students;
        public Feed feed;

        public Classroom(String name, String teacher) {
            // Save the classroom fields.
            this.name = name;
            this.teacher = teacher;

            this.qrCode = "";
            this.students = new ArrayList<>();
            this.feed = new Feed();
        }
    }

    /**
     * Represents the feed structure inside a classroom.
     */
    public static class Feed {
        public List<String> announcements;
        public List<String> pending;
        public List<String> published;

        public Feed() {
            // Start each feed category with an empty list.
            this.announcements = new ArrayList<>();
            this.pending = new ArrayList<>();
            this.published = new ArrayList<>();
        }
    }
}
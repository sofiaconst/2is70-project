package com.example.eduview.data.repository;

import android.util.Log;

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
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Repository responsible for reading and updating user-related data
 * from Firebase Realtime Database.
 * This class handles access to base user records as well as role-specific
 * records for students, teachers, and parents. It also provides helper
 * methods for updating profile information, retrieving children of a parent,
 * and loading multiple students by their IDs.
 */
public class UserRepository {
    private final DatabaseReference usersRef;
    private final DatabaseReference parentsRef;
    private final DatabaseReference studentsRef;
    private final DatabaseReference teachersRef;

    /**
     * Default constructor.
     * Initializes Firebase database references for all user-related nodes.
     */
    public UserRepository() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        usersRef = db.getReference("users");
        studentsRef = db.getReference("students");
        teachersRef = db.getReference("teachers");
        parentsRef = db.getReference("parents");
    }

    /**
     * Constructor used mainly for testing.
     * Allows custom database references to be added.
     *
     * @param usersRef reference to the users node
     * @param parentsRef reference to the parents node
     * @param studentsRef reference to the students node
     * @param teachersRef reference to the teachers node
     */
    public UserRepository(DatabaseReference usersRef,
                          DatabaseReference parentsRef,
                          DatabaseReference studentsRef,
                          DatabaseReference teachersRef) {
        this.usersRef = usersRef;
        this.parentsRef = parentsRef;
        this.studentsRef = studentsRef;
        this.teachersRef = teachersRef;
    }

    /**
     * Retrieves a user by ID and returns the correct subclass based on role.
     * This method first loads the shared base user data from the users node,
     * then delegates to the appropriate role-specific fetch method.
     *
     * @param userId the ID of the user to fetch
     * @param callback callback that returns the full user object or an error
     */
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
                // Parse the user fields before loading role-specific data.
                base = parseBaseUser(snapshot);
            } catch (Exception e) {
                callback.onError(e);
                return;
            }

            // Decide which specialized fetch method to use based on the role.
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

    /**
     * Parses the shared base fields of a user from a Firebase snapshot.
     * The base data includes first name, last name, role, profile picture,
     * email, and bio. This information is common to all user types.
     *
     * @param snapshot the Firebase snapshot containing base user data
     * @return a populated UserBaseData object
     * @throws RuntimeException if required data is missing or the role is invalid
     */
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

        // Build the object that role-specific methods will extend.
        UserBaseData base = new UserBaseData(firstName, lastName, role, pfp);
        base.email = email;
        base.bio = bio;
        return base;
    }

    /**
     * Loads the student data for a user and returns a Student object.
     *
     * @param userId the student user's ID
     * @param base the already parsed base user data
     * @param callback callback that returns the Student object or an error
     */
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

            // Read student data such as the classroom ID.
            String classId = snapshot.child("classroom").getValue(String.class);

            // Combine shared base fields with student-specific data.
            Student student = new Student(
                    userId,
                    base.firstName,
                    base.lastName,
                    base.email,
                    classId
            );
            student.setProfilePictureName(base.pfp);

            // Return the fully constructed student object.
            callback.onSuccess(student);
        });
    }

    /**
     * Loads the teacher data for a user and returns a Teacher object.
     *
     * @param userId the teacher user's ID
     * @param base the already parsed base user data
     * @param callback callback that returns the Teacher object or an error
     */
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

            // Read teacher fields such as their classroom assignment.
            String classId = snapshot.child("classroom").getValue(String.class);

            // Combine common fields with teacher data.
            Teacher teacher = new Teacher(
                    userId,
                    base.firstName,
                    base.lastName,
                    base.email,
                    classId
            );
            teacher.setProfilePictureName(base.pfp);

            // Return the fully constructed teacher object.
            callback.onSuccess(teacher);
        });
    }

    /**
     * Loads the parent data for a user and returns a Parent object.
     *
     * @param userId the parent user's ID
     * @param base the already parsed base user data
     * @param callback callback that returns the Parent object or an error
     */
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

            // Extract the list of linked child IDs from the parent's node.
            List<String> childrenIDs = getChildrenIds(snapshot);

            // Combine base fields with parent child data.
            Parent parent = new Parent(
                    userId,
                    base.firstName,
                    base.lastName,
                    base.email,
                    childrenIDs
            );
            parent.setProfilePictureName(base.pfp);

            Log.d("TESTER", Arrays.toString(parent.getChildrenIDs().toArray()));

            // Return the fully constructed parent object.
            callback.onSuccess(parent);
        });
    }

    /**
     * Extracts child IDs from a parent snapshot.
     * The children are stored as values inside the children node.
     * Empty or blank child IDs are ignored.
     *
     * @param parentSnapshot the snapshot containing the parent data
     *
     * @return a list of valid child IDs
     */
    @NonNull
    private static List<String> getChildrenIds(DataSnapshot parentSnapshot) {
        DataSnapshot childrenSnapshot = parentSnapshot.child("children");
        List<String> childrenIDs = new ArrayList<>();

        if (childrenSnapshot.exists()) {
            for (DataSnapshot childSnapshot : childrenSnapshot.getChildren()) {

                String childId = childSnapshot.getValue(String.class);

                // Only store non-null and non-empty child IDs.
                if (childId != null && !childId.trim().isEmpty()) {
                    childrenIDs.add(childId);
                }
            }
        }

        return childrenIDs;
    }

    /**
     * Updates the stored profile picture value for a user.
     *
     * @param userID the user whose profile picture should be updated
     * @param profilePicture the new profile picture enum value
     */
    public void updateProfilePicture(String userID, ProfilePicture profilePicture) {
        usersRef.child(userID).child("pfp").setValue(profilePicture.name());
    }

    /**
     * Updates the classroom assignment of a student.
     *
     * @param userID the student user ID
     * @param classID the classroom ID to assign
     */
    public void updateClass(String userID, String classID) {
        studentsRef.child(userID).child("classroom").setValue(classID);
    }

    /**
     * Updates a user's biography text.
     *
     * @param userId the ID of the user whose bio should be updated
     * @param bio the new bio text
     * @param onSuccess callback invoked if the update succeeds
     * @param onError callback invoked if the update fails
     */
    public void updateBio(String userId, String bio, Runnable onSuccess, Consumer<Exception> onError) {
        usersRef.child(userId).child("bio").setValue(bio)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onError::accept);
    }

    /**
     * Retrieves the list of child IDs linked to a given parent.
     *
     * @param parentID the parent user ID
     * @param onSuccess callback receiving the list of child IDs
     * @param onError callback receiving an exception on failure
     */
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

            // Reuse the helper method so child logic stays in one place.
            List<String> childrenIDs = getChildrenIds(parentSnapshot);
            onSuccess.accept(childrenIDs);
        });
    }

    /**
     * Loads multiple students by their user IDs.
     * This method requests each user individually and collects only the users
     * that are actual Student instances. If any request fails, the error
     * callback is triggered once.
     *
     * @param studentIds the list of student IDs to load
     * @param onSuccess callback receiving the list of loaded students
     * @param onError callback receiving the first error encountered
     */
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

                    // Only add the result if the fetched user is actually a Student.
                    if (user instanceof Student) {
                        students.add((Student) user);
                    }

                    remaining[0]--;

                    // Return the collected list once all pending requests have finished.
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

    /**
     * Callback interface for asynchronous user fetch operations.
     */
    public interface UserCallback {

        /**
         * Called when a user is fetched successfully.
         *
         * @param user the loaded user object
         */
        void onSuccess(User user);

        /**
         * Called when fetching the user fails.
         *
         * @param e the exception describing the failure
         */
        void onError(Exception e);
    }
}
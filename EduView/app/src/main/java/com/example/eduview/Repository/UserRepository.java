package com.example.eduview.Repository;


import androidx.annotation.NonNull;

import com.example.eduview.Classes.Parent;
import com.example.eduview.Classes.Student;
import com.example.eduview.Classes.Teacher;
import com.example.eduview.Classes.User;
import com.example.eduview.Resources.UserRole;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
//    private final DatabaseReference classRef;


    /**
     * Default constructor used by the application.
     * Initializes Firebase database references for the users and role-specific nodes.
     */
    public UserRepository() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        usersRef = db.getReference("users");
        studentsRef = db.getReference("students");
        teachersRef = db.getReference("teachers");
        parentsRef = db.getReference("parents");
//        classRef = db.getReference("parents")
    }

    /**
     * Constructor used for unit testing.
     * Allows injecting mocked DatabaseReference objects instead of using Firebase.
     *
     * @param usersRef reference to the users node
     * @param parentsRef reference to the parents node
     * @param studentsRef reference to the students node
     * @param teachersRef reference to the teachers node
     */
    public UserRepository(DatabaseReference usersRef, DatabaseReference parentsRef,
                          DatabaseReference studentsRef, DatabaseReference teachersRef) {

        this.usersRef = usersRef;
        this.parentsRef = parentsRef;
        this.studentsRef = studentsRef;
        this.teachersRef = teachersRef;
    }

    /**
     * Retrieves a user from the Firebase database using thier ID and converts it to the correct
     * User subclass based on the stored role (Student, Teacher, or Parent) in the database.
     *
     * The method first reads the base user data from the "users" node to determine
     * the role, then fetches role-specific information from the corresponding node.
     *
     * @param userID the unique Firebase user ID
     * @param onSuccess callback invoked with the constructed User object
     * @param onError callback invoked if the user cannot be retrieved or parsed
     */
    public void fetchUser(String userID, Consumer<User> onSuccess, Consumer<Exception> onError) {
        //Retrieve user information from database, when retrieved execute the task below
        usersRef.child(userID).get().addOnCompleteListener(task -> {
            //If the firebase request fails, return an error
            if (!task.isSuccessful()) {
                onError.accept(new RuntimeException("Failed to fetch user"));
                return;
            }

            //Store firebase results in snapshot
            DataSnapshot snapshot = task.getResult();

            //If there is no such user, return an error
            if (!snapshot.exists()) {
                onError.accept(new RuntimeException("No user with that ID"));
                return;
            }

            //Retrieve User information from database
            String firstName = snapshot.child("first_name").getValue(String.class);
            String lastName = snapshot.child("last_name").getValue(String.class);
            String roleStr = snapshot.child("role").getValue(String.class);
            String pfp = snapshot.child("pfp").getValue(String.class);

            if (roleStr == null || lastName == null || firstName == null) {
                onError.accept(new RuntimeException("User information missing"));
                return;
            }

            //Turn role to enum, catch errors in role names.
            UserRole role;
            try {
                role = UserRole.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                onError.accept(new RuntimeException("Invalid user role: " + roleStr));
                return;
            }


            switch (role) {
                case STUDENT:
                    //Retrieve teacher information from database, when retrieved execute the task below
                    studentsRef.child(userID).get().addOnCompleteListener(studentTask -> {
                        //If the firebase retrieval fails, return error
                        if (!studentTask.isSuccessful()) {
                            onError.accept(new RuntimeException("Failed to fetch student data"));
                            return;
                        }

                        //Store firebase results in snapshot
                        DataSnapshot studentSnapshot = studentTask.getResult();
                        String classId = studentSnapshot.child("classroom").getValue(String.class);

                        //Create Teacher Object with information retrieved from database
                        Student user = new Student(userID, firstName, lastName, classId);

                        finalizeUser(user, pfp, onSuccess);
                    });
                    break;

                case TEACHER:
                    //Retrieve teacher information from database, when retrieved execute the task below
                    teachersRef.child(userID).get().addOnCompleteListener(teacherTask -> {
                        //If the firebase retrieval fails, return error
                        if (!teacherTask.isSuccessful()) {
                            onError.accept(new RuntimeException("Failed to fetch teacher data"));
                            return;
                        }

                        //Store firebase results in snapshot
                        DataSnapshot teacherSnapshot = teacherTask.getResult();
                        String classId = teacherSnapshot.child("classroom").getValue(String.class);
                        String email = teacherSnapshot.child("email").getValue(String.class);

                        //Create Teacher Object with information retrieved from database
                        Teacher user = new Teacher(userID, firstName, lastName, email, classId);

                        finalizeUser(user, pfp, onSuccess);
                    });
                    break;

                case PARENT:
                    //Retrieve role specific data for parent
                    parentsRef.child(userID).get().addOnCompleteListener(parentTask -> {
                        //If the firebase retrieval fails, return error
                        if (!parentTask.isSuccessful()) {
                            onError.accept(new RuntimeException("Failed to fetch parent data"));
                            return;
                        }

                        DataSnapshot parentSnapshot = parentTask.getResult();
                        List<String> childrenIDs = getChildrenIds(parentSnapshot);
                        String email = parentSnapshot.child("email").getValue(String.class);

                        Parent user = new Parent(userID, firstName, lastName, email, childrenIDs);
                        finalizeUser(user, pfp, onSuccess);
                    });
                    break;

                default:
                    onError.accept(new RuntimeException("Invalid role"));
                    return;
            }

        });
    }

    @NonNull
    private static List<String> getChildrenIds(DataSnapshot parentSnapshot) {
        DataSnapshot childrenSnapshot = parentSnapshot.child("children");
        List<String> childrenIDs = new ArrayList<>();

        //Store all children ID's of parent user in list of strings
        for (DataSnapshot childSnapshot : childrenSnapshot.getChildren()) {
            childrenIDs.add(childSnapshot.getKey());
        }
        return childrenIDs;
    }

    private void finalizeUser(User user, String pfp, Consumer<User> onSuccess) {
        if (pfp != null) user.setProfileImageURL(pfp);
        onSuccess.accept(user);
    }


    /**
     * Updates the profile picture URL of a user in the Firebase database.
     * Writes the image URL to pfp entry in user node
     *
     * @param userID the unique Firebase user ID
     * @param imageUrl the new profile picture URL
     */
    public void updateProfilePicture(String userID, String imageUrl) {
        usersRef.child(userID).child("pfp").setValue(imageUrl);
    }

    /**
     * Updates the class of student users
     *
     * @param userID the unique Firebase user ID
     * @param classID the new ID of the joined class.
     */
    public void updateClass(String userID, String classID) {
        studentsRef.child(userID).child("classroom").setValue(classID);
    }

//    public void fetchStudentsInClass(String classID, Consumer<List<User>> onSuccess, Consumer<Exception> onError) {
//        //Retrieve user information from database, when retrieved execute the task below
//        teacher.child(classID).get().addOnCompleteListener(task -> {
//            //If the firebase request fails, return an error
//            if (!task.isSuccessful()) {
//                onError.accept(new RuntimeException("Failed to fetch user"));
//                return;
//            }
//    }


    /**
     * Retrieves the list of children IDs associated with a given parent from the Firebase database.
     *
     * @param parentID the unique Firebase user ID of the parent
     * @param onSuccess callback invoked with a list of the parent's children IDs if retrieval succeeds
     * @param onError callback invoked if the Firebase request fails
     */
    public void fetchChildrenOfParent(String parentID, Consumer<List<String>> onSuccess, Consumer<Exception> onError) {
        parentsRef.child(parentID).get().addOnCompleteListener(Task -> {

            //If the firebase retrieval fails, return error
            if (!Task.isSuccessful()) {
                onError.accept(new RuntimeException("Failed to fetch parent data"));
                return;
            }

            DataSnapshot parentSnapshot = Task.getResult();
            //If there is no such user, return an error
            if (!parentSnapshot.exists()) {
                onError.accept(new RuntimeException("No user with that ID"));
                return;
            }

            List<String> childrenIDs = getChildrenIds(parentSnapshot);

            onSuccess.accept(childrenIDs);
        });
    }

}

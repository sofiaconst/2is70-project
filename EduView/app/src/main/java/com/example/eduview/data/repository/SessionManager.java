package com.example.eduview.data.repository;

/*
who is the logged-in user?
is the user logged in?
what is their role?
what is their profile?
what features do they have access to?
stores app-level session data
 */

import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.User;
import com.example.eduview.data.model.UserRole;
import com.google.firebase.auth.FirebaseUser;

/**
 * SessionManager is responsible for managing the authentication session
 * of the currently logged-in user within the application.
 *
 * Responsibilities:
 *
 * 1. Session State
 *    - Determine whether a user is currently authenticated.
 *    - Provide access to the currently authenticated FirebaseUser.
 *    - Provide access to the authenticated user's UID.
 *
 * 2. Session Lifecycle
 *    - Initialize session state when the application starts.
 *    - Handle user logout and session termination.
 *
 * 3. Session Access Point
 *    - Act as a centralized access point for authentication state
 *      throughout the application.
 *    - Allow Activities, Fragments, and ViewModels to query session state
 *      without interacting directly with FirebaseAuth.
 *
 * 4. Session Validation
 *    - Ensure that protected areas of the app are only accessible
 *      when a valid authenticated session exists.
 *
 * 5. Future Extension
 *    - May later handle session persistence logic.
 *    - May later handle user role checks (teacher/parent/student).
 *    - May later integrate with UserRepository to fetch profile data.
 *
 * Non-responsibilities:
 *
 * - Does NOT authenticate users (AuthRepository does this).
 * - Does NOT store or manage user profile data (UserRepository does this).
 * - Does NOT interact with the database.
 *
 * SessionManager acts as a thin coordination layer between the UI
 * and the authentication repository.
 */
public class SessionManager {

    private static SessionManager instance;

    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private User cachedUser;

    private SessionManager(AuthRepository authRepo, UserRepository userRepo) {
        this.authRepository = authRepo;
        this.userRepository = userRepo;
    }

    public static SessionManager getInstance(AuthRepository authRepo, UserRepository userRepo) {
        if (instance == null) instance = new SessionManager(authRepo, userRepo);
        return instance;
    }

    /**
     * Initializes the session once.
     * Safe to call multiple times; will return cached user if already loaded.
     */
    public void initializeSession(SessionCallback callback) {
        // Rotation-safe: return cached user if already loaded
        if (cachedUser != null) {
            callback.onSuccess(cachedUser);
            return;
        }

        FirebaseUser firebaseUser = authRepository.getCurrentFirebaseUser();
        if (firebaseUser == null) {
            callback.onError(new IllegalStateException("User not logged in"));
            return;
        }

        String uid = firebaseUser.getUid();

        // Fetch user from repository
        userRepository.getUserById(uid, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                cachedUser = user;

                // Load role-specific data asynchronously
                loadRoleSpecificData(user, callback);
            }

            @Override
            public void onFailure(String error) {
                callback.onError(new Exception(error));
            }
        });
    }

    private void loadRoleSpecificData(User user, SessionCallback callback) {
        switch (user.getRole()) {
            case STUDENT:
                Student student = (Student) user;
                userRepository.getParentForStudent(student.getUid(), parent -> { // no exiy
                    student.setParent(parent);
                    callback.onSuccess(student);
                }, e -> callback.onError(e));
                break;

            case TEACHER:
                Teacher teacher = (Teacher) user;
                userRepository.getStudentsForTeacher(teacher.getUid(), students -> { // no exist
                    teacher.setStudents(students);
                    callback.onSuccess(teacher);
                }, e -> callback.onError(e));
                break;

            case PARENT:
                Parent parent = (Parent) user;
                userRepository.getChildrenForParent(parent.getUid(), children -> {
                    parent.setChildren(children);
                    callback.onSuccess(parent);
                }, e -> callback.onError(e));
                break;

            default:
                // No extra role-specific data
                callback.onSuccess(user);
        }
    }
    public boolean isLoggedIn() {
        return cachedUser != null;
    }

    public User getCurrentUser() {
        requireLogin();
        return cachedUser;
    }

    public void requireLogin() {
        if (cachedUser == null) {
            throw new IllegalStateException("User is not logged in.");
        }
    }

    public UserRole getCurrentUserRole() {
        requireLogin();
        return cachedUser.getRole();
    }
}

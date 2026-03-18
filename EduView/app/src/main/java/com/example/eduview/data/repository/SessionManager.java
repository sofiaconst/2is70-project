package com.example.eduview.data.repository;

/*
who is the logged-in user?
is the user logged in?
what is their role?
what is their profile?
what features do they have access to?
stores app-level session data
 */

import android.util.Log;

import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.User;
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

    // Repositories
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private User currentUser;

    // Singleton
    private static SessionManager instance;
    private SessionManager() {
        this.authRepository = new AuthRepository();
        this.userRepository = new UserRepository();
    }

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void initializeSession(SessionCallback callback) {
        if (currentUser != null) {
            callback.onSuccess(currentUser);
            return;
        }

        FirebaseUser firebaseUser = authRepository.getCurrentFirebaseUser();
        if (firebaseUser == null) {
            callback.onError(new IllegalStateException("User not logged in"));
            return;
        }

        String uid = firebaseUser.getUid();

        userRepository.getUserById(uid, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                callback.onSuccess(currentUser);
            }

            @Override
            public void onError(Exception error) {
                callback.onError(error);
            }
        });
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logoutCurrentUser(SessionCallback callback) {
        currentUser = null;
        authRepository.logout();
        if(callback != null) {
            callback.onSuccess(null); // notify logout complete
        }
    }

    public void requireLogin() {
        if (currentUser == null) {
            throw new IllegalStateException("User is not logged in.");
        }
    }

    public User getCurrentUser() {
        requireLogin();
        return currentUser;
    }

    public interface SessionCallback {
        void onSuccess(User user);
        void onError(Exception e);

    }
}


package com.example.eduview.data.repository;

// handles authentication only

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/*
AuthRepository should be responsible only for authentication with FirebaseAuth, not user profile data.

Its responsibilities include:

 - Authentication lifecycle
        login
        register
        logout

 - Authentication state
        get current authenticated user
        get user id

 - Credential management
        password reset
        possibly email verification

 - NOT responsible for
        retrieving user profile data
        classroom membership
        names

 */
public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private final FirebaseAuth firebaseAuth;
    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Returns the currently authenticated Firebase user.
     *
     * @return FirebaseUser if logged in, null otherwise.
     */
    public FirebaseUser getCurrentUser() {
            return firebaseAuth.getCurrentUser();
    }
    /**
     * Returns the UID of the currently authenticated user.
     *
     * @return UID if logged in, null otherwise.
     */
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Attempts to authenticate a user using email and password.
     *
     * @param email user's email
     * @param password user's password
     * @return Firebase Task containing the authentication result
     */
    public Task<AuthResult> login(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    /**
     * Registers a new user with Firebase Authentication.
     *
     * NOTE: This only creates the Auth account.
     * The user profile must be created separately in UserRepository.
     *
     * @param email user email
     * @param password user password
     * @return Firebase Task containing the registration result
     */
    public Task<AuthResult> register(String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    /**
     * Signs out the currently authenticated user.
     */
    public void logout() {
        firebaseAuth.signOut();
    }

    /**
     * Sends a password reset email.
     *
     * @param email account email
     * @return Firebase task indicating success or failure
     */
    public void sendPasswordReset(String email, AuthCallback callback) {

        Log.d(TAG, "Sending password reset email to: " + email);

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Log.d(TAG, "Password reset email sent.");
                        callback.onSuccess(null);
                    } else {
                        Log.e(TAG, "Password reset failed", task.getException());
                        callback.onFailure("Failed to send reset email.");
                    }
                });
    }

    /**
     * Sends a password reset email.
     */
    public Task<Void> sendPasswordResetEmail(String email) {
        return firebaseAuth.sendPasswordResetEmail(email);
    }

    /**
     * Checks if a user is logged in.
     */
    public boolean isLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
}

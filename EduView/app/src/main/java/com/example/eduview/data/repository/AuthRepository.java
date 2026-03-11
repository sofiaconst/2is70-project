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

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public Task<AuthResult> login(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    public void login(String email, String password, AuthCallback callback) {

        Log.d(TAG, "Attempting login for: " + email);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Log.e(TAG, "FirebaseAuth login failed", task.getException());
                        callback.onFailure("Invalid credentials.");
                        return;
                    }

                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    if (user == null) {
                        Log.e(TAG, "Firebase returned null user.");
                        callback.onFailure("Authentication error.");
                        return;
                    }

                    String userId = user.getUid();
                    Log.d(TAG, "Auth success. Checking DB for userId: " + userId);

                    usersRef.child(userId).get().addOnCompleteListener(dbTask -> {

                        if (!dbTask.isSuccessful()) {
                            Log.e(TAG, "Database check failed", dbTask.getException());
                            callback.onFailure("Database error.");
                            return;
                        }

                        if (!dbTask.getResult().exists()) {
                            Log.w(TAG, "User exists in Auth but not DB.");
                            callback.onFailure("User not registered.");
                            return;
                        }

                        Log.d(TAG, "User verified in database.");
                        callback.onSuccess(user);
                    });
                });
    }

    public Task<AuthResult> register(String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    public void logout() {
        firebaseAuth.signOut();
    }


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
}

package com.example.eduview.data.repository;

// handles authentication only

public class AuthRepository {

    private FirebaseAuth auth;
    private String currentUid;

    public AuthRepository() {

    }
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
}

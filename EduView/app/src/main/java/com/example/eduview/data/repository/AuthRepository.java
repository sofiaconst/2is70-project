package com.example.eduview.data.repository;

// handles authentication only

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {

        private FirebaseAuth firebaseAuth;

        public AuthRepository() {
            firebaseAuth = FirebaseAuth.getInstance();
        }

        public FirebaseUser getCurrentUser() {
            return firebaseAuth.getCurrentUser();
        }

        public Task<AuthResult> login(String email, String password) {
            return firebaseAuth.signInWithEmailAndPassword(email, password);
        }

        public Task<AuthResult> register(String email, String password) {
            return firebaseAuth.createUserWithEmailAndPassword(email, password);
        }

        public void logout() {
            firebaseAuth.signOut();
        }
}

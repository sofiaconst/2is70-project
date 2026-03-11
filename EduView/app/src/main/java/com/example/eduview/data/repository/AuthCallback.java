package com.example.eduview.data.repository;

import com.google.firebase.auth.FirebaseUser;

public interface AuthCallback {

    void onSuccess(FirebaseUser user);

    void onFailure(String errorMessage);
}

package com.example.eduview.data.repository;

import com.example.eduview.data.model.User;

// Callback for async session initialization
public interface SessionCallback {
    void onSuccess(User user);
    void onError(Exception e);
}
package com.example.eduview.ui.main;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.eduview.R;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.SessionManager;

public class MainViewModel extends ViewModel {
    private final SessionManager sessionManager;
    private User currentUser;

    public MainViewModel() {
        this(SessionManager.getInstance());
    }

    //Constructor for Testing
    public MainViewModel(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void startSession(Runnable onUserReady) {
        sessionManager.initializeSession(new SessionManager.SessionCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                Log.d("MainViewModel", "Session initialized: " + user.getFirstName());
                if (onUserReady != null) {
                    onUserReady.run(); // Activity can safely setup navigation now
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("MainViewModel", "Failed to initialize session", e);
            }
        });
    }

    public int getMenuResForUser() {
        if (currentUser == null) {
            throw new IllegalStateException("User not loaded yet. Call startSession() first.");
        }

        Log.d("MainViewModel", "Current role = " + currentUser.getRole());

        switch (currentUser.getRole()) {
            case PARENT:
                return R.menu.bottom_nav_parent;
            case TEACHER:
            case STUDENT:
            default:
                return R.menu.bottom_nav_main;
        }
    }

    /**
     * Get the UI-ready current user.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Logout the current session and clear cached user.
     */
    public void logout() {
        sessionManager.logoutCurrentUser(null);
        currentUser = null;
    }

}

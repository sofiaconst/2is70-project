package com.example.eduview.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.R;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.SessionManager;

/**
 * The ViewModel that manages the application's session state.
 * Handles user session initialization, role-based UI decisions, and logout.
 */
public class MainViewModel extends ViewModel {
    private final SessionManager sessionManager;

    /**
     * Default constructor using the SessionManager.
     */
    public MainViewModel() {
        this(SessionManager.getInstance());
    }

    /**
     * Constructor used mainly for testing.
     *
     * @param sessionManager the session manager to use
     */
    public MainViewModel(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Initializes the user session and executes the callback.
     *
     * @param onUserReady callback when the user is successfully loaded
     */
    public void startSession(Runnable onUserReady) {
        sessionManager.initializeSession(new SessionManager.SessionCallback() {
            // On success of initializing the session
            @Override
            public void onSuccess(User user) {
                Log.d("MainViewModel", "Session initialized: " + user.getFirstName());
                if (onUserReady != null) {
                    onUserReady.run(); // Activity can safely setup navigation now
                }
            }

            // On error when initializing the session
            @Override
            public void onError(Exception e) {
                Log.e("MainViewModel", "Failed to initialize session", e);
            }
        });
    }

    /**
     * Triggers a manual reload of the current session data from the repository.
     */
    public void refreshSession() {
        sessionManager.reloadSession(null);
    }

    /**
     * Returns the corresponding bottom navigation menu resource based on the user's role.
     *
     * @return menu resource ID corresponding to the user's role
     * @throws IllegalStateException if the user has not been initialized yet
     */
    public int getMenuResForUser() {
        User user = sessionManager.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("User not loaded yet. Call startSession() first.");
        }

        Log.d("MainViewModel", "Current role = " + user.getRole());

        switch (user.getRole()) {
            case PARENT:
                return R.menu.bottom_nav_parent;
            case TEACHER:
            case STUDENT:
            default:
                return R.menu.bottom_nav_main;
        }
    }

    /**
     * Get the UI-ready current user. Always fetched from SessionManager to stay fresh.
     */
    public User getCurrentUser() {
        return sessionManager.getCurrentUser();
    }
    
    /**
     * Returns an observable LiveData of the current session user.
     */
    public LiveData<User> getSessionUser() {
        return sessionManager.getSessionUser();
    }

    /**
     * Logout the current session.
     */
    public void logout() {
        sessionManager.logoutCurrentUser(null);
    }

}

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

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.User;
import com.google.firebase.auth.FirebaseUser;

/**
 * SessionManager is responsible for managing the authentication session
 * of the currently logged-in user within the application.
 */
public class SessionManager {

    // Repositories
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    
    private final MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();
    private User currentUser;

    // Singleton
    private static SessionManager instance;
    private SessionManager() {
        this.authRepository = new AuthRepository();
        this.userRepository = new UserRepository();
    }

    //Constructor for Unit tests
    public SessionManager(AuthRepository authRepository, UserRepository userRepository) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    // For Testing Profile fragment
    public void setCurrentUserForTest(User user) {
        this.currentUser = user;
        this.currentUserLiveData.setValue(user);
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
                currentUserLiveData.postValue(user);
                callback.onSuccess(currentUser);
            }

            @Override
            public void onError(Exception error) {
                callback.onError(error);
            }
        });
    }


    public void logoutCurrentUser(SessionCallback callback) {
        currentUser = null;
        currentUserLiveData.postValue(null);
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
        return currentUser;
    }
    
    public LiveData<User> getSessionUser() {
        return currentUserLiveData;
    }

    public void reloadSession(SessionCallback callback) {
        FirebaseUser firebaseUser = authRepository.getCurrentFirebaseUser();

        if (firebaseUser == null) {
            if (callback != null) callback.onError(new IllegalStateException("User not logged in"));
            return;
        }

        String uid = firebaseUser.getUid();

        userRepository.getUserById(uid, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                currentUserLiveData.postValue(user);
                if (callback != null) callback.onSuccess(currentUser);
            }

            @Override
            public void onError(Exception error) {
                if (callback != null) callback.onError(error);
            }
        });
    }

    public interface SessionCallback {
        void onSuccess(User user);
        void onError(Exception e);
    }
}

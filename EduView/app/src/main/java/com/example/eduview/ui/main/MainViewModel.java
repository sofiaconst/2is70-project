package com.example.eduview.ui.main;

import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.AuthRepository;
import com.example.eduview.data.repository.SessionCallback;
import com.example.eduview.data.repository.SessionManager;
import com.example.eduview.data.repository.UserRepository;
import com.example.eduview.ui.signup.SignupActivity;
import com.google.firebase.auth.FirebaseUser;

public class MainViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    public MainViewModel() {
        this.authRepository = new AuthRepository();
        this.userRepository = new UserRepository();

        // Singleton SessionManager to cache current user globally
        this.sessionManager = SessionManager.getInstance(authRepository, userRepository);
    }

    public void loadCurrentUser() {
        User user = sessionManager.getCurrentUser();
        if(user != null){
            currentUser.postValue(user); // use cached user
        } else {
            Log.w("MainViewModel", "No user cached yet");
        }
    }

    public void startSession() {
        sessionManager.initializeSession(new SessionCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser.postValue(user);
                Log.d("MainViewModel", "Session initialized: " + user.getFirstName());
            }

            @Override
            public void onError(Exception e) {
                Log.e("MainViewModel", "Failed to initialize session", e);
            }
        });
    }

    // Logout user
    public void logout() {
        authRepository.logout();
        currentUser.setValue(null);
        sessionManager.setCurrentUser(null);
    }

    // LiveData for Activity/Fragment observation
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    /*
    public class MainViewModel extends ViewModel {

    private final SessionManager sessionManager;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    public MainViewModel() {
        sessionManager = new SessionManager();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void loadCurrentUser() {

        sessionManager.initializeSession(new SessionCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser.setValue(user);
            }

            @Override
            public void onError(Exception e) {
                Log.e("SESSION", "Failed to load session", e);
            }
        });

    }

}
     */
}
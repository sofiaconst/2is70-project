package com.example.eduview.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.AuthRepository;
import com.example.eduview.data.repository.SessionManager;
import com.example.eduview.data.repository.ClassroomRepository;
import com.example.eduview.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

public class MainViewModel extends ViewModel {
    private final SessionManager sessionManager;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    public MainViewModel() {
        // Use singleton SessionManager (already has repos)
        this.sessionManager = SessionManager.getInstance();
    }

    public void startSession() {
        sessionManager.initializeSession(new SessionManager.SessionCallback() {
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

    public void loadCurrentUser() {
        User user = sessionManager.getCurrentUser();
        if(user != null){
            currentUser.postValue(user); // use cached user
        } else {
            Log.w("MainViewModel", "No user stored yet");
        }
    }

    public void logout() {
        sessionManager.logoutCurrentUser(null);
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

}
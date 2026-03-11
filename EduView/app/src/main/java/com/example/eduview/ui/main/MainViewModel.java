package com.example.eduview.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.AuthRepository;
import com.example.eduview.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

public class MainViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    //private final SessionManager sessionManager;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    public MainViewModel(AuthRepository authRepository,
                         UserRepository userRepository) {
                         //SessionManager sessionManager) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        //this.sessionManager = sessionManager;
    }

    public MainViewModel() {
        this.authRepository = new AuthRepository();
        this.userRepository = new UserRepository();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void loadCurrentUser() {
        FirebaseUser firebaseUser = authRepository.getCurrentUser();

        if (firebaseUser == null) {
            Log.w("MainViewModel", "No Firebase user logged in");
            currentUser.setValue(null);
            return;
        }

        // Normally this would be:
        // String userId = firebaseUser.getUid();

        // TEMP: hardcoded user for development/testing
        String userId = "student_1";

        Log.d("MainViewModel", "Using userId: " + userId);

        userRepository.fetchUser(
                userId,
                user -> {
                    currentUser.postValue(user);
                    Log.d("MainViewModel", "User loaded: " + user.getFirstName() + " " + user.getLastName());
                },
                error -> {
                    Log.e("MainViewModel", "Failed to fetch user", error);
                }
        );
    }

    public void logout() {
        authRepository.logout();
        currentUser.setValue(null);
        //sessionManager.setCurrentUser(null);
    }
}
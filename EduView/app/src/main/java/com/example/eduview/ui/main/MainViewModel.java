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

        /*
        if (firebaseUser == null) {
            //Log.w("MainViewModel", "No user logged in");
            //return;
            userId = "student_1";
            Log.d("MainViewModel", "using userId: " + userId);
        } else {
            userId = firebaseUser.getUid();
        }

         */

        String userId = "student_1";
        Log.d("MainViewModel", "using userId: " + userId);

        userRepository.fetchUser_alt(
                userId,
                user -> {
                    // Cache in SessionManager
                    //sessionManager.setCurrentUser(user);

                    // Update LiveData so fragments observing it get notified
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
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
import com.example.eduview.data.repository.UserRepository;
import com.example.eduview.ui.signup.SignupActivity;
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

        String userId = firebaseUser.getUid();

        Log.d("MainViewModel", "Fetching user with UID: " + userId);

        userRepository.fetchUser(
                userId,
                user -> {
                    currentUser.postValue(user);
                    Log.d("MainViewModel", "User loaded: " + user.getFirstName() + " " + user.getLastName());
                    Log.d("MainViewModel", "ID: " + user.getUserId());
                    Log.d("MainViewModel", "Role: " + user.getRole());

                    if (user instanceof Teacher) {
                        Teacher t = (Teacher) user;
                        Log.d("MainViewModel", "Classroom: " + t.getClassID());
                    }
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
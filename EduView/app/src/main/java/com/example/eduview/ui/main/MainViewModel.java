package com.example.eduview.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.AuthRepository;
import com.example.eduview.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

public class MainViewModel extends ViewModel {

    private AuthRepository authRepository;
    private UserRepository userRepository;

    private MutableLiveData<User> currentUser = new MutableLiveData<>();

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }
    public void loadCurrentUser() {

        FirebaseUser firebaseUser = authRepository.getCurrentUser();

        if (firebaseUser == null) {
            return;
        }

        /*userRepository.getUser(firebaseUser.getUid())
                .observeForever(user -> {
                    currentUser.setValue(user);
                });

         */
    }

    public void logout() {
        authRepository.logout();
        currentUser.setValue(null);
    }
}

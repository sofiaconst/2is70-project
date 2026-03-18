package com.example.eduview.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.SessionManager;
import com.example.eduview.data.repository.UserRepository;
import com.example.eduview.ui.profile.ProfileUIState.BaseUserUiState;

/**
 * Exposes LiveData<BaseUserUiState>
 * Handles: name, role, classroom, profile picture
 */
public class BaseUserViewModel extends ViewModel {
    private final SessionManager sessionManager;
    private final UserRepository userRepository;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    private final MutableLiveData<BaseUserUiState> userState = new MutableLiveData<>();

    public BaseUserViewModel() {
        this.sessionManager = SessionManager.getInstance();
        this.userRepository = new UserRepository();
        loadBaseUser();
    }

    public BaseUserViewModel(SessionManager sessionManager, UserRepository userRepository) {
        this.sessionManager = sessionManager;
        this.userRepository = userRepository;
        loadBaseUser();
    }

    public void loadBaseUser() {
        User user = sessionManager.getCurrentUser(); // already guaranteed to exist
        BaseUserUiState state = new BaseUserUiState(
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getProfileImageURL()
        );
        userState.postValue(state);
    }

    public void loadUser() {
        try {
            User user = sessionManager.getCurrentUser();
            currentUser.setValue(user);
        } catch (Exception e) {
            currentUser.setValue(null);
        }
    }

    public LiveData<BaseUserUiState> getUserState() {
        return userState;
    }


    public void logout(SessionManager.SessionCallback callback) {
        sessionManager.logoutCurrentUser(callback);
    }
}
package com.example.eduview.ui.main;

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
import com.example.eduview.data.repository.ClassroomRepository;
import com.example.eduview.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

public class MainViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> classroomName = new MutableLiveData<>();
    private final MutableLiveData<String> joinStatus = new MutableLiveData<>();

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
    public MainViewModel(AuthRepository authRepository,
                         UserRepository userRepository,
                         ClassroomRepository classroomRepository) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
    }

    public MainViewModel() {
        this.authRepository = new AuthRepository();
        this.userRepository = new UserRepository();
        this.classroomRepository = new ClassroomRepository();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getClassroomName() {
        return classroomName;
    }

    public LiveData<String> getJoinStatus() {
        return joinStatus;
    }

    public void loadCurrentUser() {

        sessionManager.initializeSession(new SessionCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser.setValue(user);
            }
        if (firebaseUser == null) {
            Log.w("MainViewModel", "No Firebase user logged in");
            currentUser.setValue(null);
            classroomName.setValue(null);
            return;
        }

            @Override
            public void onError(Exception e) {
                Log.e("SESSION", "Failed to load session", e);
            }
        });

    }

}
     */
        userRepository.fetchUser(
                userId,
                user -> {
                    currentUser.postValue(user);

                    if (user instanceof Teacher) {
                        String classId = ((Teacher) user).getClassID();
                        loadClassroomName(classId);
                    } else if (user instanceof Student) {
                        String classId = ((Student) user).getClassId();
                        loadClassroomName(classId);
                    } else {
                        classroomName.postValue(null);
                    }
                },
                error -> Log.e("MainViewModel", "Failed to fetch user", error)
        );
    }

    private void loadClassroomName(String classId) {
        if (classId == null || classId.isEmpty()) {
            classroomName.postValue(null);
            return;
        }

        classroomRepository.getClassroomName(
                classId,
                classroomName::postValue,
                error -> {
                    Log.e("MainViewModel", "Failed to fetch classroom name", error);
                    classroomName.postValue(classId); // fallback to ID
                }
        );
    }

    public void joinClass(String classCode) {
        String userId = authRepository.getCurrentUserId();
        if (userId == null) {
            joinStatus.postValue("User not logged in.");
            return;
        }

        classroomRepository.joinClassroom(
                userId,
                classCode,
                () -> {
                    joinStatus.postValue("Joined class successfully!");
                    loadCurrentUser();
                },
                error -> joinStatus.postValue(error.getMessage())
        );
    }

    public void logout() {
        authRepository.logout();
        currentUser.setValue(null);
        classroomName.setValue(null);
    }
}
package com.example.eduview.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.AuthRepository;
import com.example.eduview.data.repository.ClassroomRepository;
import com.example.eduview.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> classroomName = new MutableLiveData<>();
    private final MutableLiveData<String> joinStatus = new MutableLiveData<>();
    private final MutableLiveData<List<Student>> childrenData = new MutableLiveData<>();

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

    public LiveData<List<Student>> getChildrenData() {
        return childrenData;
    }

    public void loadCurrentUser() {
        FirebaseUser firebaseUser = authRepository.getCurrentUser();

        if (firebaseUser == null) {
            Log.w("MainViewModel", "No Firebase user logged in");
            currentUser.setValue(null);
            classroomName.setValue(null);
            childrenData.setValue(null);
            return;
        }

        String userId = firebaseUser.getUid();

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
                    } else if (user instanceof Parent) {
                        loadChildrenData((Parent) user);
                        classroomName.postValue(null);
                    } else {
                        classroomName.postValue(null);
                    }
                },
                error -> Log.e("MainViewModel", "Failed to fetch user", error)
        );
    }

    public void updateBio(String bio) {
        String userId = authRepository.getCurrentUserId();
        if (userId == null) return;

        userRepository.updateBio(userId, bio, () -> {
            Log.d("MainViewModel", "Bio updated successfully");
            loadCurrentUser();
        }, error -> {
            Log.e("MainViewModel", "Failed to update bio", error);
        });
    }

    private void loadChildrenData(Parent parent) {
        List<String> childrenIds = parent.getChildrenIDs();
        if (childrenIds == null || childrenIds.isEmpty()) {
            childrenData.postValue(new ArrayList<>());
            return;
        }

        List<Student> students = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(childrenIds.size());

        for (String id : childrenIds) {
            userRepository.fetchUser(id, user -> {
                if (user instanceof Student) {
                    students.add((Student) user);
                }
                if (remaining.decrementAndGet() == 0) {
                    childrenData.postValue(students);
                }
            }, error -> {
                Log.e("MainViewModel", "Failed to fetch child: " + id, error);
                if (remaining.decrementAndGet() == 0) {
                    childrenData.postValue(students);
                }
            });
        }
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
        childrenData.setValue(null);
    }
}

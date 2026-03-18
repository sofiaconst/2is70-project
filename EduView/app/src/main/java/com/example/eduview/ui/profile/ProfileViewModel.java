package com.example.eduview.ui.profile;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.ClassroomRepository;
import com.example.eduview.data.repository.SessionManager;
import com.example.eduview.data.repository.UserRepository;
import com.example.eduview.domain.usecase.FetchClassroomNameUseCase;
import com.example.eduview.domain.usecase.GenerateQRCodeUseCase;

import java.util.ArrayList;
import java.util.List;

public class ProfileViewModel extends ViewModel {

    private final SessionManager sessionManager;
    private final FetchClassroomNameUseCase fetchClassroomNameUseCase;
    private final GenerateQRCodeUseCase generateQRCodeUseCase;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    private final MutableLiveData<ProfileUIState> uiState = new MutableLiveData<>();
    private final MutableLiveData<List<Student>> classroomStudents = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> studentsLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public ProfileViewModel() {
        this.sessionManager = SessionManager.getInstance();

        this.classroomRepository = new ClassroomRepository();
        this.userRepository = new UserRepository();

        this.fetchClassroomNameUseCase = new FetchClassroomNameUseCase(classroomRepository);
        this.generateQRCodeUseCase = new GenerateQRCodeUseCase();

        observeUser();
    }

    public ProfileViewModel(SessionManager sessionManager,
                            FetchClassroomNameUseCase fetchClassroomNameUseCase,
                            GenerateQRCodeUseCase generateQRCodeUseCase,
                            ClassroomRepository classroomRepository,
                            UserRepository userRepository) {

        this.sessionManager = sessionManager;
        this.fetchClassroomNameUseCase = fetchClassroomNameUseCase;
        this.generateQRCodeUseCase = generateQRCodeUseCase;
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;

        observeUser();
    }

    private void observeUser() {
        User user = sessionManager.getCurrentUser();

        if (user == null) return;

        Bitmap qrBitmap = null;

        if (user instanceof Teacher) {
            String classId = ((Teacher) user).getClassId();
            if (classId != null && !classId.isEmpty()) {
                qrBitmap = generateQRCodeUseCase.execute(classId);
            }
        }

        uiState.postValue(mapUserToState(user, qrBitmap, "Loading..."));
        fetchClassroomName(user);

        if (user instanceof Teacher) {
            loadTeacherStudents();
        }
    }

    private void fetchClassroomName(User user) {
        String classId = extractClassId(user);

        if (classId == null || classId.isEmpty()) return;

        fetchClassroomNameUseCase.execute(classId, new FetchClassroomNameUseCase.Callback<String>() {
            @Override
            public void onSuccess(String className) {
                updateClassroomName(user, className);
            }

            @Override
            public void onError(Exception e) {
                updateClassroomName(user, classId);
            }
        });
    }

    private void updateClassroomName(User user, String className) {
        ProfileUIState current = uiState.getValue();
        Bitmap qr = current != null ? current.qrBitmap : null;
        uiState.postValue(mapUserToState(user, qr, className));
    }

    private String extractClassId(User user) {
        if (user instanceof Teacher) {
            return ((Teacher) user).getClassId();
        }

        if (user instanceof Student) {
            return ((Student) user).getClassId();
        }

        return null;
    }

    private ProfileUIState mapUserToState(User user, Bitmap qrBitmap, String className) {
        String displayName = user.getFirstName() + " " + user.getLastName();
        String roleText = user.getRole().name();

        boolean showScan = user instanceof Student || user instanceof Parent;

        String classText;

        if (user instanceof Teacher || user instanceof Student) {
            classText = "Class: " + (className != null ? className : "None");
        } else if (user instanceof Parent) {
            classText = "Parent Profile";
        } else {
            classText = "Profile";
        }

        return new ProfileUIState(
                displayName,
                roleText,
                classText,
                showScan,
                qrBitmap
        );
    }

    public LiveData<ProfileUIState> getUIState() {
        return uiState;
    }

    public LiveData<List<Student>> getClassroomStudents() {
        return classroomStudents;
    }

    public LiveData<Boolean> getStudentsLoading() {
        return studentsLoading;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void loadTeacherStudents() {
        User currentUser = sessionManager.getCurrentUser();

        if (!(currentUser instanceof Teacher)) return;

        String classId = ((Teacher) currentUser).getClassId();

        if (classId == null || classId.isEmpty()) {
            classroomStudents.setValue(new ArrayList<>());
            return;
        }

        studentsLoading.setValue(true);

        classroomRepository.getStudentIdsForClassroom(classId, new ClassroomRepository.ClassroomCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> studentIds) {
                userRepository.getStudentsByIds(
                        studentIds,
                        students -> {
                            classroomStudents.postValue(students);
                            studentsLoading.postValue(false);
                        },
                        e -> {
                            studentsLoading.postValue(false);
                            message.postValue("Failed to load students");
                        }
                );
            }

            @Override
            public void onError(Exception e) {
                studentsLoading.postValue(false);
                message.postValue("Failed to load students");
            }
        });
    }

    public void removeStudentFromClass(Student student) {
        User currentUser = sessionManager.getCurrentUser();

        if (!(currentUser instanceof Teacher) || student == null) return;

        String classId = ((Teacher) currentUser).getClassId();

        if (classId == null || classId.isEmpty()) return;

        classroomRepository.removeStudentFromClassroom(classId, student.getUserId(),
                new ClassroomRepository.ClassroomCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        message.postValue("Student removed");
                        loadTeacherStudents();
                    }

                    @Override
                    public void onError(Exception e) {
                        message.postValue("Failed to remove student");
                    }
                });
    }

    public void logout() {
        sessionManager.logoutCurrentUser(null);
    }
}
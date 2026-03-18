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
import com.example.eduview.domain.usecase.profile.FetchClassroomNameUseCase;
import com.example.eduview.domain.usecase.profile.GenerateQRCodeUseCase;

public class ProfileViewModel2 extends ViewModel {

    private final SessionManager sessionManager;
    private final FetchClassroomNameUseCase fetchClassroomNameUseCase;
    private final GenerateQRCodeUseCase generateQRCodeUseCase;

    private final MutableLiveData<ProfileUIState> uiState = new MutableLiveData<>();


    // Default constructor (production)
    public ProfileViewModel2() {
        this.sessionManager = SessionManager.getInstance();

        ClassroomRepository repository = new ClassroomRepository();
        this.fetchClassroomNameUseCase = new FetchClassroomNameUseCase(repository);
        this.generateQRCodeUseCase = new GenerateQRCodeUseCase();

        observeUser();
    }


    // Constructor for testing / dependency injection
    public ProfileViewModel2(SessionManager sessionManager,
                             FetchClassroomNameUseCase fetchClassroomNameUseCase,
                             GenerateQRCodeUseCase generateQRCodeUseCase) {

        this.sessionManager = sessionManager;
        this.fetchClassroomNameUseCase = fetchClassroomNameUseCase;
        this.generateQRCodeUseCase = generateQRCodeUseCase;

        observeUser();
    }


    private void observeUser() {

        User user = sessionManager.getCurrentUser();

        if (user == null) return;

        // Initial placeholder state
        uiState.postValue(mapUserToState(user, null, "Loading..."));

        fetchClassroomName(user);
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
                updateClassroomName(user, classId); // fallback
            }
        });
    }


    private void updateClassroomName(User user, String className) {

        ProfileUIState current = uiState.getValue();

        //Bitmap qr = current != null ? current.qrBitmap : null;

        //uiState.postValue(mapUserToState(user, qr, className));
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
        boolean showGenerate = user instanceof Teacher && className != null;

        String classText;

        if (user instanceof Teacher || user instanceof Student) {
            classText = "Class: " + (className != null ? className : "None");
        }
        else if (user instanceof Parent) {
            classText = "Parent Profile";
        }
        else {
            classText = "Profile";
        }
/*
        return new ProfileUIState(
                displayName,
                roleText,
                classText,
                showScan,
                showGenerate,
                qrBitmap
        );

 */
        return null;
    }


    public LiveData<ProfileUIState> getUIState() {
        return uiState;
    }


    public void generateQRCode() {

        User user = sessionManager.getCurrentUser();

        if (!(user instanceof Teacher)) return;

        String classId = ((Teacher) user).getClassId();

        if (classId == null || classId.isEmpty()) return;

        Bitmap qrBitmap = generateQRCodeUseCase.execute(classId);

        ProfileUIState current = uiState.getValue();

        if (current == null) return;
/*
        ProfileUIState updated = new ProfileUIState(
                current.displayName,
                current.roleText,
                current.classText,
                current.showScanButton,
                current.showGenerateButton,
                qrBitmap
        );

 */
        ProfileUIState updated = null;

        uiState.postValue(updated);
    }


    public void logout() {
        sessionManager.logoutCurrentUser(null);
    }
}
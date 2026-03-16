package com.example.eduview.ui.profile;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.model.Classroom;
import com.example.eduview.data.repository.ClassroomRepository;
import com.example.eduview.data.repository.SessionManager;
import com.example.eduview.data.repository.UserRepository;
import com.example.eduview.domain.utils.QRCodeGenerator;
public class ProfileViewModel extends ViewModel {

    private final SessionManager sessionManager;
    private final ClassroomRepository classroomRepository;
    private final MutableLiveData<ProfileUIState> uiState = new MutableLiveData<>();

    public ProfileViewModel() {
        this.sessionManager = SessionManager.getInstance();
        this.classroomRepository = new ClassroomRepository();
        observeUser();
    }

    // DO NOT DELETE, NEEDED FOR TESTING
    public ProfileViewModel(SessionManager sessionManager, ClassroomRepository classroomRepository) {
        this.sessionManager = sessionManager;
        this.classroomRepository = classroomRepository;
        observeUser();
    }

    private void observeUser() {
        User user = sessionManager.getCurrentUser();
        if (user == null) return;

        // Post initial state with placeholder for class name
        ProfileUIState initialState = mapUserToState(user, null, "Loading...");
        uiState.postValue(initialState);

        // Fetch classroom name asynchronously
        fetchClassroomName(user);
    }

    private void fetchClassroomName(User user) { // bad, should be in domain
        String classId;
        boolean isTeacher;

        if (user instanceof Teacher) {
            classId = ((Teacher) user).getClassId();
            isTeacher = true;
        } else {
            isTeacher = false;
            if (user instanceof Student) {
                classId = ((Student) user).getClassId();
            } else {
                classId = null;
            }
        }

        if (classId == null || classId.isEmpty()) return;

        classroomRepository.getClassroomName(classId, new ClassroomRepository.ClassroomCallback<Classroom>() {
            @Override
            public void onSuccess(Classroom result) {
                postUpdatedClassroomName(user, result.getName(), isTeacher);
            }

            @Override
            public void onError(Exception e) {
                postUpdatedClassroomName(user, classId, isTeacher);
            }
        });


    }

    private void postUpdatedClassroomName(User user, String className, boolean isTeacher) {
        ProfileUIState current = uiState.getValue();
        if (current == null) current = mapUserToState(user, null, className);

        ProfileUIState updated = mapUserToState(user, current.qrBitmap, className);
        uiState.postValue(updated);
    }



//    private ProfileUIState mapUserToState(User user, Bitmap qrBitmap, String className) {
//        String displayName = user.getFirstName() + " " + user.getLastName();
//        String roleText = user.getRole().name();
//
//        String classText;
//        boolean showScan = false;
//        boolean showGenerate = false;
//
//        switch (user.getRole()) {
//            case TEACHER:
//                classText = "Class: " + (className != null ? className : "None");
//                showGenerate = className != null && !className.isEmpty();
//                break;
//
//            case STUDENT:
//                classText = "Class: " + (className != null ? className : "None");
//                showScan = true;
//                break;
//
//            case PARENT:
//                classText = "Parent Profile";
//                showScan = true;
//                break;
//
//            default:
//                classText = "Profile";
//        }
//
//        return new ProfileUIState(
//                displayName,
//                roleText,
//                classText,
//                showScan,
//                showGenerate,
//                qrBitmap
//        );
//    }

    private ProfileUIState mapUserToState(User user, Bitmap qrBitmap, String className) {
        String displayName = user.getFirstName() + " " + user.getLastName();
        String roleText = user.getRole().name();
        String classText;
        boolean showScan = user instanceof Student || user instanceof Parent;
        boolean showGenerate = user instanceof Teacher && className != null;

        if(user instanceof Teacher || user instanceof Student) {
            classText = "Class: " + (className != null ? className : "None");
        } else if(user instanceof Parent) {
            classText = "Parent Profile";
        } else {
            classText = "Profile";
        }

        return new ProfileUIState(displayName, roleText, classText, showScan, showGenerate, qrBitmap);
    }

    public LiveData<ProfileUIState> getUIState() {
        return uiState;
    }

    public void generateQRCode() {
        User user = sessionManager.getCurrentUser();
        if (!(user instanceof Teacher)) return;

        String classId = ((Teacher) user).getClassId();
        if (classId == null || classId.isEmpty()) return;

        Bitmap qrBitmap = QRCodeGenerator.generate(classId);

        ProfileUIState current = uiState.getValue();
        if (current == null) return;

        ProfileUIState updated = new ProfileUIState(
                current.displayName,
                current.roleText,
                current.classText,
                current.showScanButton,
                current.showGenerateButton,
                qrBitmap
        );

        uiState.postValue(updated);
    }

    public void logout() {
        sessionManager.logoutCurrentUser(null);
    }
}

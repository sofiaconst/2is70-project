package com.example.eduview.ui.profile;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.AuthService;
import com.example.eduview.R;
import com.example.eduview.data.model.Classroom;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.ProfilePicture;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.AuthRepository;
import com.example.eduview.data.repository.ClassroomRepository;
import com.example.eduview.data.repository.SessionManager;
import com.example.eduview.data.repository.UserRepository;

import com.example.eduview.ui.profile.profileStates.ParentProfileState;
import com.example.eduview.ui.profile.profileStates.StudentProfileState;
import com.example.eduview.ui.profile.profileStates.TeacherProfileState;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

//import com.example.eduview.domain.usecase.FetchClassroomNameUseCase;
//import com.example.eduview.domain.usecase.GenerateQRCodeUseCase;

import java.util.ArrayList;
import java.util.List;

public class ProfileViewModel extends ViewModel {



    // Repositories
    private final SessionManager sessionManager= SessionManager.getInstance();
    private final UserRepository userRepository = new UserRepository();
    private final ClassroomRepository classroomRepository = new ClassroomRepository();

    private final MutableLiveData<ProfileUIState> uiState = new MutableLiveData<>();
    private User currentUser;
    public ProfileViewModel() {
        currentUser = sessionManager.getCurrentUser();

        buildState();
    }

    // UI State
    private void buildState() {

        if (currentUser == null) return;

        String displayName = currentUser.getFirstName() + " " + currentUser.getLastName();
        String roleText = currentUser.getRole().name();
        int profilePictureResId = currentUser.getProfilePictureResourceId();

        StudentProfileState studentState = null;
        TeacherProfileState teacherState = null;
        ParentProfileState parentState = null;

        switch (currentUser.getRole()) {

            case STUDENT:
                studentState = buildStudentState((Student) currentUser);
                break;

            case TEACHER:
                teacherState = buildTeacherState((Teacher) currentUser);
                break;

            case PARENT:
                parentState = buildParentState((Parent) currentUser);
                break;
        }

        ProfileUIState state = new ProfileUIState(
                displayName,
                roleText,
                profilePictureResId,
                studentState,
                teacherState,
                parentState
        );

        uiState.setValue(state);
    }

    // === BUILD UI STATE === &&
    private StudentProfileState buildStudentState(Student student) {

        Log.d("TESTER", student.getClassId());
        if (student.getClassId() == null || student.getClassId().isEmpty()) {

            return StudentProfileState.notRegistered();
        }

        loadStudentClass(student.getClassId());
        return StudentProfileState.loading();
    }
    private TeacherProfileState buildTeacherState(Teacher teacher) {
        String classId = teacher.getClassId();

        if (classId == null || classId.isEmpty()) {
            return TeacherProfileState.error("No class assigned");
        }

        loadTeacherClass(classId);

        return TeacherProfileState.loading();
    }

    private ParentProfileState buildParentState(Parent parent) {
        String parentId = parent.getUserId();

        if (parentId == null) {
            return ParentProfileState.error("Invalid parent");
        }

        loadParentChildren(parent);

        return ParentProfileState.loading();
    }

    // === UPDATE UI STATE === //
    private void updateStudentState(StudentProfileState newStudentState) {

        ProfileUIState current = uiState.getValue();
        if (current == null) return;

        ProfileUIState updated = new ProfileUIState(
                current.displayName,
                current.roleText,
                current.profilePictureResId,
                newStudentState,
                current.teacherState,
                current.parentState
        );

        uiState.setValue(updated);
    }
    private void updateTeacherState(TeacherProfileState newTeacherState) {
        ProfileUIState current = uiState.getValue();
        if (current == null) return;

        ProfileUIState updated = new ProfileUIState(
                current.displayName,
                current.roleText,
                current.profilePictureResId,
                current.studentState,
                newTeacherState,
                current.parentState
        );

        uiState.setValue(updated);
    }

    private void updateParentState(ParentProfileState newParentState) {
        ProfileUIState current = uiState.getValue();
        if (current == null) return;

        ProfileUIState updated = new ProfileUIState(
                current.displayName,
                current.roleText,
                current.profilePictureResId,
                current.studentState,
                current.teacherState,
                newParentState
        );

        uiState.setValue(updated);
    }

    // Business Logic & Functionality

    // === BASE USER PROFILE === //
    public void updateProfilePicture(ProfilePicture pfp) {
        User user = sessionManager.getCurrentUser();
        if (user == null) return;

        userRepository.updateProfilePicture(user.getUserId(), pfp);
        user.setProfilePicture(pfp);

        ProfileUIState current = uiState.getValue();
        if (current == null) return;

        uiState.postValue(new ProfileUIState(
                user.getFirstName() + " " + user.getLastName(),
                user.getRole().name(),
                user.getProfilePictureResourceId(),
                current.studentState,
                current.teacherState,
                current.parentState
        ));
    }

    // === STUDENT CLASS === //
    private void loadStudentClass(String classId) {
        classroomRepository.getClassroomById(classId, new ClassroomRepository.ClassroomCallback<Classroom>() {
            @Override
            public void onSuccess(Classroom classroom) {
                if (classroom == null) {
                    updateStudentState(
                            StudentProfileState.error("Classroom not found")
                    );
                    return;
                }

                String className = classroom.getName();
                String teacherId = classroom.getTeacherId();

                if (teacherId == null || teacherId.isEmpty()) {
                    updateStudentState(StudentProfileState.success(className, null));
                    return;
                }

                // Second call: fetch teacher
                userRepository.getUserById(teacherId, new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User teacher) {
                        String teacherName = teacher.getFirstName() + " " + teacher.getLastName();

                        updateStudentState(
                                StudentProfileState.success(className, teacherName)
                        );
                    }

                    @Override
                    public void onError(Exception e) {

                        // Still show class name even if teacher fails
                        updateStudentState(
                                StudentProfileState.success(className, null)
                        );
                    }
                });
            }
            @Override
            public void onError(Exception e) {
                updateStudentState(
                        StudentProfileState.error(e != null ? e.getMessage() : "Failed to load classroom")
                );
            }
        });

    }
    public void joinClass(String classCode) {
        updateStudentState(StudentProfileState.loading());

        classroomRepository.joinClassroom(currentUser.getUserId(), classCode, new ClassroomRepository.ClassroomCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadStudentClass(((Student) sessionManager.getCurrentUser()).getClassId());
            }
            @Override
            public void onError(Exception e) {
                updateStudentState(StudentProfileState.error(e.getMessage()));
            }
        });
    }

    // === TEACHER CLASS === //
    private void loadTeacherClass(String classId) {

        classroomRepository.getClassroomById(classId, new ClassroomRepository.ClassroomCallback<Classroom>() {

            @Override
            public void onSuccess(Classroom classroom) {
                if (classroom == null) {
                    updateTeacherState(TeacherProfileState.error("Class not found"));
                    return;
                }

                String className = classroom.getName();
                Bitmap qr = generateQRCode(classId);

                loadTeacherStudents(classId, className, qr);
            }

            @Override
            public void onError(Exception e) {
                updateTeacherState(TeacherProfileState.error(e != null ? e.getMessage() : "Failed to load classroom"));
            }
        });
    }

    // === TEACHER "MANAGE STUDENTS" === //
    private void loadTeacherStudents(String classId, String className, Bitmap qr) {
        classroomRepository.getStudentIdsForClassroom(classId,
                new ClassroomRepository.ClassroomCallback<List<String>>() {
                    @Override
                    public void onSuccess(List<String> studentIds) {
                        userRepository.getStudentsByIds(studentIds,
                                students -> {
                                    Log.d("TESTER", Arrays.toString(students.toArray()));
                                    updateTeacherState(TeacherProfileState.success(className, qr, students));
                                },
                                e -> {
                                    updateTeacherState(TeacherProfileState.success(className, qr, new ArrayList<>()));
                                }
                        );
                    }

                    @Override
                    public void onError(Exception e) {
                        updateTeacherState(TeacherProfileState.success(className, qr, new ArrayList<>()));
                    }
                });
    }
    public void removeStudentFromClass(Student student) {
        User currentUser = sessionManager.getCurrentUser();

        if (!(currentUser instanceof Teacher) || student == null) return;

        String classId = ((Teacher) currentUser).getClassId();

        classroomRepository.removeStudentFromClassroom(
                classId,
                student.getUserId(),
                new ClassroomRepository.ClassroomCallback<Void>() {

                    @Override
                    public void onSuccess(Void result) {
                        loadTeacherClass(classId);
                    }

                    @Override
                    public void onError(Exception e) {
                        updateTeacherState(
                                TeacherProfileState.error("Failed to remove student")
                        );
                    }
                }
        );
    }

    // === PARENT "MY CHILDREN === //
    public void loadParentChildren(Parent parent) {
        if (parent == null) return;

        List<String> childrenIds = parent.getChildrenIDs();

        if (childrenIds == null || childrenIds.isEmpty()) {
            // Update the parent state with an empty list
            updateParentState(ParentProfileState.success(new ArrayList<>()));
            return;
        }

        List<Student> students = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(childrenIds.size());

        for (String id : childrenIds) {
            userRepository.getUserById(id, new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    if (user instanceof Student) {
                        students.add((Student) user);
                    }
                    if (remaining.decrementAndGet() == 0) {
                        // All children fetched → update state
                        updateParentState(ParentProfileState.success(students));
                    }
                }

                @Override
                public void onError(Exception error) {
                    Log.e("ProfileViewModel", "Failed to fetch child: " + id, error);
                    if (remaining.decrementAndGet() == 0) {
                        updateParentState(ParentProfileState.success(students));
                    }
                }
            });
        }
    }

    public void addChild(String fName, String lName, String email, String password) {
        String parentId = sessionManager.getCurrentUser().getUserId();
        if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            addChildStatus.postValue("Please fill all fields");
            return;
        }

        addChildStatus.postValue("LOADING"); // signal loading

        AuthService authService = new AuthService();
        AuthService.ChildInfo childInfo = new AuthService.ChildInfo(fName, lName, email);

        authService.addChildToParent(parentId, childInfo, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess() {
                addChildStatus.postValue("SUCCESS"); // signal success
                User currentUser = sessionManager.getCurrentUser();
                loadParentChildren((Parent) currentUser);
            }

            @Override
            public void onFailure(Exception e) {
                addChildStatus.postValue("ERROR: " + e.getMessage()); // signal failure
                Log.e("ProfileViewModel", "Failed to add child", e);
            }
        });
    }

    // === SESSION === //
    public void logout() {
        currentUser = null;
        sessionManager.logoutCurrentUser(null);
    }

    // Utils & Helpers
    private Bitmap generateQRCode(String classCode) {
        if (classCode == null || classCode.trim().isEmpty()) {
            Log.d("TESTER", "Tryna generate QR Code but its empty");
            return null;
        }

        Log.d("TESTER", "There is a QR Code");
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    classCode,
                    BarcodeFormat.QR_CODE,
                    500,
                    500
            );
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);

        } catch (WriterException e) {
            Log.e("ProfileViewModel", "QR generation failed", e);
            return null;
        }
    }

    // Getters
    public LiveData<ProfileUIState> getUIState() {
        return uiState;
    }

    private final MutableLiveData<String> addChildStatus = new MutableLiveData<>();
    public LiveData<String> getAddChildStatus() { return addChildStatus; }


    //================================== OLD ARCHITECTURE====================================//
//    public void loadChildrenData(Parent parent) {
//        List<String> childrenIds = parent.getChildrenIDs();
//
//
//        if (childrenIds == null || childrenIds.isEmpty()) {
//            childrenData.postValue(new ArrayList<>());
//            return;
//        }
//
//        List<Student> students = new ArrayList<>();
//        AtomicInteger remaining = new AtomicInteger(childrenIds.size());
//
//        for (String id : childrenIds) {
//            userRepository.getUserById(id, new UserRepository.UserCallback() {
//
//                @Override
//                public void onSuccess(User user) {
//                    if (user instanceof Student) {
//                        students.add((Student) user);
//                    }
//
//                    if (remaining.decrementAndGet() == 0) {
//                        childrenData.postValue(students);
//                    }
//                }
//
//                @Override
//                public void onError(Exception error) {
//                    Log.e("ProfileViewModel", "Failed to fetch child: " + id, error);
//
//                    if (remaining.decrementAndGet() == 0) {
//                        childrenData.postValue(students);
//                    }
//                }
//            });
//        }
//    }
//
//    public void addChild(String parentId, String fName, String lName, String email, String password) {
//
//        if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || password.isEmpty()) {
//            addChildStatus.postValue("Please fill all fields");
//            return;
//        }
//
//        AuthService authService = new AuthService();
//        AuthService.ChildInfo childInfo = new AuthService.ChildInfo(fName, lName, email);
//
//        addChildStatus.postValue("LOADING");
//
//        authService.addChildToParent(
//                parentId,
//                childInfo,
//                password,
//                new AuthService.AuthCallback() {
//                    @Override
//                    public void onSuccess() {
//                        addChildStatus.postValue("SUCCESS");
//                        loadCurrentUser(); // refresh children
//                    }
//
//                    @Override
//                    public void onFailure(Exception e) {
//                        addChildStatus.postValue("ERROR: " + e.getMessage());
//                    }
//                }
//        );
//    }



}
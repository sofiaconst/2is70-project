package com.example.eduview.ui.profile;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.AuthService;
import com.example.eduview.data.model.Classroom;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.ProfilePicture;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
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

public class ProfileViewModel extends ViewModel {

    // Repositories
    private final SessionManager sessionManager;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    private final MutableLiveData<ProfileUIState> uiState = new MutableLiveData<>();
    private final MutableLiveData<String> addChildStatus = new MutableLiveData<>();

    private User currentUser;


    public ProfileViewModel() {
        this(SessionManager.getInstance(), new UserRepository(), new ClassroomRepository());
    }

    public ProfileViewModel(SessionManager sessionManager,
                            UserRepository userRepository,
                            ClassroomRepository classroomRepository) {
        this.sessionManager = sessionManager;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;

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

    // === BUILD UI STATE ===
    private StudentProfileState buildStudentState(Student student) {
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

    // === UPDATE UI STATE ===
    private void updateStudentState(StudentProfileState newStudentState) {
        ProfileUIState current = uiState.getValue();
        if (current == null) return;

        uiState.setValue(new ProfileUIState(
                current.displayName,
                current.roleText,
                current.profilePictureResId,
                newStudentState,
                current.teacherState,
                current.parentState
        ));
    }

    private void updateTeacherState(TeacherProfileState newTeacherState) {
        ProfileUIState current = uiState.getValue();
        if (current == null) return;

        uiState.setValue(new ProfileUIState(
                current.displayName,
                current.roleText,
                current.profilePictureResId,
                current.studentState,
                newTeacherState,
                current.parentState
        ));
    }

    private void updateParentState(ParentProfileState newParentState) {
        ProfileUIState current = uiState.getValue();
        if (current == null) return;

        uiState.setValue(new ProfileUIState(
                current.displayName,
                current.roleText,
                current.profilePictureResId,
                current.studentState,
                current.teacherState,
                newParentState
        ));
    }

    // === BASE USER PROFILE ===
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

    // === STUDENT CLASS ===
    private void loadStudentClass(String classId) {
        classroomRepository.getClassroomById(classId, new ClassroomRepository.ClassroomCallback<Classroom>() {
            @Override
            public void onSuccess(Classroom classroom) {
                if (classroom == null) {
                    updateStudentState(StudentProfileState.error("Classroom not found"));
                    return;
                }

                String className = classroom.getName();
                String teacherId = classroom.getTeacherId();

                if (teacherId == null || teacherId.isEmpty()) {
                    updateStudentState(StudentProfileState.success(className, null));
                    return;
                }

                userRepository.getUserById(teacherId, new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User teacher) {
                        String teacherName = teacher.getFirstName() + " " + teacher.getLastName();
                        updateStudentState(StudentProfileState.success(className, teacherName));
                    }

                    @Override
                    public void onError(Exception e) {
                        updateStudentState(StudentProfileState.success(className, null));
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                updateStudentState(StudentProfileState.error(e != null ? e.getMessage() : "Failed to load classroom"));
            }
        });
    }

    public void joinClass(String classCode) {
        updateStudentState(StudentProfileState.loading());
        classroomRepository.joinClassroom(currentUser.getUserId(), classCode, new ClassroomRepository.ClassroomCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                sessionManager.reloadSession(new SessionManager.SessionCallback() {
                    @Override
                    public void onSuccess(User user) {
                        loadStudentClass(((Student) sessionManager.getCurrentUser()).getClassId());
                    }

                    @Override
                    public void onError(Exception e) {
                        updateStudentState(StudentProfileState.error(e.getMessage()));
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                updateStudentState(StudentProfileState.error(e.getMessage()));
            }
        });
    }

    // === TEACHER CLASS ===
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

    private void loadTeacherStudents(String classId, String className, Bitmap qr) {
        classroomRepository.getStudentIdsForClassroom(classId, new ClassroomRepository.ClassroomCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> studentIds) {
                userRepository.getStudentsByIds(studentIds,
                        students -> updateTeacherState(TeacherProfileState.success(className, qr, students)),
                        e -> updateTeacherState(TeacherProfileState.success(className, qr, new ArrayList<>()))
                );
            }

            @Override
            public void onError(Exception e) {
                updateTeacherState(TeacherProfileState.success(className, qr, new ArrayList<>()));
            }
        });
    }

    public void removeStudentFromClass(Student student) {
        User user = sessionManager.getCurrentUser();
        if (!(user instanceof Teacher) || student == null) return;

        String classId = ((Teacher) user).getClassId();
        classroomRepository.removeStudentFromClassroom(classId, student.getUserId(), new ClassroomRepository.ClassroomCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadTeacherClass(classId);
            }

            @Override
            public void onError(Exception e) {
                updateTeacherState(TeacherProfileState.error("Failed to remove student"));
            }
        });
    }

    // === PARENT "MY CHILDREN ===
    public void loadParentChildren(Parent parent) {
        if (parent == null) return;
        List<String> childrenIds = parent.getChildrenIDs();

        if (childrenIds == null || childrenIds.isEmpty()) {
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
                        updateParentState(ParentProfileState.success(students));
                    }
                }

                @Override
                public void onError(Exception error) {
                    if (remaining.decrementAndGet() == 0) {
                        updateParentState(ParentProfileState.success(students));
                    }
                }
            });
        }
    }

    public void addChild(String fName, String lName, String username, String password) {
        String parentId = sessionManager.getCurrentUser().getUserId();
        if (fName.isEmpty() || lName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            addChildStatus.postValue("Please fill all fields");
            return;
        }

        if (username.contains("@") || username.contains(" ")) {
            addChildStatus.postValue("Username must not include space character nor @ symbols");
            return;
        }

        addChildStatus.postValue("LOADING");

        // Convert username to email format
        String email = username.toLowerCase() + "@eduview.com";

        AuthService authService = new AuthService();
        AuthService.ChildInfo childInfo = new AuthService.ChildInfo(fName, lName, email);

        authService.addChildToParent(parentId, childInfo, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess() {
                sessionManager.reloadSession(new SessionManager.SessionCallback() {
                    @Override
                    public void onSuccess(User user) {
                        addChildStatus.postValue("SUCCESS");
                        loadParentChildren((Parent) user);
                    }

                    @Override
                    public void onError(Exception e) {
                        addChildStatus.postValue("SUCCESS"); // Still success adding, just session reload failed
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                String errorMsg = e.getMessage();
                if (errorMsg != null && (errorMsg.contains("Email already exists") || 
                                         errorMsg.contains("Username already exists") || 
                                         errorMsg.contains("Username already taken"))) {
                    addChildStatus.postValue("Username already exists");
                } else {
                    addChildStatus.postValue("ERROR: " + errorMsg);
                }
            }
        });
    }

    // === SESSION ===
    public void logout() {
        currentUser = null;
        uiState.postValue(null);
        sessionManager.logoutCurrentUser(null);
    }

    private Bitmap generateQRCode(String classCode) {
        if (classCode == null || classCode.trim().isEmpty()) return null;
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(classCode, BarcodeFormat.QR_CODE, 500, 500);
            return new BarcodeEncoder().createBitmap(bitMatrix);
        } catch (WriterException e) {
            return null;
        }
    }

    public LiveData<ProfileUIState> getUIState() {
        return uiState;
    }

    public LiveData<String> getAddChildStatus() {
        return addChildStatus;
    }
}

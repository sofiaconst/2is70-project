package com.example.eduview.ui.profile;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
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

/**
 * ViewModel responsible for preparing and managing all profile-related data
 * for the UI layer.
 */
public class ProfileViewModel extends ViewModel {

    // Repositories
    private final SessionManager sessionManager;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    // States and Statuses
    private final MutableLiveData<ProfileUIState> uiState = new MutableLiveData<>();
    private final MutableLiveData<String> addChildStatus = new MutableLiveData<>();

    private User currentUser;
    
    // Track live data for cleanup
    private LiveData<List<String>> liveStudentIds;
    private Observer<List<String>> studentIdsObserver;

    /**
     * Default constructor.
     */
    public ProfileViewModel() {
        this(SessionManager.getInstance(), new UserRepository(), new ClassroomRepository());
    }

    /**
     * Constructor that allows in use repositories and session manager to be used.
     *
     * @param sessionManager the session manager used to access and reload the current session
     * @param userRepository repository used for user-related operations
     * @param classroomRepository repository used for classroom-related operations
     */
    public ProfileViewModel(SessionManager sessionManager,
                            UserRepository userRepository,
                            ClassroomRepository classroomRepository) {
        this.sessionManager = sessionManager;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;

        // Read the current user from session and build the initial screen state.
        currentUser = sessionManager.getCurrentUser();
        buildState();
    }

    /**
     * Builds the initial profile UI state for the current user.
     */
    private void buildState() {
        if (currentUser == null) return;

        // Show shared profile values.
        String displayName = currentUser.getFirstName() + " " + currentUser.getLastName();
        String roleText = currentUser.getRole().name();
        int profilePictureResId = currentUser.getProfilePictureResourceId();

        // Null states before user role is known
        StudentProfileState studentState = null;
        TeacherProfileState teacherState = null;
        ParentProfileState parentState = null;

        // Build state depending on the role.
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

    /**
     * Builds the initial student state.
     *
     * @param student the currently logged-in student
     * @return the initial student profile state
     */
    private StudentProfileState buildStudentState(Student student) {
        if (student.getClassId() == null || student.getClassId().isEmpty()) {
            return StudentProfileState.notRegistered();
        }

        // Load classroom data.
        loadStudentClass(student.getClassId());
        return StudentProfileState.loading();
    }

    /**
     * Builds the initial teacher state.
     *
     * @param teacher the currently logged-in teacher
     * @return the initial teacher profile state
     */
    private TeacherProfileState buildTeacherState(Teacher teacher) {
        String classId = teacher.getClassId();
        if (classId == null || classId.isEmpty()) {
            return TeacherProfileState.error("No class assigned");
        }

        // Load the teacher's classroom and students.
        loadTeacherClass(classId);
        return TeacherProfileState.loading();
    }

    /**
     * Builds the initial specific state.
     *
     * @param parent the currently logged-in parent
     * @return the initial parent profile state
     */
    private ParentProfileState buildParentState(Parent parent) {
        String parentId = parent.getUserId();
        if (parentId == null) {
            return ParentProfileState.error("Invalid parent");
        }

        // Load the parent's child accounts.
        loadParentChildren(parent);
        return ParentProfileState.loading();
    }

    /**
     * Replaces only the student section of the current UI state.
     *
     * @param newStudentState the new student state to publish
     */
    private void updateStudentState(StudentProfileState newStudentState) {
        ProfileUIState current = uiState.getValue();
        if (current == null) return;

        // Keep the rest of the UI state unchanged and only swap the student part.
        uiState.setValue(new ProfileUIState(
                current.displayName,
                current.roleText,
                current.profilePictureResId,
                newStudentState,
                current.teacherState,
                current.parentState
        ));
    }

    /**
     * Replaces only the teacher section of the current UI state.
     *
     * @param newTeacherState the new teacher state to publish
     */
    private void updateTeacherState(TeacherProfileState newTeacherState) {
        ProfileUIState current = uiState.getValue();
        if (current == null) return;

        // Keep shared and other role states intact.
        uiState.setValue(new ProfileUIState(
                current.displayName,
                current.roleText,
                current.profilePictureResId,
                current.studentState,
                newTeacherState,
                current.parentState
        ));
    }

    /**
     * Replaces only the parent section of the current UI state.
     *
     * @param newParentState the new parent state to publish
     */
    private void updateParentState(ParentProfileState newParentState) {
        ProfileUIState current = uiState.getValue();
        if (current == null) return;

        // Update just the parent-related data while preserving the rest.
        uiState.setValue(new ProfileUIState(
                current.displayName,
                current.roleText,
                current.profilePictureResId,
                current.studentState,
                current.teacherState,
                newParentState
        ));
    }

    /**
     * Updates the current user's profile picture in the repository and in the UI state.
     *
     * @param pfp the new profile picture selection
     */
    public void updateProfilePicture(ProfilePicture pfp) {
        User user = sessionManager.getCurrentUser();
        if (user == null) return;

        // Update the new profile picture and the local user object.
        userRepository.updateProfilePicture(user.getUserId(), pfp);
        user.setProfilePicture(pfp);

        ProfileUIState current = uiState.getValue();
        if (current == null) return;

        // Create a refreshed UI state so observers can render the new image.
        uiState.postValue(new ProfileUIState(
                user.getFirstName() + " " + user.getLastName(),
                user.getRole().name(),
                user.getProfilePictureResourceId(),
                current.studentState,
                current.teacherState,
                current.parentState
        ));
    }

    /**
     * Loads detailed classroom information for a student.
     *
     * @param classId the classroom ID assigned to the student
     */
    private void loadStudentClass(String classId) {
        classroomRepository.getClassroomById(classId, new ClassroomRepository.ClassroomCallback<Classroom>() {
            // If student class was loaded
            @Override
            public void onSuccess(Classroom classroom) {
                if (classroom == null) {
                    updateStudentState(StudentProfileState.error("Classroom not found"));
                    return;
                }

                // Store class name and teacher ID of student
                String className = classroom.getName();
                String teacherId = classroom.getTeacherId();

                if (teacherId == null || teacherId.isEmpty()) {
                    updateStudentState(StudentProfileState.success(className, null));
                    return;
                }

                // Load teacher details so the student's profile can show the teacher name.
                userRepository.getUserById(teacherId, new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User teacher) {
                        String teacherName = teacher.getFirstName() + " " + teacher.getLastName();
                        updateStudentState(StudentProfileState.success(className, teacherName));
                    }

                    @Override
                    public void onError(Exception e) {
                        // If teacher loading fails still show the class information.
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

    /**
     * Attempts to join a classroom using a class code.
     *
     * @param classCode the classroom join code entered by the user
     */
    public void joinClass(String classCode) {
        updateStudentState(StudentProfileState.loading());

        classroomRepository.joinClassroom(currentUser.getUserId(), classCode, new ClassroomRepository.ClassroomCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Reload session so the current user object shows the new class membership.
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

    /**
     * Loads the teacher's classroom information and generates a QR code for the class.
     *
     * @param classId the teacher's assigned classroom ID
     */
    private void loadTeacherClass(String classId) {
        classroomRepository.getClassroomById(classId, new ClassroomRepository.ClassroomCallback<Classroom>() {
            @Override
            public void onSuccess(Classroom classroom) {
                if (classroom == null) {
                    updateTeacherState(TeacherProfileState.error("Class not found"));
                    return;
                }

                String className = classroom.getName();

                // Generate a QR code from the class ID so students can join.
                Bitmap qr = generateQRCode(classId);

                // Start observing student IDs in real-time
                if (liveStudentIds != null && studentIdsObserver != null) {
                    liveStudentIds.removeObserver(studentIdsObserver);
                }
                
                liveStudentIds = classroomRepository.getLiveStudentIdsForClassroom(classId);
                studentIdsObserver = ids -> loadTeacherStudents(classId, className, qr, ids);
                liveStudentIds.observeForever(studentIdsObserver);
            }

            @Override
            public void onError(Exception e) {
                updateTeacherState(TeacherProfileState.error(e != null ? e.getMessage() : "Failed to load classroom"));
            }
        });
    }

    /**
     * Loads the list of students that belong to a teacher's classroom.
     *
     * @param classId the classroom ID
     * @param className the classroom name
     * @param qr the generated QR code bitmap for the classroom
     * @param studentIds the list of student IDs currently in the classroom
     */
    private void loadTeacherStudents(String classId, String className, Bitmap qr, List<String> studentIds) {
        // Convert the list of student IDs into actual Student objects.
        userRepository.getStudentsByIds(studentIds,
                students -> updateTeacherState(TeacherProfileState.success(className, qr, students)),
                e -> updateTeacherState(TeacherProfileState.success(className, qr, new ArrayList<>()))
        );
    }

    /**
     * Removes a student from the teacher's current classroom.
     *
     * @param student the student to remove from the class
     */
    public void removeStudentFromClass(Student student) {
        User user = sessionManager.getCurrentUser();
        if (!(user instanceof Teacher) || student == null) return;

        String classId = ((Teacher) user).getClassId();
        classroomRepository.removeStudentFromClassroom(classId, student.getUserId(), new ClassroomRepository.ClassroomCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Real-time listener will handle the update
            }

            @Override
            public void onError(Exception e) {
                updateTeacherState(TeacherProfileState.error("Failed to remove student"));
            }
        });
    }

    /**
     * Loads all child accounts linked to a parent.
     *
     * @param parent the parent whose children should be loaded
     */
    public void loadParentChildren(Parent parent) {
        if (parent == null) return;
        List<String> childrenIds = parent.getChildrenIDs();

        // If the parent has no linked children, return an empty success.
        if (childrenIds == null || childrenIds.isEmpty()) {
            updateParentState(ParentProfileState.success(new ArrayList<>()));
            return;
        }

        List<Student> students = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(childrenIds.size());

        // Fetch each child account one by one.
        for (String id : childrenIds) {
            userRepository.getUserById(id, new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    if (user instanceof Student) {
                        students.add((Student) user);
                    }

                    // When the last request finishes publish the collected students.
                    if (remaining.decrementAndGet() == 0) {
                        updateParentState(ParentProfileState.success(students));
                    }
                }

                @Override
                public void onError(Exception error) {
                    // If one lookup fails continue and finish once all requests are done.
                    if (remaining.decrementAndGet() == 0) {
                        updateParentState(ParentProfileState.success(students));
                    }
                }
            });
        }
    }

    /**
     * Creates a new child account and links it to the currently logged in parent.
     *
     * @param fName the child's first name
     * @param lName the child's last name
     * @param username the child's username
     * @param password the child's password
     */
    public void addChild(String fName, String lName, String username, String password) {
        String parentId = sessionManager.getCurrentUser().getUserId();

        // Validation for required form fields
        if (fName.isEmpty() || lName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            addChildStatus.postValue("Please fill all fields");
            return;
        }

        // Email conversion
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
                        addChildStatus.postValue("SUCCESS");
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

    /**
     * Logs out the current user and clears the profile UI state.
     */
    public void logout() {
        // Clear local references first so the UI can react immediately.
        currentUser = null;
        uiState.postValue(null);

        // Delegate actual session termination to the session manager.
        sessionManager.logoutCurrentUser(null);
    }

    /**
     * Generates a QR code bitmap from the provided class code.
     *
     * @param classCode the class code to encode
     * @return a QR code bitmap
     */
    private Bitmap generateQRCode(String classCode) {
        if (classCode == null || classCode.trim().isEmpty()) return null;

        try {
            // Encode the class code into a square QR matrix and convert it to a bitmap.
            BitMatrix bitMatrix = new MultiFormatWriter().encode(classCode, BarcodeFormat.QR_CODE, 500, 500);
            return new BarcodeEncoder().createBitmap(bitMatrix);
        } catch (WriterException e) {
            return null;
        }
    }

    /**
     * Returns observable profile UI state.
     *
     * @return live data containing the current
     */
    public LiveData<ProfileUIState> getUIState() {
        return uiState;
    }

    /**
     * Returns observable status updates for the add-child operation.
     *
     * @return live data containing add-child status messages
     */
    public LiveData<String> getAddChildStatus() {
        return addChildStatus;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (liveStudentIds != null && studentIdsObserver != null) {
            liveStudentIds.removeObserver(studentIdsObserver);
        }
    }
}
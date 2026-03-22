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

    private final MutableLiveData<ProfileUIState> uiState = new MutableLiveData<>();

    // Repositories

    private SessionManager sessionManager;
    private UserRepository userRepository = new UserRepository();
    private ClassroomRepository classroomRepository = new ClassroomRepository();
    private User currentUser;

    // Use Cases
    //private final FetchClassroomNameUseCase fetchClassroomNameUseCase;
    //private final GenerateQRCodeUseCase generateQRCodeUseCase;

    public ProfileViewModel() {
        // Setup Repos
        this.sessionManager = SessionManager.getInstance();
        this.classroomRepository = new ClassroomRepository();

        currentUser = sessionManager.getCurrentUser();

        buildState();
//        this.fetchClassroomNameUseCase = new FetchClassroomNameUseCase(classroomRepository);
//        this.generateQRCodeUseCase = new GenerateQRCodeUseCase();
//
//        observeUser();
    }

    /*
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

 */

    private void buildState() {

        if (currentUser == null) return;

        String displayName = currentUser.getFirstName() + " " + currentUser.getLastName();
        String roleText = currentUser.getRole().name();

        StudentProfileState studentState = null;
        TeacherProfileState teacherState = null;
        //ParentProfileState parentState = null;

        switch (currentUser.getRole()) {

            case STUDENT:
                studentState = buildStudentState((Student) currentUser);
                break;

            case TEACHER:
                teacherState = buildTeacherState((Teacher) currentUser);
                break;

            case PARENT:
                //parentState = buildParentState((Parent) currentUser);
                break;
        }

        ProfileUIState state = new ProfileUIState(
                displayName,
                roleText,
                R.drawable.pfp_diplo_green,
                studentState,
                teacherState
                //parentState
        );

        uiState.setValue(state);
    }

    // UI State
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

//    private void loadTeacherClass(String classId) {
//        classroomRepository.getClassroomById(classId, new ClassroomRepository.ClassroomCallback<Classroom>() {
//            @Override
//            public void onSuccess(Classroom classroom) {
//                String classId = classroom.getId();
//                String className = classroom.getName();
//
//                List<Student> students = classroom.getStudents(); // or fetch separately
//                Bitmap qr = qrCodeGenerator.generateQRCode(classId);
//
//                updateTeacherState(TeacherProfileState.success(classId, className, qr, students));
//            }
//
//            @Override
//            public void onError(Exception e) {
//                updateTeacherState(TeacherProfileState.error(e.getMessage()));
//            }
//        });
//    }

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

    private void updateStudentState(StudentProfileState newStudentState) {

        ProfileUIState current = uiState.getValue();
        if (current == null) return;

        ProfileUIState updated = new ProfileUIState(
                current.displayName,
                current.roleText,
                current.profilePictureResId,
                newStudentState,
                current.teacherState
                //current.parentState
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
                newTeacherState
                //current.parentState
        );

        uiState.setValue(updated);
    }

    // Functionality
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

    public void logout() {
        sessionManager.logoutCurrentUser(null);
    }

    public LiveData<ProfileUIState> getUIState() {
        return uiState;
    }


    //================================== OLD ARCHITECTURE====================================//
    // Data
   /* private final MutableLiveData<String> classroomName = new MutableLiveData<>();
    private final MutableLiveData<String> joinStatus = new MutableLiveData<>();
    private final MutableLiveData<List<Student>> childrenData = new MutableLiveData<>();
    private final MutableLiveData<StudentProfileState> studentState = new MutableLiveData<>();
    private final MutableLiveData<TeacherProfileState> teacherState = new MutableLiveData<>();
    private final MutableLiveData<String> addChildStatus = new MutableLiveData<>();

    private final MutableLiveData<List<Student>> classroomStudents = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> studentsLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();


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
            public void onSuccess(User user) {
                currentUser.postValue(user);
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

        classroomRepository.joinClassroom(userId, classCode, new ClassroomRepository.ClassroomCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                joinStatus.postValue("Joined class successfully!");
                loadCurrentUser(); // refresh user info to reflect classroom membership
            }

            @Override
            public void onError(Exception e) {
                joinStatus.postValue("Failed to join class: " + e.getMessage());
            }
        });
    }
*//*
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
                user.getProfilePictureResourceId(),
                showScan,
                qrBitmap
        );
    }*//*


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

    public void updateProfilePicture(ProfilePicture pfp) {
        User user = sessionManager.getCurrentUser();
        if (user == null) return;

        userRepository.updateProfilePicture(user.getUserId(), pfp);
        user.setProfilePicture(pfp);

        // Refresh UI
        ProfileUIState current = uiState.getValue();
        if (current != null) {
            uiState.postValue(mapUserToState(user, current.qrBitmap, current.classText.replace("Class: ", "")));
        }
    }



    public void loadChildrenData(Parent parent) {
        List<String> childrenIds = parent.getChildrenIDs();


        if (childrenIds == null || childrenIds.isEmpty()) {
            childrenData.postValue(new ArrayList<>());
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
                        childrenData.postValue(students);
                    }
                }

                @Override
                public void onError(Exception error) {
                    Log.e("ProfileViewModel", "Failed to fetch child: " + id, error);

                    if (remaining.decrementAndGet() == 0) {
                        childrenData.postValue(students);
                    }
                }
            });
        }
    }

    public void addChild(String parentId, String fName, String lName, String email, String password) {

        if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            addChildStatus.postValue("Please fill all fields");
            return;
        }

        AuthService authService = new AuthService();
        AuthService.ChildInfo childInfo = new AuthService.ChildInfo(fName, lName, email);

        addChildStatus.postValue("LOADING");

        authService.addChildToParent(
                parentId,
                childInfo,
                password,
                new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        addChildStatus.postValue("SUCCESS");
                        loadCurrentUser(); // refresh children
                    }

                    @Override
                    public void onFailure(Exception e) {
                        addChildStatus.postValue("ERROR: " + e.getMessage());
                    }
                }
        );
    }


    public void loadTeacherProfile(String classId) {
        teacherState.postValue(TeacherProfileState.loading());

        classroomRepository.getClassroomById(classId, new ClassroomRepository.ClassroomCallback<Classroom>() {
            @Override
            public void onSuccess(Classroom classroom) {
                if (classroom == null) {
                    teacherState.postValue(TeacherProfileState.error("Classroom not found"));
                    return;
                }

                teacherState.postValue(TeacherProfileState.success(classroom.getName(), generateQRCode(classId)));
            }

            @Override
            public void onError(Exception e) {
                teacherState.postValue(TeacherProfileState.error(e.getMessage()));
            }
        });


    }

    public LiveData<String> getAddChildStatus() {
        return addChildStatus;
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


    // Getters

    public LiveData<User> getCurrentUser() { return currentUser; }
    public LiveData<String> getClassroomName() {
        return classroomName;
    }
    public LiveData<String> getJoinStatus() { return joinStatus; }
    public LiveData<List<Student>> getChildrenData() {
        return childrenData;
    }
    public LiveData<StudentProfileState> getStudentState() {return studentState;}
    public LiveData<TeacherProfileState> getTeacherState() {return teacherState;}
*/

}
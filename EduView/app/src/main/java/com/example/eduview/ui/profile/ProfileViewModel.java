package com.example.eduview.ui.profile;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.AuthService;
import com.example.eduview.data.model.Classroom;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.AuthRepository;
import com.example.eduview.data.repository.ClassroomRepository;
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

public class ProfileViewModel extends ViewModel {

    // Reposiories
    private final AuthRepository authRepository = new AuthRepository();
    private final UserRepository userRepository = new UserRepository();
    private final ClassroomRepository classroomRepository = new ClassroomRepository();

    // Data
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> classroomName = new MutableLiveData<>();
    private final MutableLiveData<String> joinStatus = new MutableLiveData<>();
    private final MutableLiveData<List<Student>> childrenData = new MutableLiveData<>();
    private final MutableLiveData<StudentProfileState> studentState = new MutableLiveData<>();
    private final MutableLiveData<TeacherProfileState> teacherState = new MutableLiveData<>();
    private final MutableLiveData<String> addChildStatus = new MutableLiveData<>();

    public LiveData<String> getAddChildStatus() {
        return addChildStatus;
    }

    // Constructor
    public ProfileViewModel() {
        loadCurrentUser();
    }

    /**
     * Temporarily load current user (delegate from MainViewModel if needed)
     */
    public void loadCurrentUser() {
        String userId = authRepository.getCurrentUserId();
        if (userId == null) return;

        // You can replace this with your repository call to fetch the user
        userRepository.getUserById(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser.postValue(user);
            }

            @Override
            public void onError(Exception e) {
                Log.e("ProfileViewModel", "Failed to load user", e);
            }
        });
    }

    /**
     * Join a classroom using the class code scanned
     */
    public void joinClass(String classCode) {
        String userId = authRepository.getCurrentUserId();
        if (userId == null) {
            joinStatus.postValue("User not logged in.");
            return;
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

    public void loadStudentProfile(String classId) {
        studentState.postValue(StudentProfileState.loading());

        classroomRepository.getClassroomById(classId, new ClassroomRepository.ClassroomCallback<Classroom>() {
            @Override
            public void onSuccess(Classroom classroom) {
                if (classroom == null) {
                    studentState.postValue(StudentProfileState.error("Classroom not found"));
                    return;
                }

                String teacherId = classroom.getTeacherId();
                if (teacherId == null || teacherId.isEmpty()) {
                    // No teacher assigned
                    studentState.postValue(StudentProfileState.success(classroom.getName(), null));
                    return;
                }

                // Fetch teacher details
                userRepository.getUserById(teacherId, new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User teacher) {
                        String teacherName = teacher.getFirstName() + " " + teacher.getLastName();
                        studentState.postValue(StudentProfileState.success(classroom.getName(), teacherName));
                    }

                    @Override
                    public void onError(Exception e) {
                        // Could still display class name even if teacher fails
                        studentState.postValue(StudentProfileState.success(classroom.getName(), null));
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                studentState.postValue(StudentProfileState.error(e.getMessage()));
            }
        });
    }

    public void setStudentUnregistered() {
        studentState.postValue(StudentProfileState.notRegistered());
    }

    public void logout() {
        authRepository.logout();
        currentUser.setValue(null);
        classroomName.setValue(null);
        childrenData.setValue(null);
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

    public Bitmap generateQRCode(String classCode) {
        if (classCode == null || classCode.trim().isEmpty()) {
            return null;
        }

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
            Log.e("ProfileFragment", "Error generating QR code", e);
            return null;
        }
    }

    public void generateTeacherQR(String classId) {
        /*
        Bitmap qr = generateQRCode(classId);

        teacherState.setValue(teacherState.copyWithQr(qr));

         */
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


}
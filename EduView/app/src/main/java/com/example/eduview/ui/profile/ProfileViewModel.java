package com.example.eduview.ui.profile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.data.model.Classroom;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.AuthRepository;
import com.example.eduview.data.repository.ClassroomRepository;
import com.example.eduview.data.repository.UserRepository;
import com.example.eduview.ui.profile.profileStates.StudentProfileState;

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

    public void fetchClassroomName(String classId) {
        classroomRepository.getClassroomById(
                classId,
                new ClassroomRepository.ClassroomCallback<Classroom>() {
                    @Override
                    public void onSuccess(Classroom classroom) {
                        if (classroom != null && classroom.getName() != null) {
                            classroomName.postValue(classroom.getName());
                        } else {
                            classroomName.postValue("Unknown");
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        classroomName.postValue("Error");
                        Log.e("ProfileViewModel", "Error loading class name", e);
                    }
                }
        );
    }

    public void processStudent(Student student) {
        if (student == null) return;

        String classId = student.getClassId();

        // Not registered
        if (classId == null || classId.isEmpty()) {
            studentState.setValue(StudentProfileState.notRegistered());
            return;
        }

        // Loading state
        studentState.setValue(StudentProfileState.loading());

        // Fetch classroom data
        classroomRepository.getClassroomById(
                classId,
                new ClassroomRepository.ClassroomCallback<Classroom>() {
                    @Override
                    public void onSuccess(Classroom classroom) {
                        if (classroom != null) {
                            String className = classroom.getName();
                            //String teacherName = classroom.getTeacherName(); // adjust if needed

                            studentState.postValue(
                                    StudentProfileState.success(className, "teacherName")
                            );
                        } else {
                            studentState.postValue(
                                    StudentProfileState.error("Classroom data is null")
                            );
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        studentState.postValue(
                                StudentProfileState.error(e.getMessage())
                        );
                    }
                }
        );


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

    public void loadChildrenData(Parent parent) {
        List<String> childrenIds = parent.getChildrenIDs();
        Log.d("TESTER", Arrays.toString(childrenIds.toArray()));

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
                        Log.d("TESTER", user.getFirstName());
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
}
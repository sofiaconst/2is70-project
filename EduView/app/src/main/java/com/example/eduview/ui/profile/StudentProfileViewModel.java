package com.example.eduview.ui.profile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.data.model.Classroom;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.repository.ClassroomRepository;
import com.example.eduview.data.repository.SessionManager;

/**
 * Extends/uses BaseUserViewModel or composition
 * Handles: scan QR code, join class, show class info
 */
public class StudentProfileViewModel extends ViewModel {


    private final ClassroomRepository classroomRepository = new ClassroomRepository();

    // OUTPUT → observed by Fragment
    private final MutableLiveData<String> className = new MutableLiveData<>();

    public LiveData<String> getClassName() {
        return className;
    }

    // INPUT → called by Fragment to trigger loading

    public void loadClassName(String classId) {

        if (classId == null || classId.isEmpty()) {
            className.setValue("Not registered");
            return;
        }

        className.setValue("Loading...");
        Log.d("TESTER", "Loading...");

        classroomRepository.getClassroomName(
                classId,
                new ClassroomRepository.ClassroomCallback<Classroom>() {

                    @Override
                    public void onSuccess(Classroom classroom) {
                        if (classroom != null && classroom.getName() != null) {
                            Log.d("TESTER", classroom.getName());
                            className.postValue(classroom.getName());
                        } else {
                            className.postValue("Unknown class");
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        className.postValue("Error loading class");
                    }
                }
        );
    }
}
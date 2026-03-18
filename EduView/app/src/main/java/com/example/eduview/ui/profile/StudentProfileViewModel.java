package com.example.eduview.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.data.repository.ClassroomRepository;
import com.example.eduview.domain.usecase.profile.FetchClassroomNameUseCase;

/**
 * Extends/uses BaseUserViewModel or composition
 * Handles: scan QR code, join class, show class info
 */
public class StudentProfileViewModel extends ViewModel {

    private final FetchClassroomNameUseCase fetchClassroomNameUseCase;

    private final MutableLiveData<String> className = new MutableLiveData<>();
    private String cachedClassId = null;
    private final MutableLiveData<String> classTeacher = new MutableLiveData<>();
    private String cachedClassTeacher = null;

    public StudentProfileViewModel() {
        ClassroomRepository repository = new ClassroomRepository();
        this.fetchClassroomNameUseCase = new FetchClassroomNameUseCase(repository);
    }

    public LiveData<String> getClassName() {
        return className;
    }

    public void loadClassName(String classId) {
        if (classId == null || classId.isEmpty()) return;

        // Check if we already loaded this classId
        if (cachedClassId != null && cachedClassId.equals(classId) && className.getValue() != null) {
            return; // already loaded
        }

        cachedClassId = classId;

        fetchClassroomNameUseCase.execute(classId, new FetchClassroomNameUseCase.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                className.postValue(result); // updates LiveData
            }

            @Override
            public void onError(Exception e) {
                className.postValue(null);
            }
        });
    }
}
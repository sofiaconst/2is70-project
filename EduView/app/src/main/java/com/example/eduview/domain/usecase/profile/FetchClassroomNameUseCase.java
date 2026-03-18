package com.example.eduview.domain.usecase.profile;

import com.example.eduview.data.model.Classroom;
import com.example.eduview.data.repository.ClassroomRepository;

public class FetchClassroomNameUseCase {

    private final ClassroomRepository repository;

    public FetchClassroomNameUseCase(ClassroomRepository repository) {
        this.repository = repository;
    }

    public void execute(String classId, Callback<String> callback) {

        repository.getClassroomName(classId, new ClassroomRepository.ClassroomCallback<Classroom>() {

            @Override
            public void onSuccess(Classroom classroom) {
                if (classroom != null && classroom.getName() != null) {
                    callback.onSuccess(classroom.getName());
                } else {
                    callback.onError(new Exception("Invalid classroom data"));
                }
            }
            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
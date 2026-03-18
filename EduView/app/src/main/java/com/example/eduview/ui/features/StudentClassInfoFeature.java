package com.example.eduview.ui.features;

import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;

import com.example.eduview.R;
import com.example.eduview.data.model.Student;
import com.example.eduview.ui.profile.StudentProfileViewModel;

public class StudentClassInfoFeature implements ProfileFeature {

    private final View rootView;
    private final StudentProfileViewModel viewModel;
    private final Student student;

    private View cardClassInfo;
    private TextView classNameText;
    private TextView teacherNameText;
    private TextView notRegisteredText;
    public StudentClassInfoFeature(View rootView, StudentProfileViewModel viewModel, Student student) {
        this.rootView = rootView;
        this.viewModel = viewModel;
        this.student = student;

        initViews();
        observeViewModel();
    }

    private void initViews() {
        cardClassInfo = rootView.findViewById(R.id.cardClassInfo);
        classNameText = rootView.findViewById(R.id.tvClassName);
        teacherNameText = rootView.findViewById(R.id.tvTeacherName);
        notRegisteredText = rootView.findViewById(R.id.tvNotRegistered);

        // Hide everything initially
        cardClassInfo.setVisibility(View.GONE);
        classNameText.setVisibility(View.GONE);
        teacherNameText.setVisibility(View.GONE);
        notRegisteredText.setVisibility(View.VISIBLE);
    }

    private void observeViewModel() {
        // Observe registration state

        viewModel.getIsRegistered().observe((LifecycleOwner) rootView.getContext(), registered -> {
            if (registered != null && registered) {
                notRegisteredText.setVisibility(View.GONE);
                classNameText.setVisibility(View.VISIBLE);
                teacherNameText.setVisibility(View.VISIBLE);
            } else {
                notRegisteredText.setVisibility(View.VISIBLE);
                classNameText.setVisibility(View.GONE);
                teacherNameText.setVisibility(View.GONE);
            }
        });



        // Observe class name
        viewModel.getClassName().observe((LifecycleOwner) rootView.getContext(), name -> {
            if (name != null) classNameText.setText("Class: " + name);
        });
/*
        // Observe teacher name
        viewModel.getTeacherName().observe((LifecycleOwner) rootView.getContext(), name -> {
            if (name != null) teacherNameText.setText("Teacher: " + name);
        });

 */
    }

    @Override
    public void show() {
        cardClassInfo.setVisibility(View.VISIBLE);

        // Trigger loading from ViewModel
        String classId = student.getClassId();
        if (classId == null || classId.isEmpty()) {
            //viewModel.setNotRegistered();
        } else {
            //viewModel.loadClassAndTeacherName(classId);
        }
    }

    @Override
    public void hide() {
        cardClassInfo.setVisibility(View.GONE);
    }
}
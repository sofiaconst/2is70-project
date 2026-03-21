package com.example.eduview.ui.profile.profileFeatures;

import android.view.View;
import android.widget.TextView;

import com.example.eduview.R;
import com.example.eduview.data.model.Student;
import com.example.eduview.ui.profile.ProfileViewModel;
import com.example.eduview.ui.profile.profileStates.StudentProfileState;

public class StudentProfileFeature {

    private final ProfileViewModel viewModel;

    private final TextView classText;
    private final TextView tvClassName;
    private final TextView tvTeacherName;
    private final TextView tvNotRegistered;

    private final View cardClassInfo;
    private final View cardQRCode;
    private final View buttonScanQR;
    private final View tvQRLabel;
    private final View ivQRCode;

    public StudentProfileFeature(View root, ProfileViewModel viewModel) {
        this.viewModel = viewModel;

        classText = root.findViewById(R.id.Teacher_Class_Text);
        tvClassName = root.findViewById(R.id.tvClassName);
        tvTeacherName = root.findViewById(R.id.tvTeacherName);
        tvNotRegistered = root.findViewById(R.id.tvNotRegistered);

        cardClassInfo = root.findViewById(R.id.cardClassInfo);
        cardQRCode = root.findViewById(R.id.cardQRCode);
        buttonScanQR = root.findViewById(R.id.buttonScanQR);
        tvQRLabel = root.findViewById(R.id.tvQRLabel);
        ivQRCode = root.findViewById(R.id.ivQRCode);
    }

    public void bind(StudentProfileState state) {

        if (state == null) return;

        boolean registered = state.isRegistered();

        classText.setText(
                state.isLoading()
                        ? "Loading..."
                        : state.getClassName() != null
                        ? "Class: " + state.getClassName()
                        : "Class: Not Registered"
        );

        tvClassName.setText(
                state.getClassName() != null
                        ? "Class: " + state.getClassName()
                        : "Class: None"
        );

        tvTeacherName.setText(
                state.getTeacherName() != null
                        ? "Teacher: " + state.getTeacherName()
                        : "Teacher: Unknown"
        );

        // Visibility
        classText.setVisibility(View.VISIBLE);
        cardClassInfo.setVisibility(registered ? View.VISIBLE : View.GONE);
        tvNotRegistered.setVisibility(registered ? View.GONE : View.VISIBLE);

        cardQRCode.setVisibility(registered ? View.GONE : View.VISIBLE);
        buttonScanQR.setVisibility(registered ? View.GONE : View.VISIBLE);
        tvQRLabel.setVisibility(registered ? View.GONE : View.VISIBLE);
        ivQRCode.setVisibility(registered ? View.GONE : View.VISIBLE);


    }

    public void handleStudent(Student student) {
        String classId = student.getClassId();

        if (classId == null || classId.isEmpty()) {
            viewModel.setStudentUnregistered();
        } else {
            viewModel.loadStudentProfile(classId);
        }
    }

}
package com.example.eduview.ui.profile;

import android.view.View;
import android.widget.TextView;

import com.example.eduview.R;

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

}
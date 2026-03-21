package com.example.eduview.ui.profile.profileFeatures;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eduview.R;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.ui.profile.ProfileViewModel;
import com.example.eduview.ui.profile.profileStates.TeacherProfileState;
import com.google.android.material.card.MaterialCardView;

public class TeacherProfileFeature {

    private final ProfileViewModel viewModel;

    private final MaterialCardView cardQRCode;
    private final TextView classText;
    private final Button buttonGenerateQR;
    private final ImageView ivQRCode;
    private final TextView tvQRLabel;
    public TeacherProfileFeature(View root, ProfileViewModel viewModel) {
        this.viewModel = viewModel;

        cardQRCode = root.findViewById(R.id.cardQRCode);
        classText = root.findViewById(R.id.Teacher_Class_Text);
        buttonGenerateQR = root.findViewById(R.id.buttonGenerateQR);
        ivQRCode = root.findViewById(R.id.ivQRCode);
        tvQRLabel = root.findViewById(R.id.tvQRLabel);
    }

    public void bind(TeacherProfileState state) {
        if (state == null) return;

        // Always visible for teacher
        classText.setVisibility(View.VISIBLE);
        cardQRCode.setVisibility(View.VISIBLE);
        buttonGenerateQR.setVisibility(View.GONE);
        tvQRLabel.setVisibility(View.VISIBLE);

        // ---- CLASS TEXT ----
        if (state.getErrorMessage() != null) {
            classText.setText("Error: " + state.getErrorMessage());
        } else if (state.getClassName() != null) {
            classText.setText("Class: " + state.getClassName());
        } else {
            classText.setText("Class: None");
        }

        // ---- QR CODE ----
        if (state.getQrBitmap() != null) {
            ivQRCode.setImageBitmap(state.getQrBitmap());
            ivQRCode.setVisibility(View.VISIBLE);
        } else {
            ivQRCode.setVisibility(View.GONE);
        }
    }

    public void handleTeacher(Teacher teacher) {
        String classId = teacher.getClassId();

            viewModel.loadTeacherProfile(classId);

    }

}
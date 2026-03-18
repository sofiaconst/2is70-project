package com.example.eduview.ui.features;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;

import com.example.eduview.R;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.ui.profile.TeacherProfileViewModel;
import com.google.android.material.card.MaterialCardView;

public class TeacherQRCodeFeature implements ProfileFeature {

    private final View rootView;
    private final TeacherProfileViewModel viewModel;
    private final Teacher teacher;

    private MaterialCardView cardQRCode;
    private TextView tvQRLabel;
    private ImageView ivQRCode;
    private Button buttonGenerateQR;

    public TeacherQRCodeFeature(View rootView, TeacherProfileViewModel viewModel, Teacher teacher) {
        this.rootView = rootView;
        this.viewModel = viewModel;
        this.teacher = teacher;

        initViews();
    }

    private void initViews() {
        cardQRCode = rootView.findViewById(R.id.cardQRCode);
        tvQRLabel = rootView.findViewById(R.id.tvQRLabel);
        ivQRCode = rootView.findViewById(R.id.ivQRCode);
        buttonGenerateQR = rootView.findViewById(R.id.buttonGenerateQR);
    }

    @Override
    public void show() {
        cardQRCode.setVisibility(View.VISIBLE);

        // Observe class name (optional: show in label)
        viewModel.getClassName().observe((LifecycleOwner) rootView.getContext(), name -> {
            tvQRLabel.setText("Class: " + name);
        });

        // Load class name for QR generation
        viewModel.loadClassName(teacher.getClassId());

        // Handle QR generation click
        buttonGenerateQR.setOnClickListener(v -> {
            // Assume ViewModel exposes QR bitmap generation
            /*
            viewModel.generateClassQRCode(teacher.getClassId(), bitmap -> {
                ivQRCode.setImageBitmap(bitmap);
            });

             */
        });
    }

    @Override
    public void hide() {
        cardQRCode.setVisibility(View.GONE);
    }
}
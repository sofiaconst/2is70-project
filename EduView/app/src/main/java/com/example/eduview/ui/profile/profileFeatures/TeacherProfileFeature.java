package com.example.eduview.ui.profile.profileFeatures;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eduview.R;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.ui.profile.ProfileUIState;
import com.example.eduview.ui.profile.ProfileViewModel;
import com.example.eduview.ui.profile.profileStates.StudentProfileState;
import com.example.eduview.ui.profile.profileStates.TeacherProfileState;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class TeacherProfileFeature {

    private View root;

    private final MaterialCardView cardQRCode;
    private final TextView classText;
    private final ImageView ivQRCode;
    private final TextView tvQRLabel;
    public TeacherProfileFeature(View root) {
        this.root = root;
        cardQRCode = root.findViewById(R.id.cardQRCode);
        classText = root.findViewById(R.id.Teacher_Class_Text);
        ivQRCode = root.findViewById(R.id.ivQRCode);
        tvQRLabel = root.findViewById(R.id.tvQRLabel);
    }

    public void bind(ProfileUIState uiState) {
/*
        if (uiState == null || uiState.teacherState == null) {
            reset();
            return;
        }

        TeacherProfileState state = uiState.teacherState;

        reset();

        Context context = root.getContext();

        if (state.isLoading()) {
            return;
        }

        // Always visible for teacher
        classText.setVisibility(View.VISIBLE);
        cardQRCode.setVisibility(View.VISIBLE);
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
        }*/
    }

  /*  public void bind(ProfileUIState uiState) {
       if (uiState == null || uiState.studentState == null) {
            reset();
            return;
        }



        reset();

        Context context = root.getContext();

        if (state.isLoading()) {
            return;
        }
        Log.d("TESTER", uiState.displayName + " is registered " + state.isRegistered());

        if (!state.isRegistered()) {
            cardClassInfo.setVisibility(View.GONE);

            cardClassQRCode.setVisibility(View.VISIBLE);
            btnScanQR.setVisibility(View.VISIBLE);
            tvQRLabel.setVisibility(View.VISIBLE);
            tvClassLabel.setVisibility(View.VISIBLE);
            tvClassLabel.setText("Class: Not Registered");

        } else {
            cardClassQRCode.setVisibility(View.GONE);

            cardClassInfo.setVisibility(View.VISIBLE);
            tvClassLabel.setVisibility(View.VISIBLE);
            tvClassName.setText("Class: " + state.getClassName());
            tvTeacherName.setText("Teacher: " + state.getTeacherName());
            tvClassLabel.setText("Class: " + state.getClassName());

        }
        if (state.getErrorMessage() != null) {
            Toast.makeText(context, state.getErrorMessage(), Toast.LENGTH_SHORT).show();
        }
    }*/

    private void reset() {
        /*cardClassQRCode.setVisibility(View.GONE);
        btnScanQR.setVisibility(View.GONE);
        cardClassInfo.setVisibility(View.GONE);
        tvClassLabel.setVisibility(View.GONE);*/
    }



}
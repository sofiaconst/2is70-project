package com.example.eduview.ui.profile.profileFeatures;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eduview.R;
import com.example.eduview.data.model.Student;
import com.example.eduview.ui.profile.ProfileUIState;
import com.example.eduview.ui.profile.ProfileViewModel;
import com.example.eduview.ui.profile.profileStates.StudentProfileState;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

public class StudentProfileFeature {

    private View root;

    private final TextView tvClassLabel;

    // --------------------- QR SECTION --------------------- //

    private final MaterialCardView cardClassQRCode;
    private final TextView tvQRLabel;
    private final MaterialButton btnScanQR;


    // --------------------- STUDENT: CLASS INFO --------------------- //
    private final MaterialCardView cardClassInfo;
    private final TextView tvClassName, tvTeacherName;

    public StudentProfileFeature(View root) {
        this.root = root;
        tvClassLabel = root.findViewById(R.id.Teacher_Class_Text);

        cardClassQRCode = root.findViewById(R.id.mcvClassQRCode);
        btnScanQR = root.findViewById(R.id.buttonScanQR);

        cardClassInfo = root.findViewById(R.id.cardClassInfo);
        tvClassName = root.findViewById(R.id.tvClassName);
        tvTeacherName = root.findViewById(R.id.tvTeacherName);
        tvQRLabel = root.findViewById(R.id.tvQRLabel);
    }
    public void bind(ProfileUIState uiState) {
        if (uiState == null || uiState.studentState == null) {
            reset();
            return;
        }

        StudentProfileState state = uiState.studentState;

        reset();

        Context context = root.getContext();

        if (state.isLoading()) {
            return;
        }
        Log.d("TESTER", uiState.displayName + " is registered " + state.isRegistered());

        tvClassLabel.setVisibility(View.VISIBLE);
        btnScanQR.setVisibility(View.VISIBLE);
        tvQRLabel.setVisibility(View.VISIBLE);

        cardClassInfo.setVisibility(state.isRegistered() ? View.VISIBLE : View.GONE);
        cardClassQRCode.setVisibility(state.isRegistered() ? View.GONE : View.VISIBLE);
        if (!state.isRegistered()) {
            tvClassLabel.setText("Class: Not Registered");

        } else {
            tvClassName.setText("Class: " + state.getClassName());
            tvTeacherName.setText("Teacher: " + state.getTeacherName());
            tvClassLabel.setText("Class: " + state.getClassName());

        }
        if (state.getErrorMessage() != null) {
            Toast.makeText(context, state.getErrorMessage(), Toast.LENGTH_SHORT).show();
        }

        Log.d("TESTER", tvClassLabel.getText().toString());
    }

    private void reset() {
        cardClassQRCode.setVisibility(View.GONE);
        btnScanQR.setVisibility(View.GONE);
        cardClassInfo.setVisibility(View.GONE);
        tvClassLabel.setVisibility(View.GONE);
    }



}
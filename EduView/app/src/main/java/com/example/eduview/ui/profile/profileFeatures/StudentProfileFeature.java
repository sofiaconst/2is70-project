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

/**
 * Handles the student UI elements shown on the profile fragment.
 * Displays information on classroom, QR code scanning, and the class info.
 */
public class StudentProfileFeature {

    private View root;
    private final TextView tvClassLabel;

    // QR Code Section

    private final MaterialCardView cardClassQRCode;
    private final TextView tvQRLabel;
    private final MaterialButton btnScanQR;


    // Student Class Info
    private final MaterialCardView cardClassInfo;
    private final TextView tvClassName, tvTeacherName;

    /**
     * Creates the student profile features and initializes all UI components for the student.
     *
     * @param root root view containing the student profile layout
     */
    public StudentProfileFeature(View root) {
        this.root = root;
        // Class name
        tvClassLabel = root.findViewById(R.id.Teacher_Class_Text);

        // QR Code scanning
        cardClassQRCode = root.findViewById(R.id.mcvClassQRCode);
        btnScanQR = root.findViewById(R.id.buttonScanQR);

        // Class information
        cardClassInfo = root.findViewById(R.id.cardClassInfo);
        tvClassName = root.findViewById(R.id.tvClassName);
        tvTeacherName = root.findViewById(R.id.tvTeacherName);
        tvQRLabel = root.findViewById(R.id.tvQRLabel);
    }

    /**
     * Updates the student profile UI based on the provided profile.
     *
     * @param uiState complete profile UI state containing the student state
     */
    public void bind(ProfileUIState uiState) {
        // Validating whether UI state is null
        if (uiState == null || uiState.studentState == null) {
            reset();
            return;
        }

        // Extract student state and context for UI operations.
        StudentProfileState state = uiState.studentState;

        // Reset UI to a clean baseline before applying new state.
        reset();

        Context context = root.getContext();
        // Loading State
        if (state.isLoading()) {
            return;
        }
        Log.d("TESTER", uiState.displayName + " is registered " + state.isRegistered());

        // Show Base UI before specific states
        tvClassLabel.setVisibility(View.VISIBLE);
        btnScanQR.setVisibility(View.VISIBLE);
        tvQRLabel.setVisibility(View.VISIBLE);

        // Checks if the student is registered
        cardClassInfo.setVisibility(state.isRegistered() ? View.VISIBLE : View.GONE);
        cardClassQRCode.setVisibility(state.isRegistered() ? View.GONE : View.VISIBLE);
        if (!state.isRegistered()) {
            tvClassLabel.setText("Class: Not Registered");

        } else {
            tvClassName.setText("Class: " + state.getClassName());
            tvTeacherName.setText("Teacher: " + state.getTeacherName());
            tvClassLabel.setText("Class: " + state.getClassName());

        }
        // Error State
        if (state.getErrorMessage() != null) {
            Toast.makeText(context, state.getErrorMessage(), Toast.LENGTH_SHORT).show();
        }

        Log.d("TESTER", tvClassLabel.getText().toString());
    }

    /**
     * Hides all student profile sections before showing a new state.
     */
    private void reset() {
        cardClassQRCode.setVisibility(View.GONE);
        btnScanQR.setVisibility(View.GONE);
        cardClassInfo.setVisibility(View.GONE);
        tvClassLabel.setVisibility(View.GONE);
    }



}
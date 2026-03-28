package com.example.eduview.ui.profile.profileFeatures;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduview.R;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.ui.adapters.StudentManagerAdapter;
import com.example.eduview.ui.profile.ProfileUIState;
import com.example.eduview.ui.profile.ProfileViewModel;
import com.example.eduview.ui.profile.profileStates.StudentProfileState;
import com.example.eduview.ui.profile.profileStates.TeacherProfileState;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * Handles the teacher UI elements shown on the profile fragment.
 * Displays information on classroom, QR code data, and the student list where they can be managed.
 */
public class TeacherProfileFeature {

    private View root;

    private final TextView tvClassLabel;

    // QR Section

    private final MaterialCardView cardClassQRCode;
    private final MaterialCardView cardQRCode;
    private final ImageView ivQRCode;
    private final TextView tvQRLabel, tvQRSubtext;

    // Manage Students
    private RecyclerView rvStudents;
    private StudentManagerAdapter adapter;
    private MaterialCardView cardManageStudents;

    /**
     * Creates the teacher profile features and initializes all UI components for the teacher.
     *
     * @param root root view containing the teacher profile layout
     * @param adapter adapter used to display the list of students
     */
    public TeacherProfileFeature(View root, StudentManagerAdapter adapter) {
        this.root = root;
        tvClassLabel = root.findViewById(R.id.Teacher_Class_Text);
        cardClassQRCode = root.findViewById(R.id.mcvClassQRCode);
        tvQRLabel = root.findViewById(R.id.tvQRLabel);
        tvQRSubtext = root.findViewById(R.id.tvQRSubtext);
        cardQRCode = root.findViewById(R.id.cardQRCode);
        ivQRCode = root.findViewById(R.id.ivQRCode);

        rvStudents = root.findViewById(R.id.rvStudents);
        cardManageStudents = root.findViewById(R.id.manageStudentsCard);

        this.adapter = adapter;

        rvStudents.setLayoutManager(new LinearLayoutManager(root.getContext()));
        rvStudents.setAdapter(adapter);
    }

    /**
     * Updates the teacher profile UI based on the provided profile.
     *
     * @param uiState complete profile UI state containing the teacher state
     */
    public void bind(ProfileUIState uiState) {
        if (uiState == null || uiState.teacherState == null) {
            reset();
            return;
        }

        // Extract teacher state and context for UI operations.
        TeacherProfileState state = uiState.teacherState;
        Context context = root.getContext();

        // Reset UI to a clean baseline before applying new state.
        reset();

        // Show Base UI before specific states
        tvClassLabel.setVisibility(View.VISIBLE);
        cardClassQRCode.setVisibility(View.VISIBLE);
        tvQRLabel.setVisibility(View.VISIBLE);
        tvQRSubtext.setVisibility(View.VISIBLE);
        cardManageStudents.setVisibility(View.VISIBLE);

        // Loading State

        // While data is being fetched, show placeholder text and hide QR code.
        if (state.isLoading()) {
            tvClassLabel.setText("Class: Loading...");
            ivQRCode.setVisibility(View.GONE);
            return;
        }

        // Error State
        if (state.getErrorMessage() != null) {
            tvClassLabel.setText("Error: " + state.getErrorMessage());
            ivQRCode.setVisibility(View.GONE);
            // Show a short message to inform the user
            Toast.makeText(context, state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Success State

        // Display class name if available, otherwise show fallback text
        if (state.getClassName() != null) {
            tvClassLabel.setText("Class: " + state.getClassName());
        } else {
            tvClassLabel.setText("Class: None");
        }

        // If QR code exists, display it; otherwise hide the image view
        if (state.getQrCode() != null) {
            cardQRCode.setVisibility(View.VISIBLE);
            ivQRCode.setVisibility(View.VISIBLE);
            ivQRCode.setImageBitmap(state.getQrCode());
        } else {
            // Still show card container but hide QR image itself
            cardQRCode.setVisibility(View.VISIBLE);
            ivQRCode.setVisibility(View.GONE);
        }

        // Student Management

        // Show list of students if available, otherwise hide the RecyclerView
        if (state.getStudents() != null && !state.getStudents().isEmpty()) {
            rvStudents.setVisibility(View.VISIBLE);
            // Update adapter with latest student list
            adapter.submitList(state.getStudents());
        } else {
            rvStudents.setVisibility(View.GONE);
        }
    }

    /**
     * Hides all teacher profile sections before showing a new state.
     */
    private void reset() {
        cardClassQRCode.setVisibility(View.GONE);
        tvClassLabel.setVisibility(View.GONE);
        cardManageStudents.setVisibility(View.GONE);
    }


}
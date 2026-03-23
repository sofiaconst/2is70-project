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

public class TeacherProfileFeature {

    private View root;

    private final TextView tvClassLabel;

    // --------------------- QR SECTION --------------------- //

    private final MaterialCardView cardClassQRCode;
    private final MaterialCardView cardQRCode;
    private final ImageView ivQRCode;
    private final TextView tvQRLabel, tvQRSubtext;

    //--------------------- MANAGE STUDENTS -----------//
    private RecyclerView rvStudents;
    private StudentManagerAdapter adapter;
    private MaterialCardView cardManageStudents;


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

    public void bind(ProfileUIState uiState) {
        if (uiState == null || uiState.teacherState == null) {
            reset();
            return;
        }

        TeacherProfileState state = uiState.teacherState;
        Context context = root.getContext();

        reset();

        // ---- SHOW BASE UI ----
        tvClassLabel.setVisibility(View.VISIBLE);
        cardClassQRCode.setVisibility(View.VISIBLE);
        tvQRLabel.setVisibility(View.VISIBLE);
        tvQRSubtext.setVisibility(View.VISIBLE);
        cardManageStudents.setVisibility(View.VISIBLE);

        // ---- LOADING STATE ----
        if (state.isLoading()) {
            tvClassLabel.setText("Class: Loading...");
            ivQRCode.setVisibility(View.GONE);
            return;
        }

        // ---- ERROR STATE ----
        if (state.getErrorMessage() != null) {
            tvClassLabel.setText("Error: " + state.getErrorMessage());
            ivQRCode.setVisibility(View.GONE);

            Toast.makeText(context, state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // ---- SUCCESS STATE ----

        // Class name
        if (state.getClassName() != null) {
            tvClassLabel.setText("Class: " + state.getClassName());
        } else {
            tvClassLabel.setText("Class: None");
        }

        // QR Code
        if (state.getQrCode() != null) {
            cardQRCode.setVisibility(View.VISIBLE);
            ivQRCode.setVisibility(View.VISIBLE);
            ivQRCode.setImageBitmap(state.getQrCode());
        } else {
            cardQRCode.setVisibility(View.VISIBLE);
            ivQRCode.setVisibility(View.GONE);
        }

        // ---- STUDENT MANAGEMENT ----//

        if (state.getStudents() != null && !state.getStudents().isEmpty()) {
            rvStudents.setVisibility(View.VISIBLE);
            adapter.submitList(state.getStudents());
        } else {
            rvStudents.setVisibility(View.GONE);
            Log.d("TESTER", "nobody here buddy");
            //TODO: Signal that there are currently no students
        }
    }

    private void reset() {
        cardClassQRCode.setVisibility(View.GONE);
        tvClassLabel.setVisibility(View.GONE);
        cardManageStudents.setVisibility(View.GONE);
    }


}
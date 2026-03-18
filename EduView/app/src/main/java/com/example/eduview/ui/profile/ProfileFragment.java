package com.example.eduview.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduview.R;
import com.example.eduview.ui.login.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;

    private ShapeableImageView profileImage;
    private TextView tvEditPfp;
    private TextView userNameText;
    private TextView roleText;
    private TextView classText;
    private EditText aboutMeEditText;
    private MaterialButton logoutButton;

    private TextView tvQRLabel;
    private ImageView ivQRCode;
    private MaterialButton buttonScanQR;

    private MaterialCardView manageStudentsCard;
    private RecyclerView rvStudents;
    private ProgressBar progressStudents;
    private StudentManagerAdapter studentManagerAdapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(root);
        setupViewModel();
        setupStudentList();
        setupListeners();
        observeState();

        return root;
    }

    private void initViews(View root) {
        profileImage = root.findViewById(R.id.profileImage);
        tvEditPfp = root.findViewById(R.id.tvEditPfp);
        userNameText = root.findViewById(R.id.User_name_text);
        roleText = root.findViewById(R.id.textViewRole);
        classText = root.findViewById(R.id.Teacher_Class_Text);
        aboutMeEditText = root.findViewById(R.id.etAboutMe);

        logoutButton = root.findViewById(R.id.buttonLogout);

        tvQRLabel = root.findViewById(R.id.tvQRLabel);
        ivQRCode = root.findViewById(R.id.ivQRCode);
        buttonScanQR = root.findViewById(R.id.buttonScanQR);

        manageStudentsCard = root.findViewById(R.id.manageStudentsCard);
        rvStudents = root.findViewById(R.id.rvStudents);
        progressStudents = root.findViewById(R.id.progressStudents);
    }

    private void setupViewModel() {
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
    }

    private void setupStudentList() {
        studentManagerAdapter = new StudentManagerAdapter(student ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Remove student")
                        .setMessage("Remove " + student.getFirstName() + " " + student.getLastName() + " from this class?")
                        .setPositiveButton("Remove", (dialog, which) ->
                                profileViewModel.removeStudentFromClass(student))
                        .setNegativeButton("Cancel", null)
                        .show()
        );

        rvStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStudents.setAdapter(studentManagerAdapter);
    }

    private void setupListeners() {
        logoutButton.setOnClickListener(v -> {
            profileViewModel.logout();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        tvEditPfp.setOnClickListener(v -> showPfpSelectionDialog());

        // buttonScanQR.setOnClickListener(v -> profileViewModel.startQRScan());
    }

    private void showPfpSelectionDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pfp_selection, null);
        RecyclerView rvPfps = dialogView.findViewById(R.id.rvPfpSelection);
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Choose Profile Picture")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .create();

        PfpAdapter adapter = new PfpAdapter(pfp -> {
            profileViewModel.updateProfilePicture(pfp);
            dialog.dismiss();
        });

        rvPfps.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rvPfps.setAdapter(adapter);
        
        dialog.show();
    }

    private void observeState() {
        profileViewModel.getUIState().observe(getViewLifecycleOwner(), this::render);

        profileViewModel.getClassroomStudents().observe(getViewLifecycleOwner(), students -> {
            studentManagerAdapter.submitList(students);
        });

        profileViewModel.getStudentsLoading().observe(getViewLifecycleOwner(), loading -> {
            progressStudents.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        profileViewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void render(ProfileUIState state) {
        userNameText.setText(state.displayName);
        roleText.setText(state.roleText);
        classText.setText(state.classText);
        profileImage.setImageResource(state.profilePictureResId);

        buttonScanQR.setVisibility(state.showScanButton ? View.VISIBLE : View.GONE);

        if (state.qrBitmap != null) {
            tvQRLabel.setVisibility(View.VISIBLE);
            ivQRCode.setVisibility(View.VISIBLE);
            ivQRCode.setImageBitmap(state.qrBitmap);
        } else {
            tvQRLabel.setVisibility(View.GONE);
            ivQRCode.setVisibility(View.GONE);
        }

        boolean isTeacher = "TEACHER".equalsIgnoreCase(state.roleText);
        manageStudentsCard.setVisibility(isTeacher ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        profileViewModel.loadTeacherStudents();
    }
}
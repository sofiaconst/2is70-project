package com.example.eduview.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduview.R;
import com.example.eduview.ui.adapters.ChildAdapter;
import com.example.eduview.ui.adapters.PfpAdapter;
import com.example.eduview.ui.adapters.StudentManagerAdapter;
import com.example.eduview.ui.login.LoginActivity;
import com.example.eduview.ui.profile.profileFeatures.ParentProfileFeature;
import com.example.eduview.ui.profile.profileFeatures.StudentProfileFeature;
import com.example.eduview.ui.profile.profileFeatures.TeacherProfileFeature;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class ProfileFragment extends Fragment {

    // --------------------- VIEWMODEL --------------------- //
    private ProfileViewModel profileVM;

    // --------------------- BASE UI --------------------- //
    private ImageView ivPfp;
    private TextView tvEditPfp;
    private TextView tvFullName, tvUserRole, tvClassLabel;
    private MaterialButton btnLogout;

    // --------------------- QR SECTION --------------------- //
    private MaterialCardView cardQRCode;
    private TextView tvQRCodeLabel;
    private ImageView ivQRCode;
    private MaterialButton btnScanQR;


    // --------------------- STUDENT: CLASS INFO --------------------- //
    private MaterialCardView cardClassInfo;
    private TextView tvClassName, tvTeacherName, tvNotRegistered;

    // --------------------- PARENT: MY CHILDREN --------------------- //
    private MaterialCardView cardMyChildren;
    private TextView tvNoChildren;
    private RecyclerView rvChildren;
    private ChildAdapter childAdapter;
    private View btnAddChild;

    // --------------------- TEACHER: MANGAE STUDENTS --------------------- //
    private MaterialCardView cardManageStudents;
    private ProgressBar pbStudents;
    private RecyclerView rvStudents;
    private StudentManagerAdapter studentManagerAdapter;

    // --------------------- FEATURES --------------------- //
    private StudentProfileFeature studentFeature;
    private TeacherProfileFeature teacherFeature;
    private ParentProfileFeature parentFeature;

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        initBaseViews(root);
        initClassQRCodeViews(root);
        initClassInfoViews(root);
        initMyChildrenViews(root);
        initManageStudentsViews(root);

        return root;
    }

    private void initBaseViews(View root) {
        tvFullName = root.findViewById(R.id.User_name_text);
        tvUserRole = root.findViewById(R.id.textViewRole);
        tvClassLabel = root.findViewById(R.id.Teacher_Class_Text);
        ivPfp = root.findViewById(R.id.profileImage);
        tvEditPfp = root.findViewById(R.id.tvEditPfp);
        btnLogout = root.findViewById(R.id.buttonLogout);
    }

    private void initClassQRCodeViews(View root) {
        tvQRCodeLabel = root.findViewById(R.id.tvQRLabel);
        cardQRCode = root.findViewById(R.id.cardQRCode);
        ivQRCode = root.findViewById(R.id.ivQRCode);
        btnScanQR = root.findViewById(R.id.buttonScanQR);
    }

    private void initClassInfoViews(View root) {
        cardClassInfo = root.findViewById(R.id.cardClassInfo);
        tvClassName = root.findViewById(R.id.tvClassName);
        tvTeacherName = root.findViewById(R.id.tvTeacherName);
    }

    private void initMyChildrenViews(View root) {
        cardMyChildren = root.findViewById(R.id.cardMyChildren);
        rvChildren = root.findViewById(R.id.rvChildren);
        tvNoChildren = root.findViewById(R.id.tvNoChildren);
        btnAddChild = root.findViewById(R.id.btnAddChild);
    }

    private void initManageStudentsViews(View root) {
        cardManageStudents = root.findViewById(R.id.manageStudentsCard);
        pbStudents = root.findViewById(R.id.progressStudents);
        rvStudents = root.findViewById(R.id.rvStudents);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileVM = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupRecyclerViews();
        setupListeners();

        studentFeature = new StudentProfileFeature(getView());
        teacherFeature = new TeacherProfileFeature(getView(), studentManagerAdapter);
        parentFeature = new ParentProfileFeature(getView(), childAdapter);

        profileVM.getUIState().observe(getViewLifecycleOwner(), this::render);
    }

    private void setupRecyclerViews() {
        studentManagerAdapter = new StudentManagerAdapter(student -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Remove student")
                    .setMessage("Remove " + student.getFirstName() + "?")
                    .setPositiveButton("Remove", (d, w) ->
                            profileVM.removeStudentFromClass(student))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        rvStudents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvStudents.setAdapter(studentManagerAdapter);

        childAdapter = new ChildAdapter();
        rvChildren.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChildren.setAdapter(childAdapter);

    }

    private void render(ProfileUIState state) {
        if (state == null) return;

        tvFullName.setText(state.displayName);
        tvUserRole.setText(state.roleText);
        ivPfp.setImageResource(state.profilePictureResId);

        resetVisibility();


        if (state.studentState != null) {
            studentFeature.bind(state);
        }

        if (state.teacherState != null) {
            teacherFeature.bind(state);
        }

        if (state.parentState != null) {
            parentFeature.bind(state);
        }
    }

    private void resetVisibility() {
        cardQRCode.setVisibility(View.GONE);
        cardMyChildren.setVisibility(View.GONE);
        tvClassLabel.setVisibility(View.GONE);
        cardClassInfo.setVisibility(View.GONE);
    }

    private void setupListeners() {
        tvEditPfp.setOnClickListener(v -> showPfpSelectionDialog());

        btnLogout.setOnClickListener(v -> {
            profileVM.logout();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        btnScanQR.setOnClickListener(v -> startScanner());

        btnAddChild.setOnClickListener(v -> showAddChildDialog());

    }

    private void showPfpSelectionDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_pfp_selection, null);

        RecyclerView rvPfps = dialogView.findViewById(R.id.rvPfpSelection);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Choose Profile Picture")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .create();

        PfpAdapter adapter = new PfpAdapter(pfp -> {
            profileVM.updateProfilePicture(pfp);
            dialog.dismiss();
        });

        rvPfps.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rvPfps.setAdapter(adapter);

        dialog.show();
    }

    private void startScanner() {
        ScanOptions options = new ScanOptions();
        options.setCaptureActivity(CustomScannerActivity.class);
        options.setPrompt("Scan the Classroom QR Code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        qrCodeLauncher.launch(options);
    }

    private final ActivityResultLauncher<ScanOptions> qrCodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String scannedCode = result.getContents();
                    Log.d("ProfileFragment", "Scanned QR code = " + scannedCode);
                    profileVM.joinClass(scannedCode);
                }
            }
    );

    private void showAddChildDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_child, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.TransparentDialog)
                .setView(dialogView)
                .create();

        EditText etFirstName = dialogView.findViewById(R.id.etChildFirstName);
        EditText etLastName = dialogView.findViewById(R.id.etChildLastName);
        EditText etUsername = dialogView.findViewById(R.id.etChildUsername);
        EditText etPassword = dialogView.findViewById(R.id.etParentPassword);
        
        TextInputLayout tilFirstName = (TextInputLayout) etFirstName.getParent().getParent();
        TextInputLayout tilLastName = (TextInputLayout) etLastName.getParent().getParent();
        TextInputLayout tilUsername = (TextInputLayout) etUsername.getParent().getParent();
        TextInputLayout tilPassword = (TextInputLayout) etPassword.getParent().getParent();

        Button btnAdd = dialogView.findViewById(R.id.btnAdd);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            boolean hasError = false;

            // Reset backgrounds
            etFirstName.setBackgroundResource(R.drawable.bg_input_rounded);
            etLastName.setBackgroundResource(R.drawable.bg_input_rounded);
            etUsername.setBackgroundResource(R.drawable.bg_input_rounded);
            etPassword.setBackgroundResource(R.drawable.bg_input_rounded);

            if (firstName.isEmpty()) {
                etFirstName.setBackgroundResource(R.drawable.bg_input_error);
                hasError = true;
            }
            if (lastName.isEmpty()) {
                etLastName.setBackgroundResource(R.drawable.bg_input_error);
                hasError = true;
            }
            if (username.isEmpty()) {
                etUsername.setBackgroundResource(R.drawable.bg_input_error);
                hasError = true;
            } else if (username.contains("@") || username.contains(" ")) {
                etUsername.setBackgroundResource(R.drawable.bg_input_error);
                Toast.makeText(getContext(), "Username must not contain space characters nor @ symbols", Toast.LENGTH_SHORT).show();
                hasError = true;
            }
            if (password.isEmpty()) {
                etPassword.setBackgroundResource(R.drawable.bg_input_error);
                hasError = true;
            }

            if (hasError) {
                return;
            }

            profileVM.addChild(firstName, lastName, username, password);
        });


        // Observe status
        profileVM.getAddChildStatus().observe(getViewLifecycleOwner(), status -> {
            if (status == null) return;

            if (status.equals("LOADING")) {
                btnAdd.setEnabled(false);
                btnAdd.setText("Adding...");
            } else if (status.equals("SUCCESS")) {
                Toast.makeText(getContext(), "Child added successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else if (status.startsWith("ERROR")) {
                btnAdd.setEnabled(true);
                btnAdd.setText("Add");
                if (status.contains("Username already taken")) {
                    etUsername.setBackgroundResource(R.drawable.bg_input_error);
                }
                Toast.makeText(getContext(), status, Toast.LENGTH_LONG).show();
            } else {
                // validation message
                Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}

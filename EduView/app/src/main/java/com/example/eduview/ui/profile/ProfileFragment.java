package com.example.eduview.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduview.R;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.ui.adapters.ChildAdapter;
import com.example.eduview.ui.login.LoginActivity;
import com.example.eduview.ui.profile.profileFeatures.StudentProfileFeature;
import com.example.eduview.ui.profile.profileFeatures.TeacherProfileFeature;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileVM;
    private ShapeableImageView profileImage;
    private TextView userNameText, roleText, classText;
    private EditText aboutMeEditText;
    private Button btnSaveBio, logoutButton;

    // Teacher QR Section
    private MaterialCardView cardQRCode;
    private TextView tvQRLabel;
    private ImageView ivQRCode;
    private Button buttonGenerateQR;

    // Parent Children Section
    private MaterialCardView cardMyChildren;
    private RecyclerView rvChildren;
    private TextView tvNoChildren;
    private ChildAdapter childAdapter;
    private Button buttonScanQR; // Used by Student to scan class QR
    private View btnAddChild; // Used by Parent to open dialog

    // Student Class Info
    private MaterialCardView cardClassInfo;
    private StudentProfileFeature studentFeature;
    private TeacherProfileFeature teacherFeature;

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(root);
        return root;
    }

    // Views initializer
    private void initViews(View root) {
        initBaseViews(root);
        initTeacherViews(root);
        initParentViews(root);
        initStudentViews(root);
    }

    private void initBaseViews(View root) {
        profileImage = root.findViewById(R.id.profileImage);
        userNameText = root.findViewById(R.id.User_name_text);
        roleText = root.findViewById(R.id.materialCardView).findViewById(R.id.textViewRole);
        classText = root.findViewById(R.id.Teacher_Class_Text);
        aboutMeEditText = root.findViewById(R.id.etAboutMe);
        btnSaveBio = root.findViewById(R.id.btnSaveBio);
        logoutButton = root.findViewById(R.id.buttonLogout);
    }

    private void initStudentViews(View root) {
        // Student class info
        cardClassInfo = root.findViewById(R.id.cardClassInfo);
        buttonScanQR = root.findViewById(R.id.buttonScanQR);
    }

    private void initParentViews(View root) {
        cardMyChildren = root.findViewById(R.id.cardMyChildren);
        rvChildren = root.findViewById(R.id.rvChildren);
        tvNoChildren = root.findViewById(R.id.tvNoChildren);
        btnAddChild = root.findViewById(R.id.btnAddChild);
    }

    private void initTeacherViews(View root) {
        cardQRCode = root.findViewById(R.id.cardQRCode);
        tvQRLabel = root.findViewById(R.id.tvQRLabel);
        ivQRCode = root.findViewById(R.id.ivQRCode);
        buttonGenerateQR = root.findViewById(R.id.buttonGenerateQR);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileVM = new ViewModelProvider(this).get(ProfileViewModel.class);

        studentFeature = new StudentProfileFeature(getView(), profileVM);
        teacherFeature = new TeacherProfileFeature(getView(), profileVM);

        profileVM.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                Log.d("TESTER", user.getFirstName());
                updateUI(user);
                setupRoleBasedObservers(user);
                setupRoleBasedListeners(user);
            } else {
                Log.e("ProfileFragment", "Current user is null");
            }
        });
    }

    private void updateUI(User user) {
        updateBaseUI(user);
        resetVisibility();

        switch (user.getRole()) {
            case TEACHER:
                teacherFeature.handleTeacher((Teacher) user);
                break;

            case STUDENT:
                studentFeature.handleStudent((Student) user);
                break;

            case PARENT:
                setupRecyclerView();
                updateParentUI((Parent) user);

                break;
        }
    }

    private void updateBaseUI(User user) {
        userNameText.setText(user.getFirstName() + " " + user.getLastName());
        roleText.setText(user.getRole().name());
        aboutMeEditText.setText(user.getBio());
    }

    private void setupRoleBasedListeners(User user) {
        logoutButton.setOnClickListener(v -> {
            profileVM.logout();
            Log.d("ProfileFragment", "Navigating to LoginActivity");
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        switch (user.getRole()) {
            case PARENT:
                if (btnAddChild != null) {
                    btnAddChild.setOnClickListener(v -> showAddChildDialog());
                }
                break;

            case STUDENT:
                buttonScanQR.setOnClickListener(v -> startScanner());
                break;

            case TEACHER:
                buttonGenerateQR.setOnClickListener(v -> {
                    if (user instanceof Teacher) {
                        profileVM.generateTeacherQR(((Teacher) user).getClassId());
                    }
                });
                break;
        }
    }

    private void setupRoleBasedObservers(User user) {
        switch (user.getRole()) {
            case PARENT:
                profileVM.getChildrenData().observe(getViewLifecycleOwner(), children -> {
                    if (children != null) {
                        childAdapter.setChildren(children);
                        tvNoChildren.setVisibility(children.isEmpty() ? View.VISIBLE : View.GONE);
                        rvChildren.setVisibility(children.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                });
                break;

            case STUDENT:
                profileVM.getJoinStatus().observe(getViewLifecycleOwner(), status -> {
                    if (status != null) {
                        Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
                    }
                });

                profileVM.getStudentState().observe(getViewLifecycleOwner(), state -> {
                    studentFeature.bind(state);

                    if (state != null && state.getErrorMessage() != null) {
                        Toast.makeText(getContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                break;

            case TEACHER:
                profileVM.getTeacherState().observe(getViewLifecycleOwner(), state -> {
                    teacherFeature.bind(state);

                    if (state != null && state.getErrorMessage() != null) {
                        Toast.makeText(getContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }

    }

    private void showAddChildDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_child, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.TransparentDialog)
                .setView(dialogView)
                .create();

        EditText etFirstName = dialogView.findViewById(R.id.etChildFirstName);
        EditText etLastName = dialogView.findViewById(R.id.etChildLastName);
        EditText etEmail = dialogView.findViewById(R.id.etChildEmail);
        EditText etPassword = dialogView.findViewById(R.id.etParentPassword);
        Button btnAdd = dialogView.findViewById(R.id.btnAdd);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        btnAdd.setOnClickListener(v -> {
            User currentUser = profileVM.getCurrentUser().getValue();
            if (!(currentUser instanceof Parent)) return;

            profileVM.addChild(
                    currentUser.getUserId(),
                    etFirstName.getText().toString().trim(),
                    etLastName.getText().toString().trim(),
                    etEmail.getText().toString().trim(),
                    etPassword.getText().toString().trim()
            );
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

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
                Toast.makeText(getContext(), status, Toast.LENGTH_LONG).show();
            } else {
                // validation message
                Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        childAdapter = new ChildAdapter();
        rvChildren.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChildren.setAdapter(childAdapter);
    }

    private void updateParentUI(Parent parent) {

        profileVM.loadChildrenData(parent);
        cardMyChildren.setVisibility(View.VISIBLE);
        classText.setVisibility(View.VISIBLE);
        classText.setText("");

    }

    private void resetVisibility() {
        cardQRCode.setVisibility(View.GONE);
        cardMyChildren.setVisibility(View.GONE);
        classText.setVisibility(View.GONE);
        cardClassInfo.setVisibility(View.GONE);
    }

    private void startScanner() {
        ScanOptions options = new ScanOptions();
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

                    // NEW: delegate to ProfileViewModel instead of MainViewModel
                    profileVM.joinClass(scannedCode);
                }
            }
    );
}
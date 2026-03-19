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

import com.example.eduview.AuthRepository;
import com.example.eduview.R;
import com.example.eduview.data.model.Classroom;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.model.UserRole;
import com.example.eduview.data.repository.ClassroomRepository;
import com.example.eduview.ui.adapters.ChildAdapter;
import com.example.eduview.ui.login.LoginActivity;
import com.example.eduview.ui.main.MainViewModel;
import com.example.eduview.ui.profile.profileStates.StudentProfileState;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Collections;
import java.util.List;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileVM;

    private ShapeableImageView profileImage;
    private TextView userNameText,roleText,classText;
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
    private TextView tvTeacherName,tvClassName,tvNotRegistered,tvQRSubtext;


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

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(root);
        setupRecyclerView();
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
        tvTeacherName = root.findViewById(R.id.tvTeacherName);
        tvClassName = root.findViewById(R.id.tvClassName);
        tvNotRegistered = root.findViewById(R.id.tvNotRegistered);
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
        tvQRSubtext = root.findViewById(R.id.tvQRSubtext);
    }

    private void setupRecyclerView() {
        childAdapter = new ChildAdapter(new ClassroomRepository());
        rvChildren.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChildren.setAdapter(childAdapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViewModel();
        setupObservers();
        setupClickListeners();
    }

    // Setup
    private void setupViewModel() {
        profileVM = new ViewModelProvider(this).get(ProfileViewModel.class);
    }
    private void setupObservers() {
        profileVM.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateUI(user);
            } else {
                Log.e("ProfileFragment", "Current user is null");
            }
        });

        profileVM.getChildrenData().observe(getViewLifecycleOwner(), children -> {
            if (children != null) {
                childAdapter.setChildren(children);
                tvNoChildren.setVisibility(children.isEmpty() ? View.VISIBLE : View.GONE);
                rvChildren.setVisibility(children.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        // Observe join status for toast
        profileVM.getJoinStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
            }
        });

        profileVM.getStudentState().observe(getViewLifecycleOwner(), state -> {
            // Bind the UI in one place
            bindStudentProfileUI(state);

            // Optional: show toast if there is an error
            if (state != null && state.getErrorMessage() != null) {
                Toast.makeText(getContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupClickListeners() {
        logoutButton.setOnClickListener(v -> {
            profileVM.logout();
            Log.d("ProfileFragment", "Navigating to LoginActivity");
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        buttonScanQR.setOnClickListener(v -> startScanner());

        buttonGenerateQR.setOnClickListener(v -> {
            User user = profileVM.getCurrentUser().getValue();
            if (user instanceof Teacher) {
                String classId = ((Teacher) user).getClassId();

                Bitmap qrBitmap = generateQRCode(classId);
                if (qrBitmap != null) {
                    ivQRCode.setImageBitmap(qrBitmap);
                    tvQRLabel.setVisibility(View.VISIBLE);
                    ivQRCode.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "Could not generate QR code.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (btnAddChild != null) {
            btnAddChild.setOnClickListener(v -> showAddChildDialog());
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
            String fName = etFirstName.getText().toString().trim();
            String lName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthRepository authRepo = new AuthRepository();
            AuthRepository.ChildInfo childInfo = new AuthRepository.ChildInfo(fName, lName, email);

            User currentUser = profileVM.getCurrentUser().getValue();
            if (!(currentUser instanceof Parent)) return;

            btnAdd.setEnabled(false);
            btnAdd.setText("Adding...");

            authRepo.addChildToParent(
                    currentUser.getUserId(),
                    childInfo,
                    password,
                    new AuthRepository.AuthCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Child added successfully!", Toast.LENGTH_SHORT).show();
                            profileVM.loadCurrentUser(); // Refresh list
                            dialog.dismiss();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            btnAdd.setEnabled(true);
                            btnAdd.setText("Add");
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
            );
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // Update UI
    private void updateUI(User user) {
        updateBaseUI(user);
        resetVisibility();

        switch (user.getRole()) {
            case TEACHER:
                updateTeacherUI((Teacher) user);
                break;

            case STUDENT:
                updateStudentUI((Student) user);
                break;

            case PARENT:
                updateParentUI((Parent) user);
                break;
        }
    }
    private void updateBaseUI(User user) {
        userNameText.setText(user.getFirstName() + " " + user.getLastName());
        roleText.setText(user.getRole().name());
        aboutMeEditText.setText(user.getBio());
    }
    private void updateStudentUI(Student student) {
//        // Make sure base views are visible
//        cardQRCode.setVisibility(View.VISIBLE);
        classText.setVisibility(View.VISIBLE);
//        buttonScanQR.setVisibility(View.VISIBLE);
//        buttonGenerateQR.setVisibility(View.GONE);

        String classId = student.getClassId();

        if (classId == null || classId.isEmpty()) {
            // Ask ViewModel to emit unregistered state
            profileVM.setStudentUnregistered();
        } else {
            // Trigger loading and fetching class + teacher info
            profileVM.loadStudentProfile(classId);
        }
    }
    private void updateTeacherUI(Teacher teacher) {
        cardQRCode.setVisibility(View.VISIBLE);
        classText.setVisibility(View.VISIBLE);
        buttonGenerateQR.setVisibility(View.VISIBLE);
        buttonScanQR.setVisibility(View.GONE);
        tvQRLabel.setVisibility(View.VISIBLE);

        String classId = teacher.getClassId();
        if (classId == null || classId.isEmpty()) {
            classText.setText("Class: None");
        }
    }
    private void updateParentUI(Parent parent) {
        profileVM.loadChildrenData(parent);
        cardMyChildren.setVisibility(View.VISIBLE);
        classText.setVisibility(View.VISIBLE);
        classText.setText("Parent Profile");
    }

    private void bindStudentProfileUI(StudentProfileState state) {
        if (state == null) return;

        // Text updates
        classText.setText(state.isLoading() ? "Loading class info..." :
                state.getClassName() != null ? ("Class: " + state.getClassName()) : "Class: Not Registered");
        tvClassName.setText(state.isLoading() ? "Loading class info..." :
                state.getClassName() != null ? ("Class: " + state.getClassName()) : "Class: None");
        tvTeacherName.setText(state.isLoading() ? "" :
                state.getTeacherName() != null ? ("Teacher: " + state.getTeacherName()) : "Teacher: Unknown");

        boolean registered = state.isRegistered();

        cardClassInfo.setVisibility(registered ? View.VISIBLE : View.GONE);
        tvNotRegistered.setVisibility(registered ? View.GONE : View.VISIBLE);

        cardQRCode.setVisibility(registered ? View.GONE : View.VISIBLE);
        ivQRCode.setVisibility(cardQRCode.getVisibility());
        tvQRLabel.setVisibility(cardQRCode.getVisibility());
        tvQRSubtext.setVisibility(View.GONE);
        buttonScanQR.setVisibility(cardQRCode.getVisibility());
    }

    private void resetVisibility() {
        cardQRCode.setVisibility(View.GONE);
        cardMyChildren.setVisibility(View.GONE);
        classText.setVisibility(View.GONE);
        cardClassInfo.setVisibility(View.GONE);
    }

    private Bitmap generateQRCode(String classCode) {
        if (classCode == null || classCode.trim().isEmpty()) {
            return null;
        }

        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    classCode,
                    BarcodeFormat.QR_CODE,
                    500,
                    500
            );
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            Log.e("ProfileFragment", "Error generating QR code", e);
            return null;
        }
    }

    private void startScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan the Classroom QR Code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        qrCodeLauncher.launch(options);
    }
}
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
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.model.UserRole;
import com.example.eduview.data.repository.ClassroomRepository;
import com.example.eduview.data.repository.UserRepository;
import com.example.eduview.ui.login.LoginActivity;
import com.example.eduview.ui.main.MainViewModel;
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

    private MainViewModel mainViewModel;

    private ShapeableImageView profileImage;
    private TextView userNameText;
    private TextView roleText;
    private TextView classText;
    private EditText aboutMeEditText;
    private Button btnSaveBio;
    private Button logoutButton;

    // Teacher QR Section
    private MaterialCardView cardQRCode;
    private TextView tvQRLabel;
    private ImageView ivQRCode;
    private Button buttonGenerateQR;

    // Student Class Section
    private MaterialCardView cardClassInfo;
    private TextView tvTeacherName;
    private TextView tvClassName;
    private TextView tvNotRegistered;

    // Parent Children Section
    private MaterialCardView cardMyChildren;
    private RecyclerView rvChildren;
    private TextView tvNoChildren;
    private ChildAdapter childAdapter;
    private Button buttonScanQR; // Used by Student to scan class QR
    private View btnAddChild; // Used by Parent to open dialog

    private final ActivityResultLauncher<ScanOptions> qrCodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String scannedCode = result.getContents();
                    Log.d("ProfileFragment", "Scanned QR code = " + scannedCode);
                    mainViewModel.joinClass(scannedCode);
                }
            }
    );

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = root.findViewById(R.id.profileImage);
        userNameText = root.findViewById(R.id.User_name_text);
        roleText = root.findViewById(R.id.materialCardView).findViewById(R.id.textViewRole);
        classText = root.findViewById(R.id.Teacher_Class_Text);
        aboutMeEditText = root.findViewById(R.id.etAboutMe);
        btnSaveBio = root.findViewById(R.id.btnSaveBio);
        logoutButton = root.findViewById(R.id.buttonLogout);

        cardQRCode = root.findViewById(R.id.cardQRCode);
        tvQRLabel = root.findViewById(R.id.tvQRLabel);
        ivQRCode = root.findViewById(R.id.ivQRCode);
        buttonGenerateQR = root.findViewById(R.id.buttonGenerateQR);
        buttonScanQR = root.findViewById(R.id.buttonScanQR);

        cardClassInfo = root.findViewById(R.id.cardClassInfo);
        tvTeacherName = root.findViewById(R.id.tvTeacherName);
        tvClassName = root.findViewById(R.id.tvClassName);
        tvNotRegistered = root.findViewById(R.id.tvNotRegistered);

        cardMyChildren = root.findViewById(R.id.cardMyChildren);
        rvChildren = root.findViewById(R.id.rvChildren);
        tvNoChildren = root.findViewById(R.id.tvNoChildren);
        btnAddChild = root.findViewById(R.id.btnAddChild);

        setupRecyclerView();

        return root;
    }

    private void setupRecyclerView() {
        childAdapter = new ChildAdapter(new ClassroomRepository());
        rvChildren.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChildren.setAdapter(childAdapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        mainViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateUI(user);
            } else {
                Log.e("ProfileFragment", "Current user is null");
            }
        });

        mainViewModel.getChildrenData().observe(getViewLifecycleOwner(), children -> {
            if (children != null) {
                childAdapter.setChildren(children);
                tvNoChildren.setVisibility(children.isEmpty() ? View.VISIBLE : View.GONE);
                rvChildren.setVisibility(children.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        mainViewModel.getClassroomName().observe(getViewLifecycleOwner(), name -> {
            User user = mainViewModel.getCurrentUser().getValue();
            if (name != null && user != null) {
                if (user.getRole() == UserRole.TEACHER || user.getRole() == UserRole.STUDENT) {
                    classText.setText("Class: " + name);
                }
            }
        });

        mainViewModel.getJoinStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
            }
        });

        btnSaveBio.setOnClickListener(v -> {
            String newBio = aboutMeEditText.getText().toString().trim();
            mainViewModel.updateBio(newBio);
            Toast.makeText(getContext(), "Bio updated successfully!", Toast.LENGTH_SHORT).show();
        });

        logoutButton.setOnClickListener(v -> {
            mainViewModel.logout();
            Log.d("ProfileFragment", "Navigating to LoginActivity");
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        buttonScanQR.setOnClickListener(v -> startScanner());

        buttonGenerateQR.setOnClickListener(v -> {
            User user = mainViewModel.getCurrentUser().getValue();
            if (user instanceof Teacher) {
                String classId = ((Teacher) user).getClassID();

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
            
            User currentUser = mainViewModel.getCurrentUser().getValue();
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
                        mainViewModel.loadCurrentUser(); // Refresh list
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

    private void updateUI(User user) {
        userNameText.setText(user.getFirstName() + " " + user.getLastName());
        roleText.setText(user.getRole().name());
        aboutMeEditText.setText(user.getBio());

        // Default visibility
        cardQRCode.setVisibility(View.GONE);
        cardMyChildren.setVisibility(View.GONE);
        classText.setVisibility(View.GONE);
        cardClassInfo.setVisibility(View.GONE);
        buttonScanQR.setVisibility(View.GONE);

        if (user.getRole() == UserRole.TEACHER && user instanceof Teacher) {
            cardQRCode.setVisibility(View.VISIBLE);
            classText.setVisibility(View.VISIBLE);
            buttonGenerateQR.setVisibility(View.VISIBLE);
            buttonScanQR.setVisibility(View.GONE);
            tvQRLabel.setVisibility(View.VISIBLE);
            
            String classId = ((Teacher) user).getClassID();
            if (classId == null || classId.isEmpty()) {
                classText.setText("Class: None");
            }

        } else if (user.getRole() == UserRole.STUDENT) {
            classText.setVisibility(View.VISIBLE);
            buttonGenerateQR.setVisibility(View.GONE);
            cardClassInfo.setVisibility(View.VISIBLE);
            
            String classId = ((Student) user).getClassId();
            classText.setText("Class: " + (classId != null ? classId : "None"));

            if (classId == null || classId.isEmpty()) {
                cardQRCode.setVisibility(View.GONE);
                buttonScanQR.setVisibility(View.VISIBLE);
                tvNotRegistered.setVisibility(View.VISIBLE);
                tvTeacherName.setVisibility(View.GONE);
                tvClassName.setVisibility(View.GONE);
            } else {
                cardQRCode.setVisibility(View.GONE);
                buttonScanQR.setVisibility(View.GONE);
                tvNotRegistered.setVisibility(View.GONE);
                tvTeacherName.setVisibility(View.VISIBLE);
                tvClassName.setVisibility(View.VISIBLE);

                ClassroomRepository classroomRepo = new ClassroomRepository();
                classroomRepo.getClassroomName(classId, name -> {
                    tvClassName.setText("Class: " + name);
                }, e -> tvClassName.setText("Class: Error"));

                classroomRepo.getClassroomTeacher(classId, teacherId -> {
                    UserRepository userRepo = new UserRepository();
                    userRepo.fetchUser(teacherId, teacher -> {
                        tvTeacherName.setText("Teacher: " + teacher.getFirstName() + " " + teacher.getLastName());
                    }, e -> tvTeacherName.setText("Teacher: Error"));
                }, e -> tvTeacherName.setText("Teacher: Error"));
            }

        } else if (user.getRole() == UserRole.PARENT) {
            cardMyChildren.setVisibility(View.VISIBLE);
            classText.setVisibility(View.VISIBLE);
            classText.setText("Parent Profile");
        }
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
        options.setCaptureActivity(CustomScannerActivity.class);
        options.setPrompt("Scan the Classroom QR Code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        qrCodeLauncher.launch(options);
    }
}

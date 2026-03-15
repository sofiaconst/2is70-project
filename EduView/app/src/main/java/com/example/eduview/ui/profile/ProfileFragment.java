package com.example.eduview.ui.profile;

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

import com.example.eduview.R;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.model.UserRole;
import com.example.eduview.ui.login.LoginActivity;
import com.example.eduview.ui.main.MainViewModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class ProfileFragment extends Fragment {

    private MainViewModel mainViewModel;

    private ShapeableImageView profileImage;
    private TextView userNameText;
    private TextView roleText;
    private TextView classText;
    private EditText aboutMeEditText;
    private Button logoutButton;

    private TextView tvQRLabel;
    private ImageView ivQRCode;
    private Button buttonScanQR;
    private Button buttonGenerateQR;
/*
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

 */

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
        logoutButton = root.findViewById(R.id.buttonLogout);

        tvQRLabel = root.findViewById(R.id.tvQRLabel);
        ivQRCode = root.findViewById(R.id.ivQRCode);
        buttonScanQR = root.findViewById(R.id.buttonScanQR);
        buttonGenerateQR = root.findViewById(R.id.buttonGenerateQR);

        return root;
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
/*
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
*/
        logoutButton.setOnClickListener(v -> {
            mainViewModel.logout();
            Log.d("ProfileFragment", "Navigating to LoginActivity");
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });
/*
        buttonScanQR.setOnClickListener(v -> startScanner());

        buttonGenerateQR.setOnClickListener(v -> {
            User user = mainViewModel.getCurrentUser().getValue();
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

         */
    }

    private void updateUI(User user) {
        userNameText.setText(user.getFirstName() + " " + user.getLastName());
        roleText.setText(user.getRole().name());

        tvQRLabel.setVisibility(View.GONE);
        ivQRCode.setVisibility(View.GONE);
        buttonScanQR.setVisibility(View.GONE);
        buttonGenerateQR.setVisibility(View.GONE);

        if (user.getRole() == UserRole.TEACHER && user instanceof Teacher) {
            String classId = ((Teacher) user).getClassId();

            if (classId != null && !classId.isEmpty()) {
                buttonGenerateQR.setVisibility(View.VISIBLE);
            } else {
                classText.setText("Class: None");
            }

        } else if (user.getRole() == UserRole.STUDENT || user.getRole() == UserRole.PARENT) {
            buttonScanQR.setVisibility(View.VISIBLE);

            if (user instanceof Student) {
                String classId = ((Student) user).getClassId();
                classText.setText("Class: " + (classId != null ? classId : "None"));
            } else if (user instanceof Parent) {
                classText.setText("Parent Profile");
            } else {
                classText.setText("Profile");
            }
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
        options.setPrompt("Scan the Classroom QR Code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        //qrCodeLauncher.launch(options);
    }
}
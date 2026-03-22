package com.example.eduview.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduview.R;
import com.example.eduview.ui.adapters.ChildAdapter;
import com.example.eduview.ui.adapters.StudentManagerAdapter;
import com.example.eduview.ui.login.LoginActivity;
import com.example.eduview.ui.profile.profileFeatures.StudentProfileFeature;
import com.example.eduview.ui.profile.profileFeatures.TeacherProfileFeature;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class ProfileFragment extends Fragment {

    // --------------------- VIEWMODEL --------------------- //
    private ProfileViewModel profileVM;

    // --------------------- BASE UI --------------------- //
    private ShapeableImageView ivPfp;
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
    // private ParentProfileFeature parentFeature;

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
        //parentFeature = new ParentProfileFeature(getView(), profileVM);

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
    }
    private void render(ProfileUIState state) {

        if (state == null) return;
        // Base UI
        tvFullName.setText(state.displayName);
        tvUserRole.setText(state.roleText);
        ivPfp.setImageResource(state.profilePictureResId);

        resetVisibility();

//        // QR
//        btnScanQR.setVisibility(state.showScanButton ? View.VISIBLE : View.GONE);
//
//        if (state.qrBitmap != null) {
//            cardQRCode.setVisibility(View.VISIBLE);
//            ivQRCode.setImageBitmap(state.qrBitmap);
//        }

        // Delegate to features
        studentFeature.bind(state);
        teacherFeature.bind(state);
        // parentFeature.bind(state);

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
    /*
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pfp_selection, null);
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

        dialog.show();*/
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

    private void showAddChildDialog() {
/*        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_child, null);
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
        });*/
    }
}



//=================OLD ARCHITECTURE================//
//
//
//
//
//    //---------------------SOFIA-----------------//
//
/// /---------------------MINE-----------------//
//
//        btnCancel.setOnClickListener(v -> dialog.dismiss());
//
//        dialog.show();
//
//        profileVM.getAddChildStatus().observe(getViewLifecycleOwner(), status -> {
//            if (status == null) return;
//
//            if (status.equals("LOADING")) {
//                btnAdd.setEnabled(false);
//                btnAdd.setText("Adding...");
//            } else if (status.equals("SUCCESS")) {
//                Toast.makeText(getContext(), "Child added successfully!", Toast.LENGTH_SHORT).show();
//                dialog.dismiss();
//            } else if (status.startsWith("ERROR")) {
//                btnAdd.setEnabled(true);
//                btnAdd.setText("Add");
//                Toast.makeText(getContext(), status, Toast.LENGTH_LONG).show();
//            } else {
//                // validation message
//                Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void updateParentUI (Parent parent){
//
//        profileVM.loadChildrenData(parent);
//        cardMyChildren.setVisibility(View.VISIBLE);
//        tvClassLabel.setVisibility(View.VISIBLE);
//        tvClassLabel.setText("");
//
//    }
//
//
//
////---------------------MINE-----------------//
////---------------------SOFIA-----------------//
//
//    @Override
//    public void onResume () {
//        super.onResume();
//        profileVM.loadTeacherStudents();
//    }
//
//    // ---------- old architecture, here for reference
//
//    private void render (ProfileUIState state){
//        tvFullName.setText(state.displayName);
//        tvUserRole.setText(state.roleText);
//        tvClassLabel.setText(state.classText);
//        ivPfp.setImageResource(state.profilePictureResId);
//
//        btnScanQR.setVisibility(state.showScanButton ? View.VISIBLE : View.GONE);
//
//        if (state.qrBitmap != null) {
//            tvQRCodeLabel.setVisibility(View.VISIBLE);
//            ivQRCode.setVisibility(View.VISIBLE);
//            ivQRCode.setImageBitmap(state.qrBitmap);
//        } else {
//            tvQRCodeLabel.setVisibility(View.GONE);
//            ivQRCode.setVisibility(View.GONE);
//        }
//
//        boolean isTeacher = "TEACHER".equalsIgnoreCase(state.roleText);
//        cardManageStudents.setVisibility(isTeacher ? View.VISIBLE : View.GONE);
//    }
//
//
//    private void observeState () {
//        profileVM.getUIState().observe(getViewLifecycleOwner(), this::render);
//
//        profileVM.getClassroomStudents().observe(getViewLifecycleOwner(), students -> {
//            studentManagerAdapter.submitList(students);
//        });
//
//        profileVM.getStudentsLoading().observe(getViewLifecycleOwner(), loading -> {
//            pbStudents.setVisibility(loading ? View.VISIBLE : View.GONE);
//        });
//
//        profileVM.getMessage().observe(getViewLifecycleOwner(), msg -> {
//            if (msg != null && !msg.isEmpty()) {
//                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
//
//
//            }
//        });
//    }
//
// */
//
//
//
//    private void setupRoleBasedListeners (User user){
//        btnLogout.setOnClickListener(v -> {
//            profileVM.logout();
//            Log.d("ProfileFragment", "Navigating to LoginActivity");
//            startActivity(new Intent(requireActivity(), LoginActivity.class));
//            requireActivity().finish();
//        });
//
//        switch (user.getRole()) {
//            case PARENT:
//                if (btnAddChild != null) {
//                    btnAddChild.setOnClickListener(v -> showAddChildDialog());
//                }
//                break;
//
//            case STUDENT:
//                btnScanQR.setOnClickListener(v -> startScanner());
//                break;
//
//            case TEACHER:
//                break;
//        }
//    }
//
//    private void setupRoleBasedObservers (User user){
//        switch (user.getRole()) {
//            case PARENT:
//                profileVM.getChildrenData().observe(getViewLifecycleOwner(), children -> {
//                    if (children != null) {
//                        childAdapter.setChildren(children);
//                        tvNoChildren.setVisibility(children.isEmpty() ? View.VISIBLE : View.GONE);
//                        rvChildren.setVisibility(children.isEmpty() ? View.GONE : View.VISIBLE);
//                    }
//                });
//                break;
//
//            case STUDENT:
//                profileVM.getJoinStatus().observe(getViewLifecycleOwner(), status -> {
//                    if (status != null) {
//                        Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//                profileVM.getStudentState().observe(getViewLifecycleOwner(), state -> {
//                    studentFeature.bind(state);
//
//                    if (state != null && state.getErrorMessage() != null) {
//                        Toast.makeText(getContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//                break;
//
//            case TEACHER:
//                profileVM.getTeacherState().observe(getViewLifecycleOwner(), state -> {
//                    teacherFeature.bind(state);
//
//                    if (state != null && state.getErrorMessage() != null) {
//                        Toast.makeText(getContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//                break;
//        }
//
//    }
//
//}


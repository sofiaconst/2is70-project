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

/**
 * Fragment that displays and manages the user profile screen.
 * Handles role-based UI (student, teacher, parent), user actions, and UI rendering.
 */
public class ProfileFragment extends Fragment {

    // View Model
    private ProfileViewModel profileVM;

    // Base UI
    private ImageView ivPfp;
    private TextView tvEditPfp;
    private TextView tvFullName, tvUserRole, tvClassLabel;
    private MaterialButton btnLogout;

    // QR Code Section
    private MaterialCardView cardQRCode;
    private TextView tvQRCodeLabel;
    private ImageView ivQRCode;
    private MaterialButton btnScanQR;


    // Student Class Info
    private MaterialCardView cardClassInfo;
    private TextView tvClassName, tvTeacherName, tvNotRegistered;

    // Parent Children Info
    private MaterialCardView cardMyChildren;
    private TextView tvNoChildren;
    private RecyclerView rvChildren;
    private ChildAdapter childAdapter;
    private View btnAddChild;

    // Teacher Student Manager
    private MaterialCardView cardManageStudents;
    private ProgressBar pbStudents;
    private RecyclerView rvStudents;
    private StudentManagerAdapter studentManagerAdapter;

    // Profile Features
    private StudentProfileFeature studentFeature;
    private TeacherProfileFeature teacherFeature;
    private ParentProfileFeature parentFeature;

    public ProfileFragment() {
    }

    /**
     * Initializes the different views needed in the profile fragment when the fragment view is
     * initially created.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the base profile view xml
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Methods for different views
        initBaseViews(root);
        initClassQRCodeViews(root);
        initClassInfoViews(root);
        initMyChildrenViews(root);
        initManageStudentsViews(root);

        return root;
    }

    /**
     * Initializes basic profile UI components.
     *
     * @param root the base profile view xml
     */
    private void initBaseViews(View root) {
        tvFullName = root.findViewById(R.id.User_name_text);
        tvUserRole = root.findViewById(R.id.textViewRole);
        tvClassLabel = root.findViewById(R.id.Teacher_Class_Text);
        ivPfp = root.findViewById(R.id.profileImage);
        tvEditPfp = root.findViewById(R.id.tvEditPfp);
        btnLogout = root.findViewById(R.id.buttonLogout);
    }

    /**
     * Initializes QR code related UI components.
     *
     * @param root the base profile view xml
     */
    private void initClassQRCodeViews(View root) {
        tvQRCodeLabel = root.findViewById(R.id.tvQRLabel);
        cardQRCode = root.findViewById(R.id.cardQRCode);
        ivQRCode = root.findViewById(R.id.ivQRCode);
        btnScanQR = root.findViewById(R.id.buttonScanQR);
    }

    /**
     * Initializes student class information UI.
     *
     * @param root the base profile view xml
     */
    private void initClassInfoViews(View root) {
        cardClassInfo = root.findViewById(R.id.cardClassInfo);
        tvClassName = root.findViewById(R.id.tvClassName);
        tvTeacherName = root.findViewById(R.id.tvTeacherName);
    }

    /**
     * Initializes the parent's children list UI components.
     *
     * @param root the base profile view xml
     */
    private void initMyChildrenViews(View root) {
        cardMyChildren = root.findViewById(R.id.cardMyChildren);
        rvChildren = root.findViewById(R.id.rvChildren);
        tvNoChildren = root.findViewById(R.id.tvNoChildren);
        btnAddChild = root.findViewById(R.id.btnAddChild);
    }

    /**
     * Initializes the teacher's manage students UI components.
     *
     * @param root the base profile view xml
     */
    private void initManageStudentsViews(View root) {
        cardManageStudents = root.findViewById(R.id.manageStudentsCard);
        pbStudents = root.findViewById(R.id.progressStudents);
        rvStudents = root.findViewById(R.id.rvStudents);
    }

    /**
     * Initializes the ViewModel sets up listeners and features of different roles when the view is
     * first created.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initializing Profile ViewModel
        profileVM = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Set up recycler views and listeners
        setupRecyclerViews();
        setupListeners();

        // Set up different features depending on role
        studentFeature = new StudentProfileFeature(getView());
        teacherFeature = new TeacherProfileFeature(getView(), studentManagerAdapter);
        parentFeature = new ParentProfileFeature(getView(), childAdapter);

        // Observes the UI state that should be shown
        profileVM.getUIState().observe(getViewLifecycleOwner(), this::render);
    }

    /**
     * Sets up RecyclerViews and their adapters.
     */
    private void setupRecyclerViews() {
        // Set up student manager view for teacher
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

        // Initializing adapter for parent viewing children
        childAdapter = new ChildAdapter();
        rvChildren.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChildren.setAdapter(childAdapter);

    }

    /**
     * Renders UI based on the provided state.
     *
     * @param state used to access the different role states
     */
    private void render(ProfileUIState state) {
        if (state == null) return;

        // Render basic user information
        tvFullName.setText(state.displayName);
        tvUserRole.setText(state.roleText);
        ivPfp.setImageResource(state.profilePictureResId);

        resetVisibility();

        // Bind the feature to profile fragment according to the role state
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

    /**
     * Hides all optional UI sections before rendering a new state.
     */
    private void resetVisibility() {
        cardQRCode.setVisibility(View.GONE);
        cardMyChildren.setVisibility(View.GONE);
        tvClassLabel.setVisibility(View.GONE);
        cardClassInfo.setVisibility(View.GONE);
    }

    /**
     * Sets up UI listeners.
     */
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

    /**
     * Displays a dialog allowing the user to select a profile picture.
     */
    private void showPfpSelectionDialog() {
        // Inflate the layout for the profile picture selection
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_pfp_selection, null);

        // Get reference to the RecyclerView that will display profile pictures
        RecyclerView rvPfps = dialogView.findViewById(R.id.rvPfpSelection);

        // Create the dialog with a title and the custom layout
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Choose Profile Picture")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .create();

        // Create listener for selecting a profile picture
        PfpAdapter adapter = new PfpAdapter(pfp -> {
            // Update profile picture in ViewModel
            profileVM.updateProfilePicture(pfp);
            dialog.dismiss();
        });

        // Set layout manager to display items in a grid
        rvPfps.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        // Attach adapter to RecyclerView
        rvPfps.setAdapter(adapter);

        dialog.show();
    }

    /**
     * Starts the QR scanner for joining a classroom.
     */
    private void startScanner() {
        ScanOptions options = new ScanOptions();
        options.setCaptureActivity(CustomScannerActivity.class);
        options.setPrompt("Scan the Classroom QR Code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        qrCodeLauncher.launch(options);
    }

    /**
     * Handles QR scan result and triggers class join.
     */
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

    /**
     * Displays dialog for adding a child account.
     */
    private void showAddChildDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_child, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.TransparentDialog)
                .setView(dialogView)
                .create();

        // Child information
        EditText etFirstName = dialogView.findViewById(R.id.etChildFirstName);
        EditText etLastName = dialogView.findViewById(R.id.etChildLastName);
        EditText etUsername = dialogView.findViewById(R.id.etChildUsername);
        EditText etPassword = dialogView.findViewById(R.id.etParentPassword);

        // Get parent of child
        TextInputLayout tilFirstName = (TextInputLayout) etFirstName.getParent().getParent();
        TextInputLayout tilLastName = (TextInputLayout) etLastName.getParent().getParent();
        TextInputLayout tilUsername = (TextInputLayout) etUsername.getParent().getParent();
        TextInputLayout tilPassword = (TextInputLayout) etPassword.getParent().getParent();

        // Initialize buttons
        Button btnAdd = dialogView.findViewById(R.id.btnAdd);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            // User input
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

            // Validate inputs
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

            // Stop if validation failed
            if (hasError) {
                return;
            }

            // Trigger ViewModel action
            profileVM.addChild(firstName, lastName, username, password);
        });


        // Observe status
        profileVM.getAddChildStatus().observe(getViewLifecycleOwner(), status -> {
            if (status == null) return;

            // States of child according to status
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

        // Adjust dialog width for landscape orientation
        boolean isLandscape =
                getResources().getConfiguration().orientation
                        == android.content.res.Configuration.ORIENTATION_LANDSCAPE;

        if (isLandscape) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.8),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
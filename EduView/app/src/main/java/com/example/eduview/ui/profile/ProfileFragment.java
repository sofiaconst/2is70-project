package com.example.eduview.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eduview.R;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.SessionManager.SessionCallback;
import com.example.eduview.ui.login.LoginActivity;
import com.example.eduview.ui.main.MainActivity;
import com.google.android.material.imageview.ShapeableImageView;
import com.example.eduview.ui.profile.ProfileUIState.BaseUserUiState;

public class ProfileFragment extends Fragment {
    // Base user info
    private TextView fullNameText, roleText, classNameText;
    private ShapeableImageView profileImage;
    private Button logoutButton;

    // Role-specific sections

    private View studentSection, teacherSection, parentSection;

    // ViewModels
    private BaseUserViewModel baseUserVM;
    private StudentProfileViewModel studentVM;
    private TeacherProfileViewModel teacherVM;
    private ParentProfileViewModel parentVM;
    private User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile_trial, container, false);
        initViews(root);
        setupViewModel();

        logoutButton.setOnClickListener(v -> {
            baseUserVM.logout(new SessionCallback() {
                @Override
                public void onSuccess(User user) {
                    navigateToLogin();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "Logout failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
        return root;
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void initViews(View root) {
        fullNameText = root.findViewById(R.id.User_name_text);
        roleText = root.findViewById(R.id.textViewRole);
        classNameText = root.findViewById(R.id.Teacher_Class_Text);
        logoutButton = root.findViewById(R.id.buttonLogout);
/*
        teacherSection = root.findViewById(R.id.cardQRCode);
        parentSection = root.findViewById(R.id.cardMyChildren);
        studentClassInfoSection = root.findViewById(R.id.cardClassInfo);

        // Hide everything initially
        teacherSection.setVisibility(View.GONE);
        parentSection.setVisibility(View.GONE);
        studentClassInfoSection.setVisibility(View.GONE);
        classNameText.setVisibility(View.GONE);

 */
    }

    private void setupViewModel() {
        baseUserVM = new ViewModelProvider(this).get(BaseUserViewModel.class);

        baseUserVM.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                updateBaseUI(user);
                setupRoleUI(user);
            } else {
                Log.e("ProfileFragment", "User is null");
            }
        });

        baseUserVM.loadUser();

    }

    private void updateBaseUI(User user) {
        fullNameText.setText(user.getFirstName() + " " + user.getLastName());
        roleText.setText(user.getRole().name());
    }

    private void setupRoleUI(User user) {

        switch (user.getRole()) {

            case STUDENT:
                //studentClassInfoSection.setVisibility(View.VISIBLE);
                setupStudentSection((Student) user);
                break;

            case TEACHER:
                //teacherSection.setVisibility(View.VISIBLE);
                setupTeacherSection((Teacher) user);
                break;

            case PARENT:
                //parentSection.setVisibility(View.VISIBLE);
                // Parent logic later
                break;
        }
    }

    private void setupStudentSection(Student student) {

        studentVM = new ViewModelProvider(this).get(StudentProfileViewModel.class);

        classNameText.setVisibility(View.VISIBLE);

        studentVM.getClassName().observe(getViewLifecycleOwner(), name -> {
            classNameText.setText("Class: " + name);
        });

        String classId = student.getClassId();
        studentVM.loadClassName(classId);
    }

    private void setupTeacherSection(Teacher teacher) {

        teacherVM = new ViewModelProvider(this).get(TeacherProfileViewModel.class);

        classNameText.setVisibility(View.VISIBLE);

        teacherVM.getClassName().observe(getViewLifecycleOwner(), name -> {
            classNameText.setText("Class: " + name);
        });

        String classId = teacher.getClassId();
        teacherVM.loadClassName(classId);
    }
/*
    private void updateBaseUserUI(BaseUserUiState state) {
        // Common info
        fullNameText.setText(state.fullName);
        roleText.setText(String.valueOf(state.role));
        // Load profile image

        // Hide all sections initially
        //studentSection.setVisibility(View.GONE);
        //teacherSection.setVisibility(View.GONE);
        //parentSection.setVisibility(View.GONE);

        // Show role-specific section and attach ViewModel logic
        switch (state.role) {
            case STUDENT:
                //studentSection.setVisibility(View.VISIBLE);
                setupStudentSection();
                //observeStudentData();
                break;
            case TEACHER:
                //teacherSection.setVisibility(View.VISIBLE);
                setupTeacherSection();
                observeTeacherData();
                break;
            case PARENT:
                parentSection.setVisibility(View.VISIBLE);
                setupParentSection();
                //observeParentData();
                break;
        }


    }

    private void observeTeacherData() {
        teacherVM.getClassName.observe(getViewLifecycleOwner(), name -> {
            classNameText.setText(name);
        });
    }

    private void setupStudentSection() {
        studentVM = new ViewModelProvider(this).get(StudentProfileViewModel.class);
        // Observe student data, QR scanning, etc.
        // studentVM.getStudentState().observe(...);
    }

    private void setupTeacherSection() {
        //teacherVM = new ViewModelProvider(this).get(TeacherProfileViewModel.class);
        // Observe teacher data, QR generation, student list, etc.
    }

    private void setupParentSection() {
        //parentVM = new ViewModelProvider(this).get(ParentProfileViewModel.class);
        // Observe children list, add child logic, etc.
    }
    */
}
package com.example.eduview.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduview.R;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.SessionManager.SessionCallback;
import com.example.eduview.ui.adapters.ChildAdapter;
import com.example.eduview.ui.features.ProfileFeature;
import com.example.eduview.ui.features.StudentClassInfoFeature;
import com.example.eduview.ui.features.TeacherQRCodeFeature;
import com.example.eduview.ui.login.LoginActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {


    // Base user info
    private TextView fullNameText, roleText;
    private ShapeableImageView profileImage;
    private Button logoutButton;

    // Feature references
    private final List<ProfileFeature> features = new ArrayList<>();

    // ViewModel
    private ProfileViewModel profileVM;
    private User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile_trial, container, false);
        initViews(root);
        setupViewModel();
        setupLogout();
        return root;
    }


    private void initViews(View root) {
        fullNameText = root.findViewById(R.id.User_name_text);
        roleText = root.findViewById(R.id.textViewRole);
        profileImage = root.findViewById(R.id.profileImage); // implement loading later
        logoutButton = root.findViewById(R.id.buttonLogout);

        // Role-specific
        root.findViewById(R.id.cardClassInfo).setVisibility(View.GONE);
        root.findViewById(R.id.cardQRCode).setVisibility(View.GONE);
        root.findViewById(R.id.cardMyChildren).setVisibility(View.GONE);
    }

    private void setupViewModel() {
        profileVM = new ViewModelProvider(this).get(ProfileViewModel.class);

        profileVM.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                updateBaseUI(user);
                setupRoleFeatures(user);
            } else {
                Log.e("ProfileFragment", "User is null");
            }
        });

        profileVM.loadUser();
    }

    private void setupLogout() {
        logoutButton.setOnClickListener(v -> {
            profileVM.logout(new SessionCallback() {
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
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void updateBaseUI(User user) {
        fullNameText.setText(user.getFirstName() + " " + user.getLastName());
        roleText.setText(user.getRole().name());

        // TODO: load profile image with Glide/Picasso
        // Glide.with(this).load(user.getProfileImageUrl()).into(profileImage);
    }

    /**
     * Set up role-specific features declaratively.
     * Each feature handles its own binding and visibility.
     */
    private void setupRoleFeatures(User user) {
        // Hide old features
        features.forEach(ProfileFeature::hide);
        features.clear();

        // Add new features for the role
        features.addAll(getFeaturesForUser(user));

        // Show all features
        features.forEach(ProfileFeature::show);
    }

    /**
     * Factory method: returns features based on user role.
     */
    private List<ProfileFeature> getFeaturesForUser(User user) {
        List<ProfileFeature> featureList = new ArrayList<>();

        switch (user.getRole()) {
            case STUDENT:
                featureList.add(new StudentClassInfoFeature(requireView(), profileVM, (Student) user));
                // Add more student features here
                break;

            case TEACHER:
                featureList.add(new TeacherQRCodeFeature(requireView(), profileVM, (Teacher) user));
                // Add more teacher features here
                break;

            case PARENT:

                featureList.add(new MyChildrenFeature(requireView(), profileVM, (Parent) user));
                // Add more parent features here
                break;
        }

        return featureList;
    }


    /**
     * Set up role-specific features declaratively.
     * Each feature knows how to bind to its UI elements.
     */
    /*
    private void setupRoleFeatures(User user) {

        // Clear previous features
        for (ProfileFeature feature : features) {
            feature.hide();
        }
        features.clear();

        switch (user.getRole()) {

            case STUDENT:
                studentVM = new ViewModelProvider(this).get(StudentProfileViewModel.class);

                // Load class name once
                studentVM.loadClassName(((Student) user).getClassId());

                // Inline classNameText
                studentVM.getClassName().observe(getViewLifecycleOwner(), name -> {
                    classNameText.setVisibility(name != null ? View.VISIBLE : View.GONE);
                    classNameText.setText(name != null ? "Class: " + name : "");
                });

                // Student card feature
                StudentClassInfoFeature studentClassFeature = new StudentClassInfoFeature(
                        requireView(), studentVM, (Student) user
                );
                studentClassFeature.show();
                features.add(studentClassFeature);

                break;

            case TEACHER:
                teacherVM = new ViewModelProvider(this).get(TeacherProfileViewModel.class);

                TeacherQRCodeFeature qrFeature = new TeacherQRCodeFeature(
                        requireView(), teacherVM, (Teacher) user
                );
                qrFeature.show();
                features.add(qrFeature);

                // More teacher features can be added
                break;

            case PARENT:
                // Parent-specific features (e.g., MyChildrenFeature)
                // MyChildrenFeature childrenFeature = new MyChildrenFeature(requireView(), parentVM, (Parent) user);
                // childrenFeature.show();
                // features.add(childrenFeature);
                break;
        }
    }

     */
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
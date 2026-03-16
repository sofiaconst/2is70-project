package com.example.eduview.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eduview.R;
import com.example.eduview.ui.login.LoginActivity;
import com.google.android.material.imageview.ShapeableImageView;

public class ProfileFragment extends Fragment {

        private ProfileViewModel profileViewModel;

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
            setupListeners();
            observeState();

            return root;
        }

        private void initViews(View root) {

            profileImage = root.findViewById(R.id.profileImage);
            userNameText = root.findViewById(R.id.User_name_text);
            roleText = root.findViewById(R.id.textViewRole);
            classText = root.findViewById(R.id.Teacher_Class_Text);
            aboutMeEditText = root.findViewById(R.id.etAboutMe);

            logoutButton = root.findViewById(R.id.buttonLogout);

            tvQRLabel = root.findViewById(R.id.tvQRLabel);
            ivQRCode = root.findViewById(R.id.ivQRCode);
            buttonScanQR = root.findViewById(R.id.buttonScanQR);
            buttonGenerateQR = root.findViewById(R.id.buttonGenerateQR);
        }

        private void setupViewModel() {
            profileViewModel = new ViewModelProvider(requireActivity())
                    .get(ProfileViewModel.class);
        }

        private void setupListeners() {

            logoutButton.setOnClickListener(v -> {
                profileViewModel.logout();
                startActivity(new Intent(requireActivity(), LoginActivity.class));
                requireActivity().finish();
            });

            buttonGenerateQR.setOnClickListener(v -> profileViewModel.generateQRCode());

            //buttonScanQR.setOnClickListener(v -> profileViewModel.startQRScan());
        }

        private void observeState() {

            profileViewModel.getUIState().observe(
                    getViewLifecycleOwner(),
                    this::render
            );
        }

        private void render(ProfileUIState state) {

            userNameText.setText(state.displayName);
            roleText.setText(state.roleText);
            classText.setText(state.classText);

            buttonScanQR.setVisibility(
                    state.showScanButton ? View.VISIBLE : View.GONE
            );

            buttonGenerateQR.setVisibility(
                    state.showGenerateButton ? View.VISIBLE : View.GONE
            );

            if (state.qrBitmap != null) {

                tvQRLabel.setVisibility(View.VISIBLE);
                ivQRCode.setVisibility(View.VISIBLE);
                ivQRCode.setImageBitmap(state.qrBitmap);
                buttonGenerateQR.setVisibility((View.GONE));

            } else {

                tvQRLabel.setVisibility(View.GONE);
                ivQRCode.setVisibility(View.GONE);

            }
        }
}
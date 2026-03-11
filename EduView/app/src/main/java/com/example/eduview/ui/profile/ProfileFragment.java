package com.example.eduview.ui.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.eduview.R;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.User;
import com.example.eduview.ui.main.MainViewModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.annotations.Nullable;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private MainViewModel mainViewModel;

    private ShapeableImageView profileImage;
    private TextView userNameText;
    private TextView roleText;
    private TextView classText;
    private EditText aboutMeEditText;
    private Button logoutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Bind views
        profileImage = root.findViewById(R.id.profileImage);
        userNameText = root.findViewById(R.id.User_name_text);
        roleText = root.findViewById(R.id.materialCardView).findViewById(R.id.textViewRole);
        classText = root.findViewById(R.id.Teacher_Class_Text);
        aboutMeEditText = root.findViewById(R.id.etAboutMe);
        logoutButton = root.findViewById(R.id.buttonLogout);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get shared MainViewModel from activity
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Observe user LiveData
        mainViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateUI(user);
            }
        });

        logoutButton.setOnClickListener(v -> mainViewModel.logout());
    }

    private void updateUI(User user) {
        // Set name
        userNameText.setText(user.getFirstName() + " " + user.getLastName());

        /*
        // Set profile picture if URL exists
        String pfpUrl = user.getProfileImageURL();
        if (pfpUrl != null && !pfpUrl.isEmpty()) {
            // Using Glide or similar library
            Glide.with(this)
                    .load(pfpUrl)
                    .placeholder(R.drawable.pfp_photo_button_icon)
                    .circleCrop()
                    .into(profileImage);
        }

         */

        // Set role
        roleText.setText(user.getRole().name());

        /*
        // Show extra info depending on type
        if (user instanceof Student student) {
            classText.setText("Class: " + student.getClassID());
        } else if (user instanceof Teacher teacher) {
            classText.setText("Class: " + teacher.getClassID());
        } else if (user instanceof Parent parent) {
            classText.setText("Children: " + parent.getChildrenIDs().size());
        }

         */

        // TODO: set bio if you re-add it later
    }

    public ProfileFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    /*
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
    */
}
package com.example.eduview.ui.createPost;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageCapture;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.eduview.R;
import com.example.eduview.data.model.FeedItemType;
import com.example.eduview.data.model.*;
import com.example.eduview.data.repository.SessionManager;
import com.example.eduview.ui.main.MainActivity;
import com.example.eduview.ui.main.MainViewModel;

import com.bumptech.glide.Glide;

import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;

/**
 * Fragment for the "Create Post" screen.
 *
 * This class handles:
 * - what happens when the user types in the prompt box
 * - what happens when the user taps the image area
 * - what happens when the user presses send
 * - restoring the screen state after rotation through the ViewModel
 */
public class CreatePostFragment extends Fragment {

    // ViewModel that remembers the user's draft even if the screen rotates
    private CreatePostViewModel viewModel;
    // SessionManager is treated as a black box from your teammate
    private SessionManager sessionManager;
    private ImageButton sendButton;
    private View layoutAnnouncement;
    private CheckBox announcementCheckBox;
    private EditText promptEditTextBox;
    private View layoutImageContainer;
    private ImageView postImage;
    private ImageView cameraButton;
    // CameraX
    private ImageCapture imageCapture;
    private Uri imageUri;
    private MaterialButton deleteImageButton;

    public CreatePostFragment() {
        // Required empty public constructor
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the XML layout for this screen
        return inflater.inflate(R.layout.fragment_create_post, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Connect this screen to its ViewModel.
        // This is what allows the typed text and image URL to survive rotation.
        viewModel = new ViewModelProvider(this).get(CreatePostViewModel.class);

        // Get the current session manager instance from the mainactivity getter.
        MainActivity main =(MainActivity) requireActivity();
        MainViewModel model = main.getMainViewModel();
        sessionManager = SessionManager.getInstance();

        // Link all Java fields to the views in the XML
        bindViews(view);
        setupFragmentResultListener();
        // Configure the UI based on the user's role
        configureUiForUserRole();
        // Set up all click listeners and text listeners
        setupListeners();
        // Watch the ViewModel so the UI always reflects the saved draft state
        observeViewModel();
    }

    /**
     * Finds all the views from the XML and stores them in fields
     * so we can use them in the rest of the Fragment.
     */
    private void bindViews(@NonNull View view) {
        sendButton = view.findViewById(R.id.SendButton);
        layoutAnnouncement = view.findViewById(R.id.layoutAnnouncement);
        announcementCheckBox = view.findViewById(R.id.AnnouncementCheckBox);
        promptEditTextBox = view.findViewById(R.id.PromptEditTextBox);
        layoutImageContainer = view.findViewById(R.id.layoutImageContainer);
        postImage = view.findViewById(R.id.PostImage);
        cameraButton = view.findViewById(R.id.CameraButton);
        deleteImageButton = view.findViewById(R.id.DeleteImageButton);
    }

    /**
     * Sets up what should happen when the user interacts with the screen.
     */
    private void setupListeners() {

        // Every time the user types, save that text into the ViewModel.
        // That way, if the screen rotates, the draft caption is still there.
        promptEditTextBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing needed here
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing needed here
            }
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setCaption(s.toString());
            }
        });

        cameraButton.setOnClickListener(v -> loadCameraFragment());
//            // Optional test line if you want to simulate an image being selected:
//            viewModel.setImageUrl("https://example.com/test.jpg");
        // Pressing send should attempt to create the post
        sendButton.setOnClickListener(v -> submitPost());

        deleteImageButton.setOnClickListener(v -> { viewModel.setImageUrl(""); });

    }

    private void loadCameraFragment() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_createPost_to_camera);
    }

    /**
     * Observes the ViewModel so the UI stays in sync with the saved draft.
     * This is the part that makes rotation work properly.
     */
    private void observeViewModel() {
        // Restore caption text whenever the saved caption changes.
        // This matters especially after rotation.
        viewModel.getCaption().observe(getViewLifecycleOwner(), caption -> {
            String safeCaption = caption == null ? "" : caption;
            String currentText = promptEditTextBox.getText().toString();

            // Only reset the text if it is actually different.
            // Stop the cursor from resetting every time
            if (!currentText.equals(safeCaption)) {
                promptEditTextBox.setText(safeCaption);
                promptEditTextBox.setSelection(promptEditTextBox.getText().length());
            }
        });

        // Restore image state whenever the saved image URL changes.
        // Right now we are only showing or hiding the placeholder,
        // since the real image loading will come later.
        viewModel.getImageUrl().observe(getViewLifecycleOwner(), imageUrl -> {
            boolean hasImage = imageUrl != null && !imageUrl.trim().isEmpty();

            if (hasImage) {
                cameraButton.setVisibility(View.GONE);
                postImage.setVisibility(View.VISIBLE);
                deleteImageButton.setVisibility(View.VISIBLE);

                Glide.with(this)
                        .load(imageUrl)
                        .dontAnimate()
                        .centerCrop()
                        .into(postImage);

            } else {
                cameraButton.setVisibility(View.VISIBLE);
                postImage.setVisibility(View.GONE);
                deleteImageButton.setVisibility(View.GONE);
                postImage.setImageDrawable(null);
            }
        });

        // If the ViewModel reports that the post was created successfully,
        // clear the screen so the user can start fresh.
        viewModel.getPostCreated().observe(getViewLifecycleOwner(), created -> {
            if (Boolean.TRUE.equals(created)) {
                Toast.makeText(requireContext(), "Post created", Toast.LENGTH_SHORT).show();
                clearFieldsAfterPost();
                viewModel.clearPostCreatedFlag();
            }
        });

        // If the ViewModel reports an error, show it to the user.
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearErrorMessage();
            }
        });
    }

    /**
     * Reads the needed information and asks the ViewModel to create the post.
     */
    private void submitPost() {
        String classId = null;
        // Adjust these method names if your teammate named them differently.
        User user = sessionManager.getCurrentUser();
        String authorId = user.getUserId();

        if (user.getRole() == UserRole.PARENT) {
            return;
        }
        if (user.getRole() == UserRole.TEACHER) {
            Teacher teacher = (Teacher) user;
            classId = teacher.getClassId();
        }
        if (user.getRole() == UserRole.STUDENT) {
            Student student = (Student) user;
            classId = student.getClassId();
        }

        if (authorId == null || authorId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "No logged-in user found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (classId == null || classId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "No classroom found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Decide whether this post is an announcement or a normal post.
        // If the announcement area is shown and the box is checked, use ANNOUNCEMENT.
        // Otherwise, default to PENDING for student-style posts.
        FeedItemType type = getSelectedPostType(user);

        viewModel.createPost(type, classId, authorId);
    }

    /**
     * Decides what kind of post the user is making.
     *
     * Current logic:
     * - checked announcement box -> ANNOUNCEMENT
     * - otherwise -> PENDING
     */
    private FeedItemType getSelectedPostType(User user) {
        if (user.getRole() == UserRole.STUDENT) {
            return FeedItemType.PENDING;
        }

        if (user.getRole() == UserRole.TEACHER) {
            if (announcementCheckBox.isChecked()) {
                return FeedItemType.ANNOUNCEMENT;
            }
            return FeedItemType.PUBLISHED;
        }

        throw new IllegalStateException("Parents cannot create posts");
    }

    /**
     * Clears the UI and ViewModel after a successful post.
     */
    private void clearFieldsAfterPost() {
        // Clear what the ViewModel is holding
        viewModel.setCaption("");
        viewModel.setImageUrl("");

        // Clear what the user sees
        promptEditTextBox.setText("");
        announcementCheckBox.setChecked(false);
        postImage.setImageDrawable(null);
        cameraButton.setVisibility(View.VISIBLE);
    }

    /**
     * Shows or hides teacher-only Announcments box.
     * Teachers can choose to post an announcement, so they should see the announcement checkbox area. Other users should not.
     */
    private void configureUiForUserRole() {
        User user = sessionManager.getCurrentUser();

        if (user != null && user.getRole() == UserRole.TEACHER) {
            layoutAnnouncement.setVisibility(View.VISIBLE);
        } else {
            layoutAnnouncement.setVisibility(View.GONE);
            announcementCheckBox.setChecked(false);
        }
    }
    private void setupFragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener(
                "cameraResult",
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    String imageUrl = bundle.getString("imageUrl");
                    Log.d("CreatePostFragment", "Received imageUrl from CameraFragment: " + imageUrl);

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        viewModel.setImageUrl(imageUrl);
                    }
                }
        );
    }
}
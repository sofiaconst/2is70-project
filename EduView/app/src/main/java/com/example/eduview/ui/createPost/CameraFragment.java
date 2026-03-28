package com.example.eduview.ui.createPost;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eduview.R;
import com.example.eduview.data.repository.MediaRepository;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.Executor;

import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

/**
 * Fragment that captures a photo using the device camera and uploading it for use
 * in post creation.
 */
public class CameraFragment extends Fragment {

    private PreviewView previewView;

    private ImageCapture imageCapture;
    private Executor cameraExecutor;

    public CameraFragment() {}

    /**
     * Inflates the camera screen layout
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return inflated camera view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    /**
     * Initializes UI components, executes camera callbacks, and deals with photo capturing
     * logic.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // UI Components
        previewView = view.findViewById(R.id.previewView);
        ImageButton btnCapture = view.findViewById(R.id.btnCapture);
        ImageButton btnCancel = view.findViewById(R.id.btnCancel);

        cameraExecutor = ContextCompat.getMainExecutor(requireContext());

        // Take photos with camera.
        startCamera();

        // Buttons for exiting the fragment and taking a photo.
        btnCapture.setOnClickListener(v -> takePhoto());
        btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    /**
     * Initializes the camera preview and image capture.
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Setup image capture
                imageCapture = new ImageCapture.Builder().build();

                // Use the back camera by default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        getViewLifecycleOwner(),
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (Exception e) {
                Log.e("CameraFragment", "Camera failed", e);
            }
        }, cameraExecutor);
    }

    /**
     * Captures a photo, uploads it, and returns the uploaded image URL
     * to the create post fragment.
     */
    private void takePhoto() {
        if (imageCapture == null) return;
        // Image capture is ready

        // Create a temporary file for the captured image
        File photoFile = new File(requireContext().getExternalFilesDir(null),
                System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Capture image and save it to the temporary file
        imageCapture.takePicture(outputOptions, cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        Uri imageUri = Uri.fromFile(photoFile);

                        // Upload the image
                        MediaRepository mediaRepository = new MediaRepository();
                        mediaRepository.uploadImage(imageUri,
                                new MediaRepository.MediaUploadCallback() {
                            @Override
                            public void onSuccess(String imageUrl) {
                                if (!isAdded()) {
                                    return;
                                }

                                // Send final URL back to CreatePostFragment
                                Bundle result = new Bundle();
                                result.putString("imageUrl", imageUrl);

                                getParentFragmentManager()
                                        .setFragmentResult("cameraResult", result);

                                Toast.makeText(getContext(),
                                        "Image uploaded", Toast.LENGTH_SHORT).show();

                                // Close CameraFragment
                                NavHostFragment.findNavController(CameraFragment.this).popBackStack();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(requireContext(),
                                        "Upload failed, try again", Toast.LENGTH_SHORT).show();

                                Log.e("CameraFragment", "Upload failed", e);
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(requireContext(),
                                "Capture failed", Toast.LENGTH_SHORT).show();

                        Log.e("CameraFragment", "Capture failed", exception);
                    }
                });
    }
}
package com.example.eduview.data.repository;

import android.net.Uri;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;

/**
 * This repository is responsible only for image upload.
 *
 * It:
 * 1. takes a local image Uri from the app
 * 2. uploads that image to Cloudinary
 * 3. returns the hosted image URL back to the caller
 *
 */
public class MediaRepository {

    /**
     * This is our own simpler callback that the ViewModel or Fragment can use.
     * On success a URL should be returned, linked to cloudinary images.
     */
    public interface MediaUploadCallback {
        void onSuccess(String imageUrl);
        void onError(Exception e);
    }

    /**
     * Uploads a local image to Cloudinary.
     *
     * imageUri:
     * - this is the local image location on the phone returned via CameraX
     * callback:
     * - this is how we send the result back after the upload finishes, since uploads asynchronous.
     */
    public void uploadImage(Uri imageUri, MediaUploadCallback callback) {

        MediaManager.get().upload(imageUri)
                // This tells Cloudinary to use the unsigned upload preset
                // you created in the Cloudinary dashboard.
                .unsigned("eduview_posts")

                // Optional: puts uploaded images into a folder called "posts"
                // in your Cloudinary media library to keep things organized.
                .option("folder", "posts")

                // Cloudinary requires its own UploadCallback here.
                // We use it internally, then translate its result into our own simpler callback.
                .callback(new UploadCallback() {

                    /**
                     * Called when the upload has officially started.
                     */
                    @Override
                    public void onStart(String requestId) {
                        // Nothing needed for now
                    }

                    /**
                     * Called repeatedly while the image is uploading.
                     */
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Nothing needed for now
                    }

                    /**
                     * Called when Cloudinary finishes the upload successfully.
                     *
                     * resultData contains information about the uploaded image,
                     * including the final hosted URL.
                     *
                     * "secure_url" is the HTTPS URL you want to store in Firebase
                     * inside your Post object.
                     */
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Object secureUrl = resultData.get("secure_url");

                        if (secureUrl != null) {
                            String imageUrl = secureUrl.toString();
                            callback.onSuccess(imageUrl);
                        } else {
                            callback.onError(new Exception("Upload succeeded, but no image URL was returned."));
                        }
                    }

                    /**
                     * Called if the upload fails.
                     *
                     * Cloudinary gives an ErrorInfo object describing the problem.
                     * We wrap that into a normal Exception and pass it upward,
                     * so the rest of the app can handle it more easily.
                     */
                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        callback.onError(new Exception(error.getDescription()));
                    }

                    /**
                     * Called if Cloudinary delays or reschedules the upload.
                     *
                     * For your project, the easiest thing is to treat this as an error.
                     * That keeps the logic simple.
                     *
                     * Later, if you want, you could handle this more gracefully.
                     */
                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        callback.onError(
                                new Exception("Upload was rescheduled: " + error.getDescription())
                        );
                    }
                })

                // Actually starts the upload request.
                .dispatch();
    }
}
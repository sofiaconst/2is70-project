package com.example.eduview;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Application class for EduView.
 * Initializes global services like Cloudinary for image uploading.
 */
public class EduViewApp extends Application {

    /**
     * Configures and initializes the Cloudinary MediaManager.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // Configuration of MediaManager.
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "db54akutj");
        config.put("secure", "true");

        // Initialization of MediaManager.
        MediaManager.init(this, config);
    }
}
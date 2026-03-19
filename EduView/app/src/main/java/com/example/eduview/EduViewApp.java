package com.example.eduview;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class EduViewApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "db54akutj");
        config.put("secure", "true");

        MediaManager.init(this, config);
    }
}
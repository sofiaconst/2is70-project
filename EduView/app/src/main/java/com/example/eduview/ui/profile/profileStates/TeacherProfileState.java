package com.example.eduview.ui.profile.profileStates;

import android.graphics.Bitmap;

public class TeacherProfileState {


    private final String className;
    private final Bitmap qrBitmap;
    private final String errorMessage;

    public TeacherProfileState(
            String className,
            Bitmap qrBitmap,
            String errorMessage
    ) {
        this.className = className;
        this.qrBitmap = qrBitmap;
        this.errorMessage = errorMessage;
    }

    public static TeacherProfileState loading() {
        return new TeacherProfileState("Loading...", null, null);
    }


    public static TeacherProfileState error(String message) {
        return new TeacherProfileState(null, null, message);
    }

    public static TeacherProfileState success(String className, Bitmap code) {
        return new TeacherProfileState(className, code, null);
    }

    public Bitmap getQrBitmap() {
        return qrBitmap;
    }


    public String getClassName() {
        return className;
    }


    public String getErrorMessage() {
        return errorMessage;
    }

}
package com.example.eduview.ui.profile;

import android.graphics.Bitmap;

public class ProfileUIState {

    public final String displayName;
    public final String roleText;
    public final String classText;

    public final boolean showScanButton;
    public final boolean showGenerateButton;

    public final Bitmap qrBitmap;

    public ProfileUIState(
            String displayName,
            String roleText,
            String classText,
            boolean showScanButton,
            boolean showGenerateButton,
            Bitmap qrBitmap
    ) {
        this.displayName = displayName;
        this.roleText = roleText;
        this.classText = classText;
        this.showScanButton = showScanButton;
        this.showGenerateButton = showGenerateButton;
        this.qrBitmap = qrBitmap;
    }
}
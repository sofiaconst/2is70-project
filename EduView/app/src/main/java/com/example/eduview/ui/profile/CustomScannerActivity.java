package com.example.eduview.ui.profile;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eduview.R;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.camera.CenterCropStrategy;

/**
 * Activity for scanning QR codes using the ZXing barcode scanner.
 */
public class CustomScannerActivity extends AppCompatActivity {

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;

    /**
     * Initializes the QR scanner view and sets up the custom back button.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_scanner);

        // Finds the barcode scanner view from the ID.
        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.getBarcodeView().setPreviewScalingStrategy(new CenterCropStrategy());

        // Takes the QR Code and decodes it.
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.setShowMissingCameraPermissionDialog(false);
        capture.decode();

        // Creates a listener for the button with the corresponding button.
        ImageButton btnBack = findViewById(R.id.btn_scanner_back);
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Resumes the scanner when the activity is in the foreground.
     */
    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    /**
     * Pauses the scanner when the activity is not in the foreground.
     */
    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    /**
     * Cleans up scanner when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    /**
     * Saves the scanner state to handle rotation..
     *
     * @param outState bundle where the state is stored
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }
}
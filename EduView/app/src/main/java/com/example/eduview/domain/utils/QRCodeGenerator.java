package com.example.eduview.domain.utils;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

/**
 * Generates a QR Code for the teacher so the student can join the class using it.
 */
public class QRCodeGenerator {

    /**
     * Generates a QR code bitmap from the given text.
     *
     * @param text the content to encode into the QR code
     * @return a Bitmap representing the generated QR code
     */
    public static Bitmap generate(String text) {

        try {
            // Encode the input text into a QR code matrix
            BitMatrix matrix = new MultiFormatWriter().encode(
                    text,
                    BarcodeFormat.QR_CODE,
                    500,
                    500
            );

            // Convert the BitMatrix into a Bitmap image
            BarcodeEncoder encoder = new BarcodeEncoder();

            return encoder.createBitmap(matrix);

        } catch (WriterException e) {
            // Return null if QR generation fails
            return null;
        }
    }
}
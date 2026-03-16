package com.example.eduview.domain.utils;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeGenerator {

    public static Bitmap generate(String text) {

        try {

            BitMatrix matrix = new MultiFormatWriter().encode(
                    text,
                    BarcodeFormat.QR_CODE,
                    500,
                    500
            );

            BarcodeEncoder encoder = new BarcodeEncoder();

            return encoder.createBitmap(matrix);

        } catch (WriterException e) {
            return null;
        }
    }
}
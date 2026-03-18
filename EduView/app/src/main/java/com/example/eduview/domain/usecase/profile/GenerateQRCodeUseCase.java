package com.example.eduview.domain.usecase.profile;

import android.graphics.Bitmap;

import com.example.eduview.domain.utils.QRCodeGenerator;

public class GenerateQRCodeUseCase {

    public Bitmap execute(String text) {
        return QRCodeGenerator.generate(text);
    }
}
package com.example.eduview;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Announcements extends Fragment {
    public Announcements() {
    }

    @Override
    public android.view.View onCreateView(
            android.view.LayoutInflater inflater,
            android.view.ViewGroup container,
            android.os.Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_announcements, container, false);
    }
}
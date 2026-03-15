package com.example.eduview.ui.feed;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eduview.R;

public class Pending extends Fragment {
    public Pending() { }

    @Override
    public android.view.View onCreateView(
            android.view.LayoutInflater inflater,
            android.view.ViewGroup container,
            android.os.Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_pending, container, false);
    }
}
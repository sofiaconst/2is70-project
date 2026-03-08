package com.example.eduview;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ParentSignupFragment extends Fragment {
    private int counter = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_parent_signup,
                container, false);

        Button minus = view.findViewById(R.id.btn_minus);
        Button plus = view.findViewById(R.id.btn_plus);
        TextView counterText = view.findViewById(R.id.tv_counter);

        counterText.setText(String.valueOf(counter));

        minus.setOnClickListener(v -> {
            if (counter > 1) {
                counter--;
                counterText.setText(String.valueOf(counter));
            }
            });

        plus.setOnClickListener(v -> {
            if (counter < 5) {
                counter++;
                counterText.setText(String.valueOf(counter));
            }
            });

        return view;
    }
}
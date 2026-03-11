package com.example.eduview.ui.signup;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.eduview.R;

public class TeacherSignupFragment extends Fragment {
    private EditText etClassName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_signup, container, false);
        etClassName = view.findViewById(R.id.et_class_name);
        return view;
    }

    public String getClassName() {
        if (etClassName != null) {
            return etClassName.getText().toString().trim();
        }
        return "";
    }
}
package com.example.eduview;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class ParentSignupFragment extends Fragment {
    private int counter = 1;
    private EditText etChildFirstName;
    private EditText etChildLastName;
    private TextView counterText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_signup, container, false);

        Button minus = view.findViewById(R.id.btn_minus);
        Button plus = view.findViewById(R.id.btn_plus);
        counterText = view.findViewById(R.id.tv_counter);
        etChildFirstName = view.findViewById(R.id.et_child_first_name);
        etChildLastName = view.findViewById(R.id.et_child_last_name);

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

    public List<String> getChildIds() {
        List<String> childIds = new ArrayList<>();
        if (etChildFirstName != null && etChildLastName != null) {
            String firstName = etChildFirstName.getText().toString().trim();
            String lastName = etChildLastName.getText().toString().trim();
            if (!firstName.isEmpty() && !lastName.isEmpty()) {
                // For now, using names as IDs as specified in your example (student_1, etc.)
                // In a real app, these would be unique IDs from the database.
                childIds.add(firstName + "_" + lastName);
            }
        }
        return childIds;
    }
}
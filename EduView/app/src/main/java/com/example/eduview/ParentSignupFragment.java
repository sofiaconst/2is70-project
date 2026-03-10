package com.example.eduview;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ParentSignupFragment extends Fragment {
    private int counter = 1;
    private List<View> formList = new ArrayList<>();

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
        addForm(view);

        minus.setOnClickListener(v -> {
            if (counter > 1) {
                counter--;
                counterText.setText(String.valueOf(counter));
                removeForm(view);
            }
        });

        plus.setOnClickListener(v -> {
            if (counter < 5) {
                counter++;
                counterText.setText(String.valueOf(counter));
                addForm(view);
            }
        });

        return view;
    }

    private void addForm(View view) {
        LinearLayout formContainer = view.findViewById(R.id.formContainer);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View form = inflater.inflate(R.layout.form_child, formContainer, false);

        TextView childNo = form.findViewById(R.id.tv_child_no);
        childNo.setText(String.valueOf(counter));

        formContainer.addView(form);
        formList.add(form);
    }

    private void removeForm(View view) {
        LinearLayout formContainer = view.findViewById(R.id.formContainer);

        int formCount = formContainer.getChildCount();

        if (formCount > 1) {
            formContainer.removeViewAt(formCount - 1);
            formList.remove(formCount - 1);
        }
    }

    public List<String> getChildIds() {
        List<String> childIds = new ArrayList<>();
        for (View form : formList) {
            EditText etChildFirstName = form.findViewById(R.id.et_child_first_name);
            EditText etChildLastName = form.findViewById(R.id.et_child_last_name);
            if (etChildFirstName != null && etChildLastName != null) {
                String firstName = etChildFirstName.getText().toString().trim();
                String lastName = etChildLastName.getText().toString().trim();
                if (!firstName.isEmpty() && !lastName.isEmpty()) {
                    // For now, using names as IDs as specified in your example (student_1, etc.)
                    // In a real app, these would be unique IDs from the database.
                    childIds.add(firstName + "_" + lastName);
                }
            }
        }
        return childIds;
    }
}

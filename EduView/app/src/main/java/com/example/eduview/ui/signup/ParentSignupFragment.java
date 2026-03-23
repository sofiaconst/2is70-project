package com.example.eduview.ui.signup;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eduview.AuthService;
import com.example.eduview.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParentSignupFragment extends Fragment {
    public int getCounter() {
        return counter;
    }

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

    public List<AuthService.ChildInfo> getChildrenInfo() {
        List<AuthService.ChildInfo> children = new ArrayList<>();
        Set<String> usernames = new HashSet<>();
        boolean allValid = true;
        
        for (int i = 0; i < formList.size(); i++) {
            View form = formList.get(i);
            EditText etChildFirstName = form.findViewById(R.id.et_child_first_name);
            EditText etChildLastName = form.findViewById(R.id.et_child_last_name);
            EditText etChildUsername = form.findViewById(R.id.et_child_username);
            
            if (etChildFirstName != null && etChildLastName != null && etChildUsername != null) {
                String firstName = etChildFirstName.getText().toString().trim();
                String lastName = etChildLastName.getText().toString().trim();
                String username = etChildUsername.getText().toString().trim();
                
                // Reset background/color to normal state
                etChildUsername.setBackgroundResource(R.drawable.bg_input_rounded);

                if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty()) {
                    allValid = false;
                    continue; 
                }

                if (username.contains("@") || username.contains(" ")) {
                    etChildUsername.setBackgroundResource(R.drawable.bg_input_error); // Assuming you have an error drawable, or use a color filter
                    Toast.makeText(getContext(), "Username must not contain space characters nor @ symbols", Toast.LENGTH_SHORT).show();
                    allValid = false;
                    continue;
                }

                if (usernames.contains(username.toLowerCase())) {
                    etChildUsername.setBackgroundResource(R.drawable.bg_input_error);
                    Toast.makeText(getContext(), "Duplicate username within form: " + username, Toast.LENGTH_SHORT).show();
                    allValid = false;
                    continue;
                }
                usernames.add(username.toLowerCase());
                
                String email = username.toLowerCase() + "@eduview.com";
                children.add(new AuthService.ChildInfo(firstName, lastName, email));
            }
        }
        return allValid ? children : null;
    }

    public void markUsernameError(String email) {
        String username = email.replace("@eduview.com", "");
        for (View form : formList) {
            EditText etChildUsername = form.findViewById(R.id.et_child_username);
            if (etChildUsername != null && etChildUsername.getText().toString().trim().equalsIgnoreCase(username)) {
                etChildUsername.setBackgroundResource(R.drawable.bg_input_error);
                return;
            }
        }
    }
}

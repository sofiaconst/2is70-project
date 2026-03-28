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

/**
 * Fragment for collecting child account information
 * during the parent sign up flow.
 * Allows adding and removing up to five child forms.
 */
public class ParentSignupFragment extends Fragment {

    /**
     * Returns the current number of child forms shown in the UI.
     *
     * @return number of child forms
     */
    public int getCounter() {
        return counter;
    }

    private int counter = 1;
    private List<View> formList = new ArrayList<>();

    /**
     * Inflates the parent sign up layout and initializes the child form controls.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return inflated fragment view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_parent_signup,
                container, false);

        Button minus = view.findViewById(R.id.btn_minus);
        Button plus = view.findViewById(R.id.btn_plus);
        TextView counterText = view.findViewById(R.id.tv_counter);

        // Initialize counter display and create the first child form
        counterText.setText(String.valueOf(counter));
        addForm(view);

        // Remove the last child form if above minimum
        minus.setOnClickListener(v -> {
            if (counter > 1) {
                counter--;
                counterText.setText(String.valueOf(counter));
                removeForm(view);
            }
        });

        // Add another child form if below maximum
        plus.setOnClickListener(v -> {
            if (counter < 5) {
                counter++;
                counterText.setText(String.valueOf(counter));
                addForm(view);
            }
        });

        return view;
    }

    /**
     * Adds a new child input form to the container.
     *
     * @param view root view containing the form container
     */
    private void addForm(View view) {
        LinearLayout formContainer = view.findViewById(R.id.formContainer);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View form = inflater.inflate(R.layout.form_child, formContainer, false);

        TextView childNo = form.findViewById(R.id.tv_child_no);
        childNo.setText(String.valueOf(counter));

        formContainer.addView(form);
        formList.add(form);
    }

    /**
     * Removes the last child input form from the container if more than one exists.
     *
     * @param view root view containing the form container
     */
    private void removeForm(View view) {
        LinearLayout formContainer = view.findViewById(R.id.formContainer);

        int formCount = formContainer.getChildCount();

        if (formCount > 1) {
            formContainer.removeViewAt(formCount - 1);
            formList.remove(formCount - 1);
        }
    }

    /**
     * Collects and validates all child form inputs.
     * Returns a list of child information if all fields are valid,
     * otherwise returns null.
     *
     * @return list of child info objects, or null if validation fails
     */
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
                
                // Reset background to normal state
                etChildFirstName.setBackgroundResource(R.drawable.bg_input_rounded);
                etChildLastName.setBackgroundResource(R.drawable.bg_input_rounded);
                etChildUsername.setBackgroundResource(R.drawable.bg_input_rounded);

                // Validate child information
                if (firstName.isEmpty()) {
                    etChildFirstName.setBackgroundResource(R.drawable.bg_input_error);
                    allValid = false;
                }
                if (lastName.isEmpty()) {
                    etChildLastName.setBackgroundResource(R.drawable.bg_input_error);
                    allValid = false;
                }
                if (username.isEmpty()) {
                    etChildUsername.setBackgroundResource(R.drawable.bg_input_error);
                    allValid = false;
                }

                if (!allValid) continue;

                // Validate username format
                if (username.contains("@") || username.contains(" ")) {
                    etChildUsername.setBackgroundResource(R.drawable.bg_input_error);
                    Toast.makeText(getContext(), "Username must not contain space characters nor @ symbols", Toast.LENGTH_SHORT).show();
                    allValid = false;
                    continue;
                }

                // Prevent duplicate usernames in the same form submission
                if (usernames.contains(username.toLowerCase())) {
                    etChildUsername.setBackgroundResource(R.drawable.bg_input_error);
                    Toast.makeText(getContext(), "Duplicate username within form: " + username, Toast.LENGTH_SHORT).show();
                    allValid = false;
                    continue;
                }
                usernames.add(username.toLowerCase());

                // Convert username to email format
                String email = username.toLowerCase() + "@eduview.com";
                children.add(new AuthService.ChildInfo(firstName, lastName, email));
            }
        }
        return allValid ? children : null;
    }

    /**
     * Marks the child username field matching the provided email as invalid.
     *
     * @param email child email whose username field should be highlighted
     */
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

    /**
     * Resets all child form input backgrounds to their default state.
     */
    public void resetFields() {
        for (View form : formList) {
            form.findViewById(R.id.et_child_first_name).setBackgroundResource(R.drawable.bg_input_rounded);
            form.findViewById(R.id.et_child_last_name).setBackgroundResource(R.drawable.bg_input_rounded);
            form.findViewById(R.id.et_child_username).setBackgroundResource(R.drawable.bg_input_rounded);
        }
    }
}

package com.example.eduview.ui.signup;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.eduview.R;

/**
 * Fragment for handling teacher signup input.
 * Allows entering and validating the classroom name.
 */
public class TeacherSignupFragment extends Fragment {

    private EditText etClassName;

    /**
     * Inflates the teacher signup layout and initializes views.
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
        View view = inflater.inflate(R.layout.fragment_teacher_signup, container, false);

        // Initialize classroom name input field
        etClassName = view.findViewById(R.id.et_class_name);

        return view;
    }

    /**
     * Retrieves the entered classroom name.
     *
     * @return classroom name, or empty string if not available
     */
    public String getClassName() {
        if (etClassName != null) {
            return etClassName.getText().toString().trim();
        }
        return "";
    }

    /**
     * Marks the classroom name input field as invalid.
     */
    public void markClassNameError() {
        if (etClassName != null) {
            // Highlight input field with error background
            etClassName.setBackgroundResource(R.drawable.bg_input_error);
        }
    }

    /**
     * Resets the classroom name input field to its default state.
     */
    public void resetFields() {
        if (etClassName != null) {
            // Restore normal background styling
            etClassName.setBackgroundResource(R.drawable.bg_input_rounded);
        }
    }
}
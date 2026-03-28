package com.example.eduview.ui.signup;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.eduview.AuthService;
import com.example.eduview.R;
import com.example.eduview.ui.login.LoginActivity;

import java.util.List;

/**
 * Activity for handling user sign-up for parent and teacher roles.
 * Manages role-specific fragments, validates input, and handles a level of account creation.
 */
public class SignupActivity extends AppCompatActivity {

    /**
     * Updates the password strength indicator based on password quality checks.
     *
     * @param password entered password
     * @param tvPasswordStrength text view displaying strength label
     * @param progressPasswordStrength progress bar visualizing strength
     */
    private void updatePasswordStrength(String password, TextView tvPasswordStrength, ProgressBar progressPasswordStrength) {
        // Password requirements for strength check

        // Password should have minimum length of 6
        boolean hasMinLength = password.length() >= 6;
        // Password should have an uppercase letter
        boolean hasUppercase = password.matches(".*[A-Z].*");
        // Password should have a lowercase letter
        boolean hasLowercase = password.matches(".*[a-z].*");
        // Password should have a digit
        boolean hasDigit = password.matches(".*\\d.*");
        // Password should have a symbol
        boolean hasSymbol = password.matches(".*[^A-Za-z0-9].*");

        // Adds up a score according to requirements
        int score = 0;
        if (hasMinLength) score++;
        if (hasUppercase) score++;
        if (hasDigit) score++;
        if (hasSymbol) score++;
        if (hasLowercase) score++;

        // Updates text and progress bar according to whether requirements are met.
        if (password.isEmpty()) {
            tvPasswordStrength.setText("Password strength");
            tvPasswordStrength.setTextColor(Color.parseColor("#666666"));
            progressPasswordStrength.setProgress(0, true);
            progressPasswordStrength.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#BDBDBD")));
        } else if (score <= 2) {
            // If password score is at most 2 then the password is weak
            tvPasswordStrength.setText("Password strength: Weak");
            tvPasswordStrength.setTextColor(Color.parseColor("#D32F2F"));
            progressPasswordStrength.setProgress(25, true);
            progressPasswordStrength.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#D32F2F")));
        } else if (score <= 4) {
            // If password score is at least 2 and at most 4 then the password has a medium strength
            tvPasswordStrength.setText("Password strength: Medium");
            tvPasswordStrength.setTextColor(Color.parseColor("#F9A825"));
            progressPasswordStrength.setProgress(75, true);
            progressPasswordStrength.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#F9A825")));
        } else {
            // If password score is the maximum then the password is strong
            tvPasswordStrength.setText("Password strength: Strong");
            tvPasswordStrength.setTextColor(Color.parseColor("#2E7D32"));
            progressPasswordStrength.setProgress(100, true);
            progressPasswordStrength.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#2E7D32")));
        }
    }

    /**
     * Builds a message describing which password requirements are still missing.
     *
     * @param password entered password
     * @return message listing unmet password requirements
     */
    private String getPasswordRequirementsMessage(String password) {
        StringBuilder message = new StringBuilder("Password must contain:");

        // Length check
        if (password.length() < 6) {
            message.append("\n- At least 6 characters");
        }
        // Uppercase check
        if (!password.matches(".*[A-Z].*")) {
            message.append("\n- At least one capital letter");
        }
        // Lowercase check
        if (!password.matches(".*[a-z].*")) {
            message.append("\n- At least one lowercase letter");
        }
        // Digit check
        if (!password.matches(".*\\d.*")) {
            message.append("\n- At least one number");
        }
        // Symbol check
        if (!password.matches(".*[^A-Za-z0-9].*")) {
            message.append("\n- At least one symbol");
        }

        return message.toString();
    }

    /**
     * Checks whether the entered password satisfies all required conditions.
     *
     * @param password entered password
     * @return true if the password is valid, false otherwise
     */
    private boolean isPasswordValid(String password) {
        // Password requirements
        boolean hasMinLength = password.length() >= 6;
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[^A-Za-z0-9].*");

        // Return whether password is valid
        return hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSymbol;
    }

    /**
     * Initialize UI components, layout elements, password feedback, AuthService and button logic
     * on creation of the activity.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SignupActivity", "onCreate: SignupActivity started");
        try {
            // Enable edge to edge layout and padding to the system bar
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_signup);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } catch (Exception e) {
            Log.e("SignupActivity", "Error during initialization", e);
        }

        // Initialize buttons for different sign up fragments
        Button buttonTeacher = findViewById(R.id.btn_role_teacher);
        Button buttonParent = findViewById(R.id.btn_role_parent);

        // Initialize and find UI elements from the sign up xml
        btnSignUp = findViewById(R.id.btn_signup);
        EditText etFirstName = findViewById(R.id.et_first_name);
        EditText etLastName = findViewById(R.id.et_last_name);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        TextView tvPasswordError = findViewById(R.id.tv_password_error);
        TextView tvPasswordStrength = findViewById(R.id.tv_password_strength);
        ProgressBar progressPasswordStrength = findViewById(R.id.progress_password_strength);
        TextView tvLogin = findViewById(R.id.tv_login);

        // Listens for any change in the password (i.e., when the user is typing)
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString(), tvPasswordStrength, progressPasswordStrength);
                tvPasswordError.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Initialize AuthService
        AuthService authService = new AuthService();

        // Add role tracking
        final String[] selectedRole = {null};
        selectedRole[0] = "Parent";

        // Switch to teacher sign up form
        buttonTeacher.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new TeacherSignupFragment()).commit();
            selectedRole[0] = "Teacher";
        });

        // Switch to parent sign up form
        buttonParent.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ParentSignupFragment()).commit();
            selectedRole[0] = "Parent";
        });

        // Default to parent sign up form on first creation
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ParentSignupFragment()).commit();
        }

        // Handle for sign up button
        btnSignUp.setOnClickListener(v -> {
            if (selectedRole[0] == null) {
                Toast.makeText(this, "Please select either Parent or Teacher role", Toast.LENGTH_SHORT).show();
                return;
            }

            // Initialize components of the activity view
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Reset backgrounds
            etFirstName.setBackgroundResource(R.drawable.bg_input_rounded);
            etLastName.setBackgroundResource(R.drawable.bg_input_rounded);
            etEmail.setBackgroundResource(R.drawable.bg_input_rounded);
            etPassword.setBackgroundResource(R.drawable.bg_input_rounded);

            // Check what fragment is in use
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (currentFragment instanceof ParentSignupFragment) {
                ((ParentSignupFragment) currentFragment).resetFields();
            } else if (currentFragment instanceof TeacherSignupFragment) {
                ((TeacherSignupFragment) currentFragment).resetFields();
            }

            boolean hasError = false;

            // Validate first and last name
            if (firstName.isEmpty()) {
                etFirstName.setBackgroundResource(R.drawable.bg_input_error);
                hasError = true;
            }
            if (lastName.isEmpty()) {
                etLastName.setBackgroundResource(R.drawable.bg_input_error);
                hasError = true;
            }
            // Validate email and check if it has an "@"
            if (email.isEmpty()) {
                etEmail.setBackgroundResource(R.drawable.bg_input_error);
                hasError = true;
            } else if (!email.contains("@")) {
                etEmail.setBackgroundResource(R.drawable.bg_input_error);
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                hasError = true;
            }
            // Validate password
            if (password.isEmpty()) {
                etPassword.setBackgroundResource(R.drawable.bg_input_error);
                hasError = true;
            } else if (!isPasswordValid(password)) {
                etPassword.setBackgroundResource(R.drawable.bg_input_error);
                tvPasswordError.setText(getPasswordRequirementsMessage(password));
                tvPasswordError.setVisibility(View.VISIBLE);
                hasError = true;
            } else {
                // If password exists and is valid then no error
                tvPasswordError.setVisibility(View.GONE);
            }

            // Error check from validations
            if (hasError) {
                Toast.makeText(this, "Please check the highlighted fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button to prevent double clicks
            btnSignUp.setEnabled(false);

            // Callback result for authentication result
            AuthService.AuthCallback callback = new AuthService.AuthCallback() {
                /* Return "successful" message and return to the login activity if authentication
                    successful */
                @Override
                public void onSuccess() {
                    btnSignUp.setEnabled(true);
                    Toast.makeText(SignupActivity.this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finish();
                }

                /* Return specific error message if authentication
                    failed */
                @Override
                public void onFailure(Exception e) {
                    btnSignUp.setEnabled(true);
                    String errorMsg = e.getMessage();
                    // Handle username/email conflict errors
                    if (errorMsg != null && (errorMsg.contains("Username already exists") || errorMsg.contains("Username already taken"))) {
                        if (errorMsg.contains(email)) {
                            etEmail.setBackgroundResource(R.drawable.bg_input_error);
                            if ("Parent".equals(selectedRole[0])) {
                                Toast.makeText(SignupActivity.this, "Parent's email already taken", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignupActivity.this, "Username already taken", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // May belong to a child username in parent sign up
                             if ("Parent".equals(selectedRole[0])) {
                                 Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                                 if (fragment instanceof ParentSignupFragment) {
                                     String[] parts = errorMsg.split(": ");
                                     if (parts.length > 1) {
                                         String failedEmail = parts[1].trim();
                                         ((ParentSignupFragment) fragment).markUsernameError(failedEmail);
                                     }
                                 }
                             }
                             Toast.makeText(SignupActivity.this, "Username already taken", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignupActivity.this, "Sign Up Failed: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }
            };
            // Sign up according to role
            if ("Teacher".equals(selectedRole[0])) {
                if (currentFragment instanceof TeacherSignupFragment) {
                    String className = ((TeacherSignupFragment) currentFragment).getClassName();
                    // Validate class name of Teacher while signing up
                    if (className.isEmpty()) {
                        btnSignUp.setEnabled(true);
                        ((TeacherSignupFragment) currentFragment).markClassNameError();
                        Toast.makeText(this, "Please enter classroom name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    authService.signUpTeacher(firstName, lastName, email, password, className, callback);
                }
            } else if ("Parent".equals(selectedRole[0])) {
                if (currentFragment instanceof ParentSignupFragment) {
                    List<AuthService.ChildInfo> childrenInfo = ((ParentSignupFragment) currentFragment).getChildrenInfo();
                    // Validate children information of parent while signing up
                    if (childrenInfo == null) {
                        btnSignUp.setEnabled(true);
                        Toast.makeText(this, "Please check children's information", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Validate whether children information is entered for all children
                    if (childrenInfo.size() != ((ParentSignupFragment) currentFragment).getCounter()) {
                        btnSignUp.setEnabled(true);
                        Toast.makeText(this, "Please provide information for all children", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Check if any child username matches the parent username
                    for (AuthService.ChildInfo child : childrenInfo) {
                        if (child.email.equalsIgnoreCase(email)) {
                            btnSignUp.setEnabled(true);
                            ((ParentSignupFragment) currentFragment).markUsernameError(child.email);
                            Toast.makeText(this, "Child username cannot be the same as parent email", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // Sign up parent in the database through authentication service
                    authService.signUpParent(firstName, lastName, email, password, childrenInfo, callback);
                }
            }
        });

        // Return back to login screen
        tvLogin.setOnClickListener(v -> {
            authService.signOut(); // Ensure user is signed out when navigating back
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
    private Button btnSignUp;
}

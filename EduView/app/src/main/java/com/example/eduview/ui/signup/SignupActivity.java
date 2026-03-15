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

import com.example.eduview.AuthRepository;
import com.example.eduview.R;
import com.example.eduview.ui.login.LoginActivity;

import java.util.List;

public class SignupActivity extends AppCompatActivity {

    private void updatePasswordStrength(String password, TextView tvPasswordStrength, ProgressBar progressPasswordStrength) {
        boolean hasMinLength = password.length() >= 6;
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[^A-Za-z0-9].*");

        int score = 0;
        if (hasMinLength) score++;
        if (hasUppercase) score++;
        if (hasDigit) score++;
        if (hasSymbol) score++;
        if (hasLowercase) score++;

        if (password.isEmpty()) {
            tvPasswordStrength.setText("Password strength");
            tvPasswordStrength.setTextColor(Color.parseColor("#666666"));
            progressPasswordStrength.setProgress(0, true);
            progressPasswordStrength.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#BDBDBD")));
        } else if (score <= 2) {
            tvPasswordStrength.setText("Password strength: Weak");
            tvPasswordStrength.setTextColor(Color.parseColor("#D32F2F"));
            progressPasswordStrength.setProgress(25, true);
            progressPasswordStrength.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#D32F2F")));
        } else if (score <= 4) {
            tvPasswordStrength.setText("Password strength: Medium");
            tvPasswordStrength.setTextColor(Color.parseColor("#F9A825"));
            progressPasswordStrength.setProgress(75, true);
            progressPasswordStrength.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#F9A825")));
        } else {
            tvPasswordStrength.setText("Password strength: Strong");
            tvPasswordStrength.setTextColor(Color.parseColor("#2E7D32"));
            progressPasswordStrength.setProgress(100, true);
            progressPasswordStrength.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#2E7D32")));
        }
    }

    private String getPasswordRequirementsMessage(String password) {
        StringBuilder message = new StringBuilder("Password must contain:");

        if (password.length() < 6) {
            message.append("\n- At least 6 characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            message.append("\n- At least one capital letter");
        }
        if (!password.matches(".*[a-z].*")) {
            message.append("\n- At least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            message.append("\n- At least one number");
        }
        if (!password.matches(".*[^A-Za-z0-9].*")) {
            message.append("\n- At least one symbol");
        }

        return message.toString();
    }

    private boolean isPasswordValid(String password) {
        boolean hasMinLength = password.length() >= 6;
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[^A-Za-z0-9].*");

        return hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSymbol;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SignupActivity", "onCreate: SignupActivity started");
        try {
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

        // Initialize UI elements
        Button buttonTeacher = findViewById(R.id.btn_role_teacher);
        Button buttonParent = findViewById(R.id.btn_role_parent);

        Button btnSignUp = findViewById(R.id.btn_signup);
        EditText etFirstName = findViewById(R.id.et_first_name);
        EditText etLastName = findViewById(R.id.et_last_name);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        TextView tvPasswordError = findViewById(R.id.tv_password_error);
        TextView tvPasswordStrength = findViewById(R.id.tv_password_strength);
        ProgressBar progressPasswordStrength = findViewById(R.id.progress_password_strength);
        TextView tvLogin = findViewById(R.id.tv_login);

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

        // Initialize AuthRepository
        AuthRepository authRepository = new AuthRepository();

        // Add role tracking
        final String[] selectedRole = {null};
        selectedRole[0] = "Parent";

        buttonTeacher.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new TeacherSignupFragment()).commit();
            selectedRole[0] = "Teacher";
        });

        buttonParent.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ParentSignupFragment()).commit();
            selectedRole[0] = "Parent";
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ParentSignupFragment()).commit();
        }

        btnSignUp.setOnClickListener(v -> {
            if (selectedRole[0] == null) {
                Toast.makeText(this, "Please select either Parent or Teacher role", Toast.LENGTH_SHORT).show();
                return;
            }

            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isPasswordValid(password)) {
                tvPasswordError.setText(getPasswordRequirementsMessage(password));
                tvPasswordError.setVisibility(View.VISIBLE);
                return;
            } else {
                tvPasswordError.setVisibility(View.GONE);
            }

            AuthRepository.AuthCallback callback = new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(SignupActivity.this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(SignupActivity.this, "Sign Up Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };

            if ("Teacher".equals(selectedRole[0])) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                if (fragment instanceof TeacherSignupFragment) {
                    String className = ((TeacherSignupFragment) fragment).getClassName();
                    if (className.isEmpty()) {
                        Toast.makeText(this, "Please enter classroom name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    authRepository.signUpTeacher(firstName, lastName, email, password, className, callback);
                }
            } else if ("Parent".equals(selectedRole[0])) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                if (fragment instanceof ParentSignupFragment) {
                    List<AuthRepository.ChildInfo> childrenInfo = ((ParentSignupFragment) fragment).getChildrenInfo();
                    if (childrenInfo.size() != ((ParentSignupFragment) fragment).getCounter()) {
                        Toast.makeText(this, "Please provide children information", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    authRepository.signUpParent(firstName, lastName, email, password, childrenInfo, callback);
                }
            }
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
}
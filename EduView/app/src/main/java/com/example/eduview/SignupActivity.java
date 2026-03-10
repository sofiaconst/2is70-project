package com.example.eduview;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class SignupActivity extends AppCompatActivity {

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
        TextView tvLogin = findViewById(R.id.tv_login);

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
                    List<String> childIds = ((ParentSignupFragment) fragment).getChildIds();
                    if (childIds.size() != ((ParentSignupFragment) fragment).getCounter()) {
                        Toast.makeText(this, "Please provide children information", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    authRepository.signUpParent(firstName, lastName, email, password, childIds, callback);
                }
            }
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
}
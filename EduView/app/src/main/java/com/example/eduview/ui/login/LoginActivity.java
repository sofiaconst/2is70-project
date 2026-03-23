package com.example.eduview.ui.login;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eduview.R;
import com.example.eduview.data.repository.AuthRepository;
import com.example.eduview.ui.signup.SignupActivity;
import com.example.eduview.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private AuthRepository authRepository;

    private EditText etUsername;
    private EditText etPassword;
    private TextView tvError;
    private Button btnLogin;
    private TextView tvSignup;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating LoginActivity...");

        authRepository = new AuthRepository();

        if (authRepository.getCurrentUser() != null) {
            Log.d(TAG, "Existing session detected, skipping login screen");
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setupLayout();
        initViews();
        setupListeners();

        Log.d(TAG, "LoginActivity started");
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvSignup.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to SignupActivity");
            startActivity(new Intent(this, SignupActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> openResetDialog());
    }

    public void attemptLogin() {
        String input = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d(TAG, "Login button clicked");

        if (input.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        // Check if the input is a username (no @) and append @eduview.com if it is
        String email = input;
        if (!input.contains("@")) {
            email = input + "@eduview.com";
        }

        authRepository.login(email, password)
                .addOnSuccessListener(authResult -> {

                    FirebaseUser user = authResult.getUser();

                    if (user == null) {
                        showError("Authentication error.");
                        return;
                    }

                    Log.d(TAG, "Login successful for user: " + user.getUid());

                    tvError.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {

                    Log.e(TAG, "Login failed", e);
                    showError("Invalid credentials.");

                });
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void openResetDialog() {

        Log.d(TAG, "Opening password reset dialog");

        EditText resetEmailInput = new EditText(this);
        resetEmailInput.setHint("Enter your email or username");
        resetEmailInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );

        // Prefill with current email/username if available
        String currentInput = etUsername.getText().toString().trim();
        if (!currentInput.isEmpty()) {
            resetEmailInput.setText(currentInput);
        }

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding / 2, padding, 0);

        container.addView(resetEmailInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Reset password")
                .setMessage("Enter your email or username to receive a password reset link.")
                .setView(container)
                .setPositiveButton("Send", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            String input = resetEmailInput.getText().toString().trim();

            if (input.isEmpty()) {
                resetEmailInput.setError("Please enter your email or username.");
                return;
            }

            String email = input;
            if (!input.contains("@")) {
                email = input + "@eduview.com";
            }

            if (!isValidEmail(email)) {
                resetEmailInput.setError("Enter a valid email address or username.");
                return;
            }

            v.setEnabled(false); // prevent multiple clicks

            sendPasswordReset(email, resetEmailInput, dialog);
        });
    }

    private boolean isValidEmail(String email) {
        return !email.isEmpty() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void sendPasswordReset(String email, EditText input, AlertDialog dialog) {

        Log.d(TAG, "Requesting password reset for: " + email);

        authRepository.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {

                    Log.d(TAG, "Password reset email sent");

                    Toast.makeText(
                            LoginActivity.this,
                            "If an account exists, a reset link has been sent.",
                            Toast.LENGTH_LONG
                    ).show();

                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {

                    Log.e(TAG, "Password reset failed", e);

                    input.setError("Failed to send reset email.");
                });
    }

    private void setupLayout() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        tvError = findViewById(R.id.tv_error);
        btnLogin = findViewById(R.id.btn_login);
        tvSignup = findViewById(R.id.tv_signup);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
    }
}

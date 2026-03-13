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
import com.example.eduview.data.repository.AuthCallback;
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

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authRepository = new AuthRepository();

        if (authRepository.getCurrentUser() != null) {
            Log.d(TAG, "Existing session detected, skipping login screen");
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setupLayout();
        Log.d(TAG, "LoginActivity started");

        initViews();
        setupListeners();
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

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvSignup.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to SignupActivity");
            startActivity(new Intent(this, SignupActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> openResetDialog());
    }

    /*
    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    private void attemptLogin() {
        String email = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d(TAG, "Login button clicked");

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        authRepository.login(email, password, new AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Log.d(TAG, "Login successful for user: " + user.getUid());
                runOnUiThread(() -> {
                    tvError.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Login failed: " + errorMessage);
                runOnUiThread(() -> showError(errorMessage));
            }
        });
    }

     */

    public void attemptLogin() {
        String email = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d(TAG, "Login button clicked");

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
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

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    private void openResetDialog() {
        Log.d(TAG, "Opening password reset dialog");

        EditText resetEmailInput = new EditText(this);
        resetEmailInput.setHint("Enter your email");
        resetEmailInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );

        String currentEmail = etUsername.getText().toString().trim();
        if (!currentEmail.isEmpty()) {
            resetEmailInput.setText(currentEmail);
        }

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding / 2, padding, 0);

        container.addView(resetEmailInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Reset password")
                .setMessage("Enter your email to receive a password reset link.")
                .setView(container)
                .setPositiveButton("Send", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String email = resetEmailInput.getText().toString().trim();
            if (email.isEmpty()) {
                resetEmailInput.setError("Email is required.");
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                resetEmailInput.setError("Enter a valid email address.");
                return;
            }
            sendPasswordReset(email, resetEmailInput, dialog);
        });
    }

    private void sendPasswordReset(String email, EditText input, AlertDialog dialog) {
        Log.d(TAG, "Requesting password reset for: " + email);
        authRepository.sendPasswordReset(email, new AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Log.d(TAG, "Password reset email sent");
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this,
                            "If an account with that email exists, a reset link has been sent.",
                            Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Password reset failed: " + errorMessage);
                runOnUiThread(() -> input.setError(errorMessage));
            }
        });
    }
}

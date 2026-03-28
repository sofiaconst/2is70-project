package com.example.eduview.ui.login;

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
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eduview.R;
import com.example.eduview.data.repository.AuthRepository;
import com.example.eduview.ui.signup.SignupActivity;
import com.example.eduview.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity for handling user login and password reset.
 * Redirects authenticated users directly to the main screen.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // Authenticator
    private AuthRepository authRepository;

    // UI Elements
    private EditText etUsername;
    private EditText etPassword;
    private TextView tvError;
    private Button btnLogin;
    private TextView tvSignup;
    private TextView tvForgotPassword;

    /**
     * Initializes the login screen, checks for an existing session, and sets up UI
     * components and listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating LoginActivity...");

        authRepository = new AuthRepository();

        // Skip login screen if a user is already logged in
        if (authRepository.getCurrentUser() != null) {
            Log.d(TAG, "Existing session detected, skipping login screen");
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Set up layouts, listeners and initialize views
        setupLayout();
        initViews();
        setupListeners();

        Log.d(TAG, "LoginActivity started");
    }

    /**
     * Attaches listeners to the login, sign-up, and password reset buttons.
     */
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvSignup.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to SignupActivity");
            startActivity(new Intent(this, SignupActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> openResetDialog());
    }

    /**
     * Attempts to log the user in using the entered username/email and password.
     * If a username is entered instead of an email, "@eduview.com" is added to the end.
     */
    public void attemptLogin() {
        // Inputs for login
        String input = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d(TAG, "Login button clicked");

        // Validate username/email and password fields
        if (input.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        // Check if the input is a username (no @) and append @eduview.com if it is
        String email = input;
        if (!input.contains("@")) {
            email = input + "@eduview.com";
        }

        // Login through Authentication Repository
        authRepository.login(email, password)
                .addOnSuccessListener(authResult -> {

                    FirebaseUser user = authResult.getUser();

                    if (user == null) {
                        showError("Authentication error.");
                        return;
                    }

                    Log.d(TAG, "Login successful for user: " + user.getUid());

                    // If login successful start the main activity
                    tvError.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                })
                // If login failed, show error for invalid credentials
                .addOnFailureListener(e -> {

                    Log.e(TAG, "Login failed", e);
                    showError("Invalid credentials.");

                });
    }

    /**
     * Displays an error message on the login screen.
     *
     * @param message message to display
     */
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    /**
     * Opens a dialog allowing the user to request a password reset link
     * using either an email address or username.
     */
    private void openResetDialog() {

        Log.d(TAG, "Opening password reset dialog");

        // Create input field where the user can enter either email or username
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

        // Create a container to add padding around the input field
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding / 2, padding, 0);

        // Add the input field into the padded container
        container.addView(resetEmailInput);

        // Build the dialog
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

            // Validate input
            if (input.isEmpty()) {
                resetEmailInput.setError("Please enter your email or username.");
                return;
            }

            // Convert username to email format if needed
            String email = input;
            if (!input.contains("@")) {
                email = input + "@eduview.com";
            }

            if (!isValidEmail(email)) {
                resetEmailInput.setError("Enter a valid email address or username.");
                return;
            }

            // Prevent multiple reset requests
            v.setEnabled(false);

            sendPasswordReset(email, resetEmailInput, dialog);
        });
    }


    /**
     * Checks whether the given string is a valid email address.
     *
     * @param email email string to validate
     * @return true if the email is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        return !email.isEmpty() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Sends a password reset email through the authentication repository.
     *
     * @param email validated email address to send the reset link to
     * @param input input field shown in the dialog, used for displaying errors
     * @param dialog password reset dialog to dismiss on success
     */
    private void sendPasswordReset(String email, EditText input, AlertDialog dialog) {

        Log.d(TAG, "Requesting password reset for: " + email);

        authRepository.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {

                    Log.d(TAG, "Password reset email sent");

                    // Notify user
                    Toast.makeText(
                            LoginActivity.this,
                            "If an account exists, a reset link has been sent.",
                            Toast.LENGTH_LONG
                    ).show();

                    // Close the dialog after successful request
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {

                    Log.e(TAG, "Password reset failed", e);

                    // Show error directly on the input field
                    input.setError("Failed to send reset email.");
                });
    }

    /**
     * Configures the activity layout and applies edge-to-edge window insets.
     */
    private void setupLayout() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Initializes references to all login screen UI components.
     */
    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        tvError = findViewById(R.id.tv_error);
        btnLogin = findViewById(R.id.btn_login);
        tvSignup = findViewById(R.id.tv_signup);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
    }
}
